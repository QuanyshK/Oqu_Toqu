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
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScienceViewModel by viewModel()
    private var foundPdfUrl: String? = null
    private var hasLoadedFromDeepLink = false
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startPdfDownload()
            } else {
                Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!hasLoadedFromDeepLink) {
            val deepLinkDoi = requireActivity().intent?.data?.getQueryParameter("doi")
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
        binding.btnDownloadPdf.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    startPdfDownload()
                }
            } else {
                startPdfDownload()
            }
        }

        viewModel.sciHubResult.observe(viewLifecycleOwner) { result ->
            if (viewModel.lastPdfUrl != null) {
                foundPdfUrl = viewModel.lastPdfUrl
                binding.btnOpenPdf.visibility = View.VISIBLE
                binding.btnDownloadPdf.visibility = View.VISIBLE
                binding.btnOpenPdf.text = "Open PDF"
            } else {
                foundPdfUrl = null
                binding.btnOpenPdf.visibility = View.GONE
                    foundPdfUrl = null
                    binding.btnOpenPdf.visibility = View.GONE
                    Toast.makeText(requireContext(), "PDF not found. Redirecting to support...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("oqutoqu://support"))
                    startActivity(intent)

            }
        }
    }
    private fun startPdfDownload() {
        foundPdfUrl?.let { url ->
            val intent = Intent(requireContext(), PdfDownloadService::class.java).apply {
                putExtra("pdf_url", url)
            }
            requireContext().startService(intent)
        } ?: Toast.makeText(requireContext(), "No PDF URL available", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
