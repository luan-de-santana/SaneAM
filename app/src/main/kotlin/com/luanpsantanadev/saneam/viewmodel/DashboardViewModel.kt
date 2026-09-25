package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.model.DepositoComAcesso
import com.luanpsantanadev.saneam.service.model.GrupoComContagem
import com.luanpsantanadev.saneam.service.model.ItemEstoqueBaixo
import com.luanpsantanadev.saneam.service.model.ResumoDashboard
import com.luanpsantanadev.saneam.service.repository.RepositorioDeposito
import com.luanpsantanadev.saneam.service.repository.RepositorioDashboard
import com.luanpsantanadev.saneam.service.repository.RepositorioEstoque
import com.luanpsantanadev.saneam.service.repository.RepositorioMaterial
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repositorio: RepositorioDashboard = RepositorioDashboard(),
    private val repositorioDeposito: RepositorioDeposito = RepositorioDeposito(),
    private val repositorioMaterial: RepositorioMaterial = RepositorioMaterial(),
    private val repositorioEstoque: RepositorioEstoque = RepositorioEstoque()
) : ViewModel() {

    private val _resumo = MutableLiveData<ResumoDashboard>()
    val resumo: LiveData<ResumoDashboard> get() = _resumo

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    private val _carregandoResumo = MutableLiveData(false)
    val carregandoResumo: LiveData<Boolean> get() = _carregandoResumo

    init {
        carregarResumo()
    }

    fun carregarResumo() {
        if (_carregandoResumo.value == true) return

        _carregandoResumo.value = true
        viewModelScope.launch {
                try {
                    repositorio.obterResumoDashboard()
                        .onSuccess {
                            _resumo.value = it
                        }
                        .onFailure {
                            _erro.value = it.message ?: "Erro ao carregar resumo do dashboard"
                        }
                } finally {
                    _carregandoResumo.value = false
                }
        }
    }

    suspend fun obterDepositosComAcesso(): Result<List<DepositoComAcesso>> =
        repositorioDeposito.obterDepositosComAcesso()

    suspend fun obterGruposComContagem(): Result<List<GrupoComContagem>> =
        repositorioMaterial.obterGruposComContagem()

    suspend fun obterItensEstoqueCritico(): Result<List<ItemEstoqueBaixo>> = runCatching {
        val depositos = repositorioDeposito.obterDepositosComAcesso().getOrThrow()
        depositos.flatMap { deposito ->
            repositorioEstoque.obterItensComEstoqueBaixo(deposito.id).getOrThrow()
        }.sortedWith(
            compareBy<ItemEstoqueBaixo> { it.percentualRestante }
                .thenBy { it.nomeMaterial.lowercase() }
        )
    }
}