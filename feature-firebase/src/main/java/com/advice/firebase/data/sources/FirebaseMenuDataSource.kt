package com.advice.firebase.data.sources

import com.advice.core.local.Menu
import com.advice.core.local.NewsArticle
import com.advice.data.session.UserSession
import com.advice.data.sources.MenuDataSource
import com.advice.firebase.extensions.snapshotFlow
import com.advice.firebase.extensions.toMenu
import com.advice.firebase.extensions.toObjectsOrEmpty
import com.advice.firebase.models.FirebaseMenu
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.concatWith
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import timber.log.Timber

class FirebaseMenuDataSource(
    private val userSession: UserSession,
    private val firestore: FirebaseFirestore,
) : MenuDataSource {
    @OptIn(FlowPreview::class)
    override fun get(): Flow<List<Menu>> {
        // todo: investigate a better way to return an init state when the conference has changed.
        return (userSession.getConference().flatMapMerge { conference ->
            flowOf(emptyList<Menu>()).onCompletion {
                if (it == null) emitAll(firestore.collection("conferences")
                    .document(conference.code)
                    .collection("menus")
                    .snapshotFlow()
                    .map { querySnapshot ->
                        querySnapshot.toObjectsOrEmpty(FirebaseMenu::class.java)
                            .mapNotNull { it.toMenu() }
                    })
            }
        })
    }
}