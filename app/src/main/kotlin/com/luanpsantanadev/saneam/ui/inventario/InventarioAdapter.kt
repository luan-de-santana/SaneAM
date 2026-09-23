package com.luanpsantanadev.saneam.ui.inventario

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.service.model.ResumoMaterialDeposito
import com.luanpsantanadev.saneam.databinding.ItemMaterialInventarioBinding
import com.luanpsantanadev.saneam.service.util.IconeHelper

class InventarioAdapter(
    private val onItemClick: (ResumoMaterialDeposito) -> Unit
) : ListAdapter<ResumoMaterialDeposito, InventarioAdapter.MaterialViewHolder>(DiffCallback) {

    class MaterialViewHolder(val binding: ItemMaterialInventarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialInventarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context

        with(holder.binding) {
            val textQuantMinima = "mín ${item.quantidadeMinima}"
            val textSubtitulo = "${item.codigoAlpha} • ${item.nomeDeposito}"
            val resId = IconeHelper.obterIconeGrupo(item.iconeGrupo)

            txtNome.text = item.nomeMaterial
            imgIcone.setImageResource(resId)
            txtQuantidade.text = item.quantidade.toString()
            txtUnidade.text = item.unidadeMedida
            txtMinimo.text = textQuantMinima
            txtSubtitulo.text = textSubtitulo
            cardItemContainer.setOnClickListener {
                onItemClick(item)
            }

            val backgroundDrawable = GradientDrawable().apply {
                setColor(ContextCompat.getColor(context, R.color.white))
                cornerRadius = 16.toPx(context) // Converte raio para pixels
            }

            // Lógica de Alerta de Estoque Mínimo
            val isEstoqueBaixo = item.quantidade <= item.quantidadeMinima

            if (isEstoqueBaixo) {
                txtQuantidade.setTextColor(ContextCompat.getColor(context, R.color.red_dark))
                progressEstoque.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.custom_progress_red)
                // Aplica a borda vermelha manualmente
                backgroundDrawable.setStroke(
                    2.toPx(context).toInt(),
                    ContextCompat.getColor(context, R.color.red_light)
                )
            } else {
                txtQuantidade.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                progressEstoque.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.custom_progress_green)
                // Sem borda
                backgroundDrawable.setStroke(0, Color.TRANSPARENT)
            }
            cardItemContainer.background = backgroundDrawable

            // Cálculo da barra de progresso
            val maxProgress = (item.quantidadeMinima * 2).coerceAtLeast(1.0)
            progressEstoque.max = maxProgress.toInt()
            progressEstoque.progress = item.quantidade.coerceAtMost(maxProgress).toInt()
        }
    }

    fun atualizarDados(novaLista: List<ResumoMaterialDeposito>) {
        submitList(novaLista)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ResumoMaterialDeposito>() {
        override fun areItemsTheSame(oldItem: ResumoMaterialDeposito, newItem: ResumoMaterialDeposito): Boolean {
            return oldItem.idEstoque == newItem.idEstoque
        }

        override fun areContentsTheSame(oldItem: ResumoMaterialDeposito, newItem: ResumoMaterialDeposito): Boolean {
            return oldItem == newItem
        }
    }
}

fun Int.toPx(context: android.content.Context): Float {
    return this * context.resources.displayMetrics.density
}