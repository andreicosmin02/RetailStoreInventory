package com.example.retailstoreinventory.data.mappers

import com.example.retailstoreinventory.data.local.entities.AuditLogEntity
import com.example.retailstoreinventory.data.local.entities.InventoryStateEntity
import com.example.retailstoreinventory.data.local.entities.ProductEntity
import com.example.retailstoreinventory.data.local.entities.TransactionEntity
import com.example.retailstoreinventory.data.models.AuditLog
import com.example.retailstoreinventory.data.models.InventoryState
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.models.Transaction

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    barcode = barcode
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    productId = productId,
    quantity = quantity,
    priceAtSale = priceAtSale,
    total = total,
    transactionDate = transactionDate,
    createdAt = createdAt
)

fun InventoryStateEntity.toDomain(): InventoryState = InventoryState(
    productId = productId,
    quantityOnHand = quantityOnHand,
    quantityAtThreshold = quantityAtThreshold,
    lastUpdated = lastUpdated
)

fun AuditLogEntity.toDomain(): AuditLog = AuditLog(
    id = id,
    entityType = entityType,
    entityId = entityId,
    action = action,
    oldValue = oldValue,
    newValue = newValue,
    timestamp = timestamp
)

/**
 * Extension functions to map domain models to database entities
 */

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    quantity = quantity,
    price = price,
    barcode = barcode,
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    productId = productId,
    quantity = quantity,
    priceAtSale = priceAtSale,
    total = total,
    transactionDate = transactionDate,
    createdAt = createdAt
)

fun InventoryState.toEntity(): InventoryStateEntity = InventoryStateEntity(
    productId = productId,
    quantityOnHand = quantityOnHand,
    quantityAtThreshold = quantityAtThreshold,
    lastUpdated = lastUpdated
)

fun AuditLog.toEntity(): AuditLogEntity = AuditLogEntity(
    id = id,
    entityType = entityType,
    entityId = entityId,
    action = action,
    oldValue = oldValue,
    newValue = newValue,
    timestamp = timestamp
)