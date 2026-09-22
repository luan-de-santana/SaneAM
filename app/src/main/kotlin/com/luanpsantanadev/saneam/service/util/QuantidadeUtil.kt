package com.luanpsantanadev.saneam.service.util

fun parseQuantidadeMovimentacao(texto: String?): Double? {
    if (texto.isNullOrBlank()) return null

    val quantidade = texto.trim().replace(',', '.').toDoubleOrNull() ?: return null
    if (!quantidade.isFinite() || quantidade <= 0.0) return null

    return quantidade
}

fun Double?.ehQuantidadeMovimentoValida(): Boolean =
    this != null && isFinite() && this > 0.0
