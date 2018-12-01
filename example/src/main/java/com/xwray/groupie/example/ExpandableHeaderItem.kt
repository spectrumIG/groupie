package com.xwray.groupie.example

import android.graphics.drawable.Animatable
import androidx.annotation.StringRes
import android.view.View
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.example.item.HeaderItem
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_header.*

class ExpandableHeaderItem(@StringRes titleStringResId: Int,
                           @StringRes subtitleResId: Int)
    : HeaderItem(titleStringResId, subtitleResId), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)

        // Initial icon state -- not animated.
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse else R.drawable.expand)
            setOnClickListener {
                expandableGroup.onToggleExpanded()
                bindIcon(viewHolder)
            }
        }
    }

    private fun bindIcon(viewHolder: ViewHolder) {
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse_animated else R.drawable.expand_animated)
            (drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }
}
