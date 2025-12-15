package com.foliolib.app.data.remote.api

import com.foliolib.app.data.remote.dto.OpenLibrarySearchResponse
import com.foliolib.app.data.remote.dto.OpenLibraryWorkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 40
    ): Response<OpenLibrarySearchResponse>

    @GET("search.json")
    suspend fun searchByIsbn(
        @Query("isbn") isbn: String
    ): Response<OpenLibrarySearchResponse>

    @GET("works/{workId}.json")
    suspend fun getWork(
        @Path("workId") workId: String
    ): Response<OpenLibraryWorkResponse>
}
