package io.nekohasekai.sagernet.utils

import android.app.Activity
import androidx.core.app.ActivityCompat
import io.nekohasekai.sagernet.database.DataStore

class ThemeStateManager(private val activity: Activity) {

    private data class ThemeState(
        val appTheme: Int,
        val showHomeBanner: Boolean,
        val homeBannerHeight: Int,
        val headerTopRowPadding: Int,
        val customHomeBannerUri: String,
        val indicatorStyle: String,
        val selectedBannerStyleEnabled: Boolean,
        val selectedBannerDim: Int,
        val customSelectedBannerUri: String
    )

    private var currentState: ThemeState = fetchCurrentState()

    private fun fetchCurrentState(): ThemeState {
        return ThemeState(
            appTheme = DataStore.appTheme,
            showHomeBanner = DataStore.showHomeBanner,
            homeBannerHeight = DataStore.homeBannerHeight,
            headerTopRowPadding = DataStore.headerTopRowPadding,
            customHomeBannerUri = DataStore.customHomeBannerUri,
            indicatorStyle = DataStore.indicatorStyle,
            selectedBannerStyleEnabled = DataStore.selectedBannerStyleEnabled,
            selectedBannerDim = DataStore.selectedBannerDim,
            customSelectedBannerUri = DataStore.customSelectedBannerUri
        )
    }

    fun checkThemeChangedAndRecreate() {
        val newState = fetchCurrentState()
        
        if (currentState != newState) {
            activity.setTheme(Theme.getTheme(newState.appTheme))
            currentState = newState
            ActivityCompat.recreate(activity)
        }
    }
}
