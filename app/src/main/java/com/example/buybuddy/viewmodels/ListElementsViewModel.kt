package com.example.buybuddy.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buybuddy.R
import com.example.buybuddy.states.ListElement
import com.example.buybuddy.states.UserList
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ListElementsViewModel(private val listId: String, private val context: Context) :
    ViewModel() {

    private val _userList = MutableStateFlow<UserList?>(UserList())
    val userList: StateFlow<UserList?> = _userList.asStateFlow()

    private val _listElement = MutableStateFlow<List<ListElement>?>(emptyList())
    val listElement: StateFlow<List<ListElement>?> = _listElement.asStateFlow()

    private val auth = Firebase.auth

    init {
        viewModelScope.launch {
            _userList.value = getList()
            _listElement.value = getElements()
        }
    }

    fun getListId(): String {
        return listId
    }

    suspend fun getList(): UserList? = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance(
            context.getString(R.string.realtime_database_url)
        )
        val account = auth.currentUser
        val target = database.reference.child("Users").child(account?.uid ?: "unknown_account")
            .child("Lists").child(listId)

        target.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val singleItem = snapshot.getValue(UserList::class.java)
                continuation.resume(singleItem)
            } else {
                continuation.resume(UserList())
            }
        }
    }

    suspend fun getElements(): List<ListElement>? = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance(
            context.getString(R.string.realtime_database_url)
        )
        val account = auth.currentUser
        val target = database.reference
            .child("Users")
            .child(account?.uid ?: "unknown_account")
            .child("Lists")
            .child(listId)
            .child("elements")

        target.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val elementsList = mutableListOf<ListElement>()
                val snapshot = task.result

                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(ListElement::class.java)?.let { list ->
                        elementsList.add(list)
                    }
                }
                val sortedElementsList = elementsList.sortedBy { it.category }
                continuation.resume(sortedElementsList)
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    fun deleteUserElement(elementId: String) {
        viewModelScope.launch {
            val success = suspendCoroutine { continuation ->
                val account = auth.currentUser
                val database = FirebaseDatabase.getInstance(
                    context.getString(R.string.realtime_database_url)
                )

                database.reference
                    .child("Users")
                    .child(account?.uid ?: "unknown_account")
                    .child("Lists")
                    .child(listId)
                    .child("elements")
                    .child(elementId)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        continuation.resume(task.isSuccessful)
                    }
            }

            if (success) {
                viewModelScope.async {
                    _userList.value = getList()
                    _listElement.value = getElements()
                }.await()
            }
        }
    }

    fun completeElement(elementId: String) {
        viewModelScope.launch {
            val success = suspendCoroutine { continuation ->
                val account = auth.currentUser
                val database = FirebaseDatabase.getInstance(
                    context.getString(R.string.realtime_database_url)
                )
                val target = database.reference
                    .child("Users")
                    .child(account?.uid ?: "unknown_account")
                    .child("Lists")
                    .child(listId)
                    .child("elements")
                    .child(elementId)
                    .child("completed")
                target.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        val singleItem = snapshot.getValue(Boolean::class.java)
                        database.reference
                            .child("Users")
                            .child(account?.uid ?: "unknown_account")
                            .child("Lists")
                            .child(listId)
                            .child("elements")
                            .child(elementId)
                            .child("completed")
                            .setValue(!singleItem!!)
                            .addOnCompleteListener { task1 ->
                                continuation.resume(task1.isSuccessful)
                            }
                    } else {
                        continuation.resume(task.isSuccessful)
                    }
                }

            }

            if (success) {
                viewModelScope.async {
                    _userList.value = getList()
                    _listElement.value = getElements()
                }.await()

            }
        }
    }
}