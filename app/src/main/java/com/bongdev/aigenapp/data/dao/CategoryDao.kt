package com.bongdev.aigenapp.data.dao

import androidx.room.*
import com.bongdev.aigenapp.data.Category
import com.bongdev.aigenapp.data.CategoryWithExpenses
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithExpenses(): Flow<List<CategoryWithExpenses>>
} 