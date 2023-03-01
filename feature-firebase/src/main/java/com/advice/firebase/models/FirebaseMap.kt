package com.advice.firebase.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FirebaseMap(
    val name: String = "",
    val file: String = "",
    val description: String? = null
) : Parcelable