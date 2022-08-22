package com.jcarrasco96.socialnet.services

import com.jcarrasco96.socialnet.models.ApiError
import com.jcarrasco96.socialnet.models.Post
import com.jcarrasco96.socialnet.models.json.PostJson
import com.jcarrasco96.socialnet.models.Posts
import retrofit2.Call
import retrofit2.http.*

interface PostsService {

    @GET("posts")
    fun index(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Call<Posts>

    @GET("posts/owner/")
    fun owner(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Call<Posts>

    @GET("posts/{id}/")
    fun view(
        @Header("Authorization") token: String, @Path("id") id: Int
    ): Call<Post>

    @POST("posts/")
    fun create(
        @Header("Authorization") token: String, @Body body: PostJson
    ): Call<Post>

    @DELETE("posts/{id}/")
    fun delete(
        @Header("Authorization") token: String, @Path("id") id: Int
    ): Call<ApiError>

}
