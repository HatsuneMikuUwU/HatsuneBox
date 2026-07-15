package io.nekohasekai.sagernet.widget

import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore

object CategoryStyleHelper {

    fun layoutForStyle(styleValue: String?): Int = when (styleValue) {
        "miku" -> R.layout.uwu_preference_category_miku_1
        "miku2" -> R.layout.uwu_preference_category_miku_2
        "teto" -> R.layout.uwu_preference_category_teto_1
        "teto2" -> R.layout.uwu_preference_category_teto_2
        "neru" -> R.layout.uwu_preference_category_neru
        "gradient" -> R.layout.uwu_preference_category_gradient
        else -> R.layout.uwu_preference_category_gradient
    }

    /**
     * Character illustration for the current style, or null when the style
     * is "gradient" (i.e. the icon badge should render normally).
     */
    fun characterDrawableForStyle(styleValue: String?): Int? = when (styleValue) {
        "miku" -> R.drawable.uwu_icon_miku
        "miku2" -> R.drawable.uwu_icon_miku_2
        "teto" -> R.drawable.uwu_icon_teto
        "teto2" -> R.drawable.uwu_icon_teto_2
        "neru" -> R.drawable.uwu_icon_neru
        else -> null
    }

    fun applyToGroup(styleValue: String?, group: PreferenceGroup) {
        val layout = layoutForStyle(styleValue)
        for (i in 0 until group.preferenceCount) {
            val pref = group.getPreference(i)
            if (pref is PreferenceCategory) pref.layoutResource = layout
            if (pref is PreferenceGroup) applyToGroup(styleValue, pref)
        }
    }

    fun applyToFragment(fragment: PreferenceFragmentCompat) {
        val saved = DataStore.categoryStyle
        fragment.preferenceScreen?.let { applyToGroup(saved, it) }
    }
}
