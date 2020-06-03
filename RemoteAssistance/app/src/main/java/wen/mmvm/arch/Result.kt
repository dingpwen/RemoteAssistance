package wen.mmvm.arch

import java.lang.Exception

sealed class Result<out T: Any> {
    data class Success<out T: Any>(val data:T):Result<T>()
    data class Loading<out T: Any>(val message:String, val data:T?):Result<T>()
    data class Error<out T: Any>(val exception:Exception, val data:T):Result<T>()
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error<*> -> "Error[exception=$exception]"
            is Loading<*> -> "Loading[message=$message]"
        }
    }
}