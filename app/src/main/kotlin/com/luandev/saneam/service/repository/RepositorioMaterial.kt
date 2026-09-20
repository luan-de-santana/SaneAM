package com.luandev.saneam.service.repository

import com.luandev.saneam.service.model.Grupo
import com.luandev.saneam.service.model.Material
import com.luandev.saneam.service.model.ResumoMaterial
import com.luandev.saneam.service.model.ResumoMaterialDeposito
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.service.util.ConstantsSaneAM.Supabase
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositorioMaterial(private val cliente: SupabaseClient = SupabaseClientProvider.client) {

    suspend fun criarGrupo(grupo: Grupo): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.GRUPOS].insert(grupo)
        }
    }

    suspend fun obterTodosGrupos(): Result<List<Grupo>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.GRUPOS].select().decodeList<Grupo>()
        }
    }

    suspend fun criarMaterial(material: Material): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.MATERIAIS].insert(material)
        }
    }

    suspend fun obterTodosMateriais(): Result<List<Material>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.MATERIAIS].select().decodeList<Material>()
        }
    }

    suspend fun obterMaterialPorCodigoAlpha(codigoAlpha: Int): Result<Material?> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.MATERIAIS]
                .select { filter { eq(Supabase.COL_CODIGO_ALPHA, codigoAlpha) } }
                .decodeSingleOrNull<Material>()
        }
    }

    suspend fun obterResumoTodosMateriais(): Result<List<ResumoMaterial>> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.VISAO_RESUMO_MATERIAIS]
                .select()
                .decodeList<ResumoMaterial>()
        }
    }

    suspend fun obterResumoMateriaisPaginado(
        pagina: Int,
        tamanhoPagina: Int = 20
    ): Result<List<ResumoMaterial>> = runCatching {
        withContext(Dispatchers.IO) {
            val de = pagina * tamanhoPagina
            val ate = de + tamanhoPagina - 1

            cliente.postgrest[Supabase.VISAO_RESUMO_MATERIAIS]
                .select {
                    // Ordena por nome do material para manter consistência entre páginas
                    order(Supabase.COL_NOME_MATERIAL, Order.ASCENDING)
                    // Define o intervalo de linhas a serem buscadas
                    range(from = de.toLong(), to = ate.toLong())
                }.decodeList<ResumoMaterial>()
        }
    }

    suspend fun obterResumoMaterialPorCodigoAlpha(codigoAlpha: Int): Result<ResumoMaterial?> =
        runCatching {
            withContext(Dispatchers.IO) {
                cliente.postgrest[Supabase.VISAO_RESUMO_MATERIAIS]
                    .select {
                        filter { eq(Supabase.COL_CODIGO_ALPHA, codigoAlpha) }
                    }
                    .decodeSingleOrNull<ResumoMaterial>()
            }
        }

    suspend fun atualizarMaterial(id: Long, material: Material): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.MATERIAIS].update(material) {
                filter { eq(Supabase.COL_ID, id) }
            }
        }
    }

    suspend fun deletarMaterial(id: Long): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            cliente.postgrest[Supabase.MATERIAIS].delete {
                filter { eq(Supabase.COL_ID, id) }
            }
        }
    }

    suspend fun buscarMateriais(
        textoPesquisa: String,
        pagina: Int = 0,
        tamanhoPagina: Int = 10
    ): Result<List<ResumoMaterialGrupo>> = runCatching {
        withContext(Dispatchers.IO) {
            val de = pagina * tamanhoPagina
            val ate = de + tamanhoPagina - 1

            cliente.postgrest[Supabase.VISAO_RESUMO_MATERIAIS_GRUPOS]
                .select {

                    // Aplica a busca parcial no nome se houver texto de pesquisa
                    if (!textoPesquisa.isNotNullOrBlank()) {
                        filter {
                            // ilike ignora maiúsculas/minúsculas e % busca o texto em qualquer posição
                            ilike(Supabase.COL_NOME, "%${textoPesquisa.trim()}%")
                        }
                    }

                    // Ordena alfabeticamente para manter a lista estável
                    order(Supabase.COL_NOME, Order.ASCENDING)

                    // Limita a quantidade de linhas retornadas por requisição
                    range(from = de.toLong(), to = ate.toLong())
                }
                .decodeList<ResumoMaterialGrupo>()
        }
    }

    suspend fun buscarResumoMateriaisPorDeposito(
        textoPesquisa: String,
        idDeposito: Long? = null,
        pagina: Int = 0,
        tamanhoPagina: Int = 20
    ): Result<List<ResumoMaterialDeposito>> = runCatching {
        withContext(Dispatchers.IO) {
            val de = pagina * tamanhoPagina
            val ate = de + tamanhoPagina - 1

            cliente.postgrest[Supabase.VISAO_RESUMO_MATERIAIS_DEPOSITOS]
                .select {
                    // Filtra pelo local caso tenha sido fornecido
                    idDeposito?.let { id ->
                        filter { eq(Supabase.COL_ID_DEPOSITO, id) }
                    }

                    // Aplica a busca parcial no nome se houver texto de pesquisa
                    if (!textoPesquisa.isNotNullOrBlank()) {
                        filter {
                            // ilike ignora maiúsculas/minúsculas e % busca o texto em qualquer posição
                            ilike(Supabase.COL_NOME_MATERIAL, "%${textoPesquisa.trim()}%")
                        }
                    }

                    // Ordena alfabeticamente para manter a lista estável
                    order(Supabase.COL_NOME_MATERIAL, Order.ASCENDING)

                    // Limita a quantidade de linhas retornadas por requisição
                    range(from = de.toLong(), to = ate.toLong())
                }
                .decodeList<ResumoMaterialDeposito>()
        }
    }

    // Extensão auxiliar privada
    private fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

}