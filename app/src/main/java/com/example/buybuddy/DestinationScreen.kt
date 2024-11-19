package com.example.buybuddy

sealed class DestinationScreen(val route: String) {
    data object SignIn : DestinationScreen("SignIn")
    data object Main : DestinationScreen("Main")
    data object Profile : DestinationScreen("Profile")
    data object AddElement : DestinationScreen("AddElement/{listId}") {
        fun createRoute(listId: String) = "AddElement/$listId"
    }
    data object AddList : DestinationScreen("AddList")
    data object ListElements : DestinationScreen("ListElements/{listId}") {
        fun createRoute(listId: String) = "ListElements/$listId"
    }

}