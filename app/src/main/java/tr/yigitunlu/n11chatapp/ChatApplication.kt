package tr.yigitunlu.n11chatapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for the chat app
 * Handles network connectivity monitoring
 */
@HiltAndroidApp
class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Network receiver is initialized via Hilt injection
    }
}
