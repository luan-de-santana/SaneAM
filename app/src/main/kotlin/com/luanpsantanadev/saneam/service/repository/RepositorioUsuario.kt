package com.luanpsantanadev.saneam.service.repository

import com.luanpsantanadev.saneam.service.model.Perfil
import com.luanpsantanadev.saneam.service.model.PermissaoUsuario
import com.luanpsantanadev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioUsuario(private val cliente: SupabaseClient = SupabaseClientProvider.client) {
    private val depositoCache = DepositoCache(com.luanpsantanadev.saneam.SaneAMApplication.instance)

    suspend fun obterTodosPerfis(): Result<List<Perfil>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.PERFIS]
                .select { order(Supabase.COL_NOME, Order.ASCENDING) }
                .decodeList<Perfil>()
        }
    }

    suspend fun obterPerfil(idUsuario: String): Result<Perfil?> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.PERFIS]
                .select { filter { eq(Supabase.COL_ID, idUsuario) } }
                .decodeSingleOrNull<Perfil>()
        }
    }

    suspend fun obterPermissoesDoUsuario(idUsuario: String): Result<List<PermissaoUsuario>> =
        runCatching {
            withContext(Dispatchers.IO) {
                cliente.postgrest[Supabase.PERMISSOES_USUARIO]
                    .select { filter { eq(Supabase.COL_ID_USUARIO, idUsuario) } }
                    .decodeList<PermissaoUsuario>()
            }
        }

    suspend fun substituirPermissoes(
        idUsuario: String,
        permissoes: List<PermissaoUsuario>
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.PERMISSOES_USUARIO].delete {
                filter { eq(Supabase.COL_ID_USUARIO, idUsuario) }
            }
            if (permissoes.isNotEmpty()) {
                cliente.postgrest[Supabase.PERMISSOES_USUARIO].insert(permissoes)
            }
            depositoCache.limpar(idUsuario)
        }
    }

}