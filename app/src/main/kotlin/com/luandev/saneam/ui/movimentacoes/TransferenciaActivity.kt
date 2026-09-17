package com.luandev.saneam.ui.movimentacoes

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.luandev.saneam.databinding.ActivityTransferenciaBinding
import androidx.core.graphics.toColorInt

class TransferenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferenciaBinding
    private var quantidade: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateQuantidadeView()
    }

    private fun setupListeners() {
        // Ação de Voltar
        binding.btnBackCard.setOnClickListener {
            finish()
        }

        // Incrementar e decrementar quantidade
        binding.btnPlus.setOnClickListener {
            quantidade++
            updateQuantidadeView()
        }

        binding.btnMinus.setOnClickListener {
            if (quantidade > 0) {
                quantidade--
                updateQuantidadeView()
            }
        }

        // Monitorar preenchimento dos campos para validar o botão
        binding.edtMaterial.doOnTextChanged { _, _, _, _ -> checkFormValidation() }
        binding.edtOrigem.doOnTextChanged { _, _, _, _ -> checkFormValidation() }
        binding.edtDestino.doOnTextChanged { _, _, _, _ -> checkFormValidation() }

        // Ação de Confirmar Transferência
        binding.btnConfirmar.setOnClickListener {
            val material = binding.edtMaterial.text.toString()
            val origem = binding.edtOrigem.text.toString()
            val destino = binding.edtDestino.text.toString()

            Toast.makeText(
                this,
                "Transferência de $quantidade un de $material confirmada!",
                Toast.LENGTH_LONG
            ).show()

            finish() // Fecha a tela após a confirmação
        }
    }

    private fun updateQuantidadeView() {
        binding.txtQuantidade.text = quantidade.toString()
        checkFormValidation()
    }

    private fun checkFormValidation() {
        val hasMaterial = !binding.edtMaterial.text.isNullOrBlank()
        val hasOrigem = !binding.edtOrigem.text.isNullOrBlank()
        val hasDestino = !binding.edtDestino.text.isNullOrBlank()
        val hasValidQuantity = quantidade > 0

        val isFormValid = hasMaterial && hasOrigem && hasDestino && hasValidQuantity

        // Atualiza a aparência e o estado do botão
        binding.btnConfirmar.isEnabled = isFormValid
        if (isFormValid) {
            binding.btnConfirmar.setBackgroundColor("#2563EB".toColorInt()) // Azul ativo
            binding.btnConfirmar.setTextColor(Color.WHITE)
        } else {
            binding.btnConfirmar.setBackgroundColor("#E2E8F0".toColorInt()) // Cinza desativado
            binding.btnConfirmar.setTextColor("#94A3B8".toColorInt())
        }
    }
}