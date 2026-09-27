package com.luanpsantanadev.saneam.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanpsantanadev.saneam.SaneAMApplication
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import com.luanpsantanadev.saneam.service.util.emailValido
import com.luanpsantanadev.saneam.service.util.senhaForte
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.parseSessionFromUrl
import io.github.jan.supabase.gotrue.exception.AuthErrorCode
import io.github.jan.supabase.gotrue.exception.AuthRestException
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import androidx.core.net.toUri

class AuthViewModel : ViewModel() {

    private val _autState = MutableLiveData<AutState>(AutState.Parado)
    val autState: LiveData<AutState> get() = _autState
    private val _recuperaSenhaState =
        MutableLiveData<RecuperaSenhaState>(RecuperaSenhaState.Parado)
    val recuperaSenhaState: LiveData<RecuperaSenhaState> get() = _recuperaSenhaState
    private var recoverySessionReady = false
    private var recoveryLinkJob: Job? = null

    /**
     * Verifica se existe um token de sessão válido salvo localmente no Supabase
     */
    fun checarSessaoAtiva() {
        _autState.value = AutState.Carregando
        viewModelScope.launch {
            try {
                val auth = SupabaseClientProvider.client.auth
                auth.awaitInitialization()

                if (SaneAMApplication.instance.isRecuperacaoSenhaAtiva()) {
                    if (auth.currentSessionOrNull() != null) {
                        auth.signOut()
                    }
                    SaneAMApplication.instance.concluirRecuperacaoSenha()
                    _autState.value = AutState.Parado
                    return@launch
                }

                val currentSession = auth.currentSessionOrNull()

                if (currentSession != null) {
                    auth.refreshCurrentSession()
                    _autState.value = AutState.Conectado
                } else {
                    _autState.value = AutState.Parado
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _autState.value =
                    AutState.Erro(e.localizedMessage ?: "Não foi possível validar a sessão.")
            }
        }
    }

    /**
     * Realiza o login do usuário com e-mail e senha no Supabase
     */
    fun realizarLogin(emailTxt: String, passwordTxt: String) {
        if (bloquearAutenticacaoDuranteRecuperacao()) return
        if (!validarCampos(emailTxt, passwordTxt)) return
        _autState.value = AutState.Carregando
        viewModelScope.launch {
            try {
                val auth = SupabaseClientProvider.client.auth
                auth.signInWith(Email) {
                    email = emailTxt
                    password = passwordTxt
                }

                if (auth.currentUserOrNull()?.emailConfirmedAt == null) {
                    auth.signOut()
                    _autState.value =
                        AutState.Erro("Confirme seu e-mail antes de entrar no aplicativo.")
                    return@launch
                }

                _autState.value = AutState.Sucesso("Login efetuado com sucesso!")
            } catch (e: AuthRestException) {
                _autState.value = AutState.Erro(
                    if (e.errorCode == AuthErrorCode.EmailNotConfirmed) {
                        "Confirme seu e-mail antes de entrar no aplicativo."
                    } else {
                        e.localizedMessage ?: "Erro desconhecido ao fazer login."
                    }
                )
            } catch (e: Exception) {
                _autState.value =
                    AutState.Erro(e.localizedMessage ?: "Erro desconhecido ao fazer login.")
            }
        }
    }

    /**
     * Realiza o cadastro de um novo usuário (E-mail e Senha) no Supabase
     */
    fun realizarCadastro(emailTxt: String, passwordTxt: String, nomeTxt: String) {
        if (bloquearAutenticacaoDuranteRecuperacao()) return
        val nomeValido = nomeTxt.trim()

        if (!validarCampos(emailTxt, passwordTxt)) return
        if (nomeValido.isBlank() || nomeValido.length < 3) {
            _autState.value = AutState.Erro("Informe um nome com pelo menos 3 caracteres.")
            return
        }

        _autState.value = AutState.Carregando
        viewModelScope.launch {
            try {
                val auth = SupabaseClientProvider.client.auth
                auth.signUpWith(
                    Email,
                    redirectUrl = EMAIL_CONFIRMATION_REDIRECT_URL
                ) {
                    email = emailTxt
                    password = passwordTxt
                    data = buildJsonObject {
                        put("nome", nomeValido)
                    }
                }
                val emailConfirmado = auth.currentUserOrNull()?.emailConfirmedAt != null
                if (auth.currentSessionOrNull() != null) {
                    auth.signOut()
                }
                _autState.value = AutState.Aviso(
                    if (emailConfirmado) {
                        "Cadastro realizado. Entre com seu e-mail e senha."
                    } else {
                        "Cadastro realizado. Confirme seu e-mail antes de entrar no aplicativo."
                    }
                )
            } catch (e: Exception) {
                _autState.value =
                    AutState.Erro(e.localizedMessage ?: "Erro desconhecido ao cadastrar.")
            }
        }
    }

    /**
     * Solicita o envio do e-mail de recuperação sem revelar se a conta existe.
     */
    fun solicitarRecuperacaoSenha(emailTxt: String) {
        val email = emailTxt.trim()
        if (email.isBlank()) {
            _autState.value = AutState.Erro("Informe seu e-mail para recuperar a senha.")
            return
        }
        if (!emailValido(email)) {
            _autState.value = AutState.Erro("Informe um e-mail válido.")
            return
        }

        _autState.value = AutState.Carregando
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.resetPasswordForEmail(
                    email,
                    redirectUrl = PASSWORD_RESET_REDIRECT_URL
                )
                _autState.value = AutState.Aviso(
                    "Se este e-mail estiver cadastrado, você receberá instruções para redefinir sua senha."
                )
            } catch (e: AuthRestException) {
                _autState.value = if (e.errorCode == AuthErrorCode.UserNotFound) {
                    AutState.Aviso(
                        "Se este e-mail estiver cadastrado, você receberá instruções para redefinir sua senha."
                    )
                } else {
                    AutState.Erro(
                        e.localizedMessage ?: "Não foi possível solicitar a recuperação da senha."
                    )
                }
            } catch (e: Exception) {
                _autState.value =
                    AutState.Erro(
                        e.localizedMessage ?: "Não foi possível solicitar a recuperação da senha."
                    )
            }
        }
    }

    /**
     * Valida o deep link de recuperação e importa a sessão temporária do Supabase.
     */
    fun processarLinkRedefinicaoSenha(url: String?) {
        recoveryLinkJob?.cancel()
        recoverySessionReady = false
        if (url.isNullOrBlank()) {
            _recuperaSenhaState.value =
                RecuperaSenhaState.Erro(
                    "Link de recuperação inválido ou ausente.",
                    canRetry = false
                )
            return
        }

        val uri = url.toUri()
        if (uri.scheme != "saneam" || uri.host != "auth" || uri.path != "/reset-password") {
            _recuperaSenhaState.value =
                RecuperaSenhaState.Erro(
                    "Link de recuperação inválido ou ausente.",
                    canRetry = false
                )
            return
        }

        _recuperaSenhaState.value = RecuperaSenhaState.CarregandoLink
        recoveryLinkJob = viewModelScope.launch {
            try {
                SaneAMApplication.instance.iniciarRecuperacaoSenha()
                val auth = SupabaseClientProvider.client.auth
                auth.awaitInitialization()
                val session = auth.parseSessionFromUrl(url)
                if (session.type != "recovery") {
                    val erroLimpeza = encerrarSessaoRecuperacao()
                    _recuperaSenhaState.value =
                        RecuperaSenhaState.Erro(
                            erroLimpeza?.let {
                                "Este link não é válido e não foi possível encerrar a sessão temporária: $it"
                            } ?: "Este link não é válido para redefinir a senha.",
                            canRetry = false
                        )
                    return@launch
                }

                val user = auth.retrieveUser(session.accessToken)
                auth.importSession(session.copy(user = user))
                recoverySessionReady = true
                _recuperaSenhaState.value = RecuperaSenhaState.Pronto
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val causa = e.localizedMessage ?: "O link de recuperação expirou ou não é válido."
                val mensagem = encerrarSessaoRecuperacao()?.let {
                    "$causa Não foi possível encerrar a sessão temporária: $it"
                } ?: causa
                _recuperaSenhaState.value = RecuperaSenhaState.Erro(
                    mensagem,
                    canRetry = false
                )
            }
        }
    }

    /**
     * Atualiza a senha somente enquanto a sessão importada veio de um link de recuperação válido.
     */
    fun redefinirSenha(novaSenha: String, confirmarSenha: String) {
        if (!recoverySessionReady) {
            _recuperaSenhaState.value =
                RecuperaSenhaState.Erro(
                    "Solicite um novo link para redefinir sua senha.",
                    canRetry = false
                )
            return
        }
        if (!senhaForte(novaSenha)) {
            _recuperaSenhaState.value = RecuperaSenhaState.Erro(
                "A senha precisa de no mínimo 6 caracteres e conter letras e números.",
                canRetry = true
            )
            return
        }
        if (novaSenha != confirmarSenha) {
            _recuperaSenhaState.value =
                RecuperaSenhaState.Erro("As senhas não conferem.", canRetry = true)
            return
        }

        _recuperaSenhaState.value = RecuperaSenhaState.Salvando
        viewModelScope.launch {
            try {
                val auth = SupabaseClientProvider.client.auth
                auth.updateUser {
                    password = novaSenha
                }
                auth.signOut()
                recoverySessionReady = false
                SaneAMApplication.instance.concluirRecuperacaoSenha()
                _recuperaSenhaState.value = RecuperaSenhaState.Concluido
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _recuperaSenhaState.value = RecuperaSenhaState.Erro(
                    e.localizedMessage ?: "Não foi possível atualizar a senha.",
                    canRetry = true
                )
            }
        }
    }

    fun cancelarRecuperacaoSenha() {
        recoveryLinkJob?.cancel()
        _recuperaSenhaState.value = RecuperaSenhaState.Cancelando
        viewModelScope.launch {
            try {
                val app = SaneAMApplication.instance
                if (app.isRecuperacaoSenhaAtiva()) {
                    encerrarSessaoRecuperacao()?.let { error(it) }
                }
                recoverySessionReady = false
                _recuperaSenhaState.value = RecuperaSenhaState.Cancelada
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _recuperaSenhaState.value = RecuperaSenhaState.Erro(
                    e.localizedMessage ?: "Não foi possível encerrar a recuperação de senha.",
                    canRetry = true
                )
            }
        }
    }

    private suspend fun encerrarSessaoRecuperacao(): String? {
        val app = SaneAMApplication.instance
        if (!app.isRecuperacaoSenhaAtiva()) return null
        return try {
            val auth = SupabaseClientProvider.client.auth
            auth.awaitInitialization()
            if (auth.currentSessionOrNull() != null) {
                auth.signOut()
            }
            app.concluirRecuperacaoSenha()
            null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.localizedMessage ?: "Estado de recuperação ainda ativo."
        }
    }

    /**
     * Autentica o Token do Google retornado pela View diretamente no Supabase
     */
    fun realizarLoginComGoogle(idTokenString: String) {
        if (bloquearAutenticacaoDuranteRecuperacao()) return
        _autState.value = AutState.Carregando
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(IDToken) {
                    idToken = idTokenString
                    provider = Google
                }
                _autState.value = AutState.Sucesso("Autenticado via Google com sucesso!")
            } catch (e: Exception) {
                _autState.value =
                    AutState.Erro(e.localizedMessage ?: "Erro ao sincronizar com Google.")
            }
        }
    }

    /**
     * Validações básicas de negócio locais antes de gastar dados de rede
     */
    private fun validarCampos(emailTxt: String, passwordTxt: String): Boolean {
        if (emailTxt.isBlank() || passwordTxt.isBlank()) {
            _autState.value = AutState.Erro("Preencha todos os campos.")
            return false
        }
        if (!emailValido(emailTxt)) {
            _autState.value = AutState.Erro("Informe um e-mail válido.")
            return false
        }
        if (!senhaForte(passwordTxt)) {
            _autState.value =
                AutState.Erro("A senha precisa de no mínimo 6 caracteres e conter letras e números.")
            return false
        }
        return true
    }

    private fun bloquearAutenticacaoDuranteRecuperacao(): Boolean {
        if (!SaneAMApplication.instance.isRecuperacaoSenhaAtiva()) return false
        _autState.value = AutState.Erro(
            "Encerre a recuperação de senha antes de entrar ou criar uma conta."
        )
        return true
    }

    private companion object {
        const val PASSWORD_RESET_REDIRECT_URL = "saneam://auth/reset-password"
        const val EMAIL_CONFIRMATION_REDIRECT_URL = "saneam://auth/confirm-email"
    }
}