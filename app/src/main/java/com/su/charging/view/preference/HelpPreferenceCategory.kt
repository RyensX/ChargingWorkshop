package com.su.charging.view.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import com.su.charging.R

class HelpPreferenceCategory(context: Context, attributes: AttributeSet) :
    PreferenceCategory(context, attributes), View.OnClickListener {

    private val helpImageView = ImageView(context).apply {
        setImageResource(R.drawable.ic_baseline_help_outline_24)
        setOnClickListener(this@HelpPreferenceCategory)
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

    override fun onClick(v: View?) {
        AlertDialog.Builder(context, R.style.AlertDialog_AppCompat_Help)
            .setTitle("音频设置帮助")
            .setMessage(R.string.audio_setting_help)
            .setPositiveButton("我知道了", null)
            .create()
            .show()
    }
}