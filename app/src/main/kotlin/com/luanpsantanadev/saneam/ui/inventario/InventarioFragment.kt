package com.luanpsantanadev.saneam.ui.inventario

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentInventarioBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.service.model.Deposito
import com.luanpsantanadev.saneam.viewmodel.InventarioViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventarioViewModel by viewModels()
    private lateinit var adapter: InventarioAdapter
    
    private var idDepositoSelecionado: Long? = null // null significa "Todos"
    private var termoBusca: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.aplicarInsetsBarrasSistema()

        setupRecyclerView()
        setupSearch()
        setupListenersFixos()
        configurarObservadores()
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter()
        binding.rvMateriais.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMateriais.adapter = adapter
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        callbackFlow {
            val watcher = binding.edtSearch.doOnTextChanged { text, _, _, _ ->
                trySend(text?.toString()?.trim() ?: "")
            }
            awaitClose { _binding?.edtSearch?.removeTextChangedListener(watcher) }
        }
            .debounce(500.milliseconds) // Aguarda 500ms após a última digitação
            .distinctUntilChanged() // Só dispara se o texto for diferente do anterior
            .onEach { termo ->
                termoBusca = termo
                viewModel.buscarMateriais(termoBusca, idDepositoSelecionado)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupListenersFixos() {
        binding.tabTodos.setOnClickListener {
            idDepositoSelecionado = null
            atualizarFiltroVisual()
            viewModel.buscarMateriais(termoBusca, idDepositoSelecionado)
        }
    }

    private fun configurarObservadores() {
        viewModel.depositos.observe(viewLifecycleOwner) { lista ->
            renderizarAbasDepositos(lista)
        }

        viewModel.materiais.observe(viewLifecycleOwner) { lista ->
            adapter.atualizarDados(lista)
        }

        viewModel.erro.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderizarAbasDepositos(lista: List<Deposito>) {
        // "Todos" é o índice 0
        if (binding.containerDepositos.childCount > 1) {
            binding.containerDepositos.removeViews(1, binding.containerDepositos.childCount - 1)
        }

        lista.forEach { deposito ->
            val cardAba = layoutInflater.inflate(R.layout.item_aba_deposito, binding.containerDepositos, false) as CardView
            val textAba = cardAba.findViewById<TextView>(R.id.txtTabNome)
            
            textAba.text = deposito.nome
            cardAba.tag = deposito.id // Salva o ID para identificar no clique

            cardAba.setOnClickListener {
                idDepositoSelecionado = deposito.id
                atualizarFiltroVisual()
                viewModel.buscarMateriais(termoBusca, idDepositoSelecionado)
            }

            binding.containerDepositos.addView(cardAba)
        }
        
        atualizarFiltroVisual() // Garante que a seleção atual seja refletida
    }

    private fun atualizarFiltroVisual() {
        val bgWhite = requireContext().getColor(R.color.white)
        val bgBlue = requireContext().getColor(R.color.blue)
        val txtGray = requireContext().getColor(R.color.text_secondary)
        val txtWhite = Color.WHITE

        // Reset e Atualização da aba "Todos"
        binding.tabTodos.setCardBackgroundColor(if (idDepositoSelecionado == null) bgBlue else bgWhite)
        binding.txtTabTodos.setTextColor(if (idDepositoSelecionado == null) txtWhite else txtGray)

        // Iterar sobre as abas dinâmicas no container
        for (i in 1 until binding.containerDepositos.childCount) {
            val card = binding.containerDepositos.getChildAt(i) as CardView
            val textView = card.getChildAt(0) as TextView
            val isSelected = card.tag == idDepositoSelecionado

            card.setCardBackgroundColor(if (isSelected) bgBlue else bgWhite)
            textView.setTextColor(if (isSelected) txtWhite else txtGray)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}