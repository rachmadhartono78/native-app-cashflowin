package com.example.cashflowin.ui.asset

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.R
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.databinding.ItemAssetBinding
import java.text.NumberFormat
import java.util.Locale

class AssetAdapter(private val onAssetClick: (AssetInfo) -> Unit) :
    ListAdapter<AssetInfo, AssetAdapter.AssetViewHolder>(AssetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AssetViewHolder(private val binding: ItemAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(asset: AssetInfo) {
            val context = binding.root.context
            binding.tvAssetName.text = asset.name
            binding.tvAssetType.text = asset.type

            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
                maximumFractionDigits = 0
            }
            binding.tvAssetAmount.text = format.format(asset.balance)

            // Visual Polish: Icon and Color
            val colorStr = asset.color?.takeIf { it.isNotEmpty() && it != "null" } ?: when (asset.type) {
                "Bank" -> "#6366f1" // Indigo
                "Cash" -> "#10b981" // Emerald
                "E-Wallet" -> "#a855f7" // Purple
                "Investasi", "Investment" -> "#f59e0b" // Amber
                else -> "#64748b" // Slate
            }
            
            val iconName = asset.icon?.takeIf { it.isNotEmpty() && it != "null" } ?: when (asset.type) {
                "Bank" -> "ic_menu_myplaces"
                "Cash" -> "ic_menu_gallery"
                "E-Wallet" -> "ic_menu_send"
                "Investasi", "Investment" -> "ic_menu_sort_by_size"
                else -> "ic_menu_manage"
            }

            try {
                val color = Color.parseColor(colorStr)
                binding.iconContainer.setCardBackgroundColor(color)
                binding.iconContainer.backgroundTintList = ColorStateList.valueOf(color).withAlpha(40)
                binding.ivAssetIcon.imageTintList = ColorStateList.valueOf(color)
                
                val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                val androidResId = context.resources.getIdentifier(iconName, "drawable", "android")
                
                if (resId != 0) {
                    binding.ivAssetIcon.setImageResource(resId)
                } else if (androidResId != 0) {
                    binding.ivAssetIcon.setImageResource(androidResId)
                }
            } catch (e: Exception) {
                binding.iconContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_green_light))
                binding.ivAssetIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green))
            }

            binding.root.setOnClickListener { onAssetClick(asset) }
        }
    }

    class AssetDiffCallback : DiffUtil.ItemCallback<AssetInfo>() {
        override fun areItemsTheSame(oldItem: AssetInfo, newItem: AssetInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AssetInfo, newItem: AssetInfo): Boolean {
            return oldItem == newItem
        }
    }
}
