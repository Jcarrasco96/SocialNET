package com.jcarrasco96.socialnet.services

import com.jcarrasco96.socialnet.models.ApiError
import com.jcarrasco96.socialnet.models.json.LoginJson
import com.jcarrasco96.socialnet.models.LoginResult
import com.jcarrasco96.socialnet.models.json.RegisterJson
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("auth/login")
    fun login(@Body loginJson: LoginJson): Call<LoginResult>

    @POST("auth/register/")
    fun register(@Body registerJson: RegisterJson): Call<ApiError>

}
