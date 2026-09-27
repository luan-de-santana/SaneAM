package com.luanpsantanadev.saneam.ui.autenticacao

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.luanpsantanadev.saneam.BuildConfig
import com.luanpsantanadev.saneam.databinding.ActivityAuthBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.service.util.emailValido
import com.luanpsantanadev.saneam.service.util.senhaForte
import com.luanpsantanadev.saneam.ui.menu.MenuActivity
import com.luanpsantanadev.saneam.viewmodel.AutState
import com.luanpsantanadev.saneam.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
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
        viewModel.checarSessaoAtiva()
        processarLinkConfirmacao(intent?.data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processarLinkConfirmacao(intent.data)
    }

    private fun processarLinkConfirmacao(uri: Uri?) {
        if (
            uri?.scheme != EMAIL_CONFIRMATION_SCHEME ||
            uri.host != EMAIL_CONFIRMATION_HOST ||
            uri.path != EMAIL_CONFIRMATION_PATH
        ) return

        val possuiErro = uri.queryParameterNames.contains("error") ||
                uri.queryParameterNames.contains("error_code") ||
                uri.fragment?.split("&")?.any { parametro ->
                    parametro.substringBefore("=") in EMAIL_CONFIRMATION_ERROR_PARAMETERS
                } == true

        val mensagem = if (possuiErro) {
            getString(R.string.email_confirmacao_falhou)
        } else {
            getString(R.string.email_confirmacao_processada)
        }
        Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_LONG).show()
    }

    private fun configurarCliques() {
        // Evento do botão de Login
        binding.btnEntrar.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            viewModel.realizarLogin(email, password)
        }

        binding.txtEsqueciSenha.setOnClickListener {
            viewModel.solicitarRecuperacaoSenha(binding.edtEmail.text.toString())
        }

        // Evento do botão de Cadastro (Criar Conta)
        binding.btnCriarConta.setOnClickListener {
            dialogCadastro(binding.edtEmail.text.toString().trim())
        }

        // Login com o Google
        binding.btnGoogle.setOnClickListener {
            dispararInterfaceGoogle()
        }
    }

    private fun dispararInterfaceGoogle() {
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
                // Ignora caso falhe ao limpar o estado
            }

            val signInOption = GetSignInWithGoogleOption
                .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val signInRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val credential = try {
                credentialManager.getCredential(
                    this@AuthActivity,
                    signInRequest
                ).credential
            } catch (_: GetCredentialException) {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                try {
                    credentialManager.getCredential(
                        this@AuthActivity,
                        fallbackRequest
                    ).credential
                } catch (fallbackError: GetCredentialException) {
                    val mensagem = if (fallbackError is NoCredentialException) {
                        getString(R.string.google_credencial_indisponivel)
                    } else {
                        fallbackError.message
                            ?: getString(R.string.google_erro_obter_credencial)
                    }
                    mostrarErroCredencial(
                        exception = fallbackError,
                        etapa = getString(R.string.google_diagnostico_etapa_fallback),
                        mensagem = mensagem
                    )
                    return@launch
                }
            }

            autenticarComCredencial(credential)
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
            Snackbar.make(
                binding.root,
                "A credencial selecionada não é compatível com o login do Google.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun mostrarErroCredencial(
        exception: GetCredentialException,
        etapa: String,
        mensagem: String = when (exception) {
            is GetCredentialCancellationException -> getString(R.string.google_login_cancelado)
            is NoCredentialException -> getString(R.string.google_credencial_indisponivel)
            is GetCredentialInterruptedException -> getString(R.string.google_login_interrompido)
            is GetCredentialProviderConfigurationException ->
                getString(R.string.google_login_indisponivel)

            is GetCredentialUnknownException -> getString(R.string.google_falha_inesperada)
            else -> getString(R.string.google_erro_obter_credencial)
        }
    ) {
        Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_LONG)
            .setAction(R.string.compartilhar_diagnostico) {
                compartilharDiagnosticoCredencial(exception, etapa)
            }
            .show()
    }

    private fun compartilharDiagnosticoCredencial(
        exception: GetCredentialException,
        etapa: String
    ) {
        val mensagemSegura = exception.message
            ?.replace(EMAIL_REGEX, "[e-mail ocultado]")
            ?.replace(BEARER_TOKEN_REGEX, "Bearer [credencial ocultada]")
            ?.replace(JWT_REGEX, "[token ocultado]")
            ?.replace(QUERY_SECRET_REGEX, "$1=[ocultado]")
            ?.take(MAX_DIAGNOSTIC_MESSAGE_LENGTH)
            ?.ifBlank { null }
            ?: getString(R.string.sem_mensagem_tecnica)
        val relatorio = getString(
            R.string.google_relatorio_diagnostico,
            etapa,
            exception::class.java.simpleName,
            mensagemSegura,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, relatorio)
        }

        try {
            startActivity(
                Intent.createChooser(
                    intent,
                    getString(R.string.compartilhar_diagnostico)
                )
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.nenhum_app_compartilhar, Toast.LENGTH_LONG).show()
        }
    }

    private fun configurarObservadores() {
        // "Observe" que escuta qualquer alteração de estado vinda da ViewModel
        viewModel.autState.observe(this) { state ->
            when (state) {
                is AutState.Parado -> {
                    esconderCarregamento()
                }

                is AutState.Carregando -> {
                    mostrarCarregamento()
                }

                is AutState.Conectado -> {
                    // Redireciona diretamente (Sem mensagem) pois o usuário já estava logado previamente
                    esconderCarregamento()
                    irParaMenu()
                }

                is AutState.Sucesso -> {
                    esconderCarregamento()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Redireciona após realizar login com sucesso
                    irParaMenu()
                }

                is AutState.Aviso -> {
                    esconderCarregamento()
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }

                is AutState.Erro -> {
                    esconderCarregamento()
                    Snackbar.make(binding.root, state.errorMessage, Snackbar.LENGTH_LONG).show()
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
        binding.txtEsqueciSenha.isEnabled = false
    }

    private fun esconderCarregamento() {
        binding.progressBar.visibility = View.GONE
        binding.btnEntrar.isEnabled = true
        binding.btnCriarConta.isEnabled = true
        binding.btnGoogle.isEnabled = true
        binding.txtEsqueciSenha.isEnabled = true
    }

    private fun dialogCadastro(emailInicial: String) {
        val (emailLayout, emailInput) = criarCampoCadastro(
            hint = getString(R.string.email),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            imeAction = EditorInfo.IME_ACTION_NEXT
        )
        emailInput.setText(emailInicial)

        val (nomeLayout, nomeInput) = criarCampoCadastro(
            hint = getString(R.string.nome_completo),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
            imeAction = EditorInfo.IME_ACTION_NEXT
        )
        val (senhaLayout, senhaInput) = criarCampoCadastro(
            hint = getString(R.string.senha_minima),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            imeAction = EditorInfo.IME_ACTION_NEXT,
            mostrarAlternanciaSenha = true
        )
        val (confirmarSenhaLayout, confirmarSenhaInput) = criarCampoCadastro(
            hint = getString(R.string.confirmar_senha),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            imeAction = EditorInfo.IME_ACTION_DONE,
            mostrarAlternanciaSenha = true
        )

        val campos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(emailLayout, parametrosCampoCadastro())
            addView(nomeLayout, parametrosCampoCadastro())
            addView(senhaLayout, parametrosCampoCadastro())
            addView(confirmarSenhaLayout, parametrosCampoCadastro())
        }
        val rolagem = ScrollView(this).apply {
            isFillViewport = true
            addView(
                campos,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        val margemHorizontal = resources.getDimensionPixelSize(R.dimen.dialog_horizontal_margin)
        val conteudo = FrameLayout(this).apply {
            setPadding(margemHorizontal, 0, margemHorizontal, 0)
            addView(
                rolagem,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val dialog = AlertDialog.Builder(this, R.style.Theme_SaneAM_LightDialog)
            .setTitle(R.string.cadastro_nova_conta)
            .setMessage(R.string.cadastro_preencha_dados)
            .setView(conteudo)
            .setPositiveButton(R.string.cadastrar, null)
            .setNegativeButton(R.string.cancelar, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val email = emailInput.text?.toString()?.trim().orEmpty()
                val nome = nomeInput.text?.toString()?.trim().orEmpty()
                val senha = senhaInput.text?.toString().orEmpty()
                val confirmarSenha = confirmarSenhaInput.text?.toString().orEmpty()

                emailLayout.error = when {
                    email.isBlank() -> getString(R.string.email_necessario)
                    !emailValido(email) -> getString(R.string.email_invalido)
                    else -> null
                }
                nomeLayout.error = when {
                    nome.isBlank() -> getString(R.string.nome_necessario)
                    nome.length < MIN_NAME_LENGTH -> getString(R.string.nome_minimo_caracteres)
                    else -> null
                }
                senhaLayout.error = when {
                    senha.isBlank() -> getString(R.string.senha_necessaria)
                    !senhaForte(senha) -> getString(R.string.senha_requisitos_cadastro)
                    else -> null
                }
                confirmarSenhaLayout.error = when {
                    confirmarSenha.isBlank() -> getString(R.string.confirmar_senha_necessaria)
                    senha != confirmarSenha -> getString(R.string.senhas_nao_conferem)
                    else -> null
                }

                val primeiroCampoInvalido = listOf(
                    emailLayout to emailInput,
                    nomeLayout to nomeInput,
                    senhaLayout to senhaInput,
                    confirmarSenhaLayout to confirmarSenhaInput
                ).firstOrNull { (layout, _) -> layout.error != null }

                if (primeiroCampoInvalido != null) {
                    primeiroCampoInvalido.second.requestFocus()
                } else {
                    dialog.dismiss()
                    viewModel.realizarCadastro(email, senha, nome)
                }
            }
        }
        dialog.show()
    }

    private fun criarCampoCadastro(
        hint: String,
        inputType: Int,
        imeAction: Int,
        mostrarAlternanciaSenha: Boolean = false
    ): Pair<TextInputLayout, TextInputEditText> {
        val layout = TextInputLayout(this).apply {
            this.hint = hint
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxStrokeColor = getColor(R.color.indigo)
            if (mostrarAlternanciaSenha) {
                endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            }
        }
        val input = TextInputEditText(layout.context).apply {
            this.inputType = inputType
            imeOptions = imeAction
            setSingleLine()
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_secondary))
        }
        layout.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        return layout to input
    }

    private fun parametrosCampoCadastro(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = resources.getDimensionPixelSize(R.dimen.dialog_field_spacing)
        }

    private companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_DIAGNOSTIC_MESSAGE_LENGTH = 500
        const val EMAIL_CONFIRMATION_SCHEME = "saneam"
        const val EMAIL_CONFIRMATION_HOST = "auth"
        const val EMAIL_CONFIRMATION_PATH = "/confirm-email"
        val EMAIL_CONFIRMATION_ERROR_PARAMETERS = setOf("error", "error_code", "error_description")
        val EMAIL_REGEX = Regex("""[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}""")
        val BEARER_TOKEN_REGEX = Regex("""(?i)\bBearer\s+\S+""")
        val JWT_REGEX = Regex("""(?i)\beyJ[a-zA-Z0-9_-]*\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+\b""")
        val QUERY_SECRET_REGEX =
            Regex("""(?i)\b(access_token|refresh_token|id_token|token|authorization|code|credential)=([^&\s]+)""")
    }

}