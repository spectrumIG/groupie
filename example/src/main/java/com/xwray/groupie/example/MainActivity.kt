package com.xwray.groupie.example

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.xwray.groupie.*
import com.xwray.groupie.example.core.InfiniteScrollListener
import com.xwray.groupie.example.core.Prefs
import com.xwray.groupie.example.core.SettingsActivity
import com.xwray.groupie.example.core.decoration.CarouselItemDecoration
import com.xwray.groupie.example.core.decoration.DebugItemDecoration
import com.xwray.groupie.example.core.decoration.SwipeTouchCallback
import com.xwray.groupie.example.item.*
import com.xwray.groupie.groupiex.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

val INSET_TYPE_KEY = "inset_type"
val INSET = "inset"

class MainActivity : AppCompatActivity() {

    private val groupAdapter = GroupAdapter<ViewHolder>() //TODO get rid of this parameter
    private lateinit var groupLayoutManager: GridLayoutManager
    private val prefs: Prefs by lazy { Prefs.get(this) }
    private val handler = Handler()

    private val gray: Int by lazy { ContextCompat.getColor(this, R.color.background) }
    private val betweenPadding: Int by lazy { resources.getDimensionPixelSize(R.dimen.padding_small) }
    private val rainbow200: IntArray by lazy { resources.getIntArray(R.array.rainbow_200) }
    private val rainbow500: IntArray by lazy { resources.getIntArray(R.array.rainbow_500) }

    private val infiniteLoadingSection = Section(HeaderItem(R.string.infinite_loading))
    private val swipeSection = Section(HeaderItem(R.string.swipe_to_delete))

    // Normally there's no need to hold onto a reference to this list, but for demonstration
    // purposes, we'll shuffle this list and post an update periodically
    private val updatableItems: ArrayList<UpdatableItem> by lazy {
        ArrayList<UpdatableItem>().apply {
            for (i in 1..12) {
                add(UpdatableItem(rainbow200[i], i))
            }
        }
    }

    // Hold a reference to the updating group, so we can, well, update it
    private var updatingGroup = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        groupAdapter.apply {
            setOnItemClickListener(onItemClickListener)
            setOnItemLongClickListener(onItemLongClickListener)
            spanCount = 12
        }

        populateAdapter()
        groupLayoutManager = GridLayoutManager(this, groupAdapter.spanCount).apply {
            spanSizeLookup = groupAdapter.spanSizeLookup
        }

        recyclerView.apply {
            layoutManager = groupLayoutManager
            addItemDecoration(HeaderItemDecoration(gray, betweenPadding))
            addItemDecoration(InsetItemDecoration(gray, betweenPadding))
            addItemDecoration(DebugItemDecoration(context))
            adapter = groupAdapter
            addOnScrollListener(object : InfiniteScrollListener(groupLayoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    val color = rainbow200[currentPage % rainbow200.size]
                    for (i in 0..4) {
                        infiniteLoadingSection.add(CardItem(color))
                    }
                }
            })
        }

        ItemTouchHelper(touchCallback).attachToRecyclerView(recyclerView)

        fab.setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }

        prefs.registerListener(onSharedPrefChangeListener)

    }

    private fun populateAdapter() {

        // Full bleed item
        groupAdapter += Section(HeaderItem(R.string.full_bleed_item)).apply {
            add(FullBleedCardItem(R.color.purple_200))
        }

        // Update in place group
        groupAdapter += Section().apply {
            val updatingHeader = HeaderItem(
                    R.string.updating_group,
                    R.string.updating_group_subtitle,
                    R.drawable.shuffle,
                    onShuffleClicked)
            setHeader(updatingHeader)

            updatingGroup.update(updatableItems)
            add(updatingGroup)
        }

        // Expandable group
        val expandableHeaderItem = ExpandableHeaderItem(R.string.expanding_group, R.string.expanding_group_subtitle)
        groupAdapter += ExpandableGroup(expandableHeaderItem).apply {
            for (i in 0..1) {
                add(CardItem(rainbow200[1]))
            }
        }

        // Columns
        groupAdapter += Section(HeaderItem(R.string.vertical_columns)).apply {
            add(makeColumnGroup())
        }

        // Group showing even spacing with multiple columns
        groupAdapter += Section(HeaderItem(R.string.multiple_columns)).apply {
            for (i in 0..11) {
                add(SmallCardItem(rainbow200[5]))
            }
        }

        // Swipe to delete (with add button in header)
        for (i in 0..2) {
            swipeSection += SwipeToDeleteItem(rainbow200[6])
        }
        groupAdapter += swipeSection

        // Horizontal carousel
        groupAdapter += Section(HeaderItem(R.string.carousel, R.string.carousel_subtitle)).apply {
            add(makeCarouselItem())
        }

        // Update with payload
        groupAdapter += Section(HeaderItem(R.string.update_with_payload, R.string.update_with_payload_subtitle)).apply {
            rainbow500.indices.forEach { i ->
                add(HeartCardItem(rainbow200[i], i.toLong(), { item, favorite ->
                    // Pretend to make a network request
                    handler.postDelayed({
                        // Network request was successful!
                        item.setFavorite(favorite)
                        item.notifyChanged(FAVORITE)
                    }, 1000)
                }))
            }
        }

        // Infinite loading section
        groupAdapter += infiniteLoadingSection
    }

    private fun makeColumnGroup(): ColumnGroup {
        val columnItems = ArrayList<ColumnItem>()
        for (i in 1..5) {
            // First five items are red -- they'll end up in a vertical column
            columnItems += ColumnItem(rainbow200[0], i)
        }
        for (i in 6..10) {
            // Next five items are pink
            columnItems += ColumnItem(rainbow200[1], i)
        }
        return ColumnGroup(columnItems)
    }

    private fun makeCarouselItem(): CarouselItem {
        val carouselDecoration = CarouselItemDecoration(gray, betweenPadding)
        val carouselAdapter = GroupAdapter<ViewHolder>()
        for (i in 0..29) {
            carouselAdapter += CarouselCardItem(rainbow200[7])
        }
        return CarouselItem(carouselDecoration, carouselAdapter)
    }

    private val onItemClickListener = OnItemClickListener { item, _ ->
        if (item is CardItem && !TextUtils.isEmpty(item.text)) {
            Toast.makeText(this@MainActivity, item.text, Toast.LENGTH_SHORT).show()
        }
    }

    private val onItemLongClickListener = OnItemLongClickListener { item, _ ->
        if (item is CardItem && !item.text.isNullOrBlank()) {
            Toast.makeText(this@MainActivity, "Long clicked: " + item.text, Toast.LENGTH_SHORT).show()
            return@OnItemLongClickListener true
        }
        false
    }

    private val onShuffleClicked = View.OnClickListener {
        ArrayList(updatableItems).apply {
            shuffle()
            updatingGroup.update(this)
        }

        // You can also do this by forcing a change with payload
        recyclerView.post { recyclerView.invalidateItemDecorations() }
    }

    override fun onDestroy() {
        prefs.unregisterListener(onSharedPrefChangeListener)
        super.onDestroy()
    }

    private val touchCallback: SwipeTouchCallback by lazy {
        object : SwipeTouchCallback(gray) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = groupAdapter.getItem(viewHolder.adapterPosition)
                // Change notification to the adapter happens automatically when the section is
                // changed.
                swipeSection.remove(item)
            }
        }
    }

    private val onSharedPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        // This is pretty evil, try not to do this
        groupAdapter.notifyDataSetChanged()
    }

}
