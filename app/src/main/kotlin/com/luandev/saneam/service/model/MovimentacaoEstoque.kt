package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovimentacaoEstoque(
    val id: Long? = null,
    @SerialName("criado_em") val criadoEm: String? = null,
    val tipo: TipoMovimentacao,
    @SerialName("id_material") val idMaterial: Long,
    @SerialName("id_deposito") val idDeposito: Long,
    @SerialName("id_usuario") val idUsuario: String,
    val quantidade: Double,
    val motivo: String? = null
)

@Serializable
enum class TipoMovimentacao {
    @SerialName("ENTRADA") ENTRADA,
    @SerialName("SAIDA") SAIDA,
    @SerialName("ACERTO") ACERTO
}