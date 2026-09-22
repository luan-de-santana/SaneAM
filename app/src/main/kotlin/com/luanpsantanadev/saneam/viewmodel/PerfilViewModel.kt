package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class PerfilUsuario(
    val nome: String,
    val urlFoto: String?
)

class PerfilViewModel : ViewModel() {

    private val _perfil = MutableLiveData<PerfilUsuario?>()
    val perfil: LiveData<PerfilUsuario?> get() = _perfil

    private var perfilCarregado = false

    fun carregarPerfil() {
        if (perfilCarregado) return

        val usuario = SupabaseClientProvider.client.auth.currentUserOrNull()
        val metadados = usuario?.userMetadata
        val nome = metadados?.get("nome")?.jsonPrimitive?.contentOrNull
            ?: metadados?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadados?.get("name")?.jsonPrimitive?.contentOrNull
            ?: "Usuário"
        val urlFoto = metadados?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadados?.get("picture")?.jsonPrimitive?.contentOrNull

        _perfil.value = PerfilUsuario(nome, urlFoto)
        perfilCarregado = true
    }

    fun limparPerfil() {
        _perfil.value = null
        perfilCarregado = false
    }
}
