package com.dev.utilix

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.view.View

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var layoutIndicators: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        preferenceManager = PreferenceManager(this)

        setupOnboardingItems()
        setupIndicators()
        setCurrentIndicator(0)
        setupListeners()
    }

    private fun setupOnboardingItems() {
        val onBoardingItems = listOf(
            OnboardingItem(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                imageRes = R.mipmap.ic_utilix_logo
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                lottieRes = R.raw.drag
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                imageRes = R.drawable.ic_keep_awake
            )
        )

        onboardingAdapter = OnboardingAdapter(onBoardingItems)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = onboardingAdapter
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                
                if (position == onboardingAdapter.itemCount - 1) {
                    btnNext.text = getString(R.string.btn_start)
                } else {
                    btnNext.text = getString(R.string.btn_next)
                }
            }
        })
    }

    private fun setupIndicators() {
        layoutIndicators = findViewById(R.id.layoutIndicators)
        val indicators = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_dot)
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.accent_yellow))
            } else {
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.text_primary))
                imageView.alpha = 0.3f
            }
        }
    }

    private fun setupListeners() {
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        val btnSkip = findViewById<TextView>(R.id.btnSkip)

        btnNext.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingAdapter.itemCount) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        preferenceManager.isFirstRun = false
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
