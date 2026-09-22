package com.luanpsantanadev.saneam.ui.autenticacao

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.luanpsantanadev.saneam.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.luanpsantanadev.saneam.BuildConfig
import com.luanpsantanadev.saneam.databinding.ActivityAuthBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.ui.menu.MenuActivity
import com.luanpsantanadev.saneam.viewmodel.AuthState
import com.luanpsantanadev.saneam.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsBarrasSistema()
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
            exibirDialogNome(email, password)
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
                autenticarComCredencial(
                    credentialManager.getCredential(this@AuthActivity, request).credential
                )
            } catch (e: NoCredentialException) {
                try {
                    // Fallback para o fluxo explícito, que também permite escolher uma conta
                    // ainda não autorizada para este aplicativo.
                    val signInOption = GetSignInWithGoogleOption
                        .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .build()
                    val signInRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()

                    autenticarComCredencial(
                        credentialManager.getCredential(this@AuthActivity, signInRequest).credential
                    )
                } catch (fallbackError: NoCredentialException) {
                    Toast.makeText(
                        this@AuthActivity,
                        "Nenhuma conta Google está disponível. Adicione uma conta ao dispositivo e verifique se o Google Play Services está atualizado.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (fallbackError: GetCredentialException) {
                    mostrarErroCredencial(fallbackError)
                }
            } catch (e: GetCredentialCancellationException) {
                Toast.makeText(
                    this@AuthActivity,
                    "Login com Google cancelado.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: NoCredentialException) {
                Toast.makeText(
                    this@AuthActivity,
                    "Nenhuma credencial do Google foi encontrada neste dispositivo.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: GetCredentialInterruptedException) {
                Toast.makeText(
                    this@AuthActivity,
                    "Não foi possível concluir o login. Verifique sua conexão e tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: GetCredentialProviderConfigurationException) {
                Toast.makeText(
                    this@AuthActivity,
                    "O login com Google não está disponível neste dispositivo.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: GetCredentialUnknownException) {
                Toast.makeText(
                    this@AuthActivity,
                    "Falha inesperada ao obter a credencial do Google.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: GetCredentialException) {
                Toast.makeText(
                    this@AuthActivity,
                    "Não foi possível obter a credencial do Google.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun autenticarComCredencial(credential: androidx.credentials.Credential) {
        // Verifica se a credencial é do tipo ID do Google
        if (credential is CustomCredential
            && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            // Cria Google ID Token
            val credentialData = credential.data
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData)

            // Faz login no Supabase usando o ID Token do Google
            viewModel.realizarLoginComGoogle(googleIdTokenCredential.idToken)
        } else {
            Toast.makeText(
                this,
                "A credencial selecionada não é compatível com o login do Google.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun mostrarErroCredencial(exception: GetCredentialException) {
        val mensagem = when (exception) {
            is GetCredentialCancellationException -> "Login com Google cancelado."
            is GetCredentialInterruptedException ->
                "Não foi possível concluir o login. Verifique sua conexão e tente novamente."

            is GetCredentialProviderConfigurationException ->
                "O login com Google não está disponível neste dispositivo."

            is GetCredentialUnknownException ->
                "Falha inesperada ao obter a credencial do Google."

            else -> "Não foi possível obter a credencial do Google."
        }
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }

    private fun configurarObservadores() {
        // "Observe" que escuta qualquer alteração de estado vinda da ViewModel
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Parado -> {
                    esconderCarregamento()
                }

                is AuthState.Carregando -> {
                    mostrarCarregamento()
                }

                is AuthState.Conectado -> {
                    // Redireciona diretamente (Sem mensagem) pois o usuário já estava logado previamente
                    esconderCarregamento()
                    irParaMenu()
                }

                is AuthState.Sucesso -> {
                    esconderCarregamento()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Redireciona após realizar login com sucesso
                    irParaMenu()
                }

                is AuthState.Erro -> {
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

    private fun exibirDialogNome(email: String, senha: String) {
        val input = EditText(this).apply {
            hint = "Nome completo"
            setTextColor(
                getColor(com.luanpsantanadev.saneam.R.color.md_theme_light_onSurface)
            )
            setHintTextColor(
                getColor(com.luanpsantanadev.saneam.R.color.md_theme_light_primary)
            )
            backgroundTintList = ColorStateList.valueOf(
                getColor(com.luanpsantanadev.saneam.R.color.md_theme_light_primary)
            )
        }

        val horizontalMargin = resources.getDimensionPixelSize(
            com.luanpsantanadev.saneam.R.dimen.dialog_horizontal_margin
        )
        val inputContainer = FrameLayout(this).apply {
            setPadding(horizontalMargin, 0, horizontalMargin, 0)
            addView(
                input,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        AlertDialog.Builder(this, com.luanpsantanadev.saneam.R.style.Theme_SaneAM_LightDialog)
            .setTitle(getString(R.string.finalizar_cadastro))
            .setMessage(getString(R.string.como_chamado))
            .setView(inputContainer)
            .setPositiveButton("Confirmar") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty() && nome.length > 2) {
                    viewModel.realizarCadastro(email, senha, nome)
                } else {
                    Toast.makeText(this, R.string.nome_necessario, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}