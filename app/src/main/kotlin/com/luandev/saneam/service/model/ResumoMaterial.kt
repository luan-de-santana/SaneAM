package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumoMaterial(
    @SerialName("id_material") val idMaterial: Long,
    @SerialName("codigo_alpha") val codigoAlpha: Int,
    @SerialName("nome_material") val nomeMaterial: String,
    @SerialName("unidade_medida") val unidadeMedida: String,
    @SerialName("id_grupo") val idGrupo: Long,
    @SerialName("nome_grupo") val nomeGrupo: String,
    @SerialName("icone_grupo") val iconeGrupo: String, // ex: "ic_wrench" ou "ic_vest"
    @SerialName("estoque_total") val estoqueTotal: Double
)