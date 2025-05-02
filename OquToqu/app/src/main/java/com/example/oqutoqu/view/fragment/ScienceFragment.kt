package com.example.oqutoqu.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.oqutoqu.databinding.FragmentScienceBinding
import com.example.oqutoqu.viewmodel.ScienceViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScienceViewModel by viewModel()
    private var foundPdfUrl: String? = null
    private var hasLoadedFromDeepLink = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!hasLoadedFromDeepLink) {
            val deepLinkDoi = arguments?.getString("doi")
            if (!deepLinkDoi.isNullOrBlank()) {
                binding.etDoi.setText(deepLinkDoi)
                viewModel.fetchPdf(deepLinkDoi)
                hasLoadedFromDeepLink = true
            }
        }

        binding.btnSearch.setOnClickListener {
            val doi = binding.etDoi.text.toString().trim()
            if (doi.isEmpty()) {
                binding.btnOpenPdf.visibility = View.GONE
                Toast.makeText(requireContext(), "Please enter DOI", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.fetchPdf(doi)
        }

        binding.btnOpenPdf.setOnClickListener {
            foundPdfUrl?.let { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        viewModel.sciHubResult.observe(viewLifecycleOwner) { result ->
            if (viewModel.lastPdfUrl != null) {
                foundPdfUrl = viewModel.lastPdfUrl
                binding.btnOpenPdf.visibility = View.VISIBLE
                binding.btnOpenPdf.text = "Open PDF"
            } else {
                foundPdfUrl = null
                binding.btnOpenPdf.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
