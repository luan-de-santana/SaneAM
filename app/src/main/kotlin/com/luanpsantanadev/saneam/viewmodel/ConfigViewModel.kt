package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.repository.RepositorioUsuario
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class ConfigViewModel(
    private val repositorioUsuario: RepositorioUsuario = RepositorioUsuario()
) : ViewModel() {

    private val _usuarioAdministrador = MutableLiveData(false)
    val usuarioAdministrador: LiveData<Boolean> get() = _usuarioAdministrador

    init {
        verificarAdministrador()
    }

    private fun verificarAdministrador() {
        viewModelScope.launch {
            val auth = SupabaseClientProvider.client.auth
            auth.awaitInitialization()
            val idUsuario = auth.currentUserOrNull()?.id

            if (idUsuario == null) {
                _usuarioAdministrador.value = false
                return@launch
            }

            repositorioUsuario.obterPerfil(idUsuario)
                .onSuccess { perfil ->
                    _usuarioAdministrador.value = perfil?.ehAdministrador == true
                }
                .onFailure {
                    _usuarioAdministrador.value = false
                }
        }
    }
}
