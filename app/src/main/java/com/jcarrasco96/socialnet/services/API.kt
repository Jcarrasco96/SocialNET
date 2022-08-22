package com.jcarrasco96.socialnet.services

import com.jcarrasco96.socialnet.BuildConfig
import com.jcarrasco96.socialnet.models.*
import com.jcarrasco96.socialnet.models.json.*
import com.jcarrasco96.socialnet.utils.Constants
import com.jcarrasco96.socialnet.utils.Preferences
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object API {

    private fun service(): Retrofit {
        val client = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(loggingInterceptor)
        }

        client.addInterceptor(GzipInterceptor())
        client.addNetworkInterceptor {
            if (it.request().body != null) {
                Preferences.bytesSend = it.request().body!!.contentLength()
            }
            it.proceed(it.request())
        }

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
    }

    private fun authService(): AuthService {
        return service().create(AuthService::class.java)
    }

    private fun postsService(): PostsService {
        return service().create(PostsService::class.java)
    }

    private fun usersService(): UserService {
        return service().create(UserService::class.java)
    }

    // AUTH
    fun authLogin(username: String, password: String, callback: Callback<LoginResult>) {
        authService().login(LoginJson(username, password)).enqueue(callback)
    }

    fun authRegister(
        username: String, password: String, password2: String, email: String,
        callback: Callback<ApiError>
    ) {
        authService().register(RegisterJson(username, password, password2, email)).enqueue(callback)
    }

    // POSTS
    fun postsIndex(page: Int, callback: Callback<Posts>) {
        postsService().index("Bearer " + Preferences.token(), page, Constants.PAGE_SIZE_POSTS)
            .enqueue(callback)
    }

    fun postsCreate(content: String, callback: Callback<Post>) {
        postsService().create("Bearer " + Preferences.token(), PostJson(content)).enqueue(callback)
    }

    fun postsDelete(id: Int, callback: Callback<ApiError>) {
        postsService().delete("Bearer " + Preferences.token(), id).enqueue(callback)
    }

    fun postsOwner(page: Int, callback: Callback<Posts>) {
        postsService().owner("Bearer " + Preferences.token(), page, Constants.PAGE_SIZE_POSTS)
            .enqueue(callback)
    }

    // USER
    fun userSendCode(callback: Callback<ApiError>) {
        usersService().sendCode("Bearer " + Preferences.token()).enqueue(callback)
    }

    fun userAvatar(filePart: MultipartBody.Part, callback: Callback<AvatarResult>) {
        usersService().avatar("Bearer " + Preferences.token(), filePart).enqueue(callback)
    }

    fun userWallet(wallet: String, callback: Callback<ApiError>) {
        usersService().wallet("Bearer " + Preferences.token(), WalletJson(wallet)).enqueue(callback)
    }

    fun userUpdate(userJson: UserJson, callback: Callback<ApiError>) {
        usersService().update("Bearer " + Preferences.token(), userJson).enqueue(callback)
    }

    fun userChangePassword(
        oldPassword: String, password: String, password2: String, callback: Callback<ApiError>
    ) {
        usersService().changePassword(
            "Bearer " + Preferences.token(), ChangePasswordJson(oldPassword, password, password2)
        ).enqueue(callback)
    }

    fun userCurrent(callback: Callback<User>) {
        usersService().current("Bearer " + Preferences.token()).enqueue(callback)
    }

    fun userVerify(code: Int, callback: Callback<ApiError>) {
        usersService().verify("Bearer " + Preferences.token(), VerifyJson(code)).enqueue(callback)
    }

}
