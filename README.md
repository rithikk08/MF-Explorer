# MFExplorer - Mutual Fund Tracking App

This project was built to satisfy the requirements of the Mutual Fund exploration platform assignment, prioritizing modern Android development practices, robust offline capabilities, and a smooth, declarative user interface.

## 📽️ Submission Materials
* **[Watch the Video Demo Here](https://drive.google.com/file/d/1AmmHD3skTWtzGijv3B3QW27zfAdBIPWx/view?usp=sharing)**
* **[Download the APK Here](https://drive.google.com/file/d/1FMaKbaaK2Nwtox_f9tdFde_9udcZ0dYI/view?usp=sharing)** 

## Architecture

The application follows **MVVM (Model-View-ViewModel)** combined with **Clean Architecture** principles. This ensures a highly decoupled, testable, and maintainable codebase.

The project is divided into distinct layers:
*   **Presentation Layer (`presentation` & `ui.theme`)**: Contains all Jetpack Compose UI components, Activities, and ViewModels. ViewModels expose UI state using Kotlin `StateFlow`, adhering to unidirectional data flow (UDF).
*   **Domain Layer (`domain`)**: The core business logic layer. Contains Data Models, Repository Interfaces, and Use Cases. It has no dependencies on the Android framework or the Data layer.
*   **Data Layer (`data`)**: Implements the repository interfaces. It serves as the single source of truth, managing data fetching from the remote API (`mfapi.in`), caching it locally via Room, and retrieving local data when offline.
*   **Dependency Injection (`di`)**: Configures Dagger Hilt modules to provide singletons and interface bindings across the app.

### Offline-First & Caching Strategy
The Data layer implements an offline-first strategy. For the Explore screen, responses from the network are parsed and explicitly saved to the local Room database (`ExploreCacheEntity`). If the network call fails or the user is completely offline, the app gracefully falls back to displaying this cached data alongside a network error UI state.

##  Libraries Used & Their Purpose

*   **Jetpack Compose (Material 3)**: The modern declarative UI toolkit used to build the entire presentation layer, including dynamic Light/Dark theme switching, BottomSheets, and infinite scrolling lists (`LazyColumn`).
*   **Kotlin Coroutines & Flow**: Used extensively for asynchronous programming, background threading, and reactive state management across all architectural layers.
*   **Dagger Hilt**: Simplifies Dependency Injection (DI) by providing compile-time correctness and reducing boilerplate code for injecting Repositories, DAOs, and APIs into ViewModels.
*   **Retrofit 2 & OkHttp**: A type-safe HTTP client used for handling all REST API communications with `https://api.mfapi.in/`. 
*   **Gson**: Used in conjunction with Retrofit for JSON serialization/deserialization, as well as storing complex API fallback states into the Room database.
*   **Room Database**: An SQLite object mapping library used for persistence. It manages the custom "Watchlist Folders", tracks which mutual funds belong to which folder using relational queries, and handles offline caching.
*   **Vico Charts** (`com.patrykandpatrick.vico`): A lightweight and highly customizable charting library used on the Product Details screen to render the historical NAV line graphs performantly, even with years of daily data points.
*   **AndroidX DataStore**: Used for storing simple user preferences natively and asynchronously.
*   **Navigation Compose**: Handles in-app routing and backstack management entirely within Compose.

##  Run Instructions

### Prerequisites
*   **Android Studio**: Ladybug, Koala, or a recent version patched for JDK 17+.
*   **Minimum SDK**: API 26 (Android 8.0 Oreo)
*   **Target SDK**: API 36

### Steps to Run
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/rithikk08/MF-Explorer.git
   cd MFExplorer
   ```
2. **Open the Project:**
   Launch Android Studio, select **Open**, and navigate to the cloned `MFExplorer` directory.
3. **Gradle Sync:**
   Android Studio should automatically sync the Gradle files. If not, click on `File > Sync Project with Gradle Files`.
4. **Run the App:**
   *   Connect a physical Android device via USB/Wi-Fi Debugging, or start an Android Virtual Device (AVD).
   *   Click the green **Run (Play)** button in the top toolbar, or press `Shift + F10`.

### Generate an APK via Terminal
If you prefer building directly from the command line without opening Android Studio:
```bash
# On Windows
.\gradlew assembleDebug

# On macOS/Linux
./gradlew assembleDebug
```
The compiled APK will be output to: 
`app/build/outputs/apk/debug/app-debug.apk`

---

##  Implemented Features & "Brownie Points"
*   **Explore Tab**: Categorized mutual fund grids using optimal semantic search queries under the hood since `mfapi.in` lacks a category endpoint.
*   **Portfolio Management (Watchlist)**: Folder-based watchlist architecture allowing users to create, route funds into, edit, and delete lists via long-press gestures. Includes beautiful empty states.
*   **Product Details & Charts**: Complete fund details screen mapping AMC profiles and an interactive Vico NAV line graph. Includes a BottomSheet component to manage the fund's folder mapping.
*   **Search & View All**: Infinite vertically scrolling lists (`LazyColumn`) and a debounced search input (300ms delay to prevent API spamming).
*   **Modern Declarative UI & Theming**: Built 100% in Jetpack Compose with automated Light/Dark mode.
*   **Subtle Animations & Polish**: Implemented `AnimatedVisibility`, `Crossfade`, and `animateFloatAsState` (fade-ins, scale-ins, and shimmer loading pulse effects) across screens for a premium feel.