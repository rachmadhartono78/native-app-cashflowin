package com.example.cashflowin.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.R
import com.example.cashflowin.api.model.TransactionItem
import com.example.cashflowin.databinding.ItemTransactionBinding
import com.example.cashflowin.databinding.ItemTransactionHeaderBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TransactionAdapter(
    private val onItemClick: ((TransactionItem) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Any> = listOf()

    private val numberFormat = DecimalFormat("#,###").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
            groupingSeparator = '.'
        }
    }
    
    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val headerDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    data class HeaderItem(val title: String)

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    fun submitList(newList: List<TransactionItem>) {
        val groupedItems = mutableListOf<Any>()
        val groupedByDate = newList.groupBy { it.date }
        
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val todayStr = dateOnlyFormat.format(today.time)
        val yesterdayStr = dateOnlyFormat.format(yesterday.time)

        groupedByDate.keys.sortedDescending().forEach { date ->
            val transactions = groupedByDate[date] ?: emptyList()
            
            val headerTitle = when (date) {
                todayStr -> "Hari Ini"
                yesterdayStr -> "Kemarin"
                else -> {
                    try {
                        val parsedDate = inputDateFormat.parse(date)
                        if (parsedDate != null) headerDateFormat.format(parsedDate) else date
                    } catch (e: Exception) {
                        date
                    }
                }
            }
            
            groupedItems.add(HeaderItem(headerTitle))
            groupedItems.addAll(transactions)
        }
        
        val diffCallback = TransactionDiffCallback(items, groupedItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        items = groupedItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is HeaderItem) TYPE_HEADER else TYPE_TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemTransactionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TransactionViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(items[position] as HeaderItem)
        } else if (holder is TransactionViewHolder) {
            holder.bind(items[position] as TransactionItem)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(private val binding: ItemTransactionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: HeaderItem) {
            binding.tvHeaderDate.text = header.title
        }
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: TransactionItem) {
            val context = binding.root.context
            
            binding.tvCategoryName.text = transaction.category?.name ?: "Lainnya"
            if (!transaction.description.isNullOrEmpty()) {
                binding.tvDescription.text = transaction.description
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }
            
            val timeStr = try {
                if (transaction.createdAt != null) {
                    val timeDate = isoFormat.parse(transaction.createdAt)
                    if (timeDate != null) outputTimeFormat.format(timeDate) else ""
                } else ""
            } catch (e: Exception) {
                ""
            }

            if (timeStr.isNotEmpty()) {
                binding.tvDate.text = timeStr
                binding.tvDate.visibility = View.VISIBLE
            } else {
                binding.tvDate.visibility = View.GONE
            }
            
            val amountValue = transaction.amount
            val formattedAmount = numberFormat.format(amountValue)

            val categoryColor = transaction.category?.color
            val categoryIcon = transaction.category?.icon
            
            if (!categoryColor.isNullOrEmpty()) {
                try {
                    val color = Color.parseColor(categoryColor)
                    binding.cardIcon.setCardBackgroundColor(color)
                    binding.cardIcon.backgroundTintList = ColorStateList.valueOf(color).withAlpha(40)
                    binding.ivCategoryIcon.imageTintList = ColorStateList.valueOf(color)
                } catch (e: Exception) {
                    setDefaultStyle(transaction)
                }
            } else {
                setDefaultStyle(transaction)
            }

            if (!categoryIcon.isNullOrEmpty()) {
                val resId = context.resources.getIdentifier(categoryIcon, "drawable", context.packageName)
                if (resId != 0) {
                    binding.ivCategoryIcon.setImageResource(resId)
                }
            }

            if (transaction.type == "income") {
                binding.tvAmount.text = "+ Rp $formattedAmount"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income))
            } else {
                binding.tvAmount.text = "- Rp $formattedAmount"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense))
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(transaction)
            }
        }

        private fun setDefaultStyle(transaction: TransactionItem) {
            val context = binding.root.context
            if (transaction.type == "income") {
                binding.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_green_light))
                binding.ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green))
            } else {
                binding.cardIcon.setCardBackgroundColor(Color.parseColor("#F1F5F9"))
                binding.ivCategoryIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#64748B"))
            }
        }
    }

    class TransactionDiffCallback(
        private val oldList: List<Any>,
        private val newList: List<Any>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            return if (oldItem is TransactionItem && newItem is TransactionItem) {
                oldItem.id == newItem.id
            } else if (oldItem is HeaderItem && newItem is HeaderItem) {
                oldItem.title == newItem.title
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
