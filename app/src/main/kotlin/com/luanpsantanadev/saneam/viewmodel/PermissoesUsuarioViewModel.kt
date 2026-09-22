package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.model.Deposito
import com.luanpsantanadev.saneam.service.model.PapelUsuario
import com.luanpsantanadev.saneam.service.model.PermissaoUsuario
import com.luanpsantanadev.saneam.service.repository.RepositorioDeposito
import com.luanpsantanadev.saneam.service.repository.RepositorioUsuario
import kotlinx.coroutines.launch

data class PermissaoDepositoUi(val deposito: Deposito, var papel: PapelUsuario?)

class PermissoesUsuarioViewModel(
    private val repositorioUsuario: RepositorioUsuario = RepositorioUsuario(),
    private val repositorioDeposito: RepositorioDeposito = RepositorioDeposito()
) : ViewModel() {

    private val _itens = MutableLiveData<List<PermissaoDepositoUi>>()
    val itens: LiveData<List<PermissaoDepositoUi>> get() = _itens
    private val _carregando = MutableLiveData(false)
    val carregando: LiveData<Boolean> get() = _carregando
    private val _salvando = MutableLiveData(false)
    val salvando: LiveData<Boolean> get() = _salvando
    private val _mensagem = MutableLiveData<String>()
    val mensagem: LiveData<String> get() = _mensagem
    private val _salvo = MutableLiveData(false)
    val salvo: LiveData<Boolean> get() = _salvo

    fun carregar(idUsuario: String) {
        _carregando.value = true
        viewModelScope.launch {
            val depositos = repositorioDeposito.obterTodosDepositos()
            val permissoes = repositorioUsuario.obterPermissoesDoUsuario(idUsuario)
            if (depositos.isFailure) {
                _mensagem.value = depositos.exceptionOrNull()?.message
                    ?: "Não foi possível carregar os depósitos."
            } else if (permissoes.isFailure) {
                _mensagem.value = permissoes.exceptionOrNull()?.message
                    ?: "Não foi possível carregar as permissões."
            } else {
                val porDeposito = permissoes.getOrThrow().associateBy { it.idDeposito }
                _itens.value = depositos.getOrThrow().map { deposito ->
                    PermissaoDepositoUi(deposito, porDeposito[deposito.id]?.papel)
                }
            }
            _carregando.value = false
        }
    }

    fun salvar(idUsuario: String, itens: List<PermissaoDepositoUi>) {
        _salvando.value = true
        viewModelScope.launch {
            val permissoes = itens.mapNotNull { item ->
                val depositoId = item.deposito.id ?: return@mapNotNull null
                item.papel?.let { papel ->
                    PermissaoUsuario(
                        idUsuario = idUsuario,
                        idDeposito = depositoId,
                        papel = papel
                    )
                }
            }
            repositorioUsuario.substituirPermissoes(idUsuario, permissoes)
                .onSuccess {
                    _salvo.value = true
                    _mensagem.value = "Acessos atualizados com sucesso."
                }
                .onFailure {
                    _salvo.value = false
                    _mensagem.value = it.message ?: "Não foi possível salvar os acessos."
                }
            _salvando.value = false
        }
    }
}
