package com.xwray.groupie.example.item

import androidx.annotation.ColorRes

data class UpdatableItem(@ColorRes private val colorRes: Int, private val index: Int) : SmallCardItem(colorRes, index.toString()) {

    override fun getId() = index.toLong()

}
