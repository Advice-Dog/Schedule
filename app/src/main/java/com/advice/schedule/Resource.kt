package com.advice.schedule

import com.advice.core.utils.Response

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

        fun <T> error(msg: String, data: T?) = Resource(Status.ERROR, data, msg)

        fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)

        fun <T> init(data: T? = null) = Resource(Status.NOT_INITIALIZED, data, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    NOT_INITIALIZED
}
val Response<*>.dObj: Any?
    get() {
        if (this is Response.Success) {
            return this.data
        }
        return null
    }