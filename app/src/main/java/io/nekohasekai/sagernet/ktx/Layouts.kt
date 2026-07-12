package io.nekohasekai.sagernet.ktx

import android.graphics.Rect
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ui.MainActivity

class FixedLinearLayoutManager(val recyclerView: RecyclerView) :
    LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (ignored: IndexOutOfBoundsException) {
        }
    }

    private var listenerDisabled = false

    override fun scrollVerticallyBy(
        dx: Int, recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (!DataStore.showBottomBar) return super.scrollVerticallyBy(dx, recycler, state)

        val scrollRange = super.scrollVerticallyBy(dx, recycler, state)
        if (listenerDisabled) return scrollRange
        val activity = recyclerView.context as? MainActivity
        if (activity == null) {
            listenerDisabled = true
            return scrollRange
        }

        val overscroll = dx - scrollRange
        val bottomCard = activity.binding.cardBottomStatus
        
        if (overscroll > 0) {
            val view =
                (recyclerView.findViewHolderForAdapterPosition(findLastVisibleItemPosition())
                    ?: return scrollRange).itemView
            
            val itemLocation = Rect().also { view.getGlobalVisibleRect(it) }
            val cardLocation = Rect().also { bottomCard.getGlobalVisibleRect(it) }
            
            if (!itemLocation.contains(cardLocation.left, cardLocation.top) && !itemLocation.contains(
                    cardLocation.right,
                    cardLocation.bottom
                )
            ) {
                return scrollRange
            }
            
            if (bottomCard.translationY == 0f) {
                val margin = (bottomCard.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                bottomCard.animate()
                    .translationY((bottomCard.height + margin).toFloat())
                    .setDuration(200)
                    .start()
            }
        } else {
            
            if (bottomCard.translationY > 0f) {
                bottomCard.animate()
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }
        return scrollRange
    }
}
