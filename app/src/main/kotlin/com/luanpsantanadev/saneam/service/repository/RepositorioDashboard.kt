package com.luanpsantanadev.saneam.service.repository

import com.luanpsantanadev.saneam.service.model.ResumoDashboard
import com.luanpsantanadev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioDashboard(private val cliente: SupabaseClient = SupabaseClientProvider.client) {

    suspend fun obterResumoDashboard(): Result<ResumoDashboard> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest
                .rpc(Supabase.RPC_OBTER_RESUMO_DASHBOARD)
                .decodeSingle<ResumoDashboard>()
        }
    }

}