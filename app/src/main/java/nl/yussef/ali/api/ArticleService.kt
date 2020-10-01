package nl.yussef.ali.api

import nl.yussef.ali.model.User
import nl.yussef.ali.model.Result
import nl.yussef.ali.model.UserResponse
import nl.yussef.ali.model.UserResponseRegister
import retrofit2.Response
import retrofit2.http.*

interface ArticleService {
    @GET("Articles")
    suspend fun getArticles(@Header("x-authtoken") authToken: String?): Response<Result>

    @GET("Articles/{id}")
    suspend fun getNextArticles(
        @Path("id") id: Int,
        @Query("count") count: Int,
        @Header("x-authtoken") authToken: String?
    ): Response<Result>

    @GET("Articles/liked")
    suspend fun getLikedArticles(@Header("x-authtoken") authToken: String): Response<Result>

    @PUT("Articles/{id}/like")
    suspend fun likeAnArticle(
        @Path("id") id: Int,
        @Header("x-authtoken") authToken: String
    ): Response<Unit>

    @DELETE("Articles/{id}/like")
    suspend fun unlikeAnArticle(
        @Path("id") id: Int,
        @Header("x-authtoken") authToken: String
    ): Response<Unit>

    @Headers("Content-Type: application/json")
    @POST("Users/register")
    suspend fun createUser(@Body user: User): Response<UserResponseRegister>

    @Headers("Content-Type: application/json")
    @POST("Users/login")
    suspend fun login(@Body user: User): Response<UserResponse>
}