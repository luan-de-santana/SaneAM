package com.luanpsantanadev.saneam.ui.config

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.luanpsantanadev.saneam.databinding.FragmentConfiguracoesBinding
import com.luanpsantanadev.saneam.service.repository.RepositorioDeposito
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.service.repository.SupabaseClientProvider
import com.luanpsantanadev.saneam.service.util.configurarCabecalhoPadrao
import com.luanpsantanadev.saneam.ui.autenticacao.AuthActivity
import com.luanpsantanadev.saneam.viewmodel.ConfigViewModel
import com.luanpsantanadev.saneam.viewmodel.PerfilViewModel
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfiguracoesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConfigViewModel by viewModels()
    private val perfilViewModel: PerfilViewModel by activityViewModels()

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

        configurarObservadorPerfil()
        configurarCliques()
        configurarObservadores()
    }

    private fun configurarObservadorPerfil() {
        perfilViewModel.perfil.observe(viewLifecycleOwner) { perfil ->
            perfil ?: return@observe
            configurarCabecalhoPadrao(
                binding.cardWelcome.textSaudacao,
                binding.cardWelcome.textDataAtual,
                binding.cardWelcome.imagePerfil,
                perfil
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configurarCliques() {
        binding.btnUsuarios.setOnClickListener {
            startActivity(Intent(requireContext(), UsuariosActivity::class.java))
        }
        // Evento do botão de Desconectar
        binding.btnDesconectar.setOnClickListener {
            exibirDialogConfirmacaoSair()
        }
    }

    private fun configurarObservadores() {
        viewModel.usuarioAdministrador.observe(viewLifecycleOwner) { ehAdministrador ->
            binding.btnUsuarios.visibility =
                if (ehAdministrador) View.VISIBLE else View.GONE
        }
    }

    private fun exibirDialogConfirmacaoSair() {
        AlertDialog.Builder(
            requireContext(),
            com.luanpsantanadev.saneam.R.style.Theme_SaneAM_LightDialog
        )
            .setTitle(getString(com.luanpsantanadev.saneam.R.string.desconectar_titulo))
            .setMessage(getString(com.luanpsantanadev.saneam.R.string.confirmar_saida_conta))
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
                val idUsuario = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                SupabaseClientProvider.client.auth.signOut()
                perfilViewModel.limparPerfil()
                if (idUsuario != null) {
                    RepositorioDeposito().invalidarCache(idUsuario)
                }

                // Redireciona para a tela de Auth/Login
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(com.luanpsantanadev.saneam.R.string.erro_sair, e.message),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

}