package com.mtaanimation.growthos.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and provide the root dependency container for the entire app.
 */
@HiltAndroidApp
class GrowthOSApp : Application()
