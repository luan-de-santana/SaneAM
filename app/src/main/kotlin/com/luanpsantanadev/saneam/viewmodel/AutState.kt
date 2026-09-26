package com.luanpsantanadev.saneam.viewmodel

sealed class AutState {
    object Parado : AutState()
    object Carregando : AutState()
    object Conectado : AutState() // Indica que a sessão ativa já existe ao abrir o app
    data class Sucesso(val message: String) : AutState()
    data class Aviso(val message: String) : AutState()
    data class Erro(val errorMessage: String) : AutState()
}

sealed class RecuperaSenhaState {
    object Parado : RecuperaSenhaState()
    object CarregandoLink : RecuperaSenhaState()
    object Pronto : RecuperaSenhaState()
    object Salvando : RecuperaSenhaState()
    object Cancelando : RecuperaSenhaState()
    object Cancelada : RecuperaSenhaState()
    object Concluido : RecuperaSenhaState()
    data class Erro(val message: String, val canRetry: Boolean) : RecuperaSenhaState()
}