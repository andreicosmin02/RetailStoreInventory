package com.example.retailstoreinventory.data.repository

import com.example.retailstoreinventory.data.mappers.toDomain
import com.example.retailstoreinventory.data.local.daos.AuditLogDao
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import com.example.retailstoreinventory.data.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.text.ifEmpty


class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao
) : ProductRepository {

    fun observeAllProducts(): Flow<List<Product>> {
        return productDao.getAll()
            .onEach { entities ->
                println("DEBUG: DAO emitted ${entities.size} entities")
            }
            .map { entities ->
                val products = entities.map { it.toDomain() }
                println("DEBUG: Repository mapped to ${products.size} products")
                products
            }
    }

    override suspend fun getProducts(): List<Product> {
        return try {
            withContext(Dispatchers.IO) {
                val entities = productDao.getAll().first()
                entities.map { it.toDomain() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return withContext(Dispatchers.IO) {
            try {
                productDao.getByBarcode(barcode)?.toDomain()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun addProduct(product: Product): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val entity = ProductEntity(
                    id = product.id.ifEmpty { UUID.randomUUID().toString() },
                    barcode = product.barcode,
                    name = product.name,
                    quantity = product.quantity,
                    price = product.price,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                productDao.insert(entity)

                auditLogDao.insert(AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "PRODUCT",
                    entityId = entity.id,
                    action = "CREATE",
                    oldValue = null,
                    newValue = entity.toString(),
                    timestamp = System.currentTimeMillis()
                ))

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun updateProduct(product: Product): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val entity = ProductEntity(
                    id = product.id,
                    barcode = product.barcode,
                    name = product.name,
                    quantity = product.quantity,
                    price = product.price,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                productDao.update(entity)

                auditLogDao.insert(AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "PRODUCT",
                    entityId = entity.id,
                    action = "UPDATE",
                    oldValue = null,
                    newValue = entity.toString(),
                    timestamp = System.currentTimeMillis()
                ))

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val productToDelete = productDao.getById(id)
                if (productToDelete != null) {
                    auditLogDao.insert(AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = "PRODUCT",
                        entityId = id,
                        action = "DELETE",
                        oldValue = productToDelete.toString(),
                        newValue = null,
                        timestamp = System.currentTimeMillis()
                    ))
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun searchProducts(query: String): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                productDao.search(query)
                    .first()
                    .map { it.toDomain() }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun recordSale(
        productId: String,
        quantity: Int,
        price: Double
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val transaction = TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    quantity = quantity,
                    priceAtSale = price,
                    total = quantity * price,
                    transactionDate = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis()
                )
                transactionDao.insert(transaction)

                val now = System.currentTimeMillis()
                productDao.decrementQuantity(productId, quantity, now)

                auditLogDao.insert(AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "PRODUCT",
                    entityId = productId,
                    action = "UPDATE",
                    oldValue = null,
                    newValue = null,
                    timestamp = now
                ))

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}