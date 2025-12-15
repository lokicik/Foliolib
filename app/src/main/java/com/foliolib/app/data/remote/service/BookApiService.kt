package com.foliolib.app.data.remote.service

import com.foliolib.app.BuildConfig
import com.foliolib.app.data.remote.api.GoogleBooksApi
import com.foliolib.app.data.remote.api.IsbnDbApi
import com.foliolib.app.data.remote.api.OpenLibraryApi
import com.foliolib.app.data.remote.dto.GoogleBookItem
import com.foliolib.app.data.remote.dto.GoogleBooksResponse
import com.foliolib.app.data.remote.dto.OpenLibraryDoc
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookApiService @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val openLibraryApi: OpenLibraryApi,
    private val isbnDbApi: IsbnDbApi
) {

    /**
     * Search books using multi-source fallback
     * Primary: Google Books
     * Fallback: Open Library
     */
    suspend fun searchBooks(query: String): Result<List<GoogleBookItem>> {
        // Try Google Books first
        return try {
            Timber.d("Searching books with Google Books: $query")
            val response = googleBooksApi.searchBooks(
                query = query,
                apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY.takeIf { it.isNotBlank() }
            )

            if (response.isSuccessful && response.body()?.items != null) {
                Timber.d("Google Books returned ${response.body()?.items?.size} results")
                Result.success(response.body()!!.items!!)
            } else {
                Timber.w("Google Books failed, falling back to Open Library")
                searchBooksWithOpenLibrary(query)
            }
        } catch (e: Exception) {
            Timber.e(e, "Google Books error, falling back to Open Library")
            searchBooksWithOpenLibrary(query)
        }
    }

    private suspend fun searchBooksWithOpenLibrary(query: String): Result<List<GoogleBookItem>> {
        return try {
            Timber.d("Searching books with Open Library: $query")
            val response = openLibraryApi.searchBooks(query)

            if (response.isSuccessful && response.body()?.docs != null) {
                // Convert Open Library docs to Google Books format for consistency
                val items = response.body()!!.docs!!.map { it.toGoogleBookItem() }
                Timber.d("Open Library returned ${items.size} results")
                Result.success(items)
            } else {
                Result.failure(Exception("All book search APIs failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Open Library search failed")
            Result.failure(Exception("All book search APIs failed", e))
        }
    }

    /**
     * Get book by ISBN using multi-source fallback
     * Tries: Google Books → Open Library → ISBN DB
     */
    suspend fun getBookByIsbn(isbn: String): Result<GoogleBookItem> {
        // Try Google Books
        val googleResult = tryGoogleBooksIsbn(isbn)
        if (googleResult != null) return Result.success(googleResult)

        // Try Open Library
        val openLibraryResult = tryOpenLibraryIsbn(isbn)
        if (openLibraryResult != null) return Result.success(openLibraryResult)

        // Try ISBN DB
        val isbnDbResult = tryIsbnDb(isbn)
        if (isbnDbResult != null) return Result.success(isbnDbResult)

        return Result.failure(Exception("Book not found in any API"))
    }

    private suspend fun tryGoogleBooksIsbn(isbn: String): GoogleBookItem? {
        return try {
            Timber.d("Searching ISBN with Google Books: $isbn")
            val response = googleBooksApi.searchByIsbn(
                isbn = "isbn:$isbn",
                apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY.takeIf { it.isNotBlank() }
            )

            if (response.isSuccessful && response.body()?.items?.isNotEmpty() == true) {
                Timber.d("Google Books found book by ISBN")
                response.body()!!.items!!.first()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Google Books ISBN lookup failed")
            null
        }
    }

    private suspend fun tryOpenLibraryIsbn(isbn: String): GoogleBookItem? {
        return try {
            Timber.d("Searching ISBN with Open Library: $isbn")
            val response = openLibraryApi.searchByIsbn(isbn)

            if (response.isSuccessful && response.body()?.docs?.isNotEmpty() == true) {
                Timber.d("Open Library found book by ISBN")
                response.body()!!.docs!!.first().toGoogleBookItem()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Open Library ISBN lookup failed")
            null
        }
    }

    private suspend fun tryIsbnDb(isbn: String): GoogleBookItem? {
        return try {
            val apiKey = BuildConfig.ISBNDB_API_KEY
            if (apiKey.isBlank()) {
                Timber.w("ISBN DB API key not configured")
                return null
            }

            Timber.d("Searching ISBN with ISBN DB: $isbn")
            val response = isbnDbApi.getBookByIsbn(isbn, apiKey)

            if (response.isSuccessful && response.body()?.book != null) {
                Timber.d("ISBN DB found book by ISBN")
                response.body()!!.book!!.toGoogleBookItem()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "ISBN DB lookup failed")
            null
        }
    }
}

// Extension functions to convert between DTOs
private fun OpenLibraryDoc.toGoogleBookItem(): GoogleBookItem {
    return GoogleBookItem(
        id = key,
        volumeInfo = com.foliolib.app.data.remote.dto.GoogleVolumeInfo(
            title = title,
            authors = authorName,
            publisher = publisher?.firstOrNull(),
            publishedDate = publishDate?.firstOrNull() ?: firstPublishYear?.toString(),
            description = null,
            industryIdentifiers = isbn?.map {
                com.foliolib.app.data.remote.dto.GoogleIndustryIdentifier(
                    type = if (it.length == 10) "ISBN_10" else "ISBN_13",
                    identifier = it
                )
            },
            pageCount = numberOfPagesMedian,
            categories = subject,
            imageLinks = coverId?.let {
                com.foliolib.app.data.remote.dto.GoogleImageLinks(
                    thumbnail = "https://covers.openlibrary.org/b/id/$it-M.jpg",
                    small = "https://covers.openlibrary.org/b/id/$it-S.jpg",
                    medium = "https://covers.openlibrary.org/b/id/$it-M.jpg",
                    large = "https://covers.openlibrary.org/b/id/$it-L.jpg"
                )
            },
            language = language?.firstOrNull()
        )
    )
}

private fun com.foliolib.app.data.remote.dto.IsbnDbBook.toGoogleBookItem(): GoogleBookItem {
    return GoogleBookItem(
        id = isbn13 ?: isbn ?: "",
        volumeInfo = com.foliolib.app.data.remote.dto.GoogleVolumeInfo(
            title = title,
            authors = authors,
            publisher = publisher,
            publishedDate = datePublished,
            description = synopsis,
            industryIdentifiers = listOfNotNull(
                isbn?.let {
                    com.foliolib.app.data.remote.dto.GoogleIndustryIdentifier("ISBN_10", it)
                },
                isbn13?.let {
                    com.foliolib.app.data.remote.dto.GoogleIndustryIdentifier("ISBN_13", it)
                }
            ),
            pageCount = pages,
            categories = subjects,
            imageLinks = image?.let {
                com.foliolib.app.data.remote.dto.GoogleImageLinks(
                    thumbnail = it,
                    medium = it,
                    large = it
                )
            },
            language = language
        )
    )
}
