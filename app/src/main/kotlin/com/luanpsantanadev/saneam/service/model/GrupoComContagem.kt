package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GrupoComContagem(
    val id: Long,
    val nome: String,
    @SerialName("icone_res") val iconeRes: String? = null,
    @SerialName("quantidade_materiais") val quantidadeMateriais: Long
)
