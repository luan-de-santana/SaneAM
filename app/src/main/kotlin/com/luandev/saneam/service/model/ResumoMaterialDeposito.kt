package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumoMaterialDeposito(
    @SerialName("id_estoque") val idEstoque: Long,
    @SerialName("id_material") val idMaterial: Long,
    @SerialName("codigo_alpha") val codigoAlpha: Int,
    @SerialName("nome_material") val nomeMaterial: String,
    @SerialName("unidade_medida") val unidadeMedida: String,
    @SerialName("id_grupo") val idGrupo: Long,
    @SerialName("nome_grupo") val nomeGrupo: String,
    @SerialName("icone_grupo") val iconeGrupo: String, // ex: "ic_wrench" ou "ic_vest"
    @SerialName("id_deposito") val idDeposito: Long,
    @SerialName("nome_deposito") val nomeDeposito: String,
    val quantidade: Double,
    @SerialName("quantidade_minima") val quantidadeMinima: Double
)