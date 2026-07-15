package io.nekohasekai.sagernet.widget

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.getColorAttr

/**
 * Ported from MikuRay's UwuPreferenceCategory (com.neko.widget). Renders one
 * of several category header styles (gradient icon badge, or a Miku/Teto/Neru
 * character sticker), matching the currently selected "categoryStyle"
 * preference. CategoryStyleHelper.applyToFragment/applyToGroup is
 * responsible for keeping layoutResource in sync with that saved choice;
 * the constructor here only picks a sane default so a category still
 * renders correctly before the helper runs.
 *
 * For the gradient style, binding mirrors MikuRay exactly: a plain
 * ImageView (R.id.uwu_category_icon) holds the per-category icon, and its
 * parent FrameLayout gets a colorPrimary -> colorTertiary gradient oval
 * background set at bind time. app:sectionIcon only affects that style —
 * the character styles always show their fixed illustration regardless of
 * category and have no such id.
 *
 * Usage in preference XML:
 * <io.nekohasekai.sagernet.widget.UwuPreferenceCategory
 *     app:title="@string/..."
 *     app:sectionIcon="@drawable/..." />
 */
class UwuPreferenceCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : PreferenceCategory(context, attrs) {

    private var sectionIconRes: Int = 0

    init {
        layoutResource = CategoryStyleHelper.layoutForStyle(DataStore.categoryStyle)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.UwuHeaderIconView)
        sectionIconRes = ta.getResourceId(R.styleable.UwuHeaderIconView_sectionIcon, 0)
        ta.recycle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val iconView = holder.itemView.findViewById<ImageView>(R.id.uwu_category_icon)
            ?: return
        if (sectionIconRes != 0) {
            iconView.setImageResource(sectionIconRes)
        }
        val frame = iconView.parent as? ViewGroup ?: return

        val colorStart = context.getColorAttr(R.attr.colorPrimary)
        val colorEnd = context.getColorAttr(R.attr.colorTertiary)

        frame.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(colorStart, colorEnd),
        ).apply { shape = GradientDrawable.OVAL }
    }
}
