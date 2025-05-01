package com.example.oqutoqu.view.fragment

import android.os.Bundle
import android.view.*
import com.example.oqutoqu.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FaqBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_faq_bottom_sheet, container, false)
    }
}
