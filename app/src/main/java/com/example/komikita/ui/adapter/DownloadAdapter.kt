package com.example.komikita.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.komikita.R
import com.example.komikita.data.local.entity.DownloadEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DownloadAdapter(
    private val onItemClick: (download: DownloadEntity, position: Int, totalCount: Int) -> Unit,
    private val onDeleteClick: (DownloadEntity) -> Unit
) : ListAdapter<DownloadEntity, DownloadAdapter.DownloadViewHolder>(DownloadDiffCallback()) {
    
    fun getItemAtPosition(position: Int): DownloadEntity? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DownloadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvKomikTitle: TextView = itemView.findViewById(R.id.tvKomikTitle)
        private val tvChapterTitle: TextView = itemView.findViewById(R.id.tvChapterTitle)
        private val tvDownloadDate: TextView = itemView.findViewById(R.id.tvDownloadDate)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(download: DownloadEntity) {
            tvKomikTitle.text = download.komikTitle
            tvChapterTitle.text = download.chapterTitle
            
            // Format date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = Date(download.downloadedAt)
            tvDownloadDate.text = "Downloaded: ${dateFormat.format(date)}"

            itemView.setOnClickListener {
                onItemClick(download, adapterPosition, itemCount)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(download)
            }
        }
    }

    class DownloadDiffCallback : DiffUtil.ItemCallback<DownloadEntity>() {
        override fun areItemsTheSame(oldItem: DownloadEntity, newItem: DownloadEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DownloadEntity, newItem: DownloadEntity): Boolean {
            return oldItem == newItem
        }
    }
}
