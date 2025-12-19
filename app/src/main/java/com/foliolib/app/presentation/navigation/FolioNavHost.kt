package com.foliolib.app.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foliolib.app.presentation.screen.addbook.ManualEntryScreen
import com.foliolib.app.presentation.screen.editbook.EditBookScreen
import com.foliolib.app.presentation.screen.bookdetail.BookDetailScreen
import com.foliolib.app.presentation.screen.home.HomeScreen
import com.foliolib.app.presentation.screen.library.LibraryScreen
import com.foliolib.app.presentation.screen.notes.NotesScreen
import com.foliolib.app.presentation.screen.reading.ReadingScreen
import com.foliolib.app.presentation.screen.search.SearchScreen
import com.foliolib.app.presentation.screen.settings.SettingsScreen
import com.foliolib.app.presentation.screen.shelves.ShelfDetailScreen
import com.foliolib.app.presentation.screen.shelves.ShelvesScreen
import com.foliolib.app.presentation.screen.statistics.StatisticsScreen

@Composable
fun FolioApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show bottom bar
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelResId)
                                )
                            },
                            label = { 
                                Text(
                                    text = stringResource(item.labelResId),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        FolioNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun FolioNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Bottom navigation screens
        composable(Screen.Home.route) {
            HomeScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToManualEntry = {
                    navController.navigate(Screen.ManualEntry.route)
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }

        composable(Screen.Profile.route) {
            SettingsScreen()
        }

        // Detail screens
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            BookDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartReading = { bookId ->
                    navController.navigate(Screen.ReadingSession.createRoute(bookId))
                },
                onViewNotes = { bookId ->
                    navController.navigate(Screen.NotesScreen.createRoute(bookId))
                },
                onEditBook = { bookId ->
                    navController.navigate(Screen.EditBook.createRoute(bookId))
                }
            )
        }

        composable(Screen.ManualEntry.route) {
            ManualEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookAdded = {
                    navController.popBackStack()
                    navController.navigate(Screen.Library.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(
            route = Screen.EditBook.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            EditBookScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReadingSession.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            ReadingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NotesScreen.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            NotesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Shelves.route) {
            ShelvesScreen(
                onShelfClick = { shelfId ->
                    navController.navigate(Screen.ShelfDetail.createRoute(shelfId))
                }
            )
        }

        composable(
            route = Screen.ShelfDetail.route,
            arguments = listOf(navArgument("shelfId") { type = NavType.StringType })
        ) {
            ShelfDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                }
            )
        }
    }
}

// Placeholder composable for screens not yet implemented
@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
