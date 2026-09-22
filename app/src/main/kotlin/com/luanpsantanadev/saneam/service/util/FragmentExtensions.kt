package com.luanpsantanadev.saneam.service.util

import android.widget.ImageView
import android.widget.TextView
import coil.load
import coil.transform.CircleCropTransformation
import com.luanpsantanadev.saneam.viewmodel.PerfilUsuario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Preenche os campos de saudação e data atual seguindo o padrão do app.
 */
fun configurarCabecalhoPadrao(
    textSaudacao: TextView,
    textData: TextView,
    imagemPerfil: ImageView,
    perfil: PerfilUsuario
) {
    imagemPerfil.load(perfil.urlFoto) {
        placeholder(com.luanpsantanadev.saneam.R.drawable.ic_person)
        error(com.luanpsantanadev.saneam.R.drawable.ic_person)
        transformations(CircleCropTransformation())
    }

    // Definir saudação baseada no horário
    val horaAtual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saudacao = when (horaAtual) {
        in 0..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    val saudacaoFinal = "$saudacao, ${perfil.nome}"
    textSaudacao.text = saudacaoFinal

    val localePT = Locale.forLanguageTag("pt-BR")
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", localePT)
    textData.text = sdf.format(Calendar.getInstance().time)
}