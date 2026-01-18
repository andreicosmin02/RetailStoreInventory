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

@HiltViewModel
class ProductViewModel @Inject constructor(private val repository: ProductRepository) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _allProducts.asStateFlow()

    var uiState by mutableStateOf<UiState>(UiState.Success)
        private set

    val inventoryEvents: Flow<InventoryChangeEvent> = repository.observeInventoryChanges()

    private val _searchQuery = MutableStateFlow("")

    init {
        if (repository is ProductRepositoryImpl) {
            viewModelScope.launch {
                repository.observeAllProducts()
                    .collect { newList ->
                        _allProducts.value = newList
                        if (_searchQuery.value.isNotBlank()) {
                            applyFilter(_searchQuery.value, newList)
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
        val updated = _allProducts.value.map { p ->
            if (p.id == event.productId) {
                p.copy(quantity = event.newQuantity)
            } else {
                p
            }
        }

        if (_searchQuery.value.isNotBlank()) {
            applyFilter(_searchQuery.value, updated)
        } else {
            _allProducts.value = updated
        }
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return repository.getProductByBarcode(barcode)
    }

    private fun applyFilter(query: String, currentList: List<Product>) {
        val filtered = if (query.isBlank()) {
            currentList
        } else {
            currentList.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.barcode.contains(query, ignoreCase = true)
            }
        }
        _allProducts.value = filtered
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                _searchQuery.value = query
                val currentList = _allProducts.value
                applyFilter(query, currentList)
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