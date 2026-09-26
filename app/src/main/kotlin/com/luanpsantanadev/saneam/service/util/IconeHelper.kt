package com.luanpsantanadev.saneam.service.util

import com.luanpsantanadev.saneam.R

object IconeHelper {

    /**
     * Mapeia o nome do ícone vindo do Supabase para o recurso R.drawable correspondente.
     */
    fun obterIconeGrupo(nomeIcone: String?): Int {
        return when (nomeIcone) {
            "ic_cup" -> R.drawable.ic_cup
            "ic_oil" -> R.drawable.ic_oil
            "ic_link" -> R.drawable.ic_link
            "ic_fire" -> R.drawable.ic_fire
            "ic_build" -> R.drawable.ic_build
            "ic_paint" -> R.drawable.ic_paint
            "ic_monitor" -> R.drawable.ic_monitor
            "ic_carpenter" -> R.drawable.ic_carpenter
            "ic_foundation" -> R.drawable.ic_foundation
            "ic_science" -> R.drawable.ic_science
            "ic_uniform" -> R.drawable.ic_uniform
            "ic_handyman" -> R.drawable.ic_handyman
            "ic_newspaper" -> R.drawable.ic_newspaper
            "ic_cleaning" -> R.drawable.ic_cleaning
            "ic_electricity" -> R.drawable.ic_electricity
            "ic_engineering" -> R.drawable.ic_engineering
            "ic_inventory" -> R.drawable.ic_inventory
            "ic_manufacturing" -> R.drawable.ic_manufacturing
            "ic_precision" -> R.drawable.ic_precision
            "ic_plumbing" -> R.drawable.ic_plumbing
            "ic_construction" -> R.drawable.ic_construction
            "ic_security" -> R.drawable.ic_security
            "ic_security_person" -> R.drawable.ic_security_person
            "ic_shopping" -> R.drawable.ic_shopping
            "ic_water" -> R.drawable.ic_water
            "ic_grid" -> R.drawable.ic_grid
            else -> R.drawable.ic_grid // Ícone padrão
        }
    }
}