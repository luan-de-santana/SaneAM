package com.luanpsantanadev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentAcertoBinding
import com.luanpsantanadev.saneam.service.model.Deposito
import com.luanpsantanadev.saneam.service.model.ResumoMaterialGrupo
import com.luanpsantanadev.saneam.service.model.TipoMovimentacao
import com.luanpsantanadev.saneam.service.util.parseQuantidadeMovimentacao
import com.luanpsantanadev.saneam.service.util.criarAdapterSpinnerEscuro
import com.luanpsantanadev.saneam.viewmodel.DepositoSelectorViewModel
import com.luanpsantanadev.saneam.viewmodel.EstoqueViewModel
import com.luanpsantanadev.saneam.viewmodel.MovimentacaoStatus
import com.luanpsantanadev.saneam.viewmodel.MovimentacaoViewModel

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

        childFragmentManager.setFragmentResultListener(
            MaterialSelectorBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            materialSelecionado = MaterialSelectorBottomSheet.materialFromResult(result)
            val texto = materialSelecionado?.let { "${it.codigoAlpha} - ${it.nome}" } ?: ""
            binding.edtMaterial.setText(texto)
            atualizarSaldoAtual()
        }

        configurarCliques()
        configurarObservadores()
    }

    private fun configurarObservadores() {
        depositoViewModel.depositos.observe(viewLifecycleOwner) { lista ->
            listaDepositos = lista
            val adapter = requireContext().criarAdapterSpinnerEscuro(lista.map { it.nome })
            binding.spinnerDeposito.adapter = adapter
        }

        depositoViewModel.erro.observe(viewLifecycleOwner) { mensagem ->
            Toast.makeText(requireContext(), getString(R.string.erro_carregar_depositos, mensagem), Toast.LENGTH_LONG).show()
        }

        depositoViewModel.carregando.observe(viewLifecycleOwner) { carregando ->
            binding.spinnerDeposito.isEnabled = !carregando
            if (carregando) binding.btnConfirmarAcerto.isEnabled = false
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
                    Toast.makeText(requireContext(), R.string.acerto_sucesso, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), R.string.selecione_material, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (deposito?.id == null) {
                Toast.makeText(requireContext(), R.string.selecione_deposito, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantidade = parseQuantidadeMovimentacao(qtdStr)
            if (quantidade == null) {
                binding.edtSaldoReal.error = "Informe uma quantidade válida maior que zero"
                return@setOnClickListener
            }

            val motivo = binding.edtJustificativa.text.toString().trim().ifEmpty { null }

            movimentacaoViewModel.executarMovimentacao(
                tipo = TipoMovimentacao.ACERTO,
                idMaterial = materialId,
                idDeposito = deposito.id,
                quantidade = quantidade,
                motivo = motivo
            )
        }
    }

    private fun abrirSeletorMaterial() {
        val bottomSheet = MaterialSelectorBottomSheet.newInstance()
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