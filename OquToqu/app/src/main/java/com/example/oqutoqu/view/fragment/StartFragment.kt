package com.example.oqutoqu.view.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.data.manager.AuthManager
import com.example.oqutoqu.R

class StartFragment : Fragment() {

    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        authManager = AuthManager(requireContext())
        return View(requireContext())
    }

    override fun onResume() {
        super.onResume()
        val isLoggedIn = !authManager.getToken().isNullOrBlank()

        val destination = if (isLoggedIn) {
            R.id.action_startFragment_to_profileFragment
        } else {
            R.id.action_startFragment_to_loginFragment
        }

        findNavController().navigate(destination)
    }
}
