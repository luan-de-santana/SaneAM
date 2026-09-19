package com.luandev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.model.ResumoMaterialDeposito
import com.luandev.saneam.service.repository.RepositorioDeposito
import com.luandev.saneam.service.repository.RepositorioMaterial
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val repositorioDeposito: RepositorioDeposito = RepositorioDeposito(),
    private val repositorioMaterial: RepositorioMaterial = RepositorioMaterial()
) : ViewModel() {

    private val _depositos = MutableLiveData<List<Deposito>>()
    val depositos: LiveData<List<Deposito>> get() = _depositos

    private val _materiais = MutableLiveData<List<ResumoMaterialDeposito>>()
    val materiais: LiveData<List<ResumoMaterialDeposito>> get() = _materiais

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    init {
        carregarDepositos()
        buscarMateriais("", null)
    }

    fun carregarDepositos() {
        viewModelScope.launch {
            repositorioDeposito.obterTodosDepositos()
                .onSuccess { _depositos.value = it }
                .onFailure { _erro.value = "Erro ao carregar depósitos" }
        }
    }

    fun buscarMateriais(busca: String, idDeposito: Long?) {
        _carregando.value = true
        viewModelScope.launch {
            repositorioMaterial.buscarResumoMateriaisPorDeposito(busca, idDeposito)
                .onSuccess { 
                    _materiais.value = it
                    _carregando.value = false
                }
                .onFailure { 
                    _erro.value = "Erro ao carregar materiais"
                    _carregando.value = false
                }
        }
    }
}