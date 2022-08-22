package com.jcarrasco96.socialnet.services

import com.jcarrasco96.socialnet.models.*
import com.jcarrasco96.socialnet.models.json.ChangePasswordJson
import com.jcarrasco96.socialnet.models.json.UserJson
import com.jcarrasco96.socialnet.models.json.VerifyJson
import com.jcarrasco96.socialnet.models.json.WalletJson
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    @GET("user/current/")
    fun current(@Header("Authorization") token: String): Call<User>

    @Multipart
    @POST("user/avatar/")
    fun avatar(
        @Header("Authorization") token: String, @Part avatar: MultipartBody.Part
    ): Call<AvatarResult>

    @POST("user/change-password/")
    fun changePassword(
        @Header("Authorization") token: String, @Body changePasswordJson: ChangePasswordJson
    ): Call<ApiError>

    @POST("user/send-code/")
    fun sendCode(
        @Header("Authorization") token: String
    ): Call<ApiError>

    @POST("user/verify/")
    fun verify(
        @Header("Authorization") token: String, @Body verifyJson: VerifyJson
    ): Call<ApiError>

    @POST("user/wallet/")
    fun wallet(
        @Header("Authorization") token: String, @Body walletJson: WalletJson
    ): Call<ApiError>

    @PUT("user/")
    fun update(
        @Header("Authorization") token: String, @Body userJson: UserJson
    ): Call<ApiError>

}
