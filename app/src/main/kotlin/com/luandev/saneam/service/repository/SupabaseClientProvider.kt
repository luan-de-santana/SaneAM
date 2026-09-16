package com.luandev.saneam.service.repository

import com.luandev.saneam.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseClientProvider {

    // Inicialização "lazy" (preguiçosa): o cliente só é criado no exato momento em que for usado pela primeira vez
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Define o OkHttp (Ktor) como o motor de internet responsável pelas chamadas HTTP
            httpEngine = OkHttp.create()

            // Instala o módulo de Banco de Dados Relacional (Postgrest)
            install(Postgrest)

            // Instala o módulo de Gerenciamento de Usuários (Auth)
            install(Auth) {
                // Configurações extras de autenticação podem ser inseridas aqui no futuro
            }
        }
    }

}