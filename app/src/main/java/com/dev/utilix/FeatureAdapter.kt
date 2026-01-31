package com.dev.utilix

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dev.utilix.R
import java.util.Collections

class FeatureAdapter(
    private val context: Context,
    private val items: MutableList<FeatureItem>,
    private val onFeatureClick: (FeatureItem, Int) -> Unit,
    private val onFeatureLongClick: (FeatureItem, Int) -> Boolean,
    private val onItemMove: (Int, Int) -> Unit
) : RecyclerView.Adapter<FeatureAdapter.FeatureViewHolder>() {

    inner class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardContainer: CardView = itemView.findViewById(R.id.cardContainer)
        val iconFeature: ImageView = itemView.findViewById(R.id.iconFeature)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textValue: TextView = itemView.findViewById(R.id.textValue)

        fun bind(item: FeatureItem) {
            
            // Set Base Info
            iconFeature.setImageResource(item.iconRes)
            
            // Set Text
            if (item.activeText != null) {
                 textTitle.text = item.activeText
            } else {
                 textTitle.text = context.getString(item.titleRes)
            }
            
            // Special case for Counter or others using Value
            // For now, let's keep it simple and reuse title unless complex
            
            // Set Style based on Active State
            if (item.isActive) {
                // Active: Yellow Card, White Circle
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.accent_yellow))
                iconFeature.setBackgroundResource(R.drawable.bg_circle_active)
            } else {
                // Inactive: White Card, Grey Circle
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                iconFeature.setBackgroundResource(R.drawable.bg_circle_inactive)
            }

            // Click Listeners
            itemView.setOnClickListener { 
                onFeatureClick(item, adapterPosition)
            }
            
            itemView.setOnLongClickListener { 
                onFeatureLongClick(item, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feature_card, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItem(id: String, isActive: Boolean, activeText: String? = null) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index].isActive = isActive
            items[index].activeText = activeText
            notifyItemChanged(index)
        }
    }
    
    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        onItemMove(fromPosition, toPosition)
    }
}
