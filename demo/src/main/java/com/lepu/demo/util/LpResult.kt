package com.lepu.demo.util

sealed class LpResult<out T> {
    data class Success<out T>(val value: T) : LpResult<T>()

    data class Failure(val throwable: Throwable?) : LpResult<Nothing>()
}

inline fun <reified T> LpResult<T>.doSuccess(success: (T) -> Unit) {
    if (this is LpResult.Success) {
        success(value)
    }
}

inline fun <reified T> LpResult<T>.doFailure(failure: (Throwable?) -> Unit) {
    if (this is LpResult.Failure) {
        failure(throwable)
    }
}
