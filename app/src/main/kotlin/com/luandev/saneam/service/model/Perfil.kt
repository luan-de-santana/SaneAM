package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// (Permanece UUID String por causa da autenticação do Supabase)
@Serializable
data class Perfil(
    val id: String,
    val nome: String,
    val email: String,
    @SerialName("eh_administrador") val ehAdministrador: Boolean = false
)