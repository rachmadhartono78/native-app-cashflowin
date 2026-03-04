package com.example.cashflowin.ui.asset

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.AssetInfo
import com.example.cashflowin.databinding.ItemAssetBinding
import java.text.NumberFormat
import java.util.Locale

class AssetAdapter(
    private val onItemClick: (AssetInfo) -> Unit
) : ListAdapter<AssetInfo, AssetAdapter.AssetViewHolder>(AssetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AssetViewHolder(
        private val binding: ItemAssetBinding,
        private val onItemClick: (AssetInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(assetInfo: AssetInfo) {
            binding.tvAssetName.text = assetInfo.name
            binding.tvAssetType.text = assetInfo.type ?: "Asset"
            
            val balance = assetInfo.balance
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            try {
                binding.tvAssetAmount.text = format.format(balance)
            } catch (e: Exception) {
                binding.tvAssetAmount.text = "Rp $balance"
            }

            binding.root.setOnClickListener {
                onItemClick(assetInfo)
            }
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
