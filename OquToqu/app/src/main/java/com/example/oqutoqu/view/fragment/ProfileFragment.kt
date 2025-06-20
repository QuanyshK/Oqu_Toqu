package com.example.oqutoqu.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.oqutoqu.R
import com.example.oqutoqu.databinding.FragmentProfileBinding
import com.example.oqutoqu.viewmodel.ProfileViewModel
import com.example.data.manager.AuthManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.NavOptions

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModel()
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentProfileBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        authManager = AuthManager(requireContext())
        val isLoggedIn = !authManager.getToken().isNullOrBlank()

        if (!isLoggedIn) {
            findNavController().navigate(
                R.id.action_profileFragment_to_loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
            return
        }

        profileViewModel.loadUser()
        profileViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.tvEmail.text = email ?: "No email"
        }

        val intent = requireActivity().intent
        val deepLinkData = intent?.data
        if (deepLinkData?.host == "support") {
            Toast.makeText(
                requireContext(),
                "Having trouble accessing articles? Please contact our support team via WhatsApp.",
                Toast.LENGTH_LONG
            ).show()

            requireActivity().intent = Intent(intent).apply { data = null }
        }

        binding.btnSupport.setOnClickListener {
            val number = getString(R.string.support_number)
            val url = "https://wa.me/$number"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.logout()
            authManager.clearToken()
            val logoutIntent = Intent(Intent.ACTION_VIEW, Uri.parse("oqutoqu://login"))
            startActivity(logoutIntent)
            requireActivity().finish()
        }

        binding.btnFaq.setOnClickListener {
            findNavController().navigate(R.id.faqBottomSheetFragment)
            requireActivity().intent = Intent(intent).apply { data = null }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
