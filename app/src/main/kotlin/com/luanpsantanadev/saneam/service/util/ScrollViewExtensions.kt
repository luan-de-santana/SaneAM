package com.luanpsantanadev.saneam.service.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Configura o ScrollView para rolar automaticamente quando um campo específico ganha foco e o teclado abre.
 * 
 * @param campoParaRolar O campo que deve ficar visível quando o teclado abrir
 * @param contentLayout O layout de conteúdo que precisa ter o padding ajustado
 * @param scrollView O ScrollView que será rolado
 * @param onTecladoFechado Ação opcional a executar quando o teclado fechar
 */
fun View.configurarScrollComTeclado(
    campoParaRolar: View,
    contentLayout: View,
    scrollView: View,
    onTecladoFechado: (() -> Unit)? = null
) {
    var shouldScrollToCampo = false

    campoParaRolar.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            shouldScrollToCampo = true
        }
    }

    campoParaRolar.setOnClickListener {
        shouldScrollToCampo = true
    }

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        
        view.setPadding(barras.left, barras.top, barras.right, barras.bottom)

        val tecladoVisivel = insets.isVisible(WindowInsetsCompat.Type.ime())

        // Garante margem de respiro embaixo do conteúdo enquanto o teclado estiver aberto
        val basePadding = (20 * view.resources.displayMetrics.density).toInt()
        val extraScrollPadding = if (tecladoVisivel) (30 * view.resources.displayMetrics.density).toInt() else 0

        contentLayout.setPadding(
            contentLayout.paddingLeft,
            contentLayout.paddingTop,
            contentLayout.paddingRight,
            basePadding + ime.bottom + extraScrollPadding
        )

        if (tecladoVisivel) {
            if (shouldScrollToCampo && campoParaRolar.hasFocus()) {
                view.post {
                    val location = IntArray(2)
                    campoParaRolar.getLocationInWindow(location)
                    val fieldBottom = location[1] + campoParaRolar.height
                    val screenHeight = view.resources.displayMetrics.heightPixels
                    val keyboardHeight = ime.bottom
                    val targetBottom = screenHeight - keyboardHeight - 50
                    val currentScrollY = (scrollView as? android.widget.ScrollView)?.scrollY ?: 0
                    val scrollAmount = fieldBottom - targetBottom
                    if (scrollAmount > 0) {
                        scrollView.scrollTo(0, currentScrollY + scrollAmount)
                    }
                    shouldScrollToCampo = false
                }
            }
        } else {
            onTecladoFechado?.invoke()
        }

        insets
    }
    ViewCompat.requestApplyInsets(this)
}
