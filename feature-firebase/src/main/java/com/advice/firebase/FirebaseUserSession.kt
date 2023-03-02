package com.advice.firebase

import com.advice.core.local.Conference
import com.advice.core.local.User
import com.advice.data.UserSession
import com.advice.data.datasource.ConferencesDataSource
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseUserSession(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    conferencesDataSource: ConferencesDataSource,
) : UserSession {

    private val _user = MutableStateFlow<User?>(null)
    override var user: Flow<User?> = _user

    private val _conference = MutableStateFlow<Conference>(Conference.Zero)

    override var conference: Flow<Conference> = _conference

    override var isDeveloper: Boolean = false

    init {
        CoroutineScope(Job()).launch {
            conferencesDataSource.get().collect {
//                _conference.value = it.random()
                Timber.e("Conference is: ${_conference.value}")
            }
        }

        CoroutineScope(Job()).launch {
            val it = auth.signInAnonymously().await()
            val user = it.user
            Timber.e("User is now: $user")
            if (user != null) {
                _user.value = User(user.uid)
            }
        }
    }

    private fun getConference(preferred: Long, conferences: List<Conference>): Conference {
        if (preferred != -1L) {
            val pref = conferences.find { it.id == preferred && !it.hasFinished }
            if (pref != null) return pref
        }

        val list = conferences.sortedBy { it.startDate }

        val defcon = list.find { it.code == "DEFCON30" }
        if (defcon?.hasFinished == false) {
            return defcon
        }

        return list.firstOrNull { !it.hasFinished } ?: conferences.last()
    }

    override fun setConference(conference: Conference) {
        _conference.value = conference
    }

    override val currentConference: Conference
        get() = _conference.value

    override val currentUser: User?
        get() = _user.value
}

fun Task<AuthResult>.snapshotFlow(): Flow<AuthResult> = callbackFlow {
    val listenerRegistration = addOnCompleteListener {
        if (!it.isSuccessful) {
            close()
            return@addOnCompleteListener
        }
        if (it.result != null)
            trySend(it.result)
    }
    awaitClose {
        listenerRegistration
    }
}