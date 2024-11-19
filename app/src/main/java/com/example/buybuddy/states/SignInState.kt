package com.example.buybuddy.states

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isEmailValid: Boolean = true,
    val emailErrorMessage: String = "",
    val isPasswordValid: Boolean = true,
    val passwordErrorMessage: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisibility: Boolean = false,
    val isLoading: Boolean = false
)
