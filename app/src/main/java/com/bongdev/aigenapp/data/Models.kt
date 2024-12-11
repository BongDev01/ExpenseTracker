package com.bongdev.aigenapp.data

import androidx.room.*
import java.util.Date

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val date: Date = Date(),
    val note: String? = null
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconResId: Int,
    val colorResId: Int,
    val budget: Double = 0.0
)

data class CategoryWithExpenses(
    @Embedded
    val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId",
        entity = Expense::class
    )
    val expenses: List<Expense>
) {
    val totalAmount: Double
        get() = expenses.sumOf { it.amount }
} 