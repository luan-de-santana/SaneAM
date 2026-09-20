package com.luandev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luandev.saneam.service.model.MovimentacaoEstoque
import com.luandev.saneam.service.model.TipoMovimentacao
import com.luandev.saneam.service.repository.RepositorioEstoque
import com.luandev.saneam.service.repository.SupabaseClientProvider
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
        nomeMaterial: String
    ) {
        val idUsuario = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: run {
            _status.value = MovimentacaoStatus.Erro("Usuário não autenticado")
            return
        }

        _status.value = MovimentacaoStatus.Carregando

        viewModelScope.launch {
            // 1. Registrar Saída da Origem
            val movSaida = MovimentacaoEstoque(
                tipo = TipoMovimentacao.SAIDA,
                idMaterial = idMaterial,
                idDeposito = idOrigem,
                idUsuario = idUsuario,
                quantidade = quantidade,
                motivo = "Transferência de $nomeMaterial para destino"
            )

            repositorio.executarMovimentacaoEstoque(movSaida)
                .onSuccess {
                    // 2. Registrar Entrada no Destino
                    val movEntrada = MovimentacaoEstoque(
                        tipo = TipoMovimentacao.ENTRADA,
                        idMaterial = idMaterial,
                        idDeposito = idDestino,
                        idUsuario = idUsuario,
                        quantidade = quantidade,
                        motivo = "Transferência de $nomeMaterial da origem"
                    )

                    repositorio.executarMovimentacaoEstoque(movEntrada)
                        .onSuccess {
                            _status.value = MovimentacaoStatus.Sucesso
                        }
                        .onFailure {
                            _status.value = MovimentacaoStatus.Erro("Saída registrada, mas erro na entrada: ${it.message}")
                        }
                }
                .onFailure {
                    _status.value = MovimentacaoStatus.Erro("Erro na saída da origem: ${it.message}")
                }
        }
    }

    fun resetStatus() {
        _status.value = MovimentacaoStatus.Parado
    }
}