package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.luandev.saneam.databinding.FragmentSaidaBinding
import com.luandev.saneam.service.model.Deposito
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.viewmodel.DepositoSelectorViewModel

class SaidaFragment : Fragment() {

    private var _binding: FragmentSaidaBinding? = null
    private val binding get() = _binding!!

    private val depositoViewModel: DepositoSelectorViewModel by viewModels()
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
    }

    private fun configurarCliques() {
        binding.edtMaterial.setOnClickListener {
            abrirSeletorMaterial()
        }

        binding.btnConfirmarSaida.setOnClickListener {
            val qtd = binding.edtQuantidade.text.toString()
            val materialId = materialSelecionado?.id
            
            val posicaoSelecionada = binding.spinnerDeposito.selectedItemPosition
            val deposito = if (posicaoSelecionada != -1 && listaDepositos.isNotEmpty()) listaDepositos[posicaoSelecionada] else null

            if (materialId == null) {
                Toast.makeText(requireContext(), "Selecione um material", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (deposito == null) {
                Toast.makeText(requireContext(), "Selecione um depósito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (qtd.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Saída de $qtd ${materialSelecionado?.nome} de ${deposito.nome} registrada!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.edtQuantidade.error = "Informe a quantidade"
            }
        }
    }

    private fun abrirSeletorMaterial() {
        val bottomSheet = MaterialSelectorBottomSheet { material ->
            materialSelecionado = material
            binding.edtMaterial.setText(material.nome)
        }
        bottomSheet.show(childFragmentManager, MaterialSelectorBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}