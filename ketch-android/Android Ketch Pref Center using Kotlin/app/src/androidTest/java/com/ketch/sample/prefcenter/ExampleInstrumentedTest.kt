<<<<<<<< HEAD:ketch-android/Android Ketch Pref Center using Kotlin/app/src/androidTest/java/com/ketch/sample/prefcenter/ExampleInstrumentedTest.kt
package com.ketch.sample.prefcenter
========
package com.ketch.sample.pref
>>>>>>>> main:ketch-android/Android Ketch Pref Center using Kotlin/app/src/androidTest/java/com/ketch/sample/pref/ExampleInstrumentedTest.kt

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
<<<<<<<< HEAD:ketch-android/Android Ketch Pref Center using Kotlin/app/src/androidTest/java/com/ketch/sample/prefcenter/ExampleInstrumentedTest.kt
        assertEquals("com.ketch.sample.prefcenter", appContext.packageName)
========
        assertEquals("com.ketch.sample.pref", appContext.packageName)
>>>>>>>> main:ketch-android/Android Ketch Pref Center using Kotlin/app/src/androidTest/java/com/ketch/sample/pref/ExampleInstrumentedTest.kt
    }
}