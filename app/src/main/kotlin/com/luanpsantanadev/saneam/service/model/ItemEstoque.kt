package com.luanpsantanadev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemEstoque(
    val id: Long? = null,
    @SerialName("id_material") val idMaterial: Long,
    @SerialName("id_deposito") val idDeposito: Long,
    val quantidade: Double,
    @SerialName("quantidade_minima") val quantidadeMinima: Double = 0.0
) {
    val estaAbaixoDoEstoqueMinimo: Boolean
        get() = quantidade <= quantidadeMinima
}