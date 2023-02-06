package com.advice.schedule.models.firebase

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FirebaseSpeaker(
    val id: Long = -1,
    val name: String = "",
    val description: String = "",
    val link: String = "",
    val twitter: String = "",
    val title: String = "",

    val conference: String? = null,
    val updated_at: String? = null,

    val hidden: Boolean = false
) : Parcelable