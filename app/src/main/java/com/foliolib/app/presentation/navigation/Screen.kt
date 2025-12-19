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

    data object AddBook : Screen("add_book")
    data object ManualEntry : Screen("manual_entry")
    data object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: String) = "edit_book/$bookId"
    }
    data object ScanIsbn : Screen("scan_isbn")

    data object ReadingSession : Screen("reading/{bookId}") {
        fun createRoute(bookId: String) = "reading/$bookId"
    }

    data object Shelves : Screen("shelves")

    data object ShelfDetail : Screen("shelf/{shelfId}") {
        fun createRoute(shelfId: String) = "shelf/$shelfId"
    }

    data object CreateShelf : Screen("create_shelf")
    data object EditShelf : Screen("edit_shelf/{shelfId}") {
        fun createRoute(shelfId: String) = "edit_shelf/$shelfId"
    }

    data object NotesScreen : Screen("notes/{bookId}") {
        fun createRoute(bookId: String) = "notes/$bookId"
    }

    data object Goals : Screen("goals")
    data object CreateGoal : Screen("create_goal")

    data object Settings : Screen("settings")
    data object Onboarding : Screen("onboarding")
}
