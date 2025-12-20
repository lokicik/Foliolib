package com.foliolib.app.presentation.navigation

sealed class Screen(val route: String) {
    // Bottom navigation destinations
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Search : Screen("search")
    data object Statistics : Screen("statistics")
    data object Profile : Screen("profile")

    // Detail screens
    data object BookDetail : Screen("book/{bookId}") {
        fun createRoute(bookId: String) = "book/$bookId"
    }

    data object ManualEntry : Screen("manual_entry")
    data object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: String) = "edit_book/$bookId"
    }

    data object ReadingSession : Screen("reading/{bookId}") {
        fun createRoute(bookId: String) = "reading/$bookId"
    }

    data object ReadingHistory : Screen("history/{bookId}") {
        fun createRoute(bookId: String) = "history/$bookId"
    }

    data object NotesScreen : Screen("notes/{bookId}") {
        fun createRoute(bookId: String) = "notes/$bookId"
    }

    data object Settings : Screen("settings")
}
