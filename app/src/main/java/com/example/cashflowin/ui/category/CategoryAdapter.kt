package com.example.cashflowin.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.CategoryInfo
import com.example.cashflowin.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onItemClick: (CategoryInfo) -> Unit
) : ListAdapter<CategoryInfo, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onItemClick: (CategoryInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryInfo: CategoryInfo) {
            binding.tvCategoryName.text = categoryInfo.name
            binding.tvCategoryType.text = categoryInfo.type

            // Distinct visual colors depending on Type
            if (categoryInfo.type.equals("Pemasukan", ignoreCase = true) || categoryInfo.type.equals("Income", ignoreCase = true)) {
                binding.cardCategoryType.setCardBackgroundColor(Color.parseColor("#D1FAE5")) // Green bg
                binding.tvCategoryType.setTextColor(Color.parseColor("#059669")) // Green text
            } else {
                binding.cardCategoryType.setCardBackgroundColor(Color.parseColor("#FEE2E2")) // Red bg
                binding.tvCategoryType.setTextColor(Color.parseColor("#DC2626")) // Red text
            }

            binding.root.setOnClickListener {
                onItemClick(categoryInfo)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryInfo>() {
        override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
            return oldItem == newItem
        }
    }
}