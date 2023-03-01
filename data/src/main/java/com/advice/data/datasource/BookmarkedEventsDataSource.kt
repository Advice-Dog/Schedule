package com.advice.data.datasource

import com.advice.core.local.Bookmark
import com.advice.data.DataSource


interface BookmarkedEventsDataSource : DataSource<Bookmark> {

    suspend fun bookmark(id: Long, isBookmarked: Boolean)
}

