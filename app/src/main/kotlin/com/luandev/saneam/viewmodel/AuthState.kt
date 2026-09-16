package com.luandev.saneam.viewmodel

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val errorMessage: String) : AuthState()
}