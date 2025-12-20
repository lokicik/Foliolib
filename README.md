# Foliolib

A comprehensive personal library management application for Android that enables users to organize, track, and manage their book collections with advanced features including ISBN scanning, reading progress tracking, and detailed book metadata management.

## Overview

Foliolib is a native Android application designed to provide book enthusiasts with a complete digital library management solution. The application integrates with external book APIs for automatic metadata retrieval and provides offline-first functionality through local data persistence.

## Core Features

### Book Management
- Manual book entry with comprehensive metadata fields
- Automatic metadata retrieval from Google Books API and ISBNDB API
- Book editing capabilities for correcting or updating information
- Support for multiple languages and book conditions
- Cover image management with thumbnail and full-size image storage

### Reading Progress Tracking
- Reading status management (Not Started, Currently Reading, Finished)
- Current page tracking
- Reading session recording with timestamps
- Start and finish date tracking
- Reading history with detailed session logs

### Organization System
- Book categorization using publisher categories
- Advanced search and filtering capabilities

### Note-Taking
- Create and manage notes for individual books
- Persistent storage of reading annotations

### Statistics
- View comprehensive reading statistics
- Historical data analysis
- Progress visualization

### User Preferences
- Customizable application settings
- Theme and display preferences via DataStore
- Persistent user configuration

## Technical Architecture

### Technology Stack

#### Core Framework
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 36

#### Architecture Components
- **Dependency Injection**: Hilt
- **Local Database**: Room with KSP code generation
- **Navigation**: Navigation Compose
- **Asynchronous Programming**: Kotlin Coroutines
- **Lifecycle Management**: AndroidX Lifecycle and ViewModel

#### Data Layer
- **Database**: Room SQLite database with the following entities:
  - BookEntity (with indices on reading_status, title, date_added, isbn)
  - NoteEntity
  - ReadingSessionEntity
  - UserPreferencesEntity
- **Type Converters**: Custom converters for complex data types
- **Data Persistence**: DataStore Preferences for user settings

#### Network Layer
- **HTTP Client**: Retrofit with OkHttp
- **JSON Parsing**: Moshi with Kotlin code generation
- **Logging**: OkHttp Logging Interceptor and Timber
- **Image Loading**: Coil for Compose

#### UI Components
- **Permissions**: Accompanist Permissions
- **System UI Control**: Accompanist System UI Controller
- **Animations**: Lottie for Compose
- **Loading States**: Compose Shimmer effect
- **Icons**: Material Icons Extended

#### Background Processing
- **Work Scheduling**: WorkManager

#### Testing
- **Unit Testing**: JUnit, Kotlin Coroutines Test
- **Instrumentation Testing**: AndroidX Test, Espresso
- **Database Testing**: Room Testing
- **UI Testing**: Compose UI Test JUnit4

### Project Structure

```
com.foliolib.app/
├── core/
│   ├── di/              # Dependency injection modules
│   │   ├── AppModule
│   │   ├── DatabaseModule
│   │   ├── NetworkModule
│   │   └── RepositoryModule
│   └── util/            # Utility classes
│       └── HapticFeedbackUtil
├── data/
│   ├── local/
│   │   ├── dao/         # Room Data Access Objects
│   │   │   ├── BookDao
│   │   │   ├── NoteDao
│   │   │   ├── ReadingSessionDao
│   │   │   └── UserPreferencesDao
│   │   ├── database/
│   │   │   ├── FolioDatabase
│   │   │   └── Converters
│   │   └── entity/      # Room entities
│   └── remote/          # API services
├── domain/              # Business logic and use cases
└── presentation/
    ├── components/      # Reusable Compose components
    │   ├── book/
    │   ├── common/
    │   └── reading/
    ├── navigation/      # Navigation graph
    └── screen/          # Feature screens
        ├── home/
        ├── library/
        ├── addbook/
        ├── editbook/
        ├── bookdetail/
        ├── reading/
        ├── notes/
        ├── search/
        ├── statistics/
        ├── history/
        └── settings/
```

### Architecture Pattern

The application follows Clean Architecture principles with clear separation of concerns:

1. **Presentation Layer**: Jetpack Compose UI with ViewModels for state management
2. **Domain Layer**: Business logic and use cases
3. **Data Layer**: Repository pattern with Room database and Retrofit API clients

Data flow follows the unidirectional data flow pattern:
- UI emits events to ViewModels
- ViewModels process events through use cases
- Use cases interact with repositories
- Repositories coordinate between local and remote data sources
- State flows back to UI for rendering

## Configuration

### API Keys

The application requires API keys for external book metadata services. These should be added to your `local.properties` file:

```properties
GOOGLE_BOOKS_API_KEY=your_google_books_api_key
ISBNDB_API_KEY=your_isbndb_api_key
```

### Build Configuration

- **Java Version**: 11
- **Kotlin JVM Target**: 11
- **Build System**: Gradle with Kotlin DSL
- **Version Catalog**: Centralized dependency management using libs.versions.toml

## Permissions

The application requires the following Android permissions:
- **Internet**: For fetching book metadata from external APIs
- **Network State**: For checking connectivity status

## Database Schema

### Books Table
Primary entity with comprehensive book metadata including title, authors, ISBN variants, publisher information, page count, reading progress, status, ratings, and temporal data.

### Relationships
- Books to Notes: One-to-many
- Books to Reading Sessions: One-to-many

## Development

### Build Requirements
- Android Studio Hedgehog or newer
- JDK 11
- Android SDK 36
- Gradle 8.x

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test           # Unit tests
./gradlew connectedCheck # Instrumentation tests
```

## Version Information

- **Version Code**: 1
- **Version Name**: 1.0

## License

This project configuration and structure are available for reference and educational purposes.
