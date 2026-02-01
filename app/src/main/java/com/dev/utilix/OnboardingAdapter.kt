package com.dev.utilix

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat

import com.airbnb.lottie.LottieAnimationView
import android.view.View.VISIBLE
import android.view.View.GONE

data class OnboardingItem(
    val title: String,
    val description: String,
    val imageRes: Int = 0,
    val lottieRes: Int = 0
)

class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle = view.findViewById<TextView>(R.id.textTitle)
        private val textDescription = view.findViewById<TextView>(R.id.textDescription)
        private val imageOnboarding = view.findViewById<ImageView>(R.id.imgOnboarding)
        private val lottieAnimationView = view.findViewById<LottieAnimationView>(R.id.lottieAnimationView)

        fun bind(item: OnboardingItem) {
            textTitle.text = item.title
            textDescription.text = item.description
            
            if (item.lottieRes != 0) {
                imageOnboarding.visibility = GONE
                lottieAnimationView.visibility = VISIBLE
                lottieAnimationView.setAnimation(item.lottieRes)
                lottieAnimationView.playAnimation()
            } else {
                imageOnboarding.visibility = VISIBLE
                lottieAnimationView.visibility = GONE
                imageOnboarding.setImageResource(item.imageRes)
                imageOnboarding.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_primary))
                lottieAnimationView.cancelAnimation()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
