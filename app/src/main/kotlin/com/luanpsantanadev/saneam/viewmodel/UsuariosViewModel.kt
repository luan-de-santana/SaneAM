package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.model.Perfil
import com.luanpsantanadev.saneam.service.repository.RepositorioUsuario
import kotlinx.coroutines.launch

class UsuariosViewModel(
    private val repositorio: RepositorioUsuario = RepositorioUsuario()
) : ViewModel() {

    private val _usuarios = MutableLiveData<List<Perfil>>()
    val usuarios: LiveData<List<Perfil>> get() = _usuarios
    private val _carregando = MutableLiveData(false)
    val carregando: LiveData<Boolean> get() = _carregando
    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    init {
        carregarUsuarios()
    }

    private fun carregarUsuarios() {
        _carregando.value = true
        viewModelScope.launch {
            repositorio.obterTodosPerfis()
                .onSuccess { _usuarios.value = it }
                .onFailure { _erro.value = it.message ?: "Não foi possível carregar os usuários." }
            _carregando.value = false
        }
    }
}
