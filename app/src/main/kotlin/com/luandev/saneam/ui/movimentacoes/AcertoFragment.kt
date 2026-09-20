package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.luandev.saneam.databinding.FragmentAcertoBinding
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.service.model.TipoMovimentacao
import com.luandev.saneam.viewmodel.DepositoSelectorViewModel
import com.luandev.saneam.viewmodel.EstoqueViewModel
import com.luandev.saneam.viewmodel.MovimentacaoStatus
import com.luandev.saneam.viewmodel.MovimentacaoViewModel

class AcertoFragment : Fragment() {

    private var _binding: FragmentAcertoBinding? = null
    private val binding get() = _binding!!

    private val depositoViewModel: DepositoSelectorViewModel by viewModels()
    private val movimentacaoViewModel: MovimentacaoViewModel by viewModels()
    private val estoqueViewModel: EstoqueViewModel by viewModels()
    private var materialSelecionado: ResumoMaterialGrupo? = null
    private var listaDepositos: List<Deposito> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcertoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarCliques()
        configurarObservadores()
    }

    private fun configurarObservadores() {
        depositoViewModel.depositos.observe(viewLifecycleOwner) { lista ->
            listaDepositos = lista
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista.map { it.nome })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDeposito.adapter = adapter
        }

        estoqueViewModel.saldoAtual.observe(viewLifecycleOwner) { saldo ->
            binding.edtSaldoSistema.setText(saldo?.toInt()?.toString() ?: "0")
        }

        estoqueViewModel.erro.observe(viewLifecycleOwner) { mensagem ->
            Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
        }

        movimentacaoViewModel.status.observe(viewLifecycleOwner) { status ->
            when (status) {
                is MovimentacaoStatus.Carregando -> {
                    binding.btnConfirmarAcerto.isEnabled = false
                }
                is MovimentacaoStatus.Sucesso -> {
                    binding.btnConfirmarAcerto.isEnabled = true
                    Toast.makeText(requireContext(), "Acerto realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    movimentacaoViewModel.resetStatus()
                }
                is MovimentacaoStatus.Erro -> {
                    binding.btnConfirmarAcerto.isEnabled = true
                    Toast.makeText(requireContext(), status.mensagem, Toast.LENGTH_LONG).show()
                    movimentacaoViewModel.resetStatus()
                }
                else -> {}
            }
        }
    }

    private fun limparCampos() {
        materialSelecionado = null
        binding.edtMaterial.setText("")
        binding.edtSaldoReal.setText("")
        binding.edtJustificativa.setText("")
    }

    private fun configurarCliques() {
        binding.edtMaterial.setOnClickListener {
            abrirSeletorMaterial()
        }

        binding.spinnerDeposito.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarSaldoAtual()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnConfirmarAcerto.setOnClickListener {
            val qtdStr = binding.edtSaldoReal.text.toString()
            val materialId = materialSelecionado?.id
            
            val posicaoSelecionada = binding.spinnerDeposito.selectedItemPosition
            val deposito = if (posicaoSelecionada != -1 && listaDepositos.isNotEmpty()) listaDepositos[posicaoSelecionada] else null

            if (materialId == null) {
                Toast.makeText(requireContext(), "Selecione um material", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (deposito?.id == null) {
                Toast.makeText(requireContext(), "Selecione um depósito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (qtdStr.isNotEmpty()) {
                val quantidade = qtdStr.toDoubleOrNull() ?: 0.0
                val motivo = binding.edtJustificativa.text.toString().trim().ifEmpty { null }

                movimentacaoViewModel.executarMovimentacao(
                    tipo = TipoMovimentacao.ACERTO,
                    idMaterial = materialId,
                    idDeposito = deposito.id,
                    quantidade = quantidade,
                    motivo = motivo
                )
            } else {
                binding.edtSaldoReal.error = "Informe o saldo real"
            }
        }
    }

    private fun abrirSeletorMaterial() {
        val bottomSheet = MaterialSelectorBottomSheet { material ->
            materialSelecionado = material
            binding.edtMaterial.setText(material.nome)
            atualizarSaldoAtual()
        }
        bottomSheet.show(childFragmentManager, MaterialSelectorBottomSheet.TAG)
    }

    private fun atualizarSaldoAtual() {
        val matId = materialSelecionado?.id
        val pos = binding.spinnerDeposito.selectedItemPosition
        val depId = if (pos != -1 && listaDepositos.isNotEmpty()) listaDepositos[pos].id else null

        if (matId != null && depId != null) {
            estoqueViewModel.buscarSaldoAtual(matId, depId)
        } else {
            binding.edtSaldoSistema.setText("0")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}