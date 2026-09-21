package com.luandev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.repository.RepositorioDeposito
import kotlinx.coroutines.launch

class DepositoSelectorViewModel(
    private val repositorio: RepositorioDeposito = RepositorioDeposito()
) : ViewModel() {

    private val _depositos = MutableLiveData<List<Deposito>>()
    val depositos: LiveData<List<Deposito>> get() = _depositos

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    private val _carregando = MutableLiveData(false)
    val carregando: LiveData<Boolean> get() = _carregando

    init {
        carregarDepositos()
    }

    private fun carregarDepositos() {
        _carregando.value = true
        viewModelScope.launch {
            repositorio.obterTodosDepositos()
                .onSuccess { _depositos.value = it }
                .onFailure { _erro.value = it.message ?: "Erro ao carregar depósitos" }
                .also { _carregando.value = false }
        }
    }
}