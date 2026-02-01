package com.example.retailstoreinventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Provider(
    val id: String,
    val name: String,
    val category: String,
    val contact: String,
    val email: String,
    val phone: String,
    val location: String,
    val rating: Double
)

@HiltViewModel
class ProvidersViewModel @Inject constructor() : ViewModel() {

    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers: StateFlow<List<Provider>> = _providers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProviders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val mockProviders = listOf(
                    Provider(
                        id = "1",
                        name = "Fresh Produce Co.",
                        category = "Fruits & Vegetables",
                        contact = "John Smith",
                        email = "john@freshproduce.com",
                        phone = "+1-555-0101",
                        location = "Springfield",
                        rating = 4.8
                    ),
                    Provider(
                        id = "2",
                        name = "Organic Growers Inc.",
                        category = "Organic Products",
                        contact = "Sarah Johnson",
                        email = "sarah@organicgrowers.com",
                        phone = "+1-555-0102",
                        location = "Shelbyville",
                        rating = 4.6
                    ),
                    Provider(
                        id = "3",
                        name = "Bulk Foods Ltd.",
                        category = "Wholesale",
                        contact = "Mike Davis",
                        email = "mike@bulkfoods.com",
                        phone = "+1-555-0103",
                        location = "Capital City",
                        rating = 4.5
                    ),
                    Provider(
                        id = "4",
                        name = "Local Farmers Market",
                        category = "Local Produce",
                        contact = "Emma Wilson",
                        email = "emma@localfarmers.com",
                        phone = "+1-555-0104",
                        location = "Riverside",
                        rating = 4.9
                    ),
                    Provider(
                        id = "5",
                        name = "Premium Imports",
                        category = "International",
                        contact = "Carlos Rodriguez",
                        email = "carlos@premiumimports.com",
                        phone = "+1-555-0105",
                        location = "Metro City",
                        rating = 4.3
                    ),
                )

                _providers.value = mockProviders
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _providers.value = emptyList()
            }
        }
    }
}