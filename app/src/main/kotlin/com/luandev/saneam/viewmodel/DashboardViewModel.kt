package com.luandev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luandev.saneam.service.model.ResumoDashboard
import com.luandev.saneam.service.repository.RepositorioDasboard
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repositorio: RepositorioDasboard = RepositorioDasboard()
) : ViewModel() {

    private val _resumo = MutableLiveData<ResumoDashboard>()
    val resumo: LiveData<ResumoDashboard> get() = _resumo

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    init {
        carregarResumo()
    }

    fun carregarResumo() {
        viewModelScope.launch {
            repositorio.obterResumoDashboard()
                .onSuccess {
                    _resumo.value = it
                }
                .onFailure {
                    _erro.value = it.message ?: "Erro ao carregar resumo do dashboard"
                }
        }
    }
}