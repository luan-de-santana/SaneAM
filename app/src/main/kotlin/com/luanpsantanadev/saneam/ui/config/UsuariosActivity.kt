package com.luanpsantanadev.saneam.ui.config

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.ActivityUsuariosBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema

class UsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsuariosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.aplicarInsetsBarrasSistema(topo = true, laterais = true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.usuariosContainer, ListaUsuariosFragment())
                .commit()
        }
    }

    fun abrirPermissoes(idUsuario: String, nome: String, email: String) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.usuariosContainer,
                PermissoesUsuarioFragment.newInstance(idUsuario, nome, email)
            )
            .addToBackStack(null)
            .commit()
    }
}
