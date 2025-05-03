package com.example.oqutoqu.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.data.manager.AuthManager
import com.example.oqutoqu.R
import com.example.oqutoqu.databinding.FragmentLoginBinding
import com.example.oqutoqu.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModel()
    private val authManager by lazy { AuthManager(requireContext()) }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val acct = task.getResult(ApiException::class.java)
            acct?.idToken?.let { token ->
                authViewModel.loginWithGoogle(token) { success ->
                    if (success) {
                        findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                    } else {
                        Toast.makeText(requireContext(), "Auth failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google SignIn failed, code=${e.statusCode}", e)
            Toast.makeText(requireContext(), "Google SignIn Error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentLoginBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnGoogleSignIn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(requireContext(), gso)
            client.signOut().addOnCompleteListener { launcher.launch(client.signInIntent) }
            val data = requireActivity().intent?.data
            if (data?.host == "login") {
                Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()
                requireActivity().intent = Intent()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}