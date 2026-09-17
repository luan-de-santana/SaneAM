package com.luandev.saneam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luandev.saneam.service.repository.SupabaseClientProvider
import com.luandev.saneam.service.util.emailValido
import com.luandev.saneam.service.util.senhaForte
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> get() = _authState

    init {
        checarSessaoAtiva()
    }

    /**
     * Verifica se existe um token de sessão válido salvo localmente no Supabase
     */
    fun checarSessaoAtiva() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // O Supabase restaura a sessão salva no armazenamento seguro
                val currentSession = SupabaseClientProvider.client.auth.currentSessionOrNull()

                if (currentSession != null) {
                    // Sessão encontrada e válida
                    _authState.value = AuthState.LoggedIn
                } else {
                    // Nenhuma sessão ativa, permanece na tela de login
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                // Em caso de falha ao ler credenciais salvas, volta para Idle
                _authState.value = AuthState.Idle
            }
        }
    }

    /**
     * Realiza o logout do Supabase e limpa a sessão armazenada
     */
    fun desconectar() {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signOut()
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Erro ao sair da conta.")
            }
        }
    }

    /**
     * Realiza o login do usuário com e-mail e senha no Supabase
     */
    fun realizarLogin(emailTxt: String, passwordTxt: String) {
        if (!validarCampos(emailTxt, passwordTxt)) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    email = emailTxt
                    password = passwordTxt
                }
                _authState.value = AuthState.Success("Login efetuado com sucesso!")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.localizedMessage ?: "Erro desconhecido ao fazer login.")
            }
        }
    }

    /**
     * Realiza o cadastro de um novo usuário (E-mail e Senha) no Supabase
     */
    fun realizarCadastro(emailTxt: String, passwordTxt: String) {
        if (!validarCampos(emailTxt, passwordTxt)) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    email = emailTxt
                    password = passwordTxt
                }
                _authState.value =
                    AuthState.Success("Cadastro realizado! Verifique sua caixa de e-mail se necessário.")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.localizedMessage ?: "Erro desconhecido ao cadastrar.")
            }
        }
    }

    /**
     * Autentica o Token do Google retornado pela View diretamente no Supabase
     */
    fun realizarLoginComGoogle(idTokenString: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(IDToken) {
                    idToken = idTokenString
                    provider = Google
                }
                _authState.value = AuthState.Success("Autenticado via Google com sucesso!")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.localizedMessage ?: "Erro ao sincronizar com Google.")
            }
        }
    }

    /**
     * Validações básicas de negócio locais antes de gastar dados de rede
     */
    private fun validarCampos(emailTxt: String, passwordTxt: String): Boolean {
        if (emailTxt.isBlank() || passwordTxt.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos.")
            return false
        }
        if (!emailValido(emailTxt)) {
            _authState.value = AuthState.Error("Informe um e-mail válido.")
            return false
        }
        if (!senhaForte(passwordTxt)) {
            _authState.value =
                AuthState.Error("A senha precisa de no mínimo 6 caracteres e conter letras e números.")
            return false
        }
        return true
    }

}