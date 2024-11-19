package com.example.buybuddy.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buybuddy.R
import com.example.buybuddy.data.CategoriesData
import com.example.buybuddy.states.AddElementsScreenState
import com.example.buybuddy.states.ListElement
import com.example.buybuddy.states.Product
import com.example.buybuddy.states.UserList
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddElementsViewModel(
    private val listId: String,
    private val context: Context,
) : ViewModel() {

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _state = MutableStateFlow(AddElementsScreenState())
    val state = _state.asStateFlow()

    private val auth = Firebase.auth
    private val currentUser = auth.currentUser

    init{
        setCategoryAndProduct()
    }
    fun updateSelectedCategory(category: String) {
        _state.update {
            it.copy(
                selectedCategory = category,

                )
        }
    }
    fun setCategoryAndProduct() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    categories = loadCategories(),
                    products = loadProducts()
                    )
            }
        }

    }

    fun updateSelectedProduct(product: String) {
        _state.update {
            it.copy(
                selectedProduct = product,

                )
        }
    }

    fun categoryValidation(selectedCategory: String) {
        _state.value = _state.value.copy(
            isCategoryValid = isCategoryValid(selectedCategory)
        )
    }

    private fun isCategoryValid(selectedCategory: String) : Boolean {
        return selectedCategory.length < 3
    }

    fun toggleCategoryDropDown() {
        _state.update { it.copy(isCategoryDropDownExpanded = !it.isCategoryDropDownExpanded) }
    }

    fun toggleProductDropDown() {
        _state.update { it.copy(isProductDropDownExpanded = !it.isProductDropDownExpanded) }
    }

    fun onAddListElementClick() {
        addListElement(state.value.selectedProduct, state.value.selectedCategory)
    }
     fun getProductByCategory(category: String):List<String>{
         return state.value.products
                .filter { it.category == category }  // фільтруємо за категорією
                .map { it.name }  
    }
    private fun addListElement(elementName: String?, elementCategory: String?) {
        viewModelScope.launch {
            val success = suspendCoroutine { continuation ->
                val account = auth.currentUser
                val database = FirebaseDatabase.getInstance(
                    context.getString(R.string.realtime_database_url)
                )
                var newElementKey: String? = null
                var newCategoryKey: String? = null
                var newProductKey: String? = null
                try {
                    if (account?.uid == null) {
                        Log.e("AddNewListError", "Unknown User!")
                    } else {
                        newElementKey = database.reference
                            .child("Users")
                            .child(account.uid)
                            .child("Lists")
                            .child(listId)
                            .child("elements")
                            .push().key
                        newCategoryKey = database.reference
                            .child("Users")
                            .child(account.uid)
                            .child("Categories")
                            .push().key
                        newProductKey = database.reference
                            .child("Users")
                            .child(account.uid)
                            .child("Products")
                            .push().key
                    }
                } catch (e: Exception) {
                    Log.e("AddNewListError", "$e")
                }

                if (newElementKey == null) {
                    continuation.resume(false)
                    return@suspendCoroutine
                }

                val elementToAdd = elementName?.let {
                    elementCategory?.let { it1 ->
                        ListElement(
                            id = newElementKey,
                            name = it,
                            category = it1
                        )
                    }
                }
                val productToAdd = elementName?.let {
                    elementCategory?.let { it1 ->
                        Product(
                            name = it,
                            category = it1
                        )
                    }
                }

               
                if (newCategoryKey != null  && !elementCategory?.let { state.value.categories.contains(it) }!!) {
                    database.reference
                        .child("Users")
                        .child(account?.uid ?: "unknown_account")
                        .child("Categories")
                        .child(newCategoryKey)
                        .child("name")
                        .setValue(elementCategory)
                }

                if (newProductKey != null && !state.value.products.contains(productToAdd))  {
                    database.reference
                        .child("Users")
                        .child(account?.uid ?: "unknown_account")
                        .child("Products")
                        .child(newProductKey)
                        .setValue(productToAdd)
                }

                database.reference
                    .child("Users")
                    .child(account?.uid ?: "unknown_account")
                    .child("Lists")
                    .child(listId)
                    .child("elements")
                    .child(newElementKey)
                    .setValue(elementToAdd)
                    .addOnCompleteListener { task ->
                        continuation.resume(task.isSuccessful)
                    }
                
            }

        }
    }
    private fun extractName(input: String): String? {
        return input.substring(6, input.length - 1)
    }
    private suspend fun loadCategories(): List<String> = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance()
        val target = database.reference.child("Users").child(currentUser?.uid ?: "unknown_user")
            .child("Categories")

            target.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val categoryList = mutableListOf<String>()
                categoryList.addAll(CategoriesData.predefinedCategories)
                for (listSnapshot in task.result.children) {
                    listSnapshot.value?.let { category ->
                        extractName(category.toString())?.let { categoryList.add(it) }
                        Log.d("CategoryListToString", "$categoryList")
                    }
                }

                continuation.resume(categoryList)
            } else {
                continuation.resume(emptyList())
            }
        }

    }
    private suspend fun loadProducts(): List<Product> = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance()
        val target = database.reference.child("Users").child(currentUser?.uid ?: "unknown_user")
            .child("Products")

            target.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val categoryList = mutableListOf<Product>()
                for (listSnapshot in task.result.children) {
                    listSnapshot.getValue(Product::class.java)?.let { list ->
                        categoryList.add(list)
                    }
                }
                continuation.resume(categoryList)
            } else {
                continuation.resume(emptyList())
            }
        }

    }

}