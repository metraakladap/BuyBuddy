package com.example.buybuddy.states

data class User(
    val userName: String = "",
    val userID: String = "",

)
data class ListElement(
    val id: String = "",
    val name: String = "",
    val completed: Boolean = false,
    val category: String = ""
)

data class UserList(
    val id: String = "",
    val name: String = ""
)
data class Product(
    val name: String = "",
    val category: String = ""
)