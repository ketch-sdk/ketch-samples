# ketch

## [the Sample app](https://github.com/ketch-sdk/ketch-samples)

### Prerequisites
- [Android Studio](https://developer.android.com/studio) + follow the setup wizard to install SDK and Emulator

### Step 1. Clone the repository

```
git clone git@github.com:ketch-sdk/ketch-samples.git
git checkout main
cd ketch-android/Android Native SDK Sample
```

### Step 2. Run the app in Android Studio

Open the project directory `Android Native SDK Sample` in the Android Studio.

Add your organization code, property code to
`ketch-android/app/src/main/java/com/ketch/sample/MainActivity.kt`:

```kotlin
private const val ORG_CODE = "????????????????"
private const val PROPERTY = "????????????????"
```

Click Run to build and run the app on the simulator or a device.
