package com.example.retailstoreinventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailstoreinventory.data.local.daos.ProductDao
import com.example.retailstoreinventory.data.local.daos.TransactionDao
import com.example.retailstoreinventory.ui.screens.SaleRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val productDao: ProductDao
) : ViewModel() {

    private val _sales = MutableStateFlow<List<SaleRecord>>(emptyList())
    val sales: StateFlow<List<SaleRecord>> = _sales.asStateFlow()

    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSales() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Get all transactions
                val txnStartTime = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L) // Last 30 days
                val txnEndTime = System.currentTimeMillis()

                val transactions = transactionDao.getTransactionsByDateRange(
                    startDate = txnStartTime,
                    endDate = txnEndTime
                ).collect { txnList ->
                    // Convert transactions to sale records with product names
                    val saleRecords = txnList.mapNotNull { txn ->
                        val product = productDao.getById(txn.productId)
                        if (product != null) {
                            SaleRecord(
                                id = txn.id,
                                productName = product.name,
                                quantity = txn.quantity,
                                pricePerUnit = txn.priceAtSale,
                                total = txn.total,
                                timestamp = txn.transactionDate
                            )
                        } else {
                            null
                        }
                    }

                    _sales.value = saleRecords
                    _totalRevenue.value = saleRecords.sumOf { it.total }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _sales.value = emptyList()
                _totalRevenue.value = 0.0
            }
        }
    }
}