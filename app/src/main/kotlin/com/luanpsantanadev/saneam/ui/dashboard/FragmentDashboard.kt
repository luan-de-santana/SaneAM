package com.luanpsantanadev.saneam.ui.dashboard

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentDashboardBinding
import com.luanpsantanadev.saneam.service.model.GrupoComContagem
import com.luanpsantanadev.saneam.service.model.PapelUsuario
import com.luanpsantanadev.saneam.service.util.IconeHelper
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.service.util.ConstantsSaneAM.Key
import com.luanpsantanadev.saneam.service.util.configurarCabecalhoPadrao
import com.luanpsantanadev.saneam.ui.movimentacoes.TransferenciaActivity
import com.luanpsantanadev.saneam.viewmodel.DashboardViewModel
import com.luanpsantanadev.saneam.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

class FragmentDashboard : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private val perfilViewModel: PerfilViewModel by activityViewModels()

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
        binding.root.aplicarInsetsBarrasSistema()

        configurarObservadorPerfil()
        configurarCliques()
        configurarObservadores()
    }

    private fun configurarObservadores() {
        viewModel.carregandoResumo.observe(viewLifecycleOwner) { carregando ->
            binding.swipeRefreshDashboard.isRefreshing = carregando
        }

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
        binding.swipeRefreshDashboard.setOnRefreshListener {
            viewModel.carregarResumo()
        }
        binding.cardDepositos.setOnClickListener {
            exibirDialogoDepositos()
        }
        binding.cardGrupos.setOnClickListener {
            exibirDialogoGrupos()
        }
        binding.cardCriticos.setOnClickListener {
            exibirDialogoMateriaisCriticos()
        }
        binding.cardMateriais.setOnClickListener {
            irParaInventario()
        }
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

    private fun exibirDialogoDepositos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val depositos = viewModel.obterDepositosComAcesso().getOrElse {
                exibirErro(getString(R.string.erro_carregar_depositos, it.localizedMessage ?: ""))
                return@launch
            }

            val linhas = depositos
                .map { item ->
                    val permissao = item.papel?.let(::textoPermissao)
                        ?: getString(R.string.permissao_administrador)
                    getString(R.string.deposito_com_permissao, item.nome, permissao)
                }
                .toTypedArray()

            AlertDialog.Builder(requireContext(), R.style.Theme_SaneAM_LightDialog)
                .setTitle(R.string.depositos_com_acesso)
                .setItems(
                    if (linhas.isNotEmpty()) linhas
                    else arrayOf(getString(R.string.nenhum_deposito_com_acesso)),
                    null
                )
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun exibirDialogoGrupos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val grupos = viewModel.obterGruposComContagem().getOrElse {
                exibirErro(getString(R.string.erro_carregar_grupos, it.localizedMessage ?: ""))
                return@launch
            }
            val conteudo = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(8), dp(20), dp(8))
            }

            grupos.forEach { item ->
                conteudo.addView(criarLinhaGrupo(item))
            }

            val scrollView = ScrollView(requireContext()).apply {
                isFillViewport = true
                addView(conteudo)
            }

            AlertDialog.Builder(requireContext(), R.style.Theme_SaneAM_LightDialog)
                .setTitle(R.string.grupos_e_materiais)
                .setView(scrollView)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun exibirDialogoMateriaisCriticos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val itens = viewModel.obterItensEstoqueCritico().getOrElse {
                exibirErro(
                    getString(
                        R.string.erro_carregar_materiais,
                        it.localizedMessage ?: ""
                    )
                )
                return@launch
            }

            if (itens.isEmpty()) {
                AlertDialog.Builder(requireContext(), R.style.Theme_SaneAM_LightDialog)
                    .setTitle(R.string.materiais_estoque_critico)
                    .setMessage(R.string.nenhum_material_estoque_critico)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return@launch
            }

            val texto = itens.joinToString(separator = "\n\n") { item ->
                listOf(
                    getString(R.string.codigo_alpha_label, item.codigoAlpha),
                    getString(R.string.nome_material_label, item.nomeMaterial),
                    getString(R.string.deposito_material_label, item.nomeDeposito),
                    getString(R.string.quantidade_atual_label, item.quantidade.toString()),
                    getString(R.string.quantidade_minima_label, item.quantidadeMinima.toString())
                ).joinToString(separator = "\n")
            }
            val conteudo = TextView(requireContext()).apply {
                text = texto
                textSize = 12f
                setTextColor(requireContext().getColor(R.color.text_primary))
                setPadding(dp(20), dp(8), dp(20), dp(8))
            }
            val scrollView = ScrollView(requireContext()).apply {
                isFillViewport = true
                addView(conteudo)
            }

            AlertDialog.Builder(requireContext(), R.style.Theme_SaneAM_LightDialog)
                .setTitle(R.string.materiais_estoque_critico)
                .setView(scrollView)
                .setPositiveButton(R.string.compartilhar_whatsapp) { _, _ ->
                    compartilharCriticosPeloWhatsApp(texto)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun compartilharCriticosPeloWhatsApp(texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
            setPackage("com.whatsapp")
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            exibirErro(getString(R.string.whatsapp_nao_instalado))
        }
    }

    private fun criarLinhaGrupo(grupo: GrupoComContagem): View =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(10))

            addView(
                ImageView(requireContext()).apply {
                    setImageResource(IconeHelper.obterIconeGrupo(grupo.iconeRes))
                    contentDescription = getString(R.string.icone_grupo, grupo.nome)
                    imageTintList = requireContext().getColorStateList(R.color.text_primary)
                },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply {
                    marginEnd = dp(16)
                }
            )

            addView(
                TextView(requireContext()).apply {
                    val textoQuantidade = if (grupo.quantidadeMateriais == 1L) {
                        getString(R.string.quantidade_um_material)
                    } else {
                        getString(R.string.quantidade_materiais_plural, grupo.quantidadeMateriais)
                    }
                    text = getString(
                        R.string.grupo_com_quantidade,
                        grupo.nome,
                        textoQuantidade
                    )
                    setTextColor(requireContext().getColor(R.color.text_primary))
                    textSize = 16f
                },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
        }

    private fun textoPermissao(papel: PapelUsuario): String = when (papel) {
        PapelUsuario.LEITOR -> getString(R.string.permissao_leitura)
        PapelUsuario.OPERADOR -> getString(R.string.permissao_operador)
    }

    private fun exibirErro(mensagem: String) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show()
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

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