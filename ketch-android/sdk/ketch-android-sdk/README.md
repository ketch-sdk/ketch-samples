# Ketch Android SDK Sample App

## Prerequisites

- Install [Android Studio](https://developer.android.com/studio) and a Android emulator

## Quick Guide

### Step 1. Clone the repository

```
git clone git@github.com:ketch-sdk/ketch-samples.git
cd "ketch-android/sdk/Android Native SDK Sample"
```

### Step 2. Run the app in Android Studio

Open the project directory `Android Native SDK Sample` in the Android Studio.

Click Run to build and run the app on the simulator or a physical device.

### (Optional) Step 3. Use your own Ketch organization and property

By default, this sample application is connected to an existing Ketch organization with preconfigured settings.

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
