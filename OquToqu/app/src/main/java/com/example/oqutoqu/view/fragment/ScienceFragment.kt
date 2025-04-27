package com.example.oqutoqu.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
        viewModel.sciHubResult.observe(viewLifecycleOwner) { result ->
            if (result?.pdfLink != null) {
                val pdfurl = result.pdfLink
                var url = "https:$pdfurl"
                foundPdfUrl = url
                binding.btnOpenPdf.visibility = View.VISIBLE
                binding.btnOpenPdf.text = "📄 Open PDF"
                Toast.makeText(requireContext(), "PDF Found!", Toast.LENGTH_SHORT).show()
            } else {
                foundPdfUrl = null
                binding.btnOpenPdf.visibility = View.GONE
                Toast.makeText(requireContext(), "PDF not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
