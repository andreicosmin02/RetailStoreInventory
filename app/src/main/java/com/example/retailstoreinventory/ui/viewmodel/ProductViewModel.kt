package com.example.retailstoreinventory.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.retailstoreinventory.data.models.Product
import com.example.retailstoreinventory.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    var uiState by mutableStateOf<UiState>(UiState.Loading)
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                products = repository.getProducts()
                uiState = UiState.Success
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                val filteredProducts = if (query.isBlank()) {
                    repository.getProducts()
                } else {
                    repository.searchProducts(query)
                }
                products = filteredProducts
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
            loadProducts() // Refresh the list
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}