package com.shortstack.hackertracker.models.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Type(
    val id: Int,
    private val _name: String,
    val conference: String,
    private val _color: String,
    val description: String,
    val actions: List<Action>,
    var isSelected: Boolean = false
) : Parcelable {

    val shortName: String
        get() = _name.replace(" Vlg", "")

    val fullName: String
        get() = _name.replace(" Vlg", " Village")

    val color: String
        get() = if (isVillage || isWorkshop) "#FFFFFF" else _color

    val isBookmark: Boolean
        get() = _name.contains("bookmark", true)

    val isVillage: Boolean
        get() = _name.endsWith(" Vlg", true)

    val isWorkshop: Boolean
        get() = _name.endsWith(" Workshop", true)

}