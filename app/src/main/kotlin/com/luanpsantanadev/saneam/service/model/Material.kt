package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: Long? = null,
    @SerialName("codigo_alpha") val codigoAlpha: Int,
    val nome: String,
    @SerialName("unidade_medida") val unidadeMedida: String,
    @SerialName("id_grupo") val idGrupo: Long
)