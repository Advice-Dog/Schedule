package com.advice.schedule.ui.events

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advice.schedule.models.local.Action
import com.advice.schedule.models.local.Speaker
import com.advice.schedule.ui.information.speakers.SpeakerViewHolder
import com.advice.schedule.ui.search.HeaderViewHolder

class EventDetailsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ACTION = 1
    }

    private val collection = ArrayList<Any>()

    override fun getItemViewType(position: Int): Int {
        return when (collection[position]) {
            is String -> TYPE_HEADER
            is Action -> TYPE_ACTION
            else -> error("Unknown view type: ${collection[position].javaClass.simpleName}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder.inflate(parent)
            TYPE_ACTION -> ActionViewHolder.inflate(parent)
            else -> error("Unknown view type: $viewType")
        }
    }

    override fun getItemCount() = collection.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.render(collection[position] as String)
            is ActionViewHolder -> holder.render(collection[position] as Action)
        }
    }

    fun getSpanSize(position: Int, span: Int): Int {
        if (position == 0 && span == 1)
            return span

        return when (collection[position]) {
            is String -> span
            is Action -> (collection[position] as Action).getSpanCount(span)
            is Speaker -> span
            else -> error("Unknown view type: ${collection[position].javaClass.simpleName}")
        }
    }

    fun setElements(actions: List<Action>, speakers: List<Speaker>) {
        collection.clear()
        if (actions.isNotEmpty()) {
//            collection.add("Links")
            collection.addAll(actions)
        }

        if (speakers.isNotEmpty()) {
//            collection.add("Speakers")
            collection.addAll(speakers)
        }
        notifyDataSetChanged()
    }
}