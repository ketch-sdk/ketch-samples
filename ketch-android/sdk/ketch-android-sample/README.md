# Ketch Android SDK Sample App

## Prerequisites

- Install [Android Studio](https://developer.android.com/studio) and a Android emulator
- OR for terminal-based development: Install Android SDK Command Line Tools and ensure `adb` is in your PATH

## Quick Guide

### Option 1: Android Studio (GUI)

#### Step 1. Clone the repository

```
git clone git@github.com:ketch-sdk/ketch-samples.git
cd "ketch-android/sdk/ketch-android-sample"
```

#### Step 2. Run the app in Android Studio

Open the project directory `Android Native SDK Sample` in the Android Studio.

Click Run to build and run the app on the simulator or a physical device.

### Option 2: Terminal-based Development

This option allows you to edit the code in any editor (like Cursor) while running the app from the terminal.

#### Step 1. Clone the repository

```bash
git clone git@github.com:ketch-sdk/ketch-samples.git
cd "ketch-android/sdk/ketch-android-sample"
```

#### Step 2. Start your Android emulator

Start your Android emulator from Android Studio or using the command line:

```bash
# List available emulators
emulator -list-avds

# Start an emulator (replace 'Pixel_6a_API_35' with your emulator name)
emulator -avd Pixel_6a_API_35
```

#### Step 3. Verify emulator connection

```bash
adb devices
```

You should see your emulator listed (e.g., `emulator-5554 device`).

#### Step 4. Build and run the app

**Quick method (recommended):**

```bash
# Build, install, and launch in one command
./gradlew installDebug && adb shell am start -n com.ketch.sample/.MainActivity
```

**Step-by-step method:**

```bash
# Build the debug APK
./gradlew assembleDebug

# Install the APK on the emulator
./gradlew installDebug

# Launch the app
adb shell am start -n com.ketch.sample/.MainActivity
```

#### Step 5. Development workflow

After making code changes, simply run:

```bash
./gradlew installDebug && adb shell am start -n com.ketch.sample/.MainActivity
```

#### Useful debugging commands

```bash
# View app logs
adb logcat | grep -i ketch

# Clear app data (useful for testing)
adb shell pm clear com.ketch.sample

# Uninstall the app
adb uninstall com.ketch.sample

# Force stop the app
adb shell am force-stop com.ketch.sample
```

### (Optional) Step 3. Use your own Ketch organization and property

By default, this sample application is connected to a sample Ketch organization with preconfigured settings.

To use your own organization and property, modify the `init()` function within
[`MainActivity.kt`](./app/src/main/java/com/ketch/sample/MainActivity.kt#L274-283) as follows:

```kotlin
companion object {

    // ...

    private const val ORG_CODE = "your_organization_code"
    private const val PROPERTY = "your_property_code"
    private const val ADVERTISING_ID_CODE = "your_identity_name"  // e.g. "aaid"

    // ...
}
```

## Troubleshooting

### Terminal-based development issues

- **"adb not found"**: Make sure Android SDK platform-tools are installed and in your PATH
- **"No devices found"**: Ensure your emulator is running and `adb devices` shows it
- **Build errors**: Try `./gradlew clean` followed by `./gradlew assembleDebug`
- **App doesn't launch**: Check `adb logcat` for error messages
- **Permission issues**: Ensure the gradlew script is executable: `chmod +x gradlew`
