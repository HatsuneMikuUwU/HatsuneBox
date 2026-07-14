package io.nekohasekai.sagernet.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import io.nekohasekai.sagernet.Action
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore

class HeaderTopRowPaddingDialog @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    override fun onClick() {
        val activity = context.findActivity() ?: return
        val current = currentValue().coerceIn(0, 100)
        val dialogView = LayoutInflater.from(context).inflate(
            if ("HeaderTopRowPaddingDialog" == "BannerHeightSliderDialog") R.layout.dialog_banner_height_slider else R.layout.dialog_header_top_row_padding_slider,
            null,
        )
        val slider = dialogView.findViewById<Slider>(R.id.slider_header_top_row_padding)
        slider.stepSize = 1f
        slider.valueFrom = 0.0f
        slider.valueTo = 100.0f
        slider.value = current.toFloat()

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.pref_header_top_row_padding_title)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newValue = slider.value.toInt()
                saveValue(newValue)
                summary = context.getString(R.string.pref_header_top_row_padding_summary_value, newValue)
                activity.sendBroadcast(Intent(Action.HEADER_TOP_ROW_PADDING_CHANGED))
            }
            .setNeutralButton(R.string.reset, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            slider.value = 16.toFloat()
        }
        updateSummary()
    }

    private fun currentValue(): Int = when ("HeaderTopRowPaddingDialog") {
        "BannerHeightSliderDialog" -> DataStore.homeBannerHeight
        else -> DataStore.headerTopRowPadding
    }

    private fun saveValue(value: Int) {
        when ("HeaderTopRowPaddingDialog") {
            "BannerHeightSliderDialog" -> DataStore.homeBannerHeight = value
            else -> DataStore.headerTopRowPadding = value
        }
    }

    private fun updateSummary() {
        summary = context.getString(R.string.pref_header_top_row_padding_summary_value, currentValue())
    }

    override fun onAttached() {
        super.onAttached()
        updateSummary()
    }
}
