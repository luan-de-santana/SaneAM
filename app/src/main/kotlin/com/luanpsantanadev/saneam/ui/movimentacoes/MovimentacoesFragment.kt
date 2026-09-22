package com.luanpsantanadev.saneam.ui.movimentacoes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentMovimentacoesBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.service.util.ConstantsSaneAM.Key

class MovimentacoesFragment : Fragment() {

    // Referência nula para evitar memory leaks no ciclo de vida do Fragment
    private var _binding: FragmentMovimentacoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovimentacoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.aplicarInsetsBarrasSistema()

        // Recupera a aba inicial via argumentos (Navigation) ou padrão 0 (Entrada)
        val abaInicial = arguments?.getInt(Key.TAB_INICIAL) ?: 0
        selectTab(abaInicial)

        // Configuração dos cliques usando a variável binding
        binding.tabEntrada.setOnClickListener { selectTab(0) }
        binding.tabSaida.setOnClickListener { selectTab(1) }
        binding.tabAcerto.setOnClickListener { selectTab(2) }

        binding.btnTransferir.setOnClickListener {
            // Ação do botão Transferir (ex: abrir dialog ou nova tela)
            val intent = Intent(requireContext(), TransferenciaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectTab(position: Int) {
        val transparent = Color.TRANSPARENT
        val white = Color.WHITE
        val grayText = ContextCompat.getColor(requireContext(), R.color.md_theme_light_outline)
        val greenLine = ContextCompat.getColor(requireContext(), R.color.badge_green_line)
        val redLine = ContextCompat.getColor(requireContext(), R.color.badge_red_line)
        val amberLine = ContextCompat.getColor(requireContext(), R.color.badge_amber_line)

        // Reset do fundo de todos os cards das abas
        binding.tabEntrada.setCardBackgroundColor(transparent)
        binding.tabSaida.setCardBackgroundColor(transparent)
        binding.tabAcerto.setCardBackgroundColor(transparent)

        // Reset da cor do texto de todas as abas
        binding.txtTabEntrada.setTextColor(grayText)
        binding.txtTabSaida.setTextColor(grayText)
        binding.txtTabAcerto.setTextColor(grayText)

        // Define a aba ativa e instancia o Fragment correspondente
        val targetFragment: Fragment = when (position) {
            0 -> {
                binding.tabEntrada.setCardBackgroundColor(white)
                binding.txtTabEntrada.setTextColor(greenLine) // Verde
                EntradaFragment()
            }

            1 -> {
                binding.tabSaida.setCardBackgroundColor(white)
                binding.txtTabSaida.setTextColor(redLine) // Vermelho
                SaidaFragment()
            }

            else -> {
                binding.tabAcerto.setCardBackgroundColor(white)
                binding.txtTabAcerto.setTextColor(amberLine) // Laranja
                AcertoFragment()
            }
        }

        // Troca do sub-fragment no container
        childFragmentManager.beginTransaction()
            .replace(R.id.movementContainer, targetFragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpa a referência ao destruir a View do Fragment para evitar memory leaks
        _binding = null
    }
}