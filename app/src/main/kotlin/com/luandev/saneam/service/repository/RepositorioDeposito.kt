package com.luandev.saneam.service.repository

import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioDeposito(private val cliente: SupabaseClient = SupabaseClientProvider.client) {

    suspend fun criarLocal(deposito: Deposito): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.DEPOSITOS].insert(deposito)
        }
    }

    suspend fun obterTodosDepositos(): Result<List<Deposito>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.DEPOSITOS].select {
                order(Supabase.COL_NOME, Order.ASCENDING)
            }.decodeList<Deposito>()
        }
    }

}