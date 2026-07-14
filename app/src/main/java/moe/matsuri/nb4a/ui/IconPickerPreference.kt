package moe.matsuri.nb4a.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.getColorAttr

/**
 * A Preference that, instead of a plain text dropdown, opens a dialog with a
 * visual grid of icons to pick from — same layout/behaviour as MikuRay's
 * `pref_group_all_tab_icon` (a "None" row plus a RecyclerView grid of icon
 * cards with a checkmark on the selected one). Like MikuRay, the
 * preference's own leading icon and summary update to reflect the current
 * selection. The persisted value is the drawable resource name, or "none"
 * for no icon.
 */
class IconPickerPreference
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = TypedArrayUtils.getAttr(
        context,
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle
    )
) : Preference(context, attrs, defStyle) {

    companion object {
        const val NONE = "none"

        // Fallback icon shown on the preference itself when no icon is selected.
        const val DEFAULT_ICON_RES = R.drawable.ic_palette_24

        // (drawable name, display label) — labels are short technical names, not localized,
        // consistent with other picker-style preferences in this screen.
        val ICONS: List<Pair<String, String>> = listOf(
            "ic_baseline_home_24" to "Home",
            "ic_cloud" to "Cloud",
            "ic_baseline_airplanemode_active_24" to "Airplane",
            "ic_baseline_card_giftcard_24" to "Gift",
            "ic_baseline_lock_24" to "Lock",
            "ic_baseline_person_24" to "Person",
            "ic_palette_24" to "Palette",
            "ic_baseline_rule_folder_24" to "Folder",
            "ic_dns" to "DNS",
            "baseline_public_24" to "Public",
            "ic_action_note_add" to "Note",
            "ic_image_photo" to "Photo",
        )

        fun labelFor(value: String?): String {
            if (value == null || value == NONE) return "None"
            return ICONS.firstOrNull { it.first == value }?.second ?: "None"
        }
    }

    private var dialog: AlertDialog? = null

    private fun resolveIcon(name: String?): Int {
        if (name == null || name == NONE || name.isBlank()) return 0
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    /** Mirrors MikuRay's updateGroupAllTabIconSummary(): swap the leading icon + summary. */
    private fun updateIconAndSummary() {
        val current = getPersistedString(NONE)
        val resId = resolveIcon(current)
        if (resId != 0) {
            setIcon(resId)
        } else {
            setIcon(DEFAULT_ICON_RES)
        }
        summary = labelFor(current)
    }

    override fun onAttachedToHierarchy(preferenceManager: androidx.preference.PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        updateIconAndSummary()
    }

    override fun onBindViewHolder(holder: androidx.preference.PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        updateIconAndSummary()
    }

    override fun onClick() {
        super.onClick()

        val current = getPersistedString(NONE)
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_group_icon_picker, null)
        val rowNone = dialogView.findViewById<View>(R.id.row_none)
        val checkNone = dialogView.findViewById<ImageView>(R.id.check_none)
        val rv = dialogView.findViewById<RecyclerView>(R.id.rv_icons)

        val adapter = GroupIconPickerAdapter(
            context = context,
            icons = ICONS.map { it.first },
            selectedIcon = current.takeIf { it != NONE },
            onSelect = { name ->
                persistString(name)
                callChangeListener(name)
                updateIconAndSummary()
                dialog?.dismiss()
            }
        )
        rv.layoutManager = GridLayoutManager(context, 5)
        rv.adapter = adapter

        val noneSelected = current == NONE
        checkNone.visibility = if (noneSelected) View.VISIBLE else View.GONE
        if (noneSelected) {
            checkNone.imageTintList = ColorStateList.valueOf(context.getColorAttr(R.attr.colorPrimary))
        }

        rowNone.setOnClickListener {
            persistString(NONE)
            callChangeListener(NONE)
            updateIconAndSummary()
            dialog?.dismiss()
        }

        dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog?.show()
    }
}
