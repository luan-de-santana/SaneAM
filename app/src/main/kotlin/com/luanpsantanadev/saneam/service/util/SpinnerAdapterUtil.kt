package com.luanpsantanadev.saneam.service.util

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

fun Context.criarAdapterSpinnerEscuro(itens: List<String>): ArrayAdapter<String> {
    return object : ArrayAdapter<String>(
        this,
        android.R.layout.simple_spinner_item,
        itens
    ) {
        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            return aplicarCorTexto(super.getView(position, convertView, parent))
        }

        override fun getDropDownView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            return aplicarCorTexto(super.getDropDownView(position, convertView, parent))
        }

        private fun aplicarCorTexto(view: View): View {
            (view as? TextView)?.setTextColor(Color.rgb(30, 41, 59))
            return view
        }
    }.also {
        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}
