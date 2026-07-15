package moe.matsuri.nb4a.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.getColorAttr

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

        const val DEFAULT_ICON_RES = R.drawable.filter_all_solar

        val ICONS: List<Pair<String, String>> = listOf(
            "filter_all_solar" to "All",
            "filter_airplane_solar" to "Airplane",
            "filter_book_solar" to "Book",
            "filter_bots_solar" to "Bots",
            "filter_cat_solar" to "Cat",
            "filter_channel_solar" to "Channel",
            "filter_crown_solar" to "Crown",
            "filter_custom_solar" to "Custom",
            "filter_favorite_solar" to "Favorite",
            "filter_flower_solar" to "Flower",
            "filter_game_solar" to "Game",
            "filter_groups_solar" to "Groups",
            "filter_home_solar" to "Home",
            "filter_light_solar" to "Light",
            "filter_like_solar" to "Like",
            "filter_love_solar" to "Love",
            "filter_mask_solar" to "Mask",
            "filter_money_solar" to "Money",
            "filter_note_solar" to "Note",
            "filter_palette_solar" to "Palette",
            "filter_party_solar" to "Party",
            "filter_private_solar" to "Private",
            "filter_setup_solar" to "Setup",
            "filter_sport_solar" to "Sport",
            "filter_study_solar" to "Study",
            "filter_trade_solar" to "Trade",
            "filter_travel_solar" to "Travel",
            "filter_unmuted_solar" to "Unmuted",
            "filter_unread_solar" to "Unread",
            "filter_work_solar" to "Work"
        )

        fun labelFor(value: String?): String {
            if (value == null || value == NONE) return "None"
            return ICONS.firstOrNull { it.first == value }?.second ?: "None"
        }
    }

    private var dialog: AlertDialog? = null

    private fun resolveIcon(name: String?): Int {
        if (name == null || name == NONE || name.isBlank()) return 0
        
        return when (name) {
            "filter_all_solar" -> R.drawable.filter_all_solar
            "filter_airplane_solar" -> R.drawable.filter_airplane_solar
            "filter_book_solar" -> R.drawable.filter_book_solar
            "filter_bots_solar" -> R.drawable.filter_bots_solar
            "filter_cat_solar" -> R.drawable.filter_cat_solar
            "filter_channel_solar" -> R.drawable.filter_channel_solar
            "filter_crown_solar" -> R.drawable.filter_crown_solar
            "filter_custom_solar" -> R.drawable.filter_custom_solar
            "filter_favorite_solar" -> R.drawable.filter_favorite_solar
            "filter_flower_solar" -> R.drawable.filter_flower_solar
            "filter_game_solar" -> R.drawable.filter_game_solar
            "filter_groups_solar" -> R.drawable.filter_groups_solar
            "filter_home_solar" -> R.drawable.filter_home_solar
            "filter_light_solar" -> R.drawable.filter_light_solar
            "filter_like_solar" -> R.drawable.filter_like_solar
            "filter_love_solar" -> R.drawable.filter_love_solar
            "filter_mask_solar" -> R.drawable.filter_mask_solar
            "filter_money_solar" -> R.drawable.filter_money_solar
            "filter_note_solar" -> R.drawable.filter_note_solar
            "filter_palette_solar" -> R.drawable.filter_palette_solar
            "filter_party_solar" -> R.drawable.filter_party_solar
            "filter_private_solar" -> R.drawable.filter_private_solar
            "filter_setup_solar" -> R.drawable.filter_setup_solar
            "filter_sport_solar" -> R.drawable.filter_sport_solar
            "filter_study_solar" -> R.drawable.filter_study_solar
            "filter_trade_solar" -> R.drawable.filter_trade_solar
            "filter_travel_solar" -> R.drawable.filter_travel_solar
            "filter_unmuted_solar" -> R.drawable.filter_unmuted_solar
            "filter_unread_solar" -> R.drawable.filter_unread_solar
            "filter_work_solar" -> R.drawable.filter_work_solar
            else -> 0
        }
    }

    private fun updateIconAndSummary() {
        val current = getPersistedString(NONE)
        val resId = resolveIcon(current)
        
        try {
            val drawableId = if (resId != 0) resId else DEFAULT_ICON_RES
            val safeDrawable = AppCompatResources.getDrawable(context, drawableId)
            
            icon = safeDrawable
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        summary = labelFor(current)
    }

    override fun onAttachedToHierarchy(preferenceManager: androidx.preference.PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        updateIconAndSummary()
    }

    override fun onBindViewHolder(holder: androidx.preference.PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        
        holder.itemView.post {
            updateIconAndSummary()
        }
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
