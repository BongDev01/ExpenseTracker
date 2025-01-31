package com.bongdev.aigenapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bongdev.aigenapp.R
import com.bongdev.aigenapp.data.AppDatabase
import com.bongdev.aigenapp.data.Category
import com.bongdev.aigenapp.data.CategoryWithExpenses
import com.bongdev.aigenapp.data.Expense
import com.bongdev.aigenapp.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    
    val categoriesWithExpenses: StateFlow<List<CategoryWithExpenses>>
    val totalExpenses: StateFlow<Double>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao(), database.categoryDao())
        
        categoriesWithExpenses = repository.categoriesWithExpenses
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        totalExpenses = repository.totalExpenses
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

        viewModelScope.launch {
            repository.allCategories.collect { existingCategories ->
                if (existingCategories.isEmpty()) {
                    initializeDefaultCategories()
                }
            }
        }
    }

    private fun initializeDefaultCategories() {
        viewModelScope.launch {
            val defaultCategories = listOf(
                Category(name = "Food", iconResId = R.drawable.diet, colorResId = R.color.category_food, budget = 500.0),
                Category(name = "Transportation", iconResId = R.drawable.delivery, colorResId = R.color.category_transport, budget = 300.0),
                Category(name = "Entertainment", iconResId = R.drawable.online_gaming, colorResId = R.color.category_entertainment, budget = 200.0),
                Category(name = "Shopping", iconResId = R.drawable.shopping_cart, colorResId = R.color.category_shopping, budget = 400.0),
                Category(name = "Bills", iconResId = R.drawable.bill, colorResId = R.color.category_bills, budget = 1000.0),
                Category(name = "Health", iconResId = R.drawable.cardiogram, colorResId = R.color.category_health, budget = 300.0),
                Category(name = "Education", iconResId = R.drawable.learning, colorResId = R.color.category_education, budget = 500.0),
                Category(name = "Other", iconResId = R.drawable.menu, colorResId = R.color.category_other, budget = 200.0)
            )

            defaultCategories.forEach { category ->
                repository.insertCategory(category)
            }
        }
    }

    fun addExpense(amount: Double, categoryId: Long, note: String?, date: Date = Date()) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                categoryId = categoryId,
                note = note,
                date = date
            )
            repository.insertExpense(expense)
        }
    }

    fun addCategory(name: String, iconResId: Int, colorResId: Int, budget: Double) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                iconResId = iconResId,
                colorResId = colorResId,
                budget = budget
            )
            repository.insertCategory(category)
        }
    }
} 