package com.luanpsantanadev.saneam

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import com.luanpsantanadev.saneam.ui.autenticacao.AuthActivity
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

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                atividadeAtual = activity
                if (activity is AuthActivity) {
                    redirecionandoParaLogin = false
                } else if (sessaoVerificada && !sessaoAutenticada) {
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
                sessaoAutenticada = status is SessionStatus.Authenticated
                if (status is SessionStatus.NotAuthenticated) {
                    redirecionarParaLogin()
                }
            }
        }
    }

    private fun redirecionarParaLogin() {
        val activity = atividadeAtual
        if (activity == null || activity is AuthActivity || redirecionandoParaLogin) return

        redirecionandoParaLogin = true
        activity.startActivity(
            Intent(activity, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        activity.finish()
    }

    companion object {
        lateinit var instance: SaneAMApplication
            private set
    }
}
