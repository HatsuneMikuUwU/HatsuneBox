package io.nekohasekai.sagernet.utils

import android.app.Activity
import androidx.core.app.ActivityCompat
import io.nekohasekai.sagernet.database.DataStore

class ThemeStateManager(private val activity: Activity) {

    private var currentAppTheme: Int = 0

    init {
        loadState()
    }

    private fun loadState() {
        currentAppTheme = DataStore.appTheme
    }

    fun checkThemeChangedAndRecreate() {
        val newAppTheme = DataStore.appTheme

        if (currentAppTheme != newAppTheme) {
            activity.setTheme(Theme.getTheme(newAppTheme))
            loadState()
            ActivityCompat.recreate(activity)
        }
    }
}
