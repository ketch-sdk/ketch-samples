# Sample App for Ketch Mobile SDK 

## Prerequisites
- [Android Studio](https://developer.android.com/studio) + follow the setup wizard to install SDK and Emulator

## Quick Guide

### Step 1. Clone the repository

```
git clone git@github.com:ketch-sdk/ketch-samples.git
cd "ketch-android/Android Native SDK Sample"
```

### Step 2. Run the app in Android Studio

Open the project directory `Android Native SDK Sample` in the Android Studio.

Click Run to build and run the app on the simulator or a device.



Optionally you can add your organization code and property name to
`ketch-android/app/src/main/java/com/ketch/sample/MainActivity.kt`:

```kotlin
private const val ORG_CODE = "????????????????"
private const val PROPERTY = "????????????????"
```


## Updating the Sample App to the latest version of SDK

1. Clean the project by "Build" => "Clean Project"

2. (optional) Sync project from Gradle build files.

![sync-with-gradle.png](docs%2Fsync-with-gradle.png)

3. Fefresh Graddle Dependancies

![refresh-graddle-deps.png](docs%2Frefresh-graddle-deps.png)
