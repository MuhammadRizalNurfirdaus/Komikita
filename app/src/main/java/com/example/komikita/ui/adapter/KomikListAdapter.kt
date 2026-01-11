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
import com.example.komikita.data.model.ListItem
import java.util.Locale

class KomikListAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<ListItem, KomikListAdapter.KomikViewHolder>(KomikListDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KomikViewHolder {
        val binding = ItemKomikBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KomikViewHolder(binding, onClick)
    }
    
    override fun onBindViewHolder(holder: KomikViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class KomikViewHolder(
        private val binding: ItemKomikBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ListItem) {
            binding.tvTitle.text = item.title
            binding.tvType.text = item.type ?: "Unknown"
            binding.tvChapter.text = item.chapter ?: "No chapters"
            binding.tvDate.text = item.date ?: ""
            binding.tvScore.visibility = View.GONE
            
            Glide.with(binding.ivPoster.context)
                .load(item.poster)
                .placeholder(R.drawable.logokomik)
                .error(R.drawable.logokomik)
                .into(binding.ivPoster)
            
            binding.root.setOnClickListener {
                // Fix for empty slug in API results
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
    
    class KomikListDiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem) =
            oldItem.slug == newItem.slug
        
        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem) =
            oldItem == newItem
    }
}
