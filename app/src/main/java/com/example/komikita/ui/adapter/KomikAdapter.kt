package com.example.komikita.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.komikita.R
import com.example.komikita.databinding.ItemKomikBinding
import com.example.komikita.data.model.SearchItem
import java.util.Locale

class KomikAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<SearchItem, KomikAdapter.KomikViewHolder>(KomikDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KomikViewHolder {
        val binding = ItemKomikBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KomikViewHolder(binding, onClick)
    }
    
    override fun onBindViewHolder(holder: KomikViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class KomikViewHolder(
        private val binding: ItemKomikBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SearchItem) {
            binding.tvTitle.text = item.title
            
            // Handle different field names in Search API vs List API
            binding.tvType.text = item.type ?: item.status ?: "Unknown"
            binding.tvChapter.text = item.chapter ?: item.episode ?: "No chapters"
            binding.tvDate.text = item.date ?: ""
            
            // Show score if available (score or rating)
            val scoreText = item.score ?: item.rating
            if (!scoreText.isNullOrEmpty()) {
                binding.tvScore.visibility = View.VISIBLE
                binding.tvScore.text = "★ $scoreText"
            } else {
                binding.tvScore.visibility = View.GONE
            }
            
            // Load poster image
            Glide.with(binding.ivPoster.context)
                .load(item.poster)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.ivPoster)
            
            binding.root.setOnClickListener {
                // Fix for empty slug in API search results
                val slug = if (item.slug.isNotEmpty()) {
                    item.slug
                } else {
                    item.title.lowercase(Locale.ROOT)
                        .replace(" ", "-")
                        .replace(Regex("[^a-z0-9-]"), "")
                }
                onClick(slug)
            }
        }
    }
    
    class KomikDiffCallback : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem.slug == newItem.slug
        }
        
        override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem == newItem
        }
    }
}
