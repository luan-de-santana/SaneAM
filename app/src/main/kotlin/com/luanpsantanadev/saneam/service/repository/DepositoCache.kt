package com.luanpsantanadev.saneam.service.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.luanpsantanadev.saneam.service.model.Deposito
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.depositoDataStore by preferencesDataStore(name = "depositos_cache")

class DepositoCache(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun ler(idUsuario: String): List<Deposito>? {
        val preferencias = context.depositoDataStore.data.first()
        val atualizadoEm = preferencias[chaveAtualizacao(idUsuario)] ?: return null
        if (System.currentTimeMillis() - atualizadoEm >= VALIDADE_MS) {
            return null
        }

        val dados = preferencias[chaveDados(idUsuario)] ?: return null
        return runCatching { json.decodeFromString<List<Deposito>>(dados) }.getOrNull()
    }

    suspend fun salvar(idUsuario: String, depositos: List<Deposito>) {
        context.depositoDataStore.edit { preferencias ->
            preferencias[chaveDados(idUsuario)] = json.encodeToString(depositos)
            preferencias[chaveAtualizacao(idUsuario)] = System.currentTimeMillis()
        }
    }

    suspend fun limpar(idUsuario: String) {
        RepositorioDeposito.invalidarCacheEmMemoria(idUsuario)
        context.depositoDataStore.edit { preferencias ->
            preferencias.remove(chaveDados(idUsuario))
            preferencias.remove(chaveAtualizacao(idUsuario))
        }
    }

    private fun chaveDados(idUsuario: String) =
        stringPreferencesKey("dados_$idUsuario")

    private fun chaveAtualizacao(idUsuario: String) =
        longPreferencesKey("atualizado_em_$idUsuario")

    companion object {
        private const val VALIDADE_MS = 6 * 60 * 60 * 1000L
    }
}
