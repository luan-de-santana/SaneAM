package com.luanpsantanadev.saneam.ui.movimentacoes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.luanpsantanadev.saneam.databinding.ItemMaterialBinding
import com.luanpsantanadev.saneam.service.model.ResumoMaterialGrupo
import com.luanpsantanadev.saneam.service.util.IconeHelper

class MaterialAdapter(
    private val onItemClick: (ResumoMaterialGrupo) -> Unit
) : ListAdapter<ResumoMaterialGrupo, MaterialAdapter.MaterialViewHolder>(DiffCallback) {

    class MaterialViewHolder(val binding: ItemMaterialBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {
            val resId = IconeHelper.obterIconeGrupo(item.iconeGrupo)
            val textSubtitulo = "${item.codigoAlpha} • ${item.unidadeMedida}"

            txtNome.text = item.nome
            imgIcone.setImageResource(resId)
            txtSubtitulo.text = textSubtitulo

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    fun atualizarDados(novaLista: List<ResumoMaterialGrupo>) {
        submitList(novaLista)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ResumoMaterialGrupo>() {
        override fun areItemsTheSame(
            oldItem: ResumoMaterialGrupo,
            newItem: ResumoMaterialGrupo
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ResumoMaterialGrupo,
            newItem: ResumoMaterialGrupo
        ): Boolean {
            return oldItem == newItem
        }
    }
}