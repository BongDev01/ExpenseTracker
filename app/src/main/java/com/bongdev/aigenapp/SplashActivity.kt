package com.bongdev.aigenapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Start animations
        startAnimations()

        // Initialize data and navigate to MainActivity
        lifecycleScope.launch {
            try {
                delay(2000)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                startActivity(Intent(this@SplashActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }

    private fun startAnimations() {
        val icon = findViewById<android.widget.ImageView>(R.id.splashIcon)
        val appName = findViewById<android.widget.TextView>(R.id.appName)
        val tagline = findViewById<android.widget.TextView>(R.id.appTagline)

        // Icon animation
        val iconScale = ObjectAnimator.ofFloat(icon, "scaleX", 0.3f, 1f).apply {
            duration = 500
            interpolator = OvershootInterpolator()
        }
        val iconScaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.3f, 1f).apply {
            duration = 500
            interpolator = OvershootInterpolator()
        }

        // Text animations
        val nameAlpha = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 300
        }
        val taglineAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 500
        }

        // Play animations together
        AnimatorSet().apply {
            playTogether(iconScale, iconScaleY, nameAlpha, taglineAlpha)
            start()
        }
    }
} 