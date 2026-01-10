# Lycommons Android Library

A comprehensive Android utility library providing commonly needed functionality out of the box.

## Quick Setup

### Option 1: Local Module (Recommended for Development)
```gradle
// In settings.gradle.kts
include ":app"
include ":mylib"

// In app/build.gradle.kts
dependencies {
    implementation(project(":mylib"))
}
```

### Option 2: AAR File Distribution

1. **Build the AAR:**
   ```bash
   ./gradlew :mylib:assembleRelease
   # AAR will be at: mylib/build/outputs/aar/mylib-release.aar
   ```

2. **Copy to your project:**
   ```bash
   mkdir -p your-project/app/libs/
   cp mylib-release.aar your-project/app/libs/
   ```

3. **Add to dependencies:**
   ```gradle
   dependencies {
       implementation(files("libs/mylib-release.aar"))
       // + all required dependencies (see documentation)
   }
   ```

## Features

- ✅ Extension functions for Context, Views, Strings, Data operations
- ✅ Firebase integration (Auth, Firestore, Storage)
- ✅ UI utilities and animations
- ✅ Permission helpers and system integrations
- ✅ Data persistence with DataStore
- ✅ Media handling and image processing

## Documentation

See `LibraryDocumentation.html` for complete API reference and examples.

## Requirements

- Android SDK 21+
- Kotlin 1.8.0+
- AndroidX libraries

## License

[Your License Here]
