package com.advice.schedule.ui.information.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.schedule.Response
import com.advice.schedule.database.DatabaseManager
import com.advice.schedule.models.local.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class LocationsViewModel : ViewModel(), KoinComponent {

    private val database: DatabaseManager by inject()

    private var _locations = listOf<LocationContainer>()
    private val locations = MediatorLiveData<Response<List<LocationContainer>>>()

    init {
        locations.addSource(database.conference) {
            if (it == null) {
                locations.value = Response.Init
            } else {
                locations.value = Response.Loading
                locations.addSource(database.getLocations(it)) {
                    val list = it.sortedWith(compareBy({ it.hier_extent_left }, { it.hier_extent_right }))
                    // need to populate the list first
                    _locations = list.map { element -> element.toContainer(list.any { it.parent_id == element.id }) }
                    // then update the status
                    locations.value = Response.Success(updateLocations())
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                Timber.d(("Updating location list"))
                delay(LOCATION_UPDATE_DELAY)
                val data = updateLocations()
                _locations = data
                locations.value = Response.Success(data)
            }
        }
    }

    private fun updateLocations(): List<LocationContainer> {
        val list = _locations

        return list.map { location ->
            val children = location.getChildren()

            val status = if (children.isEmpty()) {
                location.getCurrentStatus()
            } else {
                // updating all children
                children.forEach {
                    it.setStatus(it.getCurrentStatus())
                }

                when {
                    children.all { it.status == LocationStatus.Open } -> LocationStatus.Open
                    children.all { it.status == LocationStatus.Closed } -> LocationStatus.Closed
                    children.all { it.status == LocationStatus.Unknown } -> LocationStatus.Unknown
                    else -> LocationStatus.Mixed
                }
            }
            location.setStatus(status)
        }
    }

    fun toggle(location: LocationContainer) {
        val list = _locations.toMutableList()

        val indexOf = list.indexOf(location)
        val isExpanded = !list[indexOf].isChildrenExpanded
        list[indexOf] = location.isChildrenExpanded(isExpanded)

        val children = location.getChildren()
        for (child in children) {
            list[list.indexOf(child)] = child
                .isExpanded(isExpanded = isExpanded)
                .isChildrenExpanded(isExpanded = isExpanded)
        }

        _locations = list
        locations.value = Response.Success(list)
    }

    private fun LocationContainer.getChildren(): List<LocationContainer> {
        val list = _locations

        var index = list.indexOf(this)
        if (index == -1)
            return emptyList()

        val result = mutableListOf<LocationContainer>()

        while (++index < list.size && list[index].depth > depth) {
            result.add(list[index])
        }
        return result
    }

    fun getLocations(): LiveData<Response<List<LocationContainer>>> = locations

    companion object {
        private const val LOCATION_UPDATE_DELAY = 30_000L
    }
}

fun Location.toContainer(hasChildren: Boolean = false): LocationContainer {
    return LocationContainer(id, name, shortName, default_status, hier_depth, schedule ?: emptyList())
        .hasChildren(hasChildren)
}

// todo: refactor LocationsViewModel to store the raw Locations instead of recreating this item when clicked.
fun LocationContainer.toLocation(): Location {
    return Location(id, title, shortTitle, null, "", defaultStatus, depth,  - 1, -1, -1, -1, null)
}