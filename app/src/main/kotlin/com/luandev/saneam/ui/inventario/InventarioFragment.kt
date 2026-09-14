package com.luandev.saneam.ui.inventario

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.luandev.saneam.R
import com.luandev.saneam.databinding.FragmentInventarioBinding
import com.luandev.saneam.model.MaterialItem

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: InventarioAdapter
    private var depositoSelecionado: String = "Todos"
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

        setupRecyclerView()
        setupSearch()
        setupTabs()
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter(getDadosMock())
        binding.rvMateriais.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMateriais.adapter = adapter
    }

    private fun setupSearch() {
        binding.edtSearch.doOnTextChanged { text, _, _, _ ->
            termoBusca = text.toString().trim()
            adapter.aplicarFiltros(depositoSelecionado, termoBusca)
        }
    }

    private fun setupTabs() {
        binding.tabTodos.setOnClickListener { selectTab("Todos") }
        binding.tabGalpaoA.setOnClickListener { selectTab("Galpão A") }
        binding.tabLojaCentro.setOnClickListener { selectTab("Loja Centro") }
        binding.tabLojaNorte.setOnClickListener { selectTab("Loja Norte") }
    }

    private fun selectTab(deposito: String) {
        depositoSelecionado = deposito

        // Reset Visual de todas as abas
        val bgWhite = Color.WHITE
        val bgBlue = Color.parseColor("#60A5FA")
        val txtGray = Color.parseColor("#64748B")
        val txtWhite = Color.WHITE

        binding.tabTodos.setCardBackgroundColor(if (deposito == "Todos") bgBlue else bgWhite)
        binding.txtTabTodos.setTextColor(if (deposito == "Todos") txtWhite else txtGray)

        binding.tabGalpaoA.setCardBackgroundColor(if (deposito == "Galpão A") bgBlue else bgWhite)
        binding.txtTabGalpaoA.setTextColor(if (deposito == "Galpão A") txtWhite else txtGray)

        binding.tabLojaCentro.setCardBackgroundColor(if (deposito == "Loja Centro") bgBlue else bgWhite)
        binding.txtTabLojaCentro.setTextColor(if (deposito == "Loja Centro") txtWhite else txtGray)

        binding.tabLojaNorte.setCardBackgroundColor(if (deposito == "Loja Norte") bgBlue else bgWhite)
        binding.txtTabLojaNorte.setTextColor(if (deposito == "Loja Norte") txtWhite else txtGray)

        // Aplica o filtro atualizado na lista
        adapter.aplicarFiltros(depositoSelecionado, termoBusca)
    }

    private fun getDadosMock(): List<MaterialItem> {
        return listOf(
            MaterialItem(
                "1",
                "Registro de Gaveta 50mm",
                "Galpão A",
                "12/2027",
                34,
                "un",
                10,
                R.drawable.ic_build
            ),
            MaterialItem("2", "Tubo PVC Soldável 25mm", "Galpão A", null, 120, "m", 50, R.drawable.ic_build),
            MaterialItem("3", "Registro de Esfera 3/4\"", "Loja Norte", null, 2, "un", 10, R.drawable.ic_build),
            MaterialItem("4", "Capacete de Segurança", "Galpão A", "06/2028", 15, "un", 5, R.drawable.ic_build),
            MaterialItem("5", "Luva de PVC 25mm", "Galpão A", null, 5, "un", 20, R.drawable.ic_construction)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}