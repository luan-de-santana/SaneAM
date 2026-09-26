package com.luanpsantanadev.saneam

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import com.luanpsantanadev.saneam.ui.autenticacao.AuthActivity
import com.luanpsantanadev.saneam.ui.autenticacao.RecuperaSenhaActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SaneAMApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var atividadeAtual: Activity? = null
    private var sessaoAutenticada = false
    private var sessaoVerificada = false
    private var redirecionandoParaLogin = false
    @Volatile
    private var recuperacaoSenhaAtiva = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        recuperacaoSenhaAtiva = preferenciasRecuperacao.getBoolean(CHAVE_RECUPERACAO_ATIVA, false)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                atividadeAtual = activity
                if (activity is AuthActivity) {
                    redirecionandoParaLogin = false
                } else if (activity !is RecuperaSenhaActivity &&
                    (recuperacaoSenhaAtiva || (sessaoVerificada && !sessaoAutenticada))
                ) {
                    redirecionarParaLogin()
                }
            }

            override fun onActivityResumed(activity: Activity) {
                atividadeAtual = activity
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) {
                if (atividadeAtual === activity) {
                    atividadeAtual = null
                }
            }
        })
        observarSessao()
    }

    private fun observarSessao() {
        applicationScope.launch {
            val auth = SupabaseClientProvider.client.auth
            auth.awaitInitialization()
            sessaoVerificada = true
            auth.sessionStatus.collect { status ->
                sessaoAutenticada =
                    !recuperacaoSenhaAtiva && status is SessionStatus.Authenticated
                if (recuperacaoSenhaAtiva) {
                    return@collect
                }
                if (status is SessionStatus.NotAuthenticated) {
                    redirecionarParaLogin()
                }
            }
        }
    }

    fun iniciarRecuperacaoSenha() {
        val salvo = preferenciasRecuperacao.edit()
            .putBoolean(CHAVE_RECUPERACAO_ATIVA, true)
            .commit()
        check(salvo) { "Não foi possível proteger a sessão de recuperação de senha." }
        recuperacaoSenhaAtiva = true
    }

    fun concluirRecuperacaoSenha() {
        val salvo = preferenciasRecuperacao.edit()
            .remove(CHAVE_RECUPERACAO_ATIVA)
            .commit()
        check(salvo) { "Não foi possível encerrar o estado de recuperação de senha." }
        recuperacaoSenhaAtiva = false
        sessaoAutenticada = SupabaseClientProvider.client.auth.currentSessionOrNull() != null
    }

    fun isRecuperacaoSenhaAtiva(): Boolean = recuperacaoSenhaAtiva

    private fun redirecionarParaLogin() {
        val activity = atividadeAtual
        if (
            activity == null ||
            activity is AuthActivity ||
            activity is RecuperaSenhaActivity ||
            redirecionandoParaLogin
        ) return

        redirecionandoParaLogin = true
        activity.startActivity(
            Intent(activity, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        activity.finish()
    }

    companion object {
        private const val PREFERENCIAS_RECUPERACAO = "recuperacao_senha"
        private const val CHAVE_RECUPERACAO_ATIVA = "sessao_ativa"

        lateinit var instance: SaneAMApplication
            private set
    }

    private val preferenciasRecuperacao
        get() = getSharedPreferences(PREFERENCIAS_RECUPERACAO, MODE_PRIVATE)
}
