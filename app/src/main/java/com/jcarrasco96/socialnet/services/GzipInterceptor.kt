package com.jcarrasco96.socialnet.services

import com.jcarrasco96.socialnet.utils.Preferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.GzipSource
import okio.buffer

class GzipInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest: Request = chain.request()
            .newBuilder()
            .addHeader("Accept-Encoding", "gzip")
            .build()
        val response = chain.proceed(newRequest)

        Preferences.bytesReceived = response.body!!.contentLength()

        return if (isGzipped(response)) {
            unzip(response)
        } else {
            response
        }
    }

    private fun unzip(response: Response): Response {
        if (response.body == null) {
            return response
        }
        val gzipSource = GzipSource(response.body!!.source())
        val bodyString = gzipSource.buffer()
            .readUtf8()
        val responseBody: ResponseBody = bodyString.toResponseBody(response.body!!.contentType())
        val strippedHeaders = response.headers.newBuilder()
            .removeAll("Content-Encoding")
            .removeAll("Content-Length")
            .build()
        return response.newBuilder()
            .headers(strippedHeaders)
            .body(responseBody)
            .message(response.message)
            .build()
    }

    private fun isGzipped(response: Response): Boolean {
        return response.header("Content-Encoding") != null && response.header("Content-Encoding")
            .equals("gzip")
    }

}
