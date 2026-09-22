package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.Serializable

@Serializable
data class Deposito(
    val id: Long? = null,
    val nome: String,
    val endereco: String? = null
)