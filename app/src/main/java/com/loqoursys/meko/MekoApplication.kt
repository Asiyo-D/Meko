package com.loqoursys.meko

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by root on 4/14/18 for LoqourSys
 */
class MekoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
