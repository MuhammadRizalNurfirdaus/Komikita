package com.example.komikita.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.komikita.data.model.Chapter
import com.example.komikita.databinding.FragmentDownloadSelectionBottomSheetBinding
import com.example.komikita.ui.adapter.DownloadGridAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DownloadSelectionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDownloadSelectionBottomSheetBinding
    private var chapters: List<Chapter> = emptyList()
    private var onDownloadClick: ((Chapter) -> Unit)? = null

    companion object {
        fun newInstance(chapters: List<Chapter>): DownloadSelectionBottomSheet {
            val fragment = DownloadSelectionBottomSheet()
            fragment.chapters = chapters
            return fragment
        }
    }
    
    fun setOnDownloadClickListener(listener: (Chapter) -> Unit) {
        onDownloadClick = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadSelectionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTotalChapters.text = "Total Chapter ${chapters.size}"
        binding.btnBack.setOnClickListener { dismiss() }

        val adapter = DownloadGridAdapter { chapter ->
            onDownloadClick?.invoke(chapter)
            dismiss()
        }

        binding.rvDownloadGrid.layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns like in image
        binding.rvDownloadGrid.adapter = adapter
        adapter.submitList(chapters)
    }
}
