package com.bongdev.aigenapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bongdev.aigenapp.data.CategoryWithExpenses
import com.bongdev.aigenapp.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onCategoryClick: (CategoryWithExpenses) -> Unit
) : ListAdapter<CategoryWithExpenses, CategoryAdapter.CategoryViewHolder>(DiffCallback()) {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(getItem(position))
                }
            }
        }
        
        fun bind(categoryWithExpenses: CategoryWithExpenses) {
            val category = categoryWithExpenses.category
            binding.categoryName.text = category.name
            binding.categoryIcon.setImageResource(category.iconResId)
            binding.categoryAmount.text = String.format("$%.2f", categoryWithExpenses.expenses.sumOf { it.amount })
            
            // Set progress
            val progress = if (category.budget > 0) {
                ((categoryWithExpenses.expenses.sumOf { it.amount } / category.budget) * 100).toInt()
                    .coerceIn(0, 100)
            } else 0
            binding.categoryProgress.progress = progress
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CategoryWithExpenses>() {
        override fun areItemsTheSame(oldItem: CategoryWithExpenses, newItem: CategoryWithExpenses): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: CategoryWithExpenses, newItem: CategoryWithExpenses): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
} 