package com.luandev.saneam.service.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.aplicarInsetsBarrasSistema(
    topo: Boolean = false,
    inferior: Boolean = true,
    laterais: Boolean = false
) {
    val paddingInicial = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            paddingInicial[0] + if (laterais) barras.left else 0,
            paddingInicial[1] + if (topo) barras.top else 0,
            paddingInicial[2] + if (laterais) barras.right else 0,
            paddingInicial[3] + if (inferior) barras.bottom else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
