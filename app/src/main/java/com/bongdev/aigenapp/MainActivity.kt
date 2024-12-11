package com.bongdev.aigenapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.bongdev.aigenapp.ui.CategoryAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import com.bongdev.aigenapp.data.Category
import com.bongdev.aigenapp.data.CategoryWithExpenses
import com.bongdev.aigenapp.data.Expense
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bongdev.aigenapp.ui.MainViewModel
import kotlinx.coroutines.launch
import android.view.View
import com.bongdev.aigenapp.ui.ExpenseAdapter
import android.animation.ValueAnimator
import android.animation.ObjectAnimator
import android.view.animation.OvershootInterpolator
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.bongdev.aigenapp.databinding.ActivityMainBinding
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var expenseAdapter: ExpenseAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top, bottom = insets.bottom)
            windowInsets
        }

        setupUI()
        observeData()
        startEnterAnimation()
    }

    private fun setupUI() {
        // Set up Categories RecyclerView
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter { categoryWithExpenses ->
            // Launch CategoryDetailActivity instead of showing expenses
            Intent(this, CategoryDetailActivity::class.java).apply {
                putExtra(CategoryDetailActivity.EXTRA_CATEGORY_ID, categoryWithExpenses.category.id)
                startActivity(this)
            }
        }
        binding.categoriesRecyclerView.adapter = categoryAdapter

        // Set up FAB
        binding.addExpenseFab.setOnClickListener {
            showAddExpenseDialog()
        }

        // Initialize with 0
        updateTotalAmount(0.0)
        binding.monthlyProgressIndicator.progress = 0
    }

    private fun observeData() {
        // Single coroutine for both observations to prevent multiple updates
        lifecycleScope.launch {
            viewModel.categoriesWithExpenses.collect { categories ->
                // Use toSet() to ensure unique categories
                val uniqueCategories = categories.distinctBy { it.category.id }
                categoryAdapter.submitList(uniqueCategories)
                updateTotalAmount(viewModel.totalExpenses.value)
                updateMonthlyProgress()
            }
        }

        lifecycleScope.launch {
            viewModel.totalExpenses.collect { total ->
                updateTotalAmount(total)
                updateMonthlyProgress()
            }
        }
    }

    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val amountInput = dialogView.findViewById<TextInputEditText>(R.id.amountInput)
        val noteInput = dialogView.findViewById<TextInputEditText>(R.id.noteInput)
        val categorySpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.categorySpinner)

        // Get unique categories
        val uniqueCategories = viewModel.categoriesWithExpenses.value
            .distinctBy { it.category.id }
            .sortedBy { it.category.name }  // Sort categories by name

        val categoryNames = uniqueCategories.map { it.category.name }
        val arrayAdapter = ArrayAdapter(this, R.layout.item_dropdown, categoryNames)
        categorySpinner.setAdapter(arrayAdapter)

        if (categoryNames.isNotEmpty()) {
            categorySpinner.setText(categoryNames[0], false)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_expense)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
                val note = noteInput.text.toString()
                val categoryName = categorySpinner.text.toString()

                if (amount > 0 && categoryName.isNotEmpty()) {
                    // Find category from unique categories list
                    val category = uniqueCategories.find { it.category.name == categoryName }?.category
                    if (category != null) {
                        viewModel.addExpense(amount, category.id, note)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun startEnterAnimation() {
        val duration = 400L
        
        // Animate categories
        binding.categoriesRecyclerView.alpha = 0f
        binding.categoriesRecyclerView.translationY = 50f
        binding.categoriesRecyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        // Animate total amount
        binding.totalAmountCard.alpha = 0f
        binding.totalAmountCard.translationY = -50f
        binding.totalAmountCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(200)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        // Animate FAB

        binding.addExpenseFab.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setStartDelay(400)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun updateTotalAmount(amount: Double) {
        // Animate text change
        val animator = ValueAnimator.ofFloat(
            binding.totalAmountText.text.toString().filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f,
            amount.toFloat()
        )
        animator.duration = 500
        animator.addUpdateListener { animation ->
            binding.totalAmountText.text = getString(R.string.amount_format, animation.animatedValue as Float)
        }
        animator.start()
    }

    private fun updateMonthlyProgress() {
        val uniqueCategories = viewModel.categoriesWithExpenses.value.distinctBy { it.category.id }
        val totalBudget = uniqueCategories.sumOf { it.category.budget }
        val totalSpent = viewModel.totalExpenses.value
        val progress = if (totalBudget > 0) {
            ((totalSpent / totalBudget) * 100).toInt().coerceIn(0, 100)
        } else 0

        // Animate progress
        ObjectAnimator.ofInt(binding.monthlyProgressIndicator, "progress", progress).apply {
            duration = 500
            interpolator = FastOutSlowInInterpolator()
            start()
        }

        // Update progress color based on percentage
        binding.monthlyProgressIndicator.setIndicatorColor(
            when {
                progress < 70 -> getColor(R.color.progress_good)
                progress < 90 -> getColor(R.color.progress_warning)
                else -> getColor(R.color.expense_red)
            }
        )
    }
}