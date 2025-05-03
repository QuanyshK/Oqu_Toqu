package com.example.oqutoqu.view.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.oqutoqu.databinding.FragmentScienceBinding
import com.example.oqutoqu.view.service.PdfDownloadService
import com.example.oqutoqu.viewmodel.ScienceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScienceViewModel by viewModel()
    private var hasLoadedFromDeepLink = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startPdfDownload()
        else Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleDeepLinkIfNeeded()

        binding.btnSearch.setOnClickListener {
            val doi = binding.etDoi.text.toString().trim()
            if (doi.isEmpty()) {
                hidePdfButtons()
                viewModel.clearResult()
                Toast.makeText(requireContext(), "Please enter DOI", Toast.LENGTH_SHORT).show()
            } else {
                hasUserSearchedManually = true
                viewModel.fetchPdf(doi)
            }
        }

        binding.btnOpenPdf.setOnClickListener {
            viewModel.lastPdfUrl?.let { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        binding.btnDownloadPdf.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = android.Manifest.permission.POST_NOTIFICATIONS
                if (requireContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(permission)
                } else {
                    startPdfDownload()
                }
            } else {
                startPdfDownload()
            }
        }

        collectSciHubResult()
    }

    private var hasUserSearchedManually = false

    private fun collectSciHubResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sciHubResult.collectLatest { result ->
                if (result?.pdfLink != null && viewModel.lastPdfUrl != null) {
                    showPdfButtons()
                } else {
                    hidePdfButtons()
                    if (hasUserSearchedManually) {
                        Toast.makeText(requireContext(), "PDF not found. Redirecting to support...", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("oqutoqu://support")))
                    }
                }
            }
        }
    }


    private fun handleDeepLinkIfNeeded() {
        if (hasLoadedFromDeepLink) return
        val deepLinkDoi = requireActivity().intent?.data?.getQueryParameter("doi")
        if (!deepLinkDoi.isNullOrBlank()) {
            binding.etDoi.setText(deepLinkDoi)
            viewModel.fetchPdf(deepLinkDoi)
            hasLoadedFromDeepLink = true
        }
    }

    private fun startPdfDownload() {
        val url = viewModel.lastPdfUrl
        if (url != null) {
            val intent = Intent(requireContext(), PdfDownloadService::class.java).apply {
                putExtra("pdf_url", url)
            }
            requireContext().startService(intent)
        } else {
            Toast.makeText(requireContext(), "No PDF URL available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPdfButtons() {
        binding.btnOpenPdf.visibility = View.VISIBLE
        binding.btnDownloadPdf.visibility = View.VISIBLE
        binding.btnOpenPdf.text = "Open PDF"
    }

    private fun hidePdfButtons() {
        binding.btnOpenPdf.visibility = View.GONE
        binding.btnDownloadPdf.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
