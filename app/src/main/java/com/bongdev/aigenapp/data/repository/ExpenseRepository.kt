package com.bongdev.aigenapp.data.repository

import com.bongdev.aigenapp.data.Category
import com.bongdev.aigenapp.data.CategoryWithExpenses
import com.bongdev.aigenapp.data.Expense
import com.bongdev.aigenapp.data.dao.CategoryDao
import com.bongdev.aigenapp.data.dao.ExpenseDao
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val categoriesWithExpenses: Flow<List<CategoryWithExpenses>> = categoryDao.getCategoriesWithExpenses()
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenses()

    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
} 