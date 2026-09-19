package com.luandev.saneam.service.repository

import com.luandev.saneam.service.model.Perfil
import com.luandev.saneam.service.model.PermissaoUsuario
import com.luandev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioUsuario(private val cliente: SupabaseClient = SupabaseClientProvider.client) {

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

}