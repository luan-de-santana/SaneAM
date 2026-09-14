package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.luandev.saneam.databinding.FragmentAcertoBinding

class AcertoFragment : Fragment() {

    private var _binding: FragmentAcertoBinding? = null
    private val binding get() = _binding!!

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

        binding.btnConfirmarAcerto.setOnClickListener {
            val qtd = binding.btnConfirmarAcerto.text.toString()
            if (qtd.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Acerto de $qtd itens registrada!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.btnConfirmarAcerto.error = "Informe a quantidade"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}