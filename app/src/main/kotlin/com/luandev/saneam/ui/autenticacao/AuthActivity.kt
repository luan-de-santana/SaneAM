package com.luandev.saneam.ui.autenticacao

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.luandev.saneam.BuildConfig
import com.luandev.saneam.databinding.ActivityAuthBinding
import com.luandev.saneam.ui.menu.MenuActivity
import com.luandev.saneam.viewmodel.AuthState
import com.luandev.saneam.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)

        configurarCliques()
        configurarObservadores()
    }

    private fun configurarCliques() {
        // Evento do botão de Login
        binding.btnEntrar.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            viewModel.realizarLogin(email, password)
        }

        // Evento do botão de Cadastro (Criar Conta)
        binding.btnCriarConta.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            viewModel.realizarCadastro(email, password)
        }

        // Login com o Google
        binding.btnGoogle.setOnClickListener {
            dispararInterfaceGoogle()
        }
    }

    private fun dispararInterfaceGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@AuthActivity, request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    // Entrega o token obtido de forma segura para a ViewModel processar no Supabase
                    viewModel.realizarLoginComGoogle(credential.idToken)
                }
            } catch (e: GetCredentialException) {
                Toast.makeText(this@AuthActivity, "Login com Google cancelado.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun configurarObservadores() {
        // "Observe" que escuta qualquer alteração de estado vinda da ViewModel
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    esconderCarregamento()
                }

                is AuthState.Loading -> {
                    mostrarCarregamento()
                }

                is AuthState.LoggedIn -> {
                    // Redireciona diretamente (Sem mensagem) pois o usuário já estava logado previamente
                    esconderCarregamento()
                    irParaMenu()
                }

                is AuthState.Success -> {
                    esconderCarregamento()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Redireciona após realizar login com sucesso
                    irParaMenu()
                }

                is AuthState.Error -> {
                    esconderCarregamento()
                    Toast.makeText(this, state.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun irParaMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        // Limpa a pilha para que o botão 'Voltar' do dispositivo não retorne à AuthActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarCarregamento() {
        // Exemplo: se tiver um ProgressBar no seu layout chamado 'progressBar'
        binding.progressBar.visibility = View.VISIBLE
        binding.btnEntrar.isEnabled = false
        binding.btnCriarConta.isEnabled = false
        binding.btnGoogle.isEnabled = false
    }

    private fun esconderCarregamento() {
        binding.progressBar.visibility = View.GONE
        binding.btnEntrar.isEnabled = true
        binding.btnCriarConta.isEnabled = true
        binding.btnGoogle.isEnabled = true
    }

    /*private fun realizarLogout() {
        lifecycleScope.launch {
            try {
                // Encerra a sessão no Supabase e remove os tokens locais
                SupabaseClientProvider.client.auth.signOut()

                // Redireciona para a tela de Auth/Login
                val intent = Intent(this@MenuActivity, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@MenuActivity, "Erro ao sair: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }*/
}