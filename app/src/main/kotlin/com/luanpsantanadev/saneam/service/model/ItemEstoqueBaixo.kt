package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemEstoqueBaixo(
    val id: Long,
    @SerialName("id_material") val idMaterial: Long,
    @SerialName("nome_material") val nomeMaterial: String,
    @SerialName("id_deposito") val idDeposito: Long,
    @SerialName("nome_deposito") val nomeDeposito: String,
    val quantidade: Double,
    @SerialName("quantidade_minima") val quantidadeMinima: Double,
    @SerialName("percentual_restante") val percentualRestante: Double
)