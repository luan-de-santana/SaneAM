package com.luandev.saneam.service.util

class ConstantsSaneAM {

    object Key {
        const val TAB_INICIAL = "tab_inicial"
    }

    object Supabase {
        // Tabelas e Views
        const val ESTOQUES = "estoques"
        const val MOVIMENTACOES = "movimentacoes"
        const val PERFIS = "perfis"
        const val PERMISSOES_USUARIO = "permissoes_usuario"
        const val DEPOSITOS = "depositos"
        const val GRUPOS = "grupos"
        const val MATERIAIS = "materiais"
        const val VISAO_ITENS_ESTOQUE_BAIXO = "visao_itens_estoque_baixo"
        const val VISAO_RESUMO_MATERIAIS = "visao_resumo_materiais"
        const val VISAO_RESUMO_MATERIAIS_GRUPOS = "visao_resumo_materiais_grupos"
        const val VISAO_RESUMO_MATERIAIS_DEPOSITOS = "visao_resumo_materiais_depositos"

        // Funções RPC
        const val RPC_OBTER_RESUMO_DASHBOARD = "obter_resumo_dashboard"
        const val RPC_TRANSFERIR_ESTOQUE = "transferir_estoque"

        // Colunas e Filtros
        const val COL_ID = "id"
        const val COL_NOME = "nome"
        const val COL_ID_MATERIAL = "id_material"
        const val COL_ID_DEPOSITO = "id_deposito"
        const val COL_ID_USUARIO = "id_usuario"
        const val COL_CODIGO_ALPHA = "codigo_alpha"
        const val COL_NOME_MATERIAL = "nome_material"
        const val COL_PERCENTUAL_RESTANTE = "percentual_restante"
    }

}