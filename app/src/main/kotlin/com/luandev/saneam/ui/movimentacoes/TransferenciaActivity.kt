package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.luandev.saneam.databinding.ActivityTransferenciaBinding
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.viewmodel.DepositoSelectorViewModel

class TransferenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferenciaBinding
    private val depositoViewModel: DepositoSelectorViewModel by viewModels()
    private var quantidade: Int = 0
    private var materialSelecionado: ResumoMaterialGrupo? = null
    private var listaDepositos: List<Deposito> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        configurarObservadores()
        updateQuantidadeView()
    }

    private fun configurarObservadores() {
        depositoViewModel.depositos.observe(this) { lista ->
            listaDepositos = lista
            val nomes = lista.map { it.nome }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            
            binding.spinnerOrigem.adapter = adapter
            binding.spinnerDestino.adapter = adapter
        }
    }

    private fun setupListeners() {
        // Ação de Voltar
        binding.btnBackCard.setOnClickListener {
            finish()
        }

        // Abre o Seletor de Material
        binding.edtMaterial.setOnClickListener {
            abrirSeletorMaterial()
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

        // Ação de Confirmar Transferência
        binding.btnConfirmar.setOnClickListener {
            val idxOrigem = binding.spinnerOrigem.selectedItemPosition
            val idxDestino = binding.spinnerDestino.selectedItemPosition
            
            val origem = if (idxOrigem != -1) listaDepositos[idxOrigem] else null
            val destino = if (idxDestino != -1) listaDepositos[idxDestino] else null

            if (materialSelecionado == null) {
                Toast.makeText(this, "Selecione um material", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (origem == null || destino == null) {
                Toast.makeText(this, "Selecione os depósitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (origem.id == destino.id) {
                Toast.makeText(this, "Origem e destino devem ser diferentes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (quantidade <= 0) {
                Toast.makeText(this, "Informe a quantidade", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Transferência de $quantidade un de ${materialSelecionado?.nome} de ${origem.nome} (ID: ${origem.id}) para ${destino.nome} (ID: ${destino.id}) confirmada!",
                Toast.LENGTH_LONG
            ).show()

            finish() // Fecha a tela após a confirmação
        }
    }

    private fun abrirSeletorMaterial() {
        val bottomSheet = MaterialSelectorBottomSheet { material ->
            materialSelecionado = material
            binding.edtMaterial.setText(material.nome)
        }
        bottomSheet.show(supportFragmentManager, MaterialSelectorBottomSheet.TAG)
    }

    private fun updateQuantidadeView() {
        binding.txtQuantidade.text = quantidade.toString()
    }
}