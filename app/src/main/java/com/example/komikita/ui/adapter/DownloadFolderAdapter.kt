package com.example.komikita.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.komikita.databinding.ItemDownloadFolderBinding

data class DownloadFolder(
    val komikSlug: String,
    val komikTitle: String,
    val chapterCount: Int
)

class DownloadFolderAdapter(
    private val onFolderClick: (DownloadFolder) -> Unit
) : RecyclerView.Adapter<DownloadFolderAdapter.ViewHolder>() {

    private var folders: List<DownloadFolder> = emptyList()

    fun submitList(newFolders: List<DownloadFolder>) {
        folders = newFolders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadFolderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount() = folders.size

    inner class ViewHolder(
        private val binding: ItemDownloadFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: DownloadFolder) {
            binding.tvFolderName.text = folder.komikTitle
            binding.tvChapterCount.text = "${folder.chapterCount} chapter"
            
            binding.root.setOnClickListener {
                onFolderClick(folder)
            }
        }
    }
}
