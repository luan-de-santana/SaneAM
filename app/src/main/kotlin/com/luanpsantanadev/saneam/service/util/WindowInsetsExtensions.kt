package com.luanpsantanadev.saneam.service.util

import android.graphics.Rect
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

fun View.rolarCampoFocadoComTeclado() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
            view.post {
                val campoFocado = view.findFocus()
                if (campoFocado != null && campoFocado !== view) {
                    campoFocado.requestRectangleOnScreen(
                        Rect(0, 0, campoFocado.width, campoFocado.height),
                        true
                    )
                }
            }
        }
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
