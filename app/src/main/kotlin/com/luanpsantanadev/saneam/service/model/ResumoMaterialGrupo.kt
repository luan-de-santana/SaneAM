package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumoMaterialGrupo(
    val id: Long? = null,
    @SerialName("codigo_alpha") val codigoAlpha: String,
    val nome: String,
    @SerialName("unidade_medida") val unidadeMedida: String,
    @SerialName("id_grupo") val idGrupo: Long,
    @SerialName("nome_grupo") val nomeGrupo: String,
    @SerialName("icone_grupo") val iconeGrupo: String?, // ex: "ic_wrench" ou "ic_vest"
)