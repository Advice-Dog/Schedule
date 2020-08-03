package com.shortstack.hackertracker.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shortstack.hackertracker.R
import com.shortstack.hackertracker.models.local.Type
import kotlinx.android.synthetic.main.view_filter.view.*

class FilterView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    companion object {
        private const val SPAN_COUNT = 2
    }

    private val adapter = FilterAdapter()


    init {
        View.inflate(context, R.layout.view_filter, this)
        list.adapter = adapter
        list.layoutManager =
            GridLayoutManager(context, SPAN_COUNT, RecyclerView.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (adapter.getItemViewType(position)) {
                            FilterAdapter.TYPE_HEADER -> SPAN_COUNT
                            else -> 1
                        }
                    }
                }
            }
    }

    fun setTypes(types: List<Type>?) {
        if (types != null) {
            val collection = ArrayList<Any>()

            types.find { it.isBookmark }?.let {
                collection.add(it)
            }

            collection.add(context.getString(R.string.types))

            val elements = types.filter { !it.isBookmark && !it.isVillage && !it.isWorkshop }
                .sortedBy { it.name }
            collection.addAll(elements)

            collection.add(context.getString(R.string.villages))

            val villages = types.filter { it.isVillage }.sortedBy { it.name }
            collection.addAll(villages)

            collection.add(context.getString(R.string.workshops))

            val workshops = types.filter { it.isWorkshop }.sortedBy { it.name }
            collection.addAll(workshops)

            adapter.setElements(collection)
        }
    }
}
