package com.bongdev.aigenapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bongdev.aigenapp.data.Expense
import com.bongdev.aigenapp.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(expense: Expense, dateFormat: SimpleDateFormat) {
            binding.expenseAmount.text = String.format("$%.2f", expense.amount)
            binding.expenseNote.text = expense.note ?: ""
            binding.expenseDate.text = dateFormat.format(expense.date)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder(
            ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position), dateFormat)
    }
} 