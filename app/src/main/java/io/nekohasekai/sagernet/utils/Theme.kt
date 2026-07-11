package io.nekohasekai.sagernet.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.app

object Theme {

    // Miku UI theme (from MikuRay). Only the 16 hues MikuRay ships are available;
    // the old PINK_SSR / LIGHT_BLUE / DEEP_ORANGE / GREY / BLACK swatches no longer exist.
    const val RED = 1
    const val PINK = 2
    const val PURPLE = 3
    const val DEEP_PURPLE = 4
    const val INDIGO = 5
    const val BLUE = 6
    const val CYAN = 7
    const val TEAL = 8
    const val GREEN = 9
    const val LIGHT_GREEN = 10
    const val LIME = 11
    const val YELLOW = 12
    const val AMBER = 13
    const val ORANGE = 14
    const val BROWN = 15
    const val BLUE_GREY = 16

    private fun defaultTheme() = PINK

    fun apply(context: Context) {
        context.setTheme(getTheme())
    }

    fun getTheme(): Int {
        return getTheme(DataStore.appTheme)
    }

    fun getTheme(theme: Int): Int {
        return when (theme) {
            RED -> R.style.AppTheme_Red
            PINK -> R.style.AppTheme_Pink
            PURPLE -> R.style.AppTheme_Purple
            DEEP_PURPLE -> R.style.AppTheme_DeepPurple
            INDIGO -> R.style.AppTheme_Indigo
            BLUE -> R.style.AppTheme_Blue
            CYAN -> R.style.AppTheme_Cyan
            TEAL -> R.style.AppTheme_Teal
            GREEN -> R.style.AppTheme_Green
            LIGHT_GREEN -> R.style.AppTheme_LightGreen
            LIME -> R.style.AppTheme_Lime
            YELLOW -> R.style.AppTheme_Yellow
            AMBER -> R.style.AppTheme_Amber
            ORANGE -> R.style.AppTheme_Orange
            BROWN -> R.style.AppTheme_Brown
            BLUE_GREY -> R.style.AppTheme_BlueGrey
            else -> getTheme(defaultTheme())
        }
    }

    var currentNightMode = -1
    fun getNightMode(): Int {
        if (currentNightMode == -1) {
            currentNightMode = DataStore.nightTheme
        }
        return getNightMode(currentNightMode)
    }

    fun getNightMode(mode: Int): Int {
        return when (mode) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_YES
            2 -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
    }

    fun usingNightMode(): Boolean {
        return when (DataStore.nightTheme) {
            1 -> true
            2 -> false
            else -> (app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }

    fun applyNightTheme() {
        AppCompatDelegate.setDefaultNightMode(getNightMode())
    }

}