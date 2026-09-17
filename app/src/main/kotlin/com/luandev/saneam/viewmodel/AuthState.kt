package com.luandev.saneam.viewmodel

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object LoggedIn : AuthState() // Indica que a sessão ativa já existe ao abrir o app
    data class Success(val message: String) : AuthState()
    data class Error(val errorMessage: String) : AuthState()
}