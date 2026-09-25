package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.Serializable

@Serializable
data class DepositoComAcesso(
    val id: Long,
    val nome: String,
    val endereco: String? = null,
    val papel: PapelUsuario? = null
)
