package com.foliolib.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IsbnDbResponse(
    @Json(name = "book") val book: IsbnDbBook? = null
)

@JsonClass(generateAdapter = true)
data class IsbnDbBook(
    @Json(name = "title") val title: String,
    @Json(name = "title_long") val titleLong: String? = null,
    @Json(name = "isbn") val isbn: String? = null,
    @Json(name = "isbn13") val isbn13: String? = null,
    @Json(name = "authors") val authors: List<String>? = null,
    @Json(name = "publisher") val publisher: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "date_published") val datePublished: String? = null,
    @Json(name = "pages") val pages: Int? = null,
    @Json(name = "subjects") val subjects: List<String>? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "image") val image: String? = null
)
