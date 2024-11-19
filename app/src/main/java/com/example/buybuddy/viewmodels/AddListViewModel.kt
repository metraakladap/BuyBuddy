package com.example.buybuddy.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.buybuddy.R
import com.example.buybuddy.states.AddListScreenState
import com.example.buybuddy.states.UserList
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddListViewModel(
    navController: NavController,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AddListScreenState())
    val state = _state.asStateFlow()
    private val auth = Firebase.auth





    fun onListNameChanged(listName: String) {
        _state.value = state.value.copy(listName = listName)
    }



    fun addUserList(userListName: String) {
        viewModelScope.launch {
            val success = suspendCoroutine { continuation ->
                val account = auth.currentUser
                val database = FirebaseDatabase.getInstance(
                    context.getString(R.string.realtime_database_url)
                )
                var newListKey: String? = null
                try {
                    if (account?.uid == null) {
                        Log.e("AddNewListError", "Unknown User!")
                    } else {
                        newListKey = database.reference
                            .child("Users")
                            .child(account.uid)
                            .child("Lists")
                            .push().key
                    }
                } catch (e: Exception) {
                    Log.e("AddNewListError", "$e")
                }

                if (newListKey == null) {
                    continuation.resume(false)
                    return@suspendCoroutine
                }

                val listToAdd = UserList(
                    id = newListKey,
                    name = state.value.listName
                )



                database.reference
                    .child("Users")
                    .child(account?.uid ?: "unknown_account")
                    .child("Lists")
                    .child(newListKey)
                    .setValue(listToAdd)
                    .addOnCompleteListener { task ->
                        continuation.resume(task.isSuccessful)
                    }


            }

        }
    }
}