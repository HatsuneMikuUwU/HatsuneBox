package io.nekohasekai.sagernet.widget

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore

/**
 * Ported from MikuRay's UwuPreferenceCategory. Renders one of several
 * category header styles (gradient icon badge, or a Miku/Teto/Neru
 * character sticker), matching the currently selected "categoryStyle"
 * preference. CategoryStyleHelper.applyToFragment/applyToGroup is
 * responsible for keeping layoutResource in sync with that saved choice;
 * the constructor here only picks a sane default so a category still
 * renders correctly before the helper runs.
 *
 * app:sectionIcon only affects the gradient style — the character styles
 * always show their fixed illustration regardless of category.
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
        val iconView = holder.itemView.findViewById<UwuHeaderIconView>(R.id.uwu_category_icon)
            ?: return
        iconView.setSectionIcon(sectionIconRes)
    }
}
