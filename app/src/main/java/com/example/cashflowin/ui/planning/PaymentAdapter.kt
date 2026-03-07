package com.example.cashflowin.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.DebtPayment
import com.example.cashflowin.databinding.ItemPaymentBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PaymentAdapter(private var payments: List<DebtPayment>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    fun updateData(newPayments: List<DebtPayment>) {
        payments = newPayments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(payments[position])
    }

    override fun getItemCount(): Int = payments.size

    class PaymentViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: DebtPayment) {
            // Formatter Angka (Tanpa desimal)
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }
            
            // Formatter Tanggal
            val displayDate = formatApiDate(payment.payment_date)
            
            binding.tvPaymentDate.text = displayDate
            binding.tvPaymentNotes.text = payment.notes ?: "Pembayaran Cicilan"
            binding.tvPaymentAsset.text = payment.asset?.name ?: "Kas/Bank"
            binding.tvPaymentAmount.text = currencyFormat.format(payment.amount)
        }

        private fun formatApiDate(dateStr: String?): String {
            if (dateStr.isNullOrEmpty()) return "-"
            
            // Format input dari API (Sesuaikan jika backend menggunakan format lain)
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            )
            
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            
            for (format in inputFormats) {
                try {
                    // Set timezone ke UTC jika ada 'Z'
                    if (format.toPattern().contains("Z")) {
                        format.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val date = format.parse(dateStr)
                    if (date != null) {
                        return outputFormat.format(date)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            return dateStr // Kembalikan string asli jika gagal parse
        }
    }
}
