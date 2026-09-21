package com.luandev.saneam.ui.config

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.luandev.saneam.databinding.FragmentConfiguracoesBinding
import com.luandev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luandev.saneam.service.repository.SupabaseClientProvider
import com.luandev.saneam.service.util.configurarCabecalhoPadrao
import com.luandev.saneam.ui.autenticacao.AuthActivity
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracoesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.aplicarInsetsBarrasSistema()

        configurarCabecalho()
        configurarCliques()
    }

    private fun configurarCabecalho() {
        configurarCabecalhoPadrao(binding.textSaudacao, binding.textDataAtual)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configurarCliques() {
        // Evento do botão de Desconectar
        binding.btnDesconectar.setOnClickListener {
            exibirDialogConfirmacaoSair()
        }
    }

    private fun exibirDialogConfirmacaoSair() {
        AlertDialog.Builder(requireContext())
            .setTitle("Desconectar")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sim, sair") { _, _ ->
                realizarLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun realizarLogout() {
        lifecycleScope.launch {
            try {
                // Encerra a sessão no Supabase e remove os tokens locais
                SupabaseClientProvider.client.auth.signOut()

                // Redireciona para a tela de Auth/Login
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao sair: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}