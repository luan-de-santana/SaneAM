package com.luandev.saneam.service.util

import com.luandev.saneam.R

object IconeHelper {

    /**
     * Mapeia o nome do ícone vindo do Supabase para o recurso R.drawable correspondente.
     */
    fun obterIconeGrupo(nomeIcone: String?): Int {
        return when (nomeIcone) {
            "ic_build" -> R.drawable.ic_build
            "ic_handyman" -> R.drawable.ic_handyman
            "ic_cleaning" -> R.drawable.ic_cleaning
            "ic_engineering" -> R.drawable.ic_engineering
            "ic_inventory" -> R.drawable.ic_inventory
            "ic_manufacturing" -> R.drawable.ic_manufacturing
            "ic_precision" -> R.drawable.ic_precision
            "ic_plumbing" -> R.drawable.ic_plumbing
            "ic_construction" -> R.drawable.ic_construction
            "ic_security" -> R.drawable.ic_security
            "ic_shopping" -> R.drawable.ic_shopping
            "ic_water" -> R.drawable.ic_water
            "ic_grid" -> R.drawable.ic_grid
            else -> R.drawable.ic_grid // Ícone padrão
        }
    }
}