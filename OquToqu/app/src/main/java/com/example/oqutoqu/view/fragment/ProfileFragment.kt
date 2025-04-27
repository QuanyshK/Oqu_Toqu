package com.example.oqutoqu.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.oqutoqu.R
import com.example.oqutoqu.databinding.FragmentProfileBinding
import com.example.oqutoqu.viewmodel.AuthViewModel
import com.example.oqutoqu.viewmodel.ProfileViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentProfileBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profileViewModel.loadUser()
        profileViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.tvEmail.text = email ?: "No email"
        }

        binding.btnSupport.setOnClickListener {
            val number = getString(R.string.support_number)
            val url = "https://wa.me/$number"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}