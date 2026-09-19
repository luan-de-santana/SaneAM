package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Grupo(
    val id: Long? = null,
    val nome: String,
    @SerialName("icone_res") val iconeRes: String? = null
)