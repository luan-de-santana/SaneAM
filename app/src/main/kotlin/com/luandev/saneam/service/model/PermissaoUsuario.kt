package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PermissaoUsuario(
    val id: Long? = null,
    @SerialName("id_usuario") val idUsuario: String,
    @SerialName("id_deposito") val idDeposito: Long,
    val papel: PapelUsuario
)

@Serializable
enum class PapelUsuario {
    @SerialName("LEITOR") LEITOR,
    @SerialName("OPERADOR") OPERADOR
}
