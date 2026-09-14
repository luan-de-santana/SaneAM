package com.luandev.saneam.ui.inventario

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luandev.saneam.R
import com.luandev.saneam.model.MaterialItem
import com.luandev.saneam.databinding.ItemMaterialInventarioBinding

class InventarioAdapter(
    private var listaCompleta: List<MaterialItem>
) : RecyclerView.Adapter<InventarioAdapter.MaterialViewHolder>() {

    private var listaFiltrada: List<MaterialItem> = listaCompleta

    inner class MaterialViewHolder(val binding: ItemMaterialInventarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialInventarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val item = listaFiltrada[position]
        val context = holder.itemView.context

        with(holder.binding) {
            txtNome.text = item.nome
            imgIcone.setImageResource(item.iconeRes)
            txtQuantidade.text = item.quantidade.toString()
            txtUnidade.text = item.unidade
            txtMinimo.text = "mín ${item.quantidadeMinima}"

            // Formatação do Subtítulo (Depósito + Validade se existir)
            if (!item.validade.isNullOrEmpty()) {
                val textoCompleto = "${item.deposito} · Val ${item.validade}"
                val spannable = SpannableString(textoCompleto)
                val inicioValidade = textoCompleto.indexOf("Val")
                val corLaranja = Color.parseColor("#C2410C")

                spannable.setSpan(
                    ForegroundColorSpan(corLaranja),
                    inicioValidade,
                    textoCompleto.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                txtSubtitulo.text = spannable
            } else {
                txtSubtitulo.text = item.deposito
            }

            val backgroundDrawable = GradientDrawable().apply {
                setColor(Color.WHITE) // Cor interna do card
                cornerRadius = 16.toPx(context) // Converte raio para pixels
            }

            // Lógica de Alerta de Estoque Mínimo
            val isEstoqueBaixo = item.quantidade <= item.quantidadeMinima

            if (isEstoqueBaixo) {
                txtQuantidade.setTextColor(Color.parseColor("#B91C1C")) // Vermelho
                progressEstoque.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.custom_progress_red)
                // Aplica a borda vermelha manualmente
                backgroundDrawable.setStroke(2.toPx(context).toInt(), Color.parseColor("#FECACA"))
            } else {
                txtQuantidade.setTextColor(Color.parseColor("#0F172A")) // Escuro padrão
                progressEstoque.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.custom_progress_green)
                // Sem borda
                backgroundDrawable.setStroke(0, Color.TRANSPARENT)
            }
            cardItemContainer.background = backgroundDrawable

            // Cálculo da barra de progresso
            val maxProgress = (item.quantidadeMinima * 2).coerceAtLeast(1)
            progressEstoque.max = maxProgress
            progressEstoque.progress = item.quantidade.coerceAtMost(maxProgress)
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun aplicarFiltros(depositoSelecionado: String?, busca: String?) {
        listaFiltrada = listaCompleta.filter { item ->
            val atendeDeposito = depositoSelecionado.isNullOrEmpty() ||
                    depositoSelecionado.equals("Todos", ignoreCase = true) ||
                    item.deposito.equals(depositoSelecionado, ignoreCase = true)

            val atendeBusca = busca.isNullOrEmpty() ||
                    item.nome.contains(busca, ignoreCase = true)

            atendeDeposito && atendeBusca
        }
        notifyDataSetChanged()
    }
}

fun Int.toPx(context: android.content.Context): Float {
    return this * context.resources.displayMetrics.density
}