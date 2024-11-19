package com.example.buybuddy.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.buybuddy.R
import com.example.buybuddy.states.User
import com.example.buybuddy.states.UserList
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel(
    private val context: Context,
    navController: NavController
) : ViewModel() {

    private val _userLists = MutableStateFlow<List<UserList>>(emptyList())
    val userLists: StateFlow<List<UserList>> = _userLists.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val auth = Firebase.auth



    init {
        viewModelScope.launch {
            _userLists.value = getUserLists()
            _currentUser.value = getUser()
        }
    }

    suspend fun getUserLists(): List<UserList> = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance(
            context.getString(R.string.realtime_database_url)
        )
        val account = auth.currentUser
        val target = database.reference.child("Users").child(account?.uid ?: "unknown_account").child("Lists")

        target.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val listsList = mutableListOf<UserList>()
                val snapshot = task.result

                snapshot.children.forEach { dataSnapshot ->
                    dataSnapshot.getValue(UserList::class.java)?.let { list ->
                        listsList.add(list)
                    }
                }

                continuation.resume(listsList)
            } else {
                continuation.resume(emptyList())
            }
        }
    }

   

    fun deleteUserList(listId: String) {
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
                    .removeValue()
                    .addOnCompleteListener { task ->
                        continuation.resume(task.isSuccessful)
                    }
            }

            if (success) {
                viewModelScope.async {
                    _userLists.value = getUserLists()
                }.await()

            }
        }
    }



    suspend fun getUser(): User? = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance(
            context.getString(R.string.realtime_database_url)
        )
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val target = database.reference.child(account?.id ?: "unknown_account")

        target.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val user = snapshot.getValue(User::class.java)
                continuation.resume(user)
            }
        }
    }
}