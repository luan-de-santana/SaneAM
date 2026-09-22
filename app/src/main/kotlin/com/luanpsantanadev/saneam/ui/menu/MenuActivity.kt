package com.luanpsantanadev.saneam.ui.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.luanpsantanadev.saneam.R
import com.luanpsantanadev.saneam.databinding.ActivityMenuBinding
import com.luanpsantanadev.saneam.service.util.aplicarInsetsBarrasSistema
import com.luanpsantanadev.saneam.viewmodel.PerfilViewModel

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private val perfilViewModel: PerfilViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        perfilViewModel.carregarPerfil()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navHostFragmentActivityMain.aplicarInsetsBarrasSistema(topo = true, inferior = false)
        binding.bottomNavigation.aplicarInsetsBarrasSistema(topo = false)

        // Configura bottom navigation
        configurarNavegacao()
    }

    private fun configurarNavegacao() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
    }

}