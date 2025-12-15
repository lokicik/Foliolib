package com.foliolib.app.data.remote.api

import com.foliolib.app.data.remote.dto.GoogleBooksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,
        @Query("key") apiKey: String? = null
    ): Response<GoogleBooksResponse>

    @GET("volumes")
    suspend fun searchByIsbn(
        @Query("q") isbn: String, // Format: "isbn:1234567890"
        @Query("key") apiKey: String? = null
    ): Response<GoogleBooksResponse>

    @GET("volumes/{volumeId}")
    suspend fun getBookById(
        @retrofit2.http.Path("volumeId") volumeId: String,
        @Query("key") apiKey: String? = null
    ): Response<com.foliolib.app.data.remote.dto.GoogleBookItem>
}
