package com.example.mad_lab_exam_3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvTitle.text = transaction.title
        holder.tvCategory.text = transaction.category
        holder.tvDate.text = transaction.date
        holder.tvAmount.text = if (transaction.type == "income") {
            "+ Rs. ${transaction.amount}"
        } else {
            "- Rs. ${transaction.amount}"
        }

        val colorRes = if (transaction.type == "income") {
            R.color.incomeGreen
        } else {
            R.color.expenseRed
        }
        holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        holder.btnEdit.setOnClickListener {
            onEdit(transaction)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(transaction)
        }
    }

    override fun getItemCount() = transactions.size
}

