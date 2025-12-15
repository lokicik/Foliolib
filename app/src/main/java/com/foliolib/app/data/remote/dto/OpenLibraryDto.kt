package com.foliolib.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenLibrarySearchResponse(
    @Json(name = "docs") val docs: List<OpenLibraryDoc>? = null,
    @Json(name = "numFound") val numFound: Int = 0
)

@JsonClass(generateAdapter = true)
data class OpenLibraryDoc(
    @Json(name = "key") val key: String,
    @Json(name = "title") val title: String,
    @Json(name = "author_name") val authorName: List<String>? = null,
    @Json(name = "isbn") val isbn: List<String>? = null,
    @Json(name = "publisher") val publisher: List<String>? = null,
    @Json(name = "publish_date") val publishDate: List<String>? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null,
    @Json(name = "number_of_pages_median") val numberOfPagesMedian: Int? = null,
    @Json(name = "subject") val subject: List<String>? = null,
    @Json(name = "cover_i") val coverId: Long? = null,
    @Json(name = "language") val language: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class OpenLibraryWorkResponse(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: Any? = null, // Can be String or Object
    @Json(name = "authors") val authors: List<OpenLibraryAuthorRef>? = null,
    @Json(name = "subjects") val subjects: List<String>? = null,
    @Json(name = "covers") val covers: List<Long>? = null
)

@JsonClass(generateAdapter = true)
data class OpenLibraryAuthorRef(
    @Json(name = "author") val author: OpenLibraryAuthorKey
)

@JsonClass(generateAdapter = true)
data class OpenLibraryAuthorKey(
    @Json(name = "key") val key: String
)
