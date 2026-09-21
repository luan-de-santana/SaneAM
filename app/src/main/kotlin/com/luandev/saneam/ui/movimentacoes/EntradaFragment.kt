package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.luandev.saneam.databinding.FragmentEntradaBinding
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.service.model.TipoMovimentacao
import com.luandev.saneam.service.util.parseQuantidadeMovimentacao
import com.luandev.saneam.viewmodel.DepositoSelectorViewModel
import com.luandev.saneam.viewmodel.MovimentacaoStatus
import com.luandev.saneam.viewmodel.MovimentacaoViewModel

class EntradaFragment : Fragment() {

    private var _binding: FragmentEntradaBinding? = null
    private val binding get() = _binding!!

    private val depositoViewModel: DepositoSelectorViewModel by viewModels()
    private val movimentacaoViewModel: MovimentacaoViewModel by viewModels()
    private var materialSelecionado: ResumoMaterialGrupo? = null
    private var listaDepositos: List<Deposito> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntradaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            MaterialSelectorBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            materialSelecionado = MaterialSelectorBottomSheet.materialFromResult(result)
            binding.edtMaterial.setText(materialSelecionado?.nome)
        }

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

        depositoViewModel.erro.observe(viewLifecycleOwner) { mensagem ->
            Toast.makeText(requireContext(), "Não foi possível carregar os depósitos: $mensagem", Toast.LENGTH_LONG).show()
        }

        depositoViewModel.carregando.observe(viewLifecycleOwner) { carregando ->
            binding.spinnerDeposito.isEnabled = !carregando
            if (carregando) binding.btnConfirmarEntrada.isEnabled = false
        }

        movimentacaoViewModel.status.observe(viewLifecycleOwner) { status ->
            when (status) {
                is MovimentacaoStatus.Carregando -> {
                    binding.btnConfirmarEntrada.isEnabled = false
                    // Opcional: mostrar um ProgressBar específico aqui
                }
                is MovimentacaoStatus.Sucesso -> {
                    binding.btnConfirmarEntrada.isEnabled = true
                    Toast.makeText(requireContext(), "Entrada realizada com sucesso!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    movimentacaoViewModel.resetStatus()
                }
                is MovimentacaoStatus.Erro -> {
                    binding.btnConfirmarEntrada.isEnabled = true
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
        binding.edtQuantidade.setText("")
        binding.edtMotivo.setText("")
    }

    private fun configurarCliques() {
        // Abre o Seletor de Material
        binding.edtMaterial.setOnClickListener {
            abrirSeletorMaterial()
        }

        binding.btnConfirmarEntrada.setOnClickListener {
            val qtdStr = binding.edtQuantidade.text.toString()
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

            val quantidade = parseQuantidadeMovimentacao(qtdStr)
            if (quantidade == null) {
                binding.edtQuantidade.error = "Informe uma quantidade válida maior que zero"
                return@setOnClickListener
            }

            val motivo = binding.edtMotivo.text.toString().trim().ifEmpty { null }

            movimentacaoViewModel.executarMovimentacao(
                tipo = TipoMovimentacao.ENTRADA,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}