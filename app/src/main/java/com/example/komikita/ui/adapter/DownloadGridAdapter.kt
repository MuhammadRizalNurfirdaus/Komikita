package com.example.komikita.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.komikita.data.model.Chapter
import com.example.komikita.databinding.ItemDownloadGridBinding

class DownloadGridAdapter(
    private val onSelectionChanged: (Set<Chapter>) -> Unit
) : RecyclerView.Adapter<DownloadGridAdapter.ViewHolder>() {

    private var chapters: List<Chapter> = emptyList()
    private val selectedChapters = mutableSetOf<Chapter>()

    fun submitList(newChapters: List<Chapter>) {
        chapters = newChapters
        selectedChapters.clear()
        notifyDataSetChanged()
    }

    fun getSelectedChapters(): Set<Chapter> = selectedChapters.toSet()

    fun selectAll() {
        selectedChapters.clear()
        selectedChapters.addAll(chapters)
        notifyDataSetChanged()
        onSelectionChanged(selectedChapters)
    }

    fun deselectAll() {
        selectedChapters.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedChapters)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chapters[position])
    }

    override fun getItemCount() = chapters.size

    inner class ViewHolder(private val binding: ItemDownloadGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chapter: Chapter) {
            // Extract number from title if possible, or show short text
            val text = chapter.chapter?.replace("Chapter ", "") ?: "?"
            binding.tvChapterNumber.text = text
            
            val isSelected = selectedChapters.contains(chapter)
            
            // Update visual state
            if (isSelected) {
                binding.cardChapter.setCardBackgroundColor(0xFF2E7D32.toInt()) // Green when selected
                binding.ivCheckmark.visibility = View.VISIBLE
            } else {
                binding.cardChapter.setCardBackgroundColor(0xFF333333.toInt()) // Default gray
                binding.ivCheckmark.visibility = View.GONE
            }
            
            binding.root.setOnClickListener {
                if (selectedChapters.contains(chapter)) {
                    selectedChapters.remove(chapter)
                } else {
                    selectedChapters.add(chapter)
                }
                notifyItemChanged(adapterPosition)
                onSelectionChanged(selectedChapters)
            }
        }
    }
}
