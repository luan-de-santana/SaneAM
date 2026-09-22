package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.model.ItemEstoque
import com.luanpsantanadev.saneam.service.repository.RepositorioEstoque
import kotlinx.coroutines.launch

class EstoqueViewModel(
    private val repositorio: RepositorioEstoque = RepositorioEstoque()
) : ViewModel() {

    private val _saldoAtual = MutableLiveData<Double?>(0.0)
    val saldoAtual: LiveData<Double?> get() = _saldoAtual

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    fun buscarSaldoAtual(idMaterial: Long, idDeposito: Long) {
        viewModelScope.launch {
            repositorio.obterItemEstoque(idMaterial, idDeposito)
                .onSuccess { item ->
                    _saldoAtual.value = item?.quantidade ?: 0.0
                }
                .onFailure {
                    _erro.value = "Erro ao buscar saldo: ${it.message}"
                }
        }
    }
}