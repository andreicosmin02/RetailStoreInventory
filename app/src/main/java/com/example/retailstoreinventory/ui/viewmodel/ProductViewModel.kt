package com.example.retailstoreinventory.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.repository.ProductRepository
import com.example.retailstoreinventory.data.repository.ProductRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    /**
     * Simple reactive products list.
     * This is only created AFTER the database is initialized,
     * so the database is guaranteed to have data.
     */
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _allProducts.asStateFlow()

    var uiState by mutableStateOf<UiState>(UiState.Success)
        private set

    init {
        if (repository is ProductRepositoryImpl) {
            viewModelScope.launch {
                repository.observeAllProducts()
                    .collect { newList ->
                        _allProducts.value = newList
                        // If there's an active search filter, apply it again to the new list
                        if (_searchQuery.value.isNotBlank()) {
                            applyFilter(_searchQuery.value, newList)
                        }
                    }
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
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
                applyFilter(query, currentList) // Apply filter to the current list in the state flow
                uiState = if (currentList.isEmpty()) {
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
}

sealed class UiState {
    object Success : UiState()
    object Empty : UiState()
    data class Error(val message: String) : UiState()
}