package com.example.komikita.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.komikita.data.model.Chapter
import com.example.komikita.databinding.ItemDownloadGridBinding

class DownloadGridAdapter(
    private val onItemClick: (Chapter) -> Unit
) : RecyclerView.Adapter<DownloadGridAdapter.ViewHolder>() {

    private var chapters: List<Chapter> = emptyList()

    fun submitList(newChapters: List<Chapter>) {
        chapters = newChapters
        notifyDataSetChanged()
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
            
            binding.root.setOnClickListener {
                onItemClick(chapter)
            }
        }
    }
}
