package com.luandev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luandev.saneam.R
import com.luandev.saneam.databinding.LayoutMaterialSelectorBottomSheetBinding
import com.luandev.saneam.service.model.ResumoMaterialGrupo
import com.luandev.saneam.viewmodel.MaterialSelectorViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds

class MaterialSelectorBottomSheet(
    private val onMaterialSelected: (ResumoMaterialGrupo) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: LayoutMaterialSelectorBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaterialSelectorViewModel by viewModels()
    private val adapter = MaterialAdapter { material ->
        onMaterialSelected(material)
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Estilo para o fundo arredondado (opcional, se não definido no layout)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutMaterialSelectorBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        configurarObservadores()
        
        // Busca inicial
        viewModel.buscarMateriais("")
    }

    private fun setupRecyclerView() {
        binding.rvMateriais.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMateriais.adapter = adapter
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        callbackFlow {
            val watcher = binding.edtSearch.doOnTextChanged { text, _, _, _ ->
                trySend(text?.toString()?.trim() ?: "")
            }
            awaitClose { binding.edtSearch.removeTextChangedListener(watcher) }
        }
            .debounce(400.milliseconds)
            .distinctUntilChanged()
            .onEach { termo ->
                viewModel.buscarMateriais(termo)
            }
            .launchIn(lifecycleScope)
    }

    private fun configurarObservadores() {
        viewModel.materiais.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }

        viewModel.carregando.observe(viewLifecycleOwner) { carregando ->
            binding.progressBar.visibility = if (carregando) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MaterialSelectorBottomSheet"
    }
}