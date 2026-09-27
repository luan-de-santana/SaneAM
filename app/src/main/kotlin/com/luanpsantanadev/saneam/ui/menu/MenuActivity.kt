package com.luanpsantanadev.saneam.ui.menu

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        configurarAjusteTeclado()

        // Configura bottom navigation
        configurarNavegacao()
    }

    private fun configurarAjusteTeclado() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val tecladoVisivel = insets.isVisible(WindowInsetsCompat.Type.ime())
            val visibilidadeMenu = if (tecladoVisivel) View.GONE else View.VISIBLE
            val layoutParams =
                binding.navHostFragmentActivityMain.layoutParams as ConstraintLayout.LayoutParams
            val margemInferior = if (tecladoVisivel) ime.bottom else 0

            if (
                layoutParams.bottomMargin != margemInferior ||
                binding.bottomNavigation.visibility != visibilidadeMenu
            ) {
                layoutParams.bottomMargin = margemInferior
                binding.navHostFragmentActivityMain.layoutParams = layoutParams
                binding.bottomNavigation.visibility = visibilidadeMenu
            }
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun configurarNavegacao() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)
    }

}