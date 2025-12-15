package com.foliolib.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleBooksResponse(
    @Json(name = "items") val items: List<GoogleBookItem>? = null,
    @Json(name = "totalItems") val totalItems: Int = 0
)

@JsonClass(generateAdapter = true)
data class GoogleBookItem(
    @Json(name = "id") val id: String,
    @Json(name = "volumeInfo") val volumeInfo: GoogleVolumeInfo
)

@JsonClass(generateAdapter = true)
data class GoogleVolumeInfo(
    @Json(name = "title") val title: String,
    @Json(name = "authors") val authors: List<String>? = null,
    @Json(name = "publisher") val publisher: String? = null,
    @Json(name = "publishedDate") val publishedDate: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "industryIdentifiers") val industryIdentifiers: List<GoogleIndustryIdentifier>? = null,
    @Json(name = "pageCount") val pageCount: Int? = null,
    @Json(name = "categories") val categories: List<String>? = null,
    @Json(name = "imageLinks") val imageLinks: GoogleImageLinks? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "averageRating") val averageRating: Float? = null
)

@JsonClass(generateAdapter = true)
data class GoogleIndustryIdentifier(
    @Json(name = "type") val type: String, // ISBN_10, ISBN_13
    @Json(name = "identifier") val identifier: String
)

@JsonClass(generateAdapter = true)
data class GoogleImageLinks(
    @Json(name = "smallThumbnail") val smallThumbnail: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "small") val small: String? = null,
    @Json(name = "medium") val medium: String? = null,
    @Json(name = "large") val large: String? = null
)
