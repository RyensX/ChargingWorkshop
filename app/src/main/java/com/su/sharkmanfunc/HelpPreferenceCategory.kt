package com.su.sharkmanfunc

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder

class HelpPreferenceCategory(context: Context, attributes: AttributeSet) :
    PreferenceCategory(context, attributes) {

    private val helpImageView = ImageView(context).apply {
        setImageResource(R.drawable.ic_baseline_help_outline_24)
        tag = R.drawable.ic_baseline_help_outline_24
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.itemView?.findViewWithTag<ImageView>(R.drawable.ic_baseline_help_outline_24)
            ?: run {
                val pa = holder?.itemView
                if (pa is ViewGroup)
                    pa.addView(helpImageView)
            }
    }
}