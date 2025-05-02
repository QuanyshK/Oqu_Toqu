package com.example.oqutoqu.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.oqutoqu.R
import com.example.oqutoqu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost

        val controller = navController.navController
        binding.bottomNavigation.setupWithNavController(controller)

        controller.addOnDestinationChangedListener { _, destination, _ ->
            println("🧭 Current destination: ${destination.label}")
            binding.bottomNavigation.visibility =
                if (destination.id == R.id.loginFragment) View.GONE else View.VISIBLE
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
        setIntent(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        val controller = navController.navController

        if (data?.scheme == "oqutoqu" && data.host == "scihub") {
            val doi = data.getQueryParameter("doi") ?: return

            val bundle = Bundle().apply {
                putString("doi", doi)
            }

            controller.navigate(
                R.id.scienceFragment,
                bundle,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .setLaunchSingleTop(true)
                    .build()
            )
        }
    }

}
