package com.foliolib.app.data.remote.api

import com.foliolib.app.data.remote.dto.IsbnDbResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface IsbnDbApi {

    @GET("book/{isbn}")
    suspend fun getBookByIsbn(
        @Path("isbn") isbn: String,
        @Header("Authorization") apiKey: String
    ): Response<IsbnDbResponse>
}
