package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.luandev.saneam.databinding.ActivityTransferenciaBinding
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luandev.saneam.viewmodel.DepositoSelectorViewModel
import com.luandev.saneam.viewmodel.MovimentacaoStatus
import com.luandev.saneam.viewmodel.MovimentacaoViewModel

class TransferenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferenciaBinding
    private val depositoViewModel: DepositoSelectorViewModel by viewModels()
    private val movimentacaoViewModel: MovimentacaoViewModel by viewModels()
    private var quantidade: Int = 0
    private var materialSelecionado: ResumoMaterialGrupo? = null
    private var listaDepositos: List<Deposito> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsBarrasSistema(topo = true, laterais = true)

        supportFragmentManager.setFragmentResultListener(
            MaterialSelectorBottomSheet.REQUEST_KEY,
            this
        ) { _, result ->
            materialSelecionado = MaterialSelectorBottomSheet.materialFromResult(result)
            binding.edtMaterial.setText(materialSelecionado?.nome)
        }

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

        depositoViewModel.erro.observe(this) { mensagem ->
            Toast.makeText(this, "Não foi possível carregar os depósitos: $mensagem", Toast.LENGTH_LONG).show()
        }

        depositoViewModel.carregando.observe(this) { carregando ->
            binding.spinnerOrigem.isEnabled = !carregando
            binding.spinnerDestino.isEnabled = !carregando
            if (carregando) binding.btnConfirmar.isEnabled = false
        }

        movimentacaoViewModel.status.observe(this) { status ->
            when (status) {
                is MovimentacaoStatus.Carregando -> {
                    binding.btnConfirmar.isEnabled = false
                }
                is MovimentacaoStatus.Sucesso -> {
                    binding.btnConfirmar.isEnabled = true
                    Toast.makeText(this, "Transferência realizada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is MovimentacaoStatus.Erro -> {
                    binding.btnConfirmar.isEnabled = true
                    Toast.makeText(this, status.mensagem, Toast.LENGTH_LONG).show()
                    movimentacaoViewModel.resetStatus()
                }
                else -> {}
            }
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
            
            val origem = if (idxOrigem != -1 && listaDepositos.isNotEmpty()) listaDepositos[idxOrigem] else null
            val destino = if (idxDestino != -1 && listaDepositos.isNotEmpty()) listaDepositos[idxDestino] else null

            if (materialSelecionado == null) {
                Toast.makeText(this, "Selecione um material", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (origem?.id == null || destino?.id == null) {
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

            val material = materialSelecionado ?: run {
                Toast.makeText(this, "Selecione um material", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idMaterial = material.id ?: run {
                Toast.makeText(this, "Material inválido. Selecione outro item.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            movimentacaoViewModel.executarTransferencia(
                idMaterial = idMaterial,
                idOrigem = origem.id,
                idDestino = destino.id,
                quantidade = quantidade.toDouble(),
                nomeMaterial = material.nome
            )
        }
    }

    private fun abrirSeletorMaterial() {
        val bottomSheet = MaterialSelectorBottomSheet.newInstance()
        bottomSheet.show(supportFragmentManager, MaterialSelectorBottomSheet.TAG)
    }

    private fun updateQuantidadeView() {
        binding.txtQuantidade.text = quantidade.toString()
    }
}