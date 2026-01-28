package com.example.retailstoreinventory.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailstoreinventory.data.models.InventoryChangeEvent
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.repository.ProductRepository
import com.example.retailstoreinventory.data.repository.ProductRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    ASC, DESC
}

@HiltViewModel
class ProductViewModel @Inject constructor(private val repository: ProductRepository) : ViewModel() {
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _fullProducts = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _allProducts.asStateFlow()

    var uiState by mutableStateOf<UiState>(UiState.Success)
        private set

    val inventoryEvents: Flow<InventoryChangeEvent> = repository.observeInventoryChanges()

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.ASC)

    init {
        if (repository is ProductRepositoryImpl) {
            viewModelScope.launch {
                repository.observeAllProducts()
                    .collect { newList ->
                        _fullProducts.value = newList

                        if (_searchQuery.value.isNotBlank()) {
                            applyFilter(_searchQuery.value, newList)
                        } else {
                            _allProducts.value = newList
                        }
                    }

            }
        }

        viewModelScope.launch {
            inventoryEvents.collectLatest { event ->
                applyInventoryEventToState(event)
            }
        }
    }

    private fun applyInventoryEventToState(event: InventoryChangeEvent) {
        val updatedFull = _fullProducts.value.map { p ->
            if (p.id == event.productId) {
                p.copy(quantity = event.newQuantity)
            } else {
                p
            }
        }

        _fullProducts.value = updatedFull

        if (_searchQuery.value.isNotBlank()) {
            applyFilter(_searchQuery.value, updatedFull)
        } else {
            _allProducts.value = updatedFull
        }
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return repository.getProductByBarcode(barcode)
    }

    private fun applySort(list: List<Product>): List<Product> {
        return when (_sortOrder.value) {
            SortOrder.ASC -> list.sortedBy { it.name.lowercase() }
            SortOrder.DESC -> list.sortedByDescending { it.name.lowercase() }
        }
    }

    private fun applyFilter(query: String, source: List<Product>) {
        val filtered = source.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.barcode.contains(query, ignoreCase = true)
        }

        _allProducts.value = applySort(filtered)
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val q = query.trim()
                _searchQuery.value = q

                val sourceList = _fullProducts.value

                if (q.isBlank()) {
                    _allProducts.value = applySort(sourceList)
                } else {
                    applyFilter(q, sourceList)
                }

                uiState = if (_allProducts.value.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success
                }
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.addProduct(product)
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleSortOrder() {
        _sortOrder.value =
            if (_sortOrder.value == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC

        val source = _fullProducts.value

        if (_searchQuery.value.isBlank()) {
            _allProducts.value = applySort(source)
        } else {
            applyFilter(_searchQuery.value, source)
        }
    }

    suspend fun recordSale(
        productId: String,
        quantity: Int,
        priceAtSale: Double
    ): Boolean {
        return try {
            if (repository is ProductRepositoryImpl) {
                repository.recordSale(productId, quantity, priceAtSale)
            } else {
                false
            }
        } catch (e: Exception) {
            uiState = UiState.Error(e.message ?: "Failed to record sale")
            false
        }
    }

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            if (repository is ProductRepositoryImpl) {
                repository.deleteProduct(productId)
            } else {
                false
            }
        } catch (e: Exception) {
            uiState = UiState.Error(e.message ?: "Failed to delete product")
            false
        }
    }


    fun getLatestProductById(productId: String): Product? {
        return _allProducts.value.firstOrNull { it.id == productId }
    }

    fun hasEnoughStock(productId: String, requestedQuantity: Int): Boolean {
        val p = getLatestProductById(productId) ?: return false
        if (requestedQuantity <= 0) return false
        return p.quantity >= requestedQuantity
    }
}

sealed class UiState {
    object Success : UiState()
    object Empty : UiState()
    data class Error(val message: String) : UiState()
}