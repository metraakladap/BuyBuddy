package com.example.buybuddy.states

import com.example.buybuddy.data.CategoriesData

data class AddElementsScreenState(
    val listName: String = "",
    val categories: List<String> = CategoriesData.predefinedCategories,
    val products: List<Product> = emptyList(),
    val selectedCategory: String = "",
    val selectedProduct: String? = null,
    val isCategoryValid: Boolean = true,
    val isCategoryDropDownExpanded: Boolean = false,
    val isProductDropDownExpanded: Boolean = false
)