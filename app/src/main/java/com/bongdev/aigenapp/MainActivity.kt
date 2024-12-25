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
import android.widget.Toast
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: MainViewModel by viewModels()
    private var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
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
            Intent(this, CategoryDetailActivity::class.java).apply {
                putExtra(CategoryDetailActivity.EXTRA_CATEGORY_ID, categoryWithExpenses.category.id)
                startActivity(this)
            }
        }
        binding.categoriesRecyclerView.adapter = categoryAdapter

        // Set up FAB menu
        binding.mainFab.setOnClickListener {
            toggleFabMenu()
        }

        binding.addExpenseFab.setOnClickListener {
            closeFabMenu()
            showAddExpenseDialog()
        }

        binding.addCategoryFab.setOnClickListener {
            closeFabMenu()
            showAddCategoryDialog()
        }

        // Initialize with 0
        updateTotalAmount(0.0)
        binding.monthlyProgressIndicator.progress = 0
    }

    private fun toggleFabMenu() {
        if (isFabMenuOpen) {
            closeFabMenu()
        } else {
            openFabMenu()
        }
    }

    private fun openFabMenu() {
        isFabMenuOpen = true
        binding.fabMenuContainer.visibility = View.VISIBLE
        binding.mainFab.animate().rotation(45f).setDuration(300).start()

        // Reset initial positions (move them below the main FAB)
        binding.addCategoryContainer.alpha = 0f
        binding.addCategoryContainer.translationY = 100f
        binding.addExpenseContainer.alpha = 0f
        binding.addExpenseContainer.translationY = 100f

        // Animate Add Expense first (it should be closer to the main FAB)
        binding.addExpenseContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .start()

        // Animate Add Category second (it should appear above the Add Expense)
        binding.addCategoryContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay(50)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun closeFabMenu() {
        isFabMenuOpen = false
        binding.mainFab.animate().rotation(0f).setDuration(300).start()

        // Animate Add Category first
        binding.addCategoryContainer.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(300)
            .start()

        // Animate Add Expense second
        binding.addExpenseContainer.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(300)
            .withEndAction {
                binding.fabMenuContainer.visibility = View.GONE
            }
            .start()
    }

    override fun onBackPressed() {
        if (isFabMenuOpen) {
            closeFabMenu()
        } else {
            super.onBackPressed()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.categoriesWithExpenses.collect { categories ->
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
        binding.addExpenseFab.scaleX = 0f
        binding.addExpenseFab.scaleY = 0f
        binding.addExpenseFab.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setStartDelay(400)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun updateTotalAmount(amount: Double) {
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

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.categoryNameInput)
        val budgetInput = dialogView.findViewById<TextInputEditText>(R.id.budgetInput)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_category)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameInput.text.toString().trim()
                val budget = budgetInput.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty() && budget > 0) {
                    viewModel.addCategory(
                        name = name,
                        iconResId = R.drawable.ic_custom_category,
                        colorResId = R.color.primary_container,
                        budget = budget
                    )
                } else {
                    Toast.makeText(this, R.string.invalid_category_input, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}