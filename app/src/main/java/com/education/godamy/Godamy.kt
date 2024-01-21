package com.education.godamy

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class Godamy :Application() {
    override fun onCreate() {
        super.onCreate()
        val fbOptions = FirebaseOptions.Builder().setProjectId("godamy-b5374").setApplicationId("com.education.godamy")
            .setApiKey("AIzaSyC23DV3SjM8N-U2Na7PtLAYvGIMUMEOJoU").setStorageBucket("godamy-b5374.appspot.com").build()
        FirebaseApp.initializeApp(this,fbOptions)
    }
}