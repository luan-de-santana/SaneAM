package com.luanpsantanadev.saneam.service.repository

import com.luanpsantanadev.saneam.service.model.ItemEstoque
import com.luanpsantanadev.saneam.service.model.ItemEstoqueBaixo
import com.luanpsantanadev.saneam.service.model.MovimentacaoEstoque
import com.luanpsantanadev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
private data class ExecutarMovimentacaoParams(
    @SerialName("p_tipo") val tipo: String,
    @SerialName("p_id_material") val idMaterial: Long,
    @SerialName("p_id_deposito") val idDeposito: Long,
    @SerialName("p_quantidade") val quantidade: Double,
    @SerialName("p_motivo") val motivo: String?
)

@Serializable
private data class TransferirEstoqueParams(
    @SerialName("p_id_material") val idMaterial: Long,
    @SerialName("p_id_origem") val idOrigem: Long,
    @SerialName("p_id_destino") val idDestino: Long,
    @SerialName("p_quantidade") val quantidade: Double,
    @SerialName("p_id_usuario") val idUsuario: String,
    @SerialName("p_motivo") val motivo: String?
)

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
            cliente.postgrest.rpc(
                Supabase.RPC_EXECUTAR_MOVIMENTACAO_ESTOQUE,
                ExecutarMovimentacaoParams(
                    tipo = movimentacao.tipo.name,
                    idMaterial = movimentacao.idMaterial,
                    idDeposito = movimentacao.idDeposito,
                    quantidade = movimentacao.quantidade,
                    motivo = movimentacao.motivo
                )
            )
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
                TransferirEstoqueParams(
                    idMaterial = idMaterial,
                    idOrigem = idOrigem,
                    idDestino = idDestino,
                    quantidade = quantidade,
                    idUsuario = idUsuario,
                    motivo = motivo
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