package com.luandev.saneam.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.luandev.saneam.R
import com.luandev.saneam.databinding.FragmentDashboardBinding
import com.luandev.saneam.service.util.ConstantsSaneAM.Key
import com.luandev.saneam.service.util.configurarCabecalhoPadrao
import com.luandev.saneam.ui.movimentacoes.TransferenciaActivity
import com.luandev.saneam.viewmodel.DashboardViewModel

class FragmentDashboard : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarCabecalho()
        configurarCliques()
        configurarObservadores()
    }

    private fun configurarObservadores() {
        viewModel.resumo.observe(viewLifecycleOwner) { resumo ->
            binding.textDepositosValue.text = resumo.totalDepositos.toInt().toString()
            binding.textGruposValue.text = resumo.totalGrupos.toInt().toString()
            binding.textMateriaisValue.text = resumo.totalMateriais.toInt().toString()
            binding.textCriticosValue.text = resumo.totalMateriaisBaixoEstoque.toInt().toString()
        }

        viewModel.erro.observe(viewLifecycleOwner) { mensagem ->
            Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarCabecalho() {
        configurarCabecalhoPadrao(binding.textSaudacao, binding.textDataAtual)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configurarCliques() {
        binding.cardNovaEntrada.setOnClickListener {
            irParaMovimentacaoEntrada()
        }
        binding.cardNovaSaida.setOnClickListener {
            irParaMovimentacaoSaida()
        }
        binding.cardTransferencia.setOnClickListener {
            irParaMovimentacaoTransferencia()
        }
        binding.cardInventario.setOnClickListener {
            irParaInventario()
        }
    }

    private fun irParaMovimentacaoEntrada() {
        val bundle = Bundle().apply {
            putInt(Key.TAB_INICIAL, 0)
        }
        findNavController().navigate(R.id.nav_moviment, bundle)
    }

    private fun irParaMovimentacaoSaida() {
        val bundle = Bundle().apply {
            putInt(Key.TAB_INICIAL, 1)
        }
        findNavController().navigate(R.id.nav_moviment, bundle)
    }

    private fun irParaMovimentacaoTransferencia() {
        val intent = Intent(requireContext(), TransferenciaActivity::class.java)
        startActivity(intent)
    }

    private fun irParaInventario() {
        findNavController().navigate(R.id.nav_inventario)
    }

}