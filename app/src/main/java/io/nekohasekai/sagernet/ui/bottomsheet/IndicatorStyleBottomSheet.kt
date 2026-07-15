package io.nekohasekai.sagernet.ui.bottomsheet

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.getColorAttr
import io.nekohasekai.sagernet.ui.IndicatorStyle
import io.nekohasekai.sagernet.ui.IndicatorStyleAdapter

/**
 * Ported from MikuRay's `IndicatorStyleBottomSheet`. Lets the user pick which
 * of the 15 selected-profile indicator styles to use in the profile list.
 */
class IndicatorStyleBottomSheet(
    private val context: Context,
    private val onSelected: () -> Unit
) {
    fun show() {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.uwu_layout_bottom_sheet_indicator_style, null)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerStyle)

        val selectedStyle = runCatching { IndicatorStyle.valueOf(DataStore.indicatorStyle) }
            .getOrDefault(IndicatorStyle.STYLE_0)

        recycler.layoutManager = LinearLayoutManager(context)

        recycler.adapter = IndicatorStyleAdapter(context, selectedStyle) { style ->
            DataStore.indicatorStyle = style.name
            onSelected()
            dialog.dismiss()
        }

        dialog.setContentView(view)

        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        val bgColor = context.getColorAttr(R.attr.colorBg)

        val bottomSheet = dialog.findViewById<android.view.View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
        if (bottomSheet != null) {
            bottomSheet.backgroundTintList = ColorStateList.valueOf(bgColor)
            bottomSheet.clipToOutline = true

            ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { v, insets ->
                val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                val screenHeight = v.resources.displayMetrics.heightPixels
                val margin = (8 * v.resources.displayMetrics.density).toInt()

                dialog.behavior.maxHeight = screenHeight - statusBarInset - margin

                insets
            }
        }

        dialog.window?.let { window ->
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.navigationBarColor = bgColor
        }

        dialog.show()
    }
}
