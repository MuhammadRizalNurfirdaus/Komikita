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
    private var onDownloadClick: ((List<Chapter>) -> Unit)? = null
    private var isAllSelected = false
    private lateinit var adapter: DownloadGridAdapter

    companion object {
        fun newInstance(chapters: List<Chapter>): DownloadSelectionBottomSheet {
            val fragment = DownloadSelectionBottomSheet()
            fragment.chapters = chapters
            return fragment
        }
    }
    
    fun setOnDownloadClickListener(listener: (List<Chapter>) -> Unit) {
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

        adapter = DownloadGridAdapter { selectedChapters ->
            updateSelectedCount(selectedChapters.size)
        }

        binding.rvDownloadGrid.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvDownloadGrid.adapter = adapter
        adapter.submitList(chapters)

        // Select All / Deselect All toggle
        binding.btnSelectAll.setOnClickListener {
            if (isAllSelected) {
                adapter.deselectAll()
                binding.btnSelectAll.text = "Pilih Semua"
                isAllSelected = false
            } else {
                adapter.selectAll()
                binding.btnSelectAll.text = "Batalkan Semua"
                isAllSelected = true
            }
        }

        // Download selected chapters
        binding.btnDownloadSelected.setOnClickListener {
            val selected = adapter.getSelectedChapters().toList()
            if (selected.isNotEmpty()) {
                onDownloadClick?.invoke(selected)
                dismiss()
            } else {
                Toast.makeText(context, "Pilih minimal 1 chapter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSelectedCount(count: Int) {
        binding.tvSelectedCount.text = "$count chapter dipilih"
        binding.btnDownloadSelected.text = "Download ($count)"
        binding.btnDownloadSelected.isEnabled = count > 0
        
        // Update select all text based on selection
        isAllSelected = count == chapters.size
        binding.btnSelectAll.text = if (isAllSelected) "Batalkan Semua" else "Pilih Semua"
    }
}
