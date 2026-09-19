package com.luandev.saneam.ui.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.luandev.saneam.R
import com.luandev.saneam.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura bottom navigation
        configurarNavegacao()
    }

    private fun configurarNavegacao() {
        val botNavigation = binding.bottomNavigation
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.nav_inicio,
                R.id.nav_inventario,
                R.id.nav_moviment,
                R.id.nav_config
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)
        botNavigation.setupWithNavController(navController)
    }

}