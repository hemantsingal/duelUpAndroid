# DuelUp - Android

A real-time multiplayer dueling game built natively for Android using Kotlin and Jetpack Compose.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM with Hilt dependency injection
- **Networking:** Retrofit + OkHttp for REST APIs, Socket.IO for real-time communication
- **Serialization:** Kotlinx Serialization
- **Navigation:** Jetpack Navigation Compose
- **Image Loading:** Coil 3
- **Animations:** Lottie Compose
- **Local Storage:** DataStore Preferences
- **Build System:** Gradle with Kotlin DSL and version catalogs

## Requirements

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35 (compile) / minimum SDK 26

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/hemantsingal/duelUpAndroid.git
   ```

2. Open the project in Android Studio.

3. Sync Gradle and build the project.

4. Run the app on an emulator or physical device (API 26+).

## Project Structure

```
app/src/main/java/com/duelup/app/
├── DuelUpApplication.kt    # Hilt application class
├── MainActivity.kt         # Single activity entry point
└── ui/
    └── theme/              # Material 3 theming (colors, typography, shapes)
```

## Deep Linking

The app supports deep links with the `duelup://duel` scheme for sharing and joining duels.

## Build Variants

- **Debug** - Development build with Compose UI tooling
- **Release** - Minified and resource-shrunk with ProGuard

## License

All rights reserved.
