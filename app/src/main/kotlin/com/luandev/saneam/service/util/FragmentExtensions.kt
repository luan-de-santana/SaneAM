package com.luandev.saneam.service.util

import android.widget.TextView
import com.luandev.saneam.service.repository.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Preenche os campos de saudação e data atual seguindo o padrão do app.
 */
fun configurarCabecalhoPadrao(textSaudacao: TextView, textData: TextView) {
    // Obter nome do usuário do Supabase
    val usuario = SupabaseClientProvider.client.auth.currentUserOrNull()
    val nomeUsuario = usuario?.userMetadata?.get("nome")?.jsonPrimitive?.content ?: "Usuário"

    // Definir saudação baseada no horário
    val horaAtual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saudacao = when (horaAtual) {
        in 0..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    val saudacaoFinal = "$saudacao, $nomeUsuario"
    textSaudacao.text = saudacaoFinal

    // Formatar data atual
    val localePT = Locale.forLanguageTag("pt-BR")
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", localePT)
    textData.text = sdf.format(Calendar.getInstance().time)
}