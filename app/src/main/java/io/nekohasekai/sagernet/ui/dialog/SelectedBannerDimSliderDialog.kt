package io.nekohasekai.sagernet.ui.dialog

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import io.nekohasekai.sagernet.Action
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore

/**
 * Ported from MikuRay's `SelectedBannerDimSliderDialog`. Lets the user pick
 * how strongly the selected-profile banner image is dimmed (0-90%).
 */
class SelectedBannerDimSliderDialog @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    override fun onClick() {
        val current = DataStore.selectedBannerDim.coerceIn(0, 90)
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_selected_banner_dim_slider, null)
        val slider = dialogView.findViewById<Slider>(R.id.slider_selected_banner_dim)
        slider.value = current.toFloat()

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.selected_banner_dim_title)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newValue = slider.value.toInt()
                DataStore.selectedBannerDim = newValue
                summary = context.getString(R.string.selected_banner_dim_summary_value, newValue)
                context.sendBroadcast(Intent(Action.SELECTED_STYLE_CHANGED))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateSummary() {
        summary = context.getString(R.string.selected_banner_dim_summary_value, DataStore.selectedBannerDim)
    }

    override fun onAttached() {
        super.onAttached()
        updateSummary()
    }
}
