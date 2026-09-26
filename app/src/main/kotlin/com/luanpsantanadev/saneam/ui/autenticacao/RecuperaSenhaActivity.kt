package com.luanpsantanadev.saneam.ui.autenticacao

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.luanpsantanadev.saneam.databinding.ActivityRecuperaSenhaBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.viewmodel.AuthViewModel
import com.luanpsantanadev.saneam.viewmodel.RecuperaSenhaState

class RecuperaSenhaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperaSenhaBinding
    private val viewModel: AuthViewModel by viewModels()
    private var cancelamentoIniciado = false
    private lateinit var backCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperaSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsBarrasSistema()
        configurarCliques()
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                voltarParaLogin()
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)
        observarEstado()
        viewModel.processarLinkRedefinicaoSenha(intent?.dataString)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.processarLinkRedefinicaoSenha(intent.dataString)
    }

    private fun configurarCliques() {
        binding.btnRedefinirSenha.setOnClickListener {
            viewModel.redefinirSenha(
                binding.edtNovaSenha.text.toString(),
                binding.edtConfirmarSenha.text.toString()
            )
        }
        binding.txtVoltarLogin.setOnClickListener {
            voltarParaLogin()
        }
    }

    private fun observarEstado() {
        viewModel.recuperaSenhaState.observe(this) { state ->
            when (state) {
                is RecuperaSenhaState.Parado -> atualizarCarregamento(
                    carregando = false,
                    podeRedefinir = false
                )

                is RecuperaSenhaState.CarregandoLink -> atualizarCarregamento(
                    carregando = true,
                    podeRedefinir = false
                )

                is RecuperaSenhaState.Pronto -> atualizarCarregamento(
                    carregando = false,
                    podeRedefinir = true
                )

                is RecuperaSenhaState.Salvando -> atualizarCarregamento(
                    carregando = true,
                    podeRedefinir = false,
                    podeCancelar = false
                )

                is RecuperaSenhaState.Cancelando -> atualizarCarregamento(
                    carregando = true,
                    podeRedefinir = false
                )

                is RecuperaSenhaState.Cancelada -> {
                    atualizarCarregamento(
                        carregando = false,
                        podeRedefinir = false
                    )
                    irParaLogin()
                }

                is RecuperaSenhaState.Concluido -> {
                    atualizarCarregamento(
                        carregando = false,
                        podeRedefinir = false
                    )
                    Snackbar.make(
                        binding.root,
                        "Senha redefinida. Entre com sua nova senha.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Ir para login") {
                        voltarParaLogin()
                    }.show()
                }

                is RecuperaSenhaState.Erro -> {
                    cancelamentoIniciado = false
                    atualizarCarregamento(false, state.canRetry)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun atualizarCarregamento(
        carregando: Boolean,
        podeRedefinir: Boolean,
        podeCancelar: Boolean = true
    ) {
        binding.progressBar.visibility = if (carregando) View.VISIBLE else View.GONE
        binding.btnRedefinirSenha.isEnabled = podeRedefinir
        binding.edtNovaSenha.isEnabled = !carregando
        binding.edtConfirmarSenha.isEnabled = !carregando
        binding.txtVoltarLogin.isEnabled = podeCancelar
        backCallback.isEnabled = podeCancelar
    }

    private fun voltarParaLogin() {
        if (cancelamentoIniciado) return
        cancelamentoIniciado = true
        viewModel.cancelarRecuperacaoSenha()
    }

    private fun irParaLogin() {
        startActivity(
            Intent(this, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
