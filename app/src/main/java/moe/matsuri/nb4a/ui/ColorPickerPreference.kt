package moe.matsuri.nb4a.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.TypedArrayUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setPadding
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.getColorAttr
import kotlin.math.roundToInt

class ColorPickerPreference
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = TypedArrayUtils.getAttr(
        context,
        androidx.preference.R.attr.editTextPreferenceStyle,
        android.R.attr.editTextPreferenceStyle
    )
) : Preference(
    context, attrs, defStyle
) {

    var inited = false

    override fun getSummary(): CharSequence? {
        val value = getPersistedInt(DEFAULT_THEME)
        val names = context.resources.getStringArray(R.array.theme_names)
        val index = value - 1
        return if (index in names.indices) names[index] else super.getSummary()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val widgetFrame = holder.findViewById(android.R.id.widget_frame) as LinearLayout

        if (!inited) {
            inited = true

            widgetFrame.addView(
                getNekoImageViewAtColor(
                    context.getColorAttr(R.attr.colorPrimary),
                    48,
                    0
                )
            )
            widgetFrame.visibility = View.VISIBLE
        }
    }

    fun getNekoImageViewAtColor(color: Int, sizeDp: Int, paddingDp: Int): ImageView {
        // dp to pixel
        val factor = context.resources.displayMetrics.density
        val size = (sizeDp * factor).roundToInt()
        val paddingSize = (paddingDp * factor).roundToInt()

        return ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(size, size)
            setPadding(paddingSize)
            setImageDrawable(getNekoAtColor(resources, color))
        }
    }

    fun getNekoAtColor(res: Resources, color: Int): Drawable {
        val neko = ResourcesCompat.getDrawable(
            res,
            R.drawable.ic_baseline_fiber_manual_record_24,
            null
        )!!
        DrawableCompat.setTint(neko.mutate(), color)
        return neko
    }

    override fun onClick() {
        super.onClick()

        lateinit var dialog: AlertDialog

        val grid = GridLayout(context).apply {
            columnCount = 4

            val colors = context.resources.getIntArray(R.array.material_colors)
            var i = 0

            for (color in colors) {
                i++ //Theme.kt

                val themeId = i
                val view = getNekoImageViewAtColor(color, 64, 0).apply {
                    setOnClickListener {
                        persistInt(themeId)
                        notifyChanged()
                        dialog.dismiss()
                        callChangeListener(themeId)
                    }
                }
                addView(view)
            }

        }

        dialog = MaterialAlertDialogBuilder(context).setTitle(title)
            .setView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                addView(grid)
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        // Keep in sync with io.nekohasekai.sagernet.utils.Theme.defaultTheme() (PINK)
        private const val DEFAULT_THEME = 8
    }
}
