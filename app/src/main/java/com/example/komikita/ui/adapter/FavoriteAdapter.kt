package com.example.komikita.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.komikita.R
import com.example.komikita.data.local.entity.FavoriteEntity

class FavoriteAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (FavoriteEntity) -> Unit
) : ListAdapter<FavoriteEntity, FavoriteAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(favorite: FavoriteEntity) {
            tvTitle.text = favorite.title
            tvType.text = favorite.type ?: "Komik"

            Glide.with(itemView.context)
                .load(favorite.poster)
                .placeholder(R.drawable.logokomik)
                .error(R.drawable.logokomik)
                .into(ivPoster)

            itemView.setOnClickListener {
                onItemClick(favorite.slug)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(favorite)
            }
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteEntity>() {
        override fun areItemsTheSame(oldItem: FavoriteEntity, newItem: FavoriteEntity): Boolean {
            return oldItem.slug == newItem.slug
        }

        override fun areContentsTheSame(oldItem: FavoriteEntity, newItem: FavoriteEntity): Boolean {
            return oldItem == newItem
        }
    }
}
