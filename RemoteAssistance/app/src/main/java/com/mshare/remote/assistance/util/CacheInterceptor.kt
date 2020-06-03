package com.mshare.remote.assistance.util

import android.content.Context
import com.mshare.remote.assistance.Constants
import okhttp3.Interceptor
import okhttp3.Response

class CacheInterceptor(context: Context): Interceptor {
    private val mContext = context
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if(Constants.isNetConnected(mContext)) {
            val maxAge = 360
            return response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        }
        return response
    }
}