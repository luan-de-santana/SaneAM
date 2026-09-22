package com.luanpsantanadev.saneam.ui.movimentacoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.LayoutMaterialSelectorBottomSheetBinding
import com.luanpsantanadev.saneam.service.model.ResumoMaterialGrupo
import com.luanpsantanadev.saneam.viewmodel.MaterialSelectorViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds

class MaterialSelectorBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutMaterialSelectorBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaterialSelectorViewModel by viewModels()
    private val adapter = MaterialAdapter { material ->
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_ID to material.id,
                RESULT_CODIGO_ALPHA to material.codigoAlpha,
                RESULT_NOME to material.nome,
                RESULT_UNIDADE_MEDIDA to material.unidadeMedida,
                RESULT_ID_GRUPO to material.idGrupo,
                RESULT_NOME_GRUPO to material.nomeGrupo,
                RESULT_ICONE_GRUPO to material.iconeGrupo
            )
        )
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
            awaitClose { _binding?.edtSearch?.removeTextChangedListener(watcher) }
        }
            .debounce(400.milliseconds)
            .distinctUntilChanged()
            .onEach { termo ->
                viewModel.buscarMateriais(termo)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
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
        const val REQUEST_KEY = "material_selector_result"

        private const val RESULT_ID = "id"
        private const val RESULT_CODIGO_ALPHA = "codigo_alpha"
        private const val RESULT_NOME = "nome"
        private const val RESULT_UNIDADE_MEDIDA = "unidade_medida"
        private const val RESULT_ID_GRUPO = "id_grupo"
        private const val RESULT_NOME_GRUPO = "nome_grupo"
        private const val RESULT_ICONE_GRUPO = "icone_grupo"

        fun newInstance() = MaterialSelectorBottomSheet()

        fun materialFromResult(result: Bundle): ResumoMaterialGrupo =
            ResumoMaterialGrupo(
                id = result.getLong(RESULT_ID).takeUnless { it == 0L },
                codigoAlpha = result.getInt(RESULT_CODIGO_ALPHA),
                nome = result.getString(RESULT_NOME).orEmpty(),
                unidadeMedida = result.getString(RESULT_UNIDADE_MEDIDA).orEmpty(),
                idGrupo = result.getLong(RESULT_ID_GRUPO),
                nomeGrupo = result.getString(RESULT_NOME_GRUPO).orEmpty(),
                iconeGrupo = result.getString(RESULT_ICONE_GRUPO).orEmpty()
            )
    }
}