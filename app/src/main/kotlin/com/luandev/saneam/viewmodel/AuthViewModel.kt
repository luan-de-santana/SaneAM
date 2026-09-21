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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Parado)
    val authState: LiveData<AuthState> get() = _authState

    init {
        checarSessaoAtiva()
    }

    /**
     * Verifica se existe um token de sessão válido salvo localmente no Supabase
     */
    fun checarSessaoAtiva() {
        _authState.value = AuthState.Carregando
        viewModelScope.launch {
            try {
                // O Supabase restaura a sessão salva no armazenamento seguro
                val currentSession = SupabaseClientProvider.client.auth.currentSessionOrNull()

                if (currentSession != null) {
                    // Sessão encontrada e válida
                    _authState.value = AuthState.Conectado
                } else {
                    // Nenhuma sessão ativa, permanece na tela de login
                    _authState.value = AuthState.Parado
                }
            } catch (e: Exception) {
                // Em caso de falha ao ler credenciais salvas, volta para Idle
                _authState.value = AuthState.Parado
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
                _authState.value = AuthState.Parado
            } catch (e: Exception) {
                _authState.value = AuthState.Erro(e.localizedMessage ?: "Erro ao sair da conta.")
            }
        }
    }

    /**
     * Realiza o login do usuário com e-mail e senha no Supabase
     */
    fun realizarLogin(emailTxt: String, passwordTxt: String) {
        if (!validarCampos(emailTxt, passwordTxt)) return
        _authState.value = AuthState.Carregando
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    email = emailTxt
                    password = passwordTxt
                }
                _authState.value = AuthState.Sucesso("Login efetuado com sucesso!")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Erro(e.localizedMessage ?: "Erro desconhecido ao fazer login.")
            }
        }
    }

    /**
     * Realiza o cadastro de um novo usuário (E-mail e Senha) no Supabase
     */
    fun realizarCadastro(emailTxt: String, passwordTxt: String, nomeTxt: String) {
        val nomeValido = nomeTxt.trim()

        if (!validarCampos(emailTxt, passwordTxt)) return
        if (nomeValido.isBlank() || nomeValido.length < 3) {
            _authState.value = AuthState.Erro("Informe um nome com pelo menos 3 caracteres.")
            return
        }

        _authState.value = AuthState.Carregando
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    email = emailTxt
                    password = passwordTxt
                    // Envia o nome dentro do objeto de metadados do usuário
                    data = buildJsonObject {
                        put("nome", nomeValido)
                    }
                }
                _authState.value =
                    AuthState.Sucesso("Cadastro realizado! Verifique sua caixa de e-mail se necessário.")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Erro(e.localizedMessage ?: "Erro desconhecido ao cadastrar.")
            }
        }
    }

    /**
     * Autentica o Token do Google retornado pela View diretamente no Supabase
     */
    fun realizarLoginComGoogle(idTokenString: String) {
        _authState.value = AuthState.Carregando
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signInWith(IDToken) {
                    idToken = idTokenString
                    provider = Google
                }
                _authState.value = AuthState.Sucesso("Autenticado via Google com sucesso!")
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Erro(e.localizedMessage ?: "Erro ao sincronizar com Google.")
            }
        }
    }

    /**
     * Validações básicas de negócio locais antes de gastar dados de rede
     */
    private fun validarCampos(emailTxt: String, passwordTxt: String): Boolean {
        if (emailTxt.isBlank() || passwordTxt.isBlank()) {
            _authState.value = AuthState.Erro("Preencha todos os campos.")
            return false
        }
        if (!emailValido(emailTxt)) {
            _authState.value = AuthState.Erro("Informe um e-mail válido.")
            return false
        }
        if (!senhaForte(passwordTxt)) {
            _authState.value =
                AuthState.Erro("A senha precisa de no mínimo 6 caracteres e conter letras e números.")
            return false
        }
        return true
    }

}