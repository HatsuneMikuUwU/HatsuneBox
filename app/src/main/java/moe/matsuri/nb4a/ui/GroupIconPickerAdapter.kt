package moe.matsuri.nb4a.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.getColorAttr

class GroupIconPickerAdapter(
    private val context: Context,
    val icons: List<String>,
    private var selectedIcon: String?,
    private val onSelect: (String) -> Unit,
) : RecyclerView.Adapter<GroupIconPickerAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.icon_card)
        val icon: ImageView = view.findViewById(R.id.icon_view)
        val check: ImageView = view.findViewById(R.id.check_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(context)
            .inflate(R.layout.item_group_icon_picker, parent, false)
        return VH(v)
    }

    override fun getItemCount() = icons.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val name = icons[position]
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        val selected = name == selectedIcon

        if (resId != 0) {
            holder.icon.setImageResource(resId)
        } else {
            holder.icon.setImageDrawable(null)
        }

        val bgColor = if (selected) context.getColorAttr(R.attr.colorPrimary) else 0
        val tint = if (selected) {
            context.getColorAttr(R.attr.colorOnPrimary)
        } else {
            context.getColorAttr(R.attr.colorOnSurfaceVariant)
        }

        holder.card.setCardBackgroundColor(bgColor)
        holder.icon.imageTintList = ColorStateList.valueOf(tint)
        holder.check.visibility = if (selected) View.VISIBLE else View.GONE
        if (selected) holder.check.imageTintList = ColorStateList.valueOf(tint)

        holder.itemView.setOnClickListener {
            val prev = selectedIcon
            selectedIcon = name
            onSelect(name)
            val prevIdx = icons.indexOf(prev)
            if (prevIdx >= 0) notifyItemChanged(prevIdx)
            notifyItemChanged(position)
        }
    }

    fun setSelected(iconName: String?) {
        val prev = selectedIcon
        selectedIcon = iconName
        val prevIdx = icons.indexOf(prev)
        val newIdx = icons.indexOf(iconName)
        if (prevIdx >= 0) notifyItemChanged(prevIdx)
        if (newIdx >= 0) notifyItemChanged(newIdx)
    }
}
