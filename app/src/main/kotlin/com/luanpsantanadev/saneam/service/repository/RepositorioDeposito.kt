package com.luanpsantanadev.saneam.service.repository

import com.luanpsantanadev.saneam.SaneAMApplication
import com.luanpsantanadev.saneam.service.model.Deposito
import com.luanpsantanadev.saneam.service.model.DepositoComAcesso
import com.luanpsantanadev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioDeposito(
    private val cliente: SupabaseClient = SupabaseClientProvider.client,
    private val cache: DepositoCache = DepositoCache(SaneAMApplication.instance)
) {

    suspend fun criarLocal(deposito: Deposito): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.DEPOSITOS].insert(deposito)
        }
    }

    suspend fun obterTodosDepositos(): Result<List<Deposito>> = runCatching {
        val auth = cliente.auth
        auth.awaitInitialization()
        val idUsuario = auth.currentUserOrNull()?.id
            ?: error("Usuário não autenticado para carregar depósitos.")
        cacheMemoria[idUsuario]?.takeIf {
            System.currentTimeMillis() - it.atualizadoEm < CACHE_VALIDADE_MS
        }?.let {
            return@runCatching it.depositos
        }
        cache.ler(idUsuario)?.let {
            cacheMemoria[idUsuario] = CacheMemoria(it, System.currentTimeMillis())
            return@runCatching it
        }

        withContext(Dispatchers.IO) {
            val depositos = cliente.postgrest[Supabase.DEPOSITOS].select {
                order(Supabase.COL_NOME, Order.ASCENDING)
            }.decodeList<Deposito>()
            cacheMemoria[idUsuario] = CacheMemoria(depositos, System.currentTimeMillis())
            cache.salvar(idUsuario, depositos)
            depositos
        }
    }

    suspend fun obterDepositosComAcesso(): Result<List<DepositoComAcesso>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest
                .rpc(Supabase.RPC_OBTER_DEPOSITOS_COM_ACESSO)
                .decodeList<DepositoComAcesso>()
        }
    }

    suspend fun invalidarCache(idUsuario: String) {
        cacheMemoria.remove(idUsuario)
        cache.limpar(idUsuario)
    }

    companion object {
        private const val CACHE_VALIDADE_MS = 6 * 60 * 60 * 1000L
        private val cacheMemoria = mutableMapOf<String, CacheMemoria>()

        fun invalidarCacheEmMemoria(idUsuario: String) {
            cacheMemoria.remove(idUsuario)
        }
    }

}

private data class CacheMemoria(
    val depositos: List<Deposito>,
    val atualizadoEm: Long
)