package com.bongdev.aigenapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bongdev.aigenapp.R
import com.bongdev.aigenapp.data.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyExpense(
    val date: Date,
    val totalAmount: Double,
    val expenses: List<Expense>
)

class DailyExpenseAdapter(private val onAddExpenseClick: (Date) -> Unit) : 
    ListAdapter<DailyExpense, DailyExpenseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_expense, parent, false)
        return ViewHolder(view, onAddExpenseClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dailyExpense = getItem(position)
        holder.bind(dailyExpense)
    }

    class ViewHolder(itemView: View, private val onAddExpenseClick: (Date) -> Unit) : 
        RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val totalAmountText: TextView = itemView.findViewById(R.id.totalAmountText)
        private val expensesRecyclerView: RecyclerView = itemView.findViewById(R.id.expensesRecyclerView)

        fun bind(dailyExpense: DailyExpense) {
            val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            dateText.text = dateFormat.format(dailyExpense.date)
            totalAmountText.text = itemView.context.getString(
                R.string.amount_format, 
                dailyExpense.totalAmount.toFloat()
            )

            expensesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            val adapter = ExpenseAdapter()
            expensesRecyclerView.adapter = adapter
            adapter.submitList(dailyExpense.expenses)

            // Add click listener for adding expense
            itemView.findViewById<View>(R.id.addExpenseButton).setOnClickListener {
                onAddExpenseClick(dailyExpense.date)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DailyExpense>() {
        override fun areItemsTheSame(oldItem: DailyExpense, newItem: DailyExpense): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyExpense, newItem: DailyExpense): Boolean {
            return oldItem == newItem
        }
    }
} 