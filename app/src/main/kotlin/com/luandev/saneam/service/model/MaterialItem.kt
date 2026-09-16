package com.luandev.saneam.service.model

data class MaterialItem(
    val id: String,
    val nome: String,
    val deposito: String,
    val validade: String? = null,
    val quantidade: Int,
    val unidade: String, // ex: "un", "m", "kg"
    val quantidadeMinima: Int,
    val iconeRes: Int // ex: R.drawable.ic_wrench ou R.drawable.ic_vest
)