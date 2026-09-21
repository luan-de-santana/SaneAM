package com.luandev.saneam.ui.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.luandev.saneam.R
import com.luandev.saneam.databinding.ActivityMenuBinding
import com.luandev.saneam.service.util.aplicarInsetsBarrasSistema

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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