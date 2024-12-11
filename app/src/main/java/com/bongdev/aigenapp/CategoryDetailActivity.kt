package com.bongdev.aigenapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bongdev.aigenapp.data.CategoryWithExpenses
import com.bongdev.aigenapp.databinding.ActivityCategoryDetailBinding
import com.bongdev.aigenapp.ui.ExpenseAdapter
import com.bongdev.aigenapp.ui.MainViewModel
import com.google.android.material.transition.platform.MaterialContainerTransform
import kotlinx.coroutines.launch

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryDetailBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top, bottom = insets.bottom)
            windowInsets
        }

        // Get category ID from intent
        val categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1)
        if (categoryId == -1L) {
            finish()
            return
        }

        setupUI()
        observeData(categoryId)
        startEnterAnimation()
    }

    private fun setupUI() {
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { 
            finishAfterTransition()
        }

        // Set up RecyclerView
        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryDetailActivity)
            expenseAdapter = ExpenseAdapter()
            adapter = expenseAdapter
        }

        // Set up add expense FAB
        binding.addExpenseFab.setOnClickListener {
            // TODO: Show add expense dialog specific to this category
        }
    }

    private fun observeData(categoryId: Long) {
        lifecycleScope.launch {
            viewModel.categoriesWithExpenses.collect { categories ->
                val categoryWithExpenses = categories.find { it.category.id == categoryId }
                categoryWithExpenses?.let { category ->
                    updateUI(category)
                }
            }
        }
    }

    private fun updateUI(categoryWithExpenses: CategoryWithExpenses) {
        with(binding) {
            // Update toolbar
            toolbar.title = categoryWithExpenses.category.name
            
            // Update category icon and color
            categoryIcon.setImageResource(categoryWithExpenses.category.iconResId)
            categoryIcon.setColorFilter(getColor(categoryWithExpenses.category.colorResId))
            
            // Update amounts with animation
            animateAmount(totalAmountText, categoryWithExpenses.expenses.sumOf { it.amount })
            
            // Update budget text
            budgetText.text = getString(R.string.budget_format, 
                categoryWithExpenses.category.budget)

            // Update progress with animation
            val progress = if (categoryWithExpenses.category.budget > 0) {
                ((categoryWithExpenses.expenses.sumOf { it.amount } / categoryWithExpenses.category.budget) * 100)
                    .toInt().coerceIn(0, 100)
            } else 0

            ObjectAnimator.ofInt(progressIndicator, "progress", progress).apply {
                duration = 500
                interpolator = FastOutSlowInInterpolator()
                start()
            }

            // Update progress color
            progressIndicator.setIndicatorColor(
                when {
                    progress < 70 -> getColor(R.color.progress_good)
                    progress < 90 -> getColor(R.color.progress_warning)
                    else -> getColor(R.color.expense_red)
                }
            )

            // Update expenses list with animation
            expenseAdapter.submitList(categoryWithExpenses.expenses.sortedByDescending { it.date })
        }
    }

    private fun startEnterAnimation() {
        val duration = 400L
        
        // Animate card
        binding.detailsCard.alpha = 0f
        binding.detailsCard.translationY = 50f
        binding.detailsCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        // Animate expenses title and list
        binding.expensesTitle.alpha = 0f
        binding.expensesRecyclerView.alpha = 0f
        binding.expensesTitle.animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(200)
            .start()
        binding.expensesRecyclerView.animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(300)
            .start()

        // Animate FAB
        binding.addExpenseFab.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setStartDelay(400)
            .start()
    }

    private fun animateAmount(textView: android.widget.TextView, targetAmount: Double) {
        val animator = ValueAnimator.ofFloat(
            textView.text.toString().filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f,
            targetAmount.toFloat()
        )
        animator.duration = 500
        animator.addUpdateListener { animation ->
            textView.text = getString(R.string.amount_format, animation.animatedValue as Float)
        }
        animator.start()
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "extra_category_id"
    }
} 