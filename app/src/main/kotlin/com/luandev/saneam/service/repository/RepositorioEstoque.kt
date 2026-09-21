package com.luandev.saneam.service.repository

import com.luandev.saneam.service.model.ItemEstoque
import com.luandev.saneam.service.model.ItemEstoqueBaixo
import com.luandev.saneam.service.model.MovimentacaoEstoque
import com.luandev.saneam.service.model.TipoMovimentacao
import com.luandev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioEstoque(private val cliente: SupabaseClient = SupabaseClientProvider.client) {

    suspend fun obterItemEstoque(idMaterial: Long, idDeposito: Long): Result<ItemEstoque?> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.ESTOQUES].select {
                filter {
                    eq(Supabase.COL_ID_MATERIAL, idMaterial)
                    eq(Supabase.COL_ID_DEPOSITO, idDeposito)
                }
            }.decodeSingleOrNull<ItemEstoque>()
        }
    }

    suspend fun executarMovimentacaoEstoque(movimentacao: MovimentacaoEstoque): Result<Unit> = runCatching {
        require(movimentacao.quantidade.isFinite() && movimentacao.quantidade > 0.0) {
            "Quantidade inválida. Informe um valor numérico maior que zero."
        }

        withContext(Dispatchers.IO) {
            val estoqueAtual = obterItemEstoque(movimentacao.idMaterial, movimentacao.idDeposito).getOrThrow()

            val quantidadeAtual = estoqueAtual?.quantidade ?: 0.0
            val novaQuantidade = when (movimentacao.tipo) {
                TipoMovimentacao.ENTRADA -> quantidadeAtual + movimentacao.quantidade
                TipoMovimentacao.SAIDA -> {
                    val restante = quantidadeAtual - movimentacao.quantidade
                    if (restante < 0) throw IllegalStateException("Estoque insuficiente!")
                    restante
                }
                TipoMovimentacao.ACERTO -> movimentacao.quantidade
            }

            val estoqueAtualizado = ItemEstoque(
                id = estoqueAtual?.id,
                idMaterial = movimentacao.idMaterial,
                idDeposito = movimentacao.idDeposito,
                quantidade = novaQuantidade,
                quantidadeMinima = estoqueAtual?.quantidadeMinima ?: 0.0
            )

            cliente.postgrest[Supabase.ESTOQUES].upsert(estoqueAtualizado)
            cliente.postgrest[Supabase.MOVIMENTACOES].insert(movimentacao)
        }
    }

    suspend fun transferirEstoque(
        idMaterial: Long,
        idOrigem: Long,
        idDestino: Long,
        quantidade: Double,
        idUsuario: String,
        motivo: String? = null
    ): Result<Unit> = runCatching {
        require(quantidade > 0) { "Quantidade deve ser maior que zero." }

        withContext(Dispatchers.IO) {
            cliente.postgrest.rpc(
                Supabase.RPC_TRANSFERIR_ESTOQUE,
                mapOf(
                    "p_id_material" to idMaterial,
                    "p_id_origem" to idOrigem,
                    "p_id_destino" to idDestino,
                    "p_quantidade" to quantidade,
                    "p_id_usuario" to idUsuario,
                    "p_motivo" to motivo
                )
            )
        }
    }

    suspend fun obterItensComEstoqueBaixo(idDeposito: Long? = null): Result<List<ItemEstoqueBaixo>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.VISAO_ITENS_ESTOQUE_BAIXO]
                .select {
                    // Aplica o filtro apenas se idLocal for informado
                    idDeposito?.let { id ->
                        filter { eq(Supabase.COL_ID_DEPOSITO, id) }
                    }
                    // Ordena do menor percentual crítico para o maior
                    order(Supabase.COL_PERCENTUAL_RESTANTE, Order.ASCENDING)
                }.decodeList<ItemEstoqueBaixo>()
        }
    }

}