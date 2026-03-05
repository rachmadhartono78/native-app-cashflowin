package com.example.cashflowin.ui.planning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashflowin.api.model.DebtPayment
import com.example.cashflowin.databinding.ItemPaymentBinding
import java.text.NumberFormat
import java.util.Locale

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
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            
            binding.tvPaymentDate.text = payment.payment_date
            binding.tvPaymentNotes.text = payment.notes ?: "Pembayaran Cicilan"
            binding.tvPaymentAsset.text = payment.asset?.name ?: "Unknown Asset"
            binding.tvPaymentAmount.text = format.format(payment.amount)
        }
    }
}
