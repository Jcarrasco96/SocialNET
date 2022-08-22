package com.jcarrasco96.socialnet.models

import com.google.gson.Gson
import okhttp3.ResponseBody
import java.io.IOException

class ApiError(val status: Int, val message: String, val trace: String?) {

    companion object {

        fun fromResponseBody(responseBody: ResponseBody?): ApiError? {
//            if (responseBody != null && responseBody.contentType()?.subtype == "json") {
            if (responseBody != null) {
                try {
                    return Gson().fromJson(responseBody.string(), ApiError::class.java)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return null
        }

    }

}
