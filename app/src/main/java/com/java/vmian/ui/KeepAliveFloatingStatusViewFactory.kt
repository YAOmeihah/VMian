package com.java.vmian.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.java.vmian.presentation.ui.model.KeepAliveFloatingStatusTextModel

object KeepAliveFloatingStatusViewFactory {
    private const val TITLE_VIEW_TAG = "vmian_floating_status_title"
    private const val SUBTITLE_VIEW_TAG = "vmian_floating_status_subtitle"

    fun create(context: Context, text: KeepAliveFloatingStatusTextModel): View {
        val density = context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(density, 10), dp(density, 7), dp(density, 12), dp(density, 7))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(density, 14).toFloat()
                setColor(Color.argb(230, 24, 24, 27))
                setStroke(dp(density, 1), Color.argb(80, 255, 255, 255))
            }
            elevation = dp(density, 8).toFloat()
        }

        val statusDot = View(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(34, 197, 94))
            }
        }
        container.addView(
            statusDot,
            LinearLayout.LayoutParams(dp(density, 8), dp(density, 8)).apply {
                marginEnd = dp(density, 8)
            }
        )

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        textColumn.addView(
            TextView(context).apply {
                tag = TITLE_VIEW_TAG
                this.text = text.title
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
                maxLines = 1
            }
        )
        textColumn.addView(
            TextView(context).apply {
                tag = SUBTITLE_VIEW_TAG
                this.text = text.subtitle
                setTextColor(Color.argb(210, 255, 255, 255))
                textSize = 10f
                includeFontPadding = false
                maxLines = 1
            }
        )

        container.addView(textColumn)
        return container
    }

    fun update(view: View?, text: KeepAliveFloatingStatusTextModel) {
        view?.findViewWithTag<TextView>(TITLE_VIEW_TAG)?.text = text.title
        view?.findViewWithTag<TextView>(SUBTITLE_VIEW_TAG)?.text = text.subtitle
    }

    fun widthPx(context: Context): Int = dp(context.resources.displayMetrics.density, 144)

    fun heightPx(context: Context): Int = dp(context.resources.displayMetrics.density, 44)

    private fun dp(density: Float, value: Int): Int = (value * density + 0.5f).toInt()
}
