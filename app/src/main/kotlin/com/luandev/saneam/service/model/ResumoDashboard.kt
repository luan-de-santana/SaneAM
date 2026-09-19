package com.luandev.saneam.service.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumoDashboard(
    @SerialName("total_depositos") val totalDepositos: Long,
    @SerialName("total_grupos") val totalGrupos: Long,
    @SerialName("total_materiais") val totalMateriais: Long,
    @SerialName("total_materiais_baixo_estoque") val totalMateriaisBaixoEstoque: Long
)