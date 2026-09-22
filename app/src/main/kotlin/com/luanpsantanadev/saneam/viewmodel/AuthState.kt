package com.luanpsantanadev.saneam.viewmodel

sealed class AuthState {
    object Parado : AuthState()
    object Carregando : AuthState()
    object Conectado : AuthState() // Indica que a sessão ativa já existe ao abrir o app
    data class Sucesso(val message: String) : AuthState()
    data class Erro(val errorMessage: String) : AuthState()
}