package com.example.komikita.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.komikita.data.model.Chapter
import com.example.komikita.databinding.FragmentChapterListBottomSheetBinding
import com.example.komikita.ui.adapter.ChapterAdapter
import com.example.komikita.ui.reader.ChapterReaderActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChapterListBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentChapterListBottomSheetBinding
    private var chapters: List<Chapter> = emptyList()
    private var komikId: String = ""
    private var komikTitle: String = ""

    companion object {
        fun newInstance(chapters: List<Chapter>, komikId: String, komikTitle: String): ChapterListBottomSheet {
            val fragment = ChapterListBottomSheet()
            fragment.chapters = chapters
            fragment.komikId = komikId
            fragment.komikTitle = komikTitle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChapterListBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ChapterAdapter { chapterId ->
            val intent = Intent(requireContext(), ChapterReaderActivity::class.java)
            intent.putExtra("CHAPTER_ID", chapterId)
            intent.putExtra("KOMIK_SLUG", komikId)
            intent.putExtra("KOMIK_TITLE", komikTitle)
            startActivity(intent)
            dismiss()
        }

        binding.rvAllChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllChapters.adapter = adapter
        adapter.submitList(chapters)
    }
}
