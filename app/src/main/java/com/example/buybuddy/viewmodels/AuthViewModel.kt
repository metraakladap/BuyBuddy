package com.example.buybuddy.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.buybuddy.responses.SignInResult
import com.example.buybuddy.states.SignInState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(
    navController: NavController,
    context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()


    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val isAuthenticated = auth.currentUser != null
        _state.value = _state.value.copy(isSignInSuccessful = isAuthenticated)
    }

    fun signInWithEmailPassword() {
        if (!validateInputs()) {
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        auth.signInWithEmailAndPassword(_state.value.email, _state.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = _state.value.copy(
                        isSignInSuccessful = true,
                        isLoading = false,
                        signInError = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isSignInSuccessful = false,
                        isLoading = false,
                        signInError = task.exception?.message ?: "Authentication failed"
                    )
                }
            }
    }

    fun signUpWithEmailPassword() {
        if (!validateInputs()) {
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        auth.createUserWithEmailAndPassword(_state.value.email, _state.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = _state.value.copy(
                        isSignInSuccessful = true,
                        isLoading = false,
                        signInError = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isSignInSuccessful = false,
                        isLoading = false,
                        signInError = task.exception?.message ?: "Sign up failed"
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
        resetState()
    }

    private fun validateInputs(): Boolean {
        val isEmailValid = isEmailValid(_state.value.email)
        val isPasswordValid = isPasswordValid(_state.value.password)

        _state.value = _state.value.copy(
            isEmailValid = isEmailValid,
            isPasswordValid = isPasswordValid,
            emailErrorMessage = if (isEmailValid) "" else "Invalid email",
            passwordErrorMessage = if (isPasswordValid) "" else "Password must be at least 8 characters"
        )

        return isEmailValid && isPasswordValid
    }

    // Google Sign In handling
    fun onSignInResult(result: SignInResult) {
        _state.value = _state.value.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        )
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun passwordValidation(password: String) {
        _state.value = _state.value.copy(
            isPasswordValid = isPasswordValid(password),
            passwordErrorMessage = if (isPasswordValid(password)) "" else "Password must be at least 8 characters"
        )
    }

    fun emailValidation(email: String) {
        _state.value = _state.value.copy(
            isEmailValid = isEmailValid(email),
            emailErrorMessage = if (isEmailValid(email)) "" else "Invalid email"
        )
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun passwordVisibility() {
        _state.value = _state.value.copy(
            passwordVisibility = !_state.value.passwordVisibility
        )
    }

    fun onEmailChanged(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(password = password)
    }
}

