package com.luanpsantanadev.saneam.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.FragmentListaUsuariosBinding
import com.luanpsantanadev.saneam.service.model.Perfil
import com.luanpsantanadev.saneam.viewmodel.UsuariosViewModel

class ListaUsuariosFragment : Fragment() {

    private var _binding: FragmentListaUsuariosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsuariosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaUsuariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnVoltar.setOnClickListener { requireActivity().finish() }
        binding.listaUsuarios.layoutManager = LinearLayoutManager(requireContext())
        viewModel.usuarios.observe(viewLifecycleOwner) { usuarios ->
            binding.listaUsuarios.adapter = UsuarioAdapter(usuarios) { usuario ->
                (requireActivity() as UsuariosActivity).abrirPermissoes(
                    usuario.id, usuario.nome, usuario.email
                )
            }
        }
        viewModel.carregando.observe(viewLifecycleOwner) {
            binding.progresso.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.erro.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private class UsuarioAdapter(
        private val itens: List<Perfil>,
        private val aoClicar: (Perfil) -> Unit
    ) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_usuario, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(itens[position], aoClicar)
        override fun getItemCount() = itens.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val nome = view.findViewById<TextView>(R.id.textNome)
            private val email = view.findViewById<TextView>(R.id.textEmail)
            fun bind(item: Perfil, click: (Perfil) -> Unit) {
                nome.text = item.nome
                email.text = item.email
                itemView.setOnClickListener { click(item) }
            }
        }
    }
}
