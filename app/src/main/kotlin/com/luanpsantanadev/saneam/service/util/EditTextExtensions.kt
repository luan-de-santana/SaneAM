package com.luanpsantanadev.saneam.service.util

import android.text.InputFilter
import android.widget.EditText

fun EditText.adicionarFiltroNumerico() {
    // Filtro que aceita apenas dígitos de 0 a 9
    val filtroApenasNumeros = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (!Character.isDigit(source[i])) {
                return@InputFilter "" // Rejeita o caractere ou texto colado
            }
        }
        null // Aceita a entrada se forem apenas números
    }

    filters = arrayOf(filtroApenasNumeros)
}