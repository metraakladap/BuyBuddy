package com.example.buybuddy

import android.app.Application
import com.google.firebase.FirebaseApp

class BuyBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}