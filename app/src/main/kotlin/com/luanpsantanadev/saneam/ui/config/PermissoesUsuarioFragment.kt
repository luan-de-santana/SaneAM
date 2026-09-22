package com.luanpsantanadev.saneam.ui.config

import android.os.Bundle
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentPermissoesUsuarioBinding
import com.luanpsantanadev.saneam.service.model.Deposito
import com.luanpsantanadev.saneam.service.model.PapelUsuario
import com.luanpsantanadev.saneam.viewmodel.PermissaoDepositoUi
import com.luanpsantanadev.saneam.viewmodel.PermissoesUsuarioViewModel

class PermissoesUsuarioFragment : Fragment() {

    private var _binding: FragmentPermissoesUsuarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PermissoesUsuarioViewModel by viewModels()
    private lateinit var idUsuario: String
    private var itens: List<PermissaoDepositoUi> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idUsuario = requireArguments().getString(ARG_ID).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissoesUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.textNomeUsuario.text = requireArguments().getString(ARG_NOME)
        binding.textEmailUsuario.text = requireArguments().getString(ARG_EMAIL)
        binding.btnVoltar.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnSalvar.setOnClickListener { viewModel.salvar(idUsuario, itens) }
        binding.listaPermissoes.layoutManager = LinearLayoutManager(requireContext())
        viewModel.itens.observe(viewLifecycleOwner) {
            itens = it
            binding.listaPermissoes.adapter = PermissaoAdapter(itens)
        }
        viewModel.carregando.observe(viewLifecycleOwner) {
            binding.progresso.visibility = if (it) View.VISIBLE else View.GONE
            binding.btnSalvar.isEnabled = !it
        }
        viewModel.salvando.observe(viewLifecycleOwner) {
            binding.btnSalvar.isEnabled = !it
        }
        viewModel.mensagem.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
        viewModel.salvo.observe(viewLifecycleOwner) { salvo ->
            if (salvo) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        viewModel.carregar(idUsuario)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private class PermissaoAdapter(
        private val itens: List<PermissaoDepositoUi>
    ) : RecyclerView.Adapter<PermissaoAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_permissao_deposito, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(itens[position])
        override fun getItemCount() = itens.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val nome = view.findViewById<TextView>(R.id.textNomeDeposito)
            private val endereco = view.findViewById<TextView>(R.id.textEnderecoDeposito)
            private val spinner = view.findViewById<android.widget.Spinner>(R.id.spinnerPapel)
            fun bind(item: PermissaoDepositoUi) {
                nome.text = item.deposito.nome
                endereco.text = item.deposito.endereco.orEmpty()
                val posicao = when (item.papel) {
                    null -> 0
                    PapelUsuario.LEITOR -> 1
                    PapelUsuario.OPERADOR -> 2
                }
                val papeis = listOf("Sem acesso", "Leitor", "Operador")
                val adapter = object : ArrayAdapter<String>(
                    itemView.context,
                    android.R.layout.simple_spinner_item,
                    papeis
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        return criarTexto(position, convertView, parent)
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = criarTexto(position, convertView, parent)
                        view.setBackgroundColor(Color.WHITE)
                        return view
                    }

                    private fun criarTexto(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = (convertView as? TextView) ?: TextView(itemView.context)
                        view.text = getItem(position)
                        view.setTextColor(Color.rgb(30, 41, 59))
                        view.textSize = 14f
                        view.gravity = android.view.Gravity.CENTER_VERTICAL
                        view.minHeight = (48 * itemView.resources.displayMetrics.density).toInt()
                        view.setPadding(16, 8, 8, 8)
                        return view
                    }
                }
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                spinner.setSelection(posicao, false)
                spinner.onItemSelectedListener =
                    object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
                        override fun onItemSelected(
                            parent: android.widget.AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            item.papel = when (position) {
                                1 -> PapelUsuario.LEITOR
                                2 -> PapelUsuario.OPERADOR
                                else -> null
                            }
                        }
                    }
            }
        }
    }

    companion object {
        private const val ARG_ID = "id_usuario"
        private const val ARG_NOME = "nome_usuario"
        private const val ARG_EMAIL = "email_usuario"

        fun newInstance(id: String, nome: String, email: String) =
            PermissoesUsuarioFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_NOME, nome)
                    putString(ARG_EMAIL, email)
                }
            }
    }
}
