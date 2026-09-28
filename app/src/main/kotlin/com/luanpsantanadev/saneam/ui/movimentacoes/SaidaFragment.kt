package com.luanpsantanadev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentSaidaBinding
import com.luanpsantanadev.saneam.service.model.Deposito
import com.luanpsantanadev.saneam.service.model.ResumoMaterialGrupo
import com.luanpsantanadev.saneam.service.model.TipoMovimentacao
import com.luanpsantanadev.saneam.service.util.adicionarFiltroNumerico
import com.luanpsantanadev.saneam.service.util.parseQuantidadeMovimentacao
import com.luanpsantanadev.saneam.service.util.criarAdapterSpinnerEscuro
import com.luanpsantanadev.saneam.service.util.rolarCampoFocadoComTeclado
import com.luanpsantanadev.saneam.viewmodel.DepositoSelectorViewModel
import com.luanpsantanadev.saneam.viewmodel.MovimentacaoStatus
import com.luanpsantanadev.saneam.viewmodel.MovimentacaoViewModel

class SaidaFragment : Fragment() {

    private var _binding: FragmentSaidaBinding? = null
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
        _binding = FragmentSaidaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.rolarCampoFocadoComTeclado()
        binding.edtQuantidade.adicionarFiltroNumerico()

        childFragmentManager.setFragmentResultListener(
            MaterialSelectorBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            materialSelecionado = MaterialSelectorBottomSheet.materialFromResult(result)
            val texto = materialSelecionado?.let { "${it.codigoAlpha} - ${it.nome}" } ?: ""
            binding.edtMaterial.setText(texto)
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
            if (carregando) binding.btnConfirmarSaida.isEnabled = false
        }

        movimentacaoViewModel.status.observe(viewLifecycleOwner) { status ->
            when (status) {
                is MovimentacaoStatus.Carregando -> {
                    binding.btnConfirmarSaida.isEnabled = false
                }
                is MovimentacaoStatus.Sucesso -> {
                    binding.btnConfirmarSaida.isEnabled = true
                    Toast.makeText(requireContext(), R.string.saida_sucesso, Toast.LENGTH_SHORT).show()
                    limparCampos()
                    movimentacaoViewModel.resetStatus()
                }
                is MovimentacaoStatus.Erro -> {
                    binding.btnConfirmarSaida.isEnabled = true
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
        binding.edtMaterial.setOnClickListener {
            abrirSeletorMaterial()
        }

        binding.btnConfirmarSaida.setOnClickListener {
            val qtdStr = binding.edtQuantidade.text.toString()
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
                binding.edtQuantidade.error = "Informe uma quantidade válida maior que zero"
                return@setOnClickListener
            }

            val motivo = binding.edtMotivo.text.toString().trim().ifEmpty { null }

            movimentacaoViewModel.executarMovimentacao(
                tipo = TipoMovimentacao.SAIDA,
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