package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.model.ResumoMaterialGrupo
import com.luanpsantanadev.saneam.service.repository.RepositorioMaterial
import kotlinx.coroutines.launch

class MaterialSelectorViewModel(
    private val repositorio: RepositorioMaterial = RepositorioMaterial()
) : ViewModel() {

    private val _materiais = MutableLiveData<List<ResumoMaterialGrupo>>()
    val materiais: LiveData<List<ResumoMaterialGrupo>> get() = _materiais

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    fun buscarMateriais(busca: String) {
        _carregando.value = true
        viewModelScope.launch {
            repositorio.buscarMateriais(busca)
                .onSuccess { 
                    _materiais.value = it
                    _carregando.value = false
                }
                .onFailure { 
                    _carregando.value = false
                }
        }
    }
}