package io.nekohasekai.sagernet.ui

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.utils.Theme

abstract class ThemedActivity : AppCompatActivity {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    var uiMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        Theme.applyNightTheme()

        super.onCreate(savedInstanceState)

        uiMode = resources.configuration.uiMode

        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                findViewById<AppBarLayout>(R.id.appbar)?.apply {
                    updatePadding(top = top)
//                Logs.w("appbar $top")
                }
//            findViewById<NavigationView>(R.id.nav_view)?.apply {
//                updatePadding(top = top)
//            }
                insets
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.uiMode != uiMode) {
            uiMode = newConfig.uiMode
            ActivityCompat.recreate(this)
        }
    }

    /**
     * Sets up a [layout_appbar_collapsing]-style toolbar for a drawer-destination sub-activity:
     * a collapsing large toolbar with an up (back) navigation icon that finishes the activity.
     * Replaces the old drawer-hamburger ToolbarFragment pattern.
     */
    fun setupCollapsingToolbar(@StringRes titleRes: Int, homeAsUp: Boolean = true) {
        setupCollapsingToolbar(getString(titleRes), homeAsUp)
    }

    fun setupCollapsingToolbar(title: CharSequence, homeAsUp: Boolean = true) {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUp)
        findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)?.title = title
    }

    fun snackbar(@StringRes resId: Int): Snackbar = snackbar("").setText(resId)
    fun snackbar(text: CharSequence): Snackbar = snackbarInternal(text).apply {
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 10
        }
    }

    internal open fun snackbarInternal(text: CharSequence): Snackbar = throw NotImplementedError()

}