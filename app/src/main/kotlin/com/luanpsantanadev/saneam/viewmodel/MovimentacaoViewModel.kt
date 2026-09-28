package com.luanpsantanadev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.service.model.MovimentacaoEstoque
import com.luanpsantanadev.saneam.service.model.TipoMovimentacao
import com.luanpsantanadev.saneam.service.repository.RepositorioEstoque
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import com.luanpsantanadev.saneam.service.util.ehQuantidadeMovimentoValida
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

sealed class MovimentacaoStatus {
    object Parado : MovimentacaoStatus()
    object Carregando : MovimentacaoStatus()
    object Sucesso : MovimentacaoStatus()
    data class Erro(val mensagem: String) : MovimentacaoStatus()
}

class MovimentacaoViewModel(
    private val repositorio: RepositorioEstoque = RepositorioEstoque()
) : ViewModel() {

    private val _status = MutableLiveData<MovimentacaoStatus>(MovimentacaoStatus.Parado)
    val status: LiveData<MovimentacaoStatus> get() = _status

    /**
     * Executa uma movimentação simples: ENTRADA, SAIDA ou ACERTO
     */
    fun executarMovimentacao(
        tipo: TipoMovimentacao,
        idMaterial: Long,
        idDeposito: Long,
        quantidade: Double,
        motivo: String? = null
    ) {
        if (!quantidade.ehQuantidadeMovimentoValida()) {
            _status.value = MovimentacaoStatus.Erro("Informe uma quantidade válida maior que zero.")
            return
        }

        val idUsuario = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: run {
            _status.value = MovimentacaoStatus.Erro("Usuário não autenticado")
            return
        }

        _status.value = MovimentacaoStatus.Carregando

        val movimentacao = MovimentacaoEstoque(
            tipo = tipo,
            idMaterial = idMaterial,
            idDeposito = idDeposito,
            idUsuario = idUsuario,
            quantidade = quantidade,
            motivo = motivo
        )

        viewModelScope.launch {
            repositorio.executarMovimentacaoEstoque(movimentacao)
                .onSuccess {
                    _status.value = MovimentacaoStatus.Sucesso
                }
                .onFailure {
                    _status.value = MovimentacaoStatus.Erro(it.message ?: "Erro ao registrar movimentação")
                }
        }
    }

    /**
     * Executa uma transferência (SAIDA da origem -> ENTRADA no destino)
     */
    fun executarTransferencia(
        idMaterial: Long,
        idOrigem: Long,
        idDestino: Long,
        quantidade: Double,
        nomeMaterial: String,
        motivo: String
    ) {
        if (!quantidade.ehQuantidadeMovimentoValida()) {
            _status.value = MovimentacaoStatus.Erro("Informe uma quantidade válida maior que zero.")
            return
        }

        val idUsuario = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: run {
            _status.value = MovimentacaoStatus.Erro("Usuário não autenticado")
            return
        }

        if (idOrigem == idDestino) {
            _status.value = MovimentacaoStatus.Erro("Origem e destino devem ser diferentes")
            return
        }

        _status.value = MovimentacaoStatus.Carregando

        viewModelScope.launch {
            repositorio.transferirEstoque(
                idMaterial = idMaterial,
                idOrigem = idOrigem,
                idDestino = idDestino,
                quantidade = quantidade,
                idUsuario = idUsuario,
                motivo = motivo.ifEmpty { "Transferência de $nomeMaterial" }
            )
                .onSuccess {
                    _status.value = MovimentacaoStatus.Sucesso
                }
                .onFailure {
                    _status.value = MovimentacaoStatus.Erro(it.message ?: "Erro ao transferir estoque")
                }
        }
    }

    fun resetStatus() {
        _status.value = MovimentacaoStatus.Parado
    }
}