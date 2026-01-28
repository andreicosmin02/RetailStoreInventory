package com.example.retailstoreinventory.data.repository

import androidx.room.withTransaction
import com.example.retailstoreinventory.data.local.RetailDatabase
import com.example.retailstoreinventory.data.local.daos.AlertDao
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.InventoryStateDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import com.example.retailstoreinventory.data.mappers.toDomain
import com.example.retailstoreinventory.data.models.InventoryChangeEvent
import com.example.retailstoreinventory.data.models.InventoryCommand
import com.example.retailstoreinventory.data.models.InventoryMutationResult
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.monitoring.LowStockAlertService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.UUID

class ProductRepositoryImpl(
    private val db: RetailDatabase,
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao,
    private val inventoryStateDao: InventoryStateDao,
    private val alertDao: AlertDao,
    private val lowStockAlertService: LowStockAlertService
) : ProductRepository {

    private val inventoryChanges = MutableSharedFlow<InventoryChangeEvent>(extraBufferCapacity = 64)

    override fun observeInventoryChanges(): Flow<InventoryChangeEvent> = inventoryChanges.asSharedFlow()

    fun observeAllProducts(): Flow<List<Product>> {
        return productDao.getAll()
            .onEach { println("DEBUG: DAO emitted ${it.size}") }
            .map { it.map { e -> e.toDomain() } }
    }

    override suspend fun getProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            productDao.getAll().first().map { it.toDomain() }
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return withContext(Dispatchers.IO) {
            productDao.getByBarcode(barcode)?.toDomain()
        }
    }

    override suspend fun addProduct(product: Product): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (product.quantity < 0) return@withContext false
                if (product.price < 0) return@withContext false

                val now = System.currentTimeMillis()
                val id = product.id.ifBlank { UUID.randomUUID().toString() }

                db.withTransaction {
                    productDao.insert(
                        ProductEntity(
                            id = id,
                            barcode = product.barcode,
                            name = product.name,
                            quantity = product.quantity,
                            price = product.price,
                            createdAt = now,
                            updatedAt = now
                        )
                    )

                    inventoryStateDao.upsert(
                        InventoryStateEntity(
                            productId = id,
                            quantityOnHand = product.quantity,
                            quantityAtThreshold = 10,
                            lastUpdated = now
                        )
                    )

                    auditLogDao.insert(
                        AuditLogEntity(
                            id = UUID.randomUUID().toString(),
                            entityType = "PRODUCT",
                            entityId = id,
                            action = "CREATE",
                            oldValue = null,
                            newValue = product.copy(id = id).toString(),
                            timestamp = now
                        )
                    )
                }

                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun updateProduct(product: Product): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val existing = productDao.getById(product.id) ?: return@withContext false
                if (product.quantity != existing.quantity) return@withContext false
                if (product.price < 0) return@withContext false

                productDao.update(
                    ProductEntity(
                        id = product.id,
                        barcode = product.barcode,
                        name = product.name,
                        quantity = existing.quantity,
                        price = product.price,
                        createdAt = existing.createdAt,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                auditLogDao.insert(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = "PRODUCT",
                        entityId = product.id,
                        action = "UPDATE",
                        oldValue = null,
                        newValue = product.toString(),
                        timestamp = System.currentTimeMillis()
                    )
                )

                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()

                // Load existing for audit + sanity
                val existing = productDao.getById(id) ?: return@withContext false

                // If there is sales history, we do NOT hard-delete (append-only ledger + FK safety)
                val txCount = transactionDao.countForProduct(id)
                if (txCount > 0) return@withContext false

                db.withTransaction {
                    // cleanup alerts (no FK assumed, safe even if 0 rows)
                    alertDao.deleteByProductId(id)

                    // delete product (inventory_state likely cascades)
                    val rows = productDao.deleteById(id)
                    if (rows == 0) throw IllegalStateException("Delete failed: product not found")

                    auditLogDao.insert(
                        AuditLogEntity(
                            id = UUID.randomUUID().toString(),
                            entityType = "PRODUCT",
                            entityId = id,
                            action = "DELETE",
                            oldValue = existing.toDomain().toString(),
                            newValue = null,
                            timestamp = now
                        )
                    )
                }

                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun searchProducts(query: String): List<Product> {
        return withContext(Dispatchers.IO) {
            productDao.search(query).first().map { it.toDomain() }
        }
    }

    suspend fun applyInventoryCommand(cmd: InventoryCommand): InventoryMutationResult {
        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                var result: InventoryMutationResult = InventoryMutationResult.Error("Mutation failed")
                var affectedProductId: String? = null

                db.withTransaction {
                    val productId = when (cmd) {
                        is InventoryCommand.Sale -> cmd.productId
                        is InventoryCommand.ReceiveOrder -> cmd.productId
                    }

                    affectedProductId = productId

                    val product = productDao.getById(productId)
                    if (product == null) {
                        result = InventoryMutationResult.Error("Product not found")
                        return@withTransaction
                    }

                    val validation = validate(cmd, product)
                    if (validation is InventoryMutationResult.Error) {
                        result = validation
                        return@withTransaction
                    }

                    when (cmd) {
                        is InventoryCommand.Sale -> {
                            val rows = productDao.decrementQuantityIfEnough(productId, cmd.quantity, now)
                            if (rows == 0) {
                                result = InventoryMutationResult.Error("Not enough stock")
                                return@withTransaction
                            }

                            val newQty = product.quantity - cmd.quantity

                            inventoryStateDao.upsert(
                                InventoryStateEntity(
                                    productId = productId,
                                    quantityOnHand = newQty,
                                    quantityAtThreshold = 10,
                                    lastUpdated = now
                                )
                            )

                            transactionDao.insert(
                                TransactionEntity(
                                    id = UUID.randomUUID().toString(),
                                    productId = productId,
                                    quantity = cmd.quantity,
                                    priceAtSale = cmd.price,
                                    total = cmd.quantity * cmd.price,
                                    transactionDate = now,
                                    createdAt = now
                                )
                            )

                            auditLogDao.insert(
                                AuditLogEntity(
                                    id = UUID.randomUUID().toString(),
                                    entityType = "INVENTORY",
                                    entityId = productId,
                                    action = "SALE",
                                    oldValue = "qty=${product.quantity}",
                                    newValue = "qty=$newQty, sold=${cmd.quantity}, price=${cmd.price}, total=${cmd.quantity * cmd.price}",
                                    timestamp = now
                                )
                            )

                            inventoryChanges.tryEmit(
                                InventoryChangeEvent(
                                    productId = productId,
                                    oldQuantity = product.quantity,
                                    newQuantity = newQty,
                                    action = "SALE",
                                    timestamp = now
                                )
                            )

                            result = InventoryMutationResult.Ok
                        }

                        is InventoryCommand.ReceiveOrder -> {
                            val rows = productDao.incrementQuantity(productId, cmd.quantity, now)
                            if (rows == 0) {
                                result = InventoryMutationResult.Error("Product not found")
                                return@withTransaction
                            }

                            val newQty = product.quantity + cmd.quantity

                            inventoryStateDao.upsert(
                                InventoryStateEntity(
                                    productId = productId,
                                    quantityOnHand = newQty,
                                    quantityAtThreshold = 10,
                                    lastUpdated = now
                                )
                            )

                            auditLogDao.insert(
                                AuditLogEntity(
                                    id = UUID.randomUUID().toString(),
                                    entityType = "INVENTORY",
                                    entityId = productId,
                                    action = "ORDER_RECEIVED",
                                    oldValue = "qty=${product.quantity}",
                                    newValue = "qty=$newQty, received=${cmd.quantity}",
                                    timestamp = now
                                )
                            )

                            inventoryChanges.tryEmit(
                                InventoryChangeEvent(
                                    productId = productId,
                                    oldQuantity = product.quantity,
                                    newQuantity = newQty,
                                    action = "ORDER_RECEIVED",
                                    timestamp = now
                                )
                            )

                            result = InventoryMutationResult.Ok
                        }
                    }
                }

                if (result is InventoryMutationResult.Ok && cmd is InventoryCommand.Sale) {
                    affectedProductId?.let { lowStockAlertService.checkProduct(it) }
                }

                result
            } catch (e: Exception) {
                InventoryMutationResult.Error("Mutation failed")
            }
        }
    }

    private fun validate(cmd: InventoryCommand, product: ProductEntity): InventoryMutationResult {
        return when (cmd) {
            is InventoryCommand.Sale -> {
                when {
                    cmd.quantity <= 0 -> InventoryMutationResult.Error("Quantity must be > 0")
                    cmd.price < 0 -> InventoryMutationResult.Error("Price must be >= 0")
                    product.quantity - cmd.quantity < 0 -> InventoryMutationResult.Error("Not enough stock")
                    else -> InventoryMutationResult.Ok
                }
            }

            is InventoryCommand.ReceiveOrder -> {
                when {
                    cmd.quantity <= 0 -> InventoryMutationResult.Error("Quantity must be > 0")
                    else -> InventoryMutationResult.Ok
                }
            }
        }
    }

    suspend fun recordSale(productId: String, quantity: Int, price: Double): Boolean {
        return applyInventoryCommand(InventoryCommand.Sale(productId, quantity, price)) is InventoryMutationResult.Ok
    }

    suspend fun recordOrderReceipt(productId: String, quantity: Int): Boolean {
        return applyInventoryCommand(InventoryCommand.ReceiveOrder(productId, quantity)) is InventoryMutationResult.Ok
    }
}