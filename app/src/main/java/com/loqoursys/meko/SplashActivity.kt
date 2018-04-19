package com.loqoursys.meko

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import utils.*

class SplashActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var mRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler = Handler()

        mRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        mRemoteConfig.setConfigSettings(configSettings)
        mRemoteConfig.setDefaults(R.xml.remote_config_defaults)

        fetchDeliveryFee()
    }

    private fun fetchDeliveryFee() {
        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }

        mRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                mRemoteConfig.activateFetched()
            }
            logData("Success ")

            deliveryFee = mRemoteConfig.getDouble(FirebaseUtil.DELIVERY_FEE_KEY).toFloat()
            logData("Fee $deliveryFee")
        }
    }

    override fun onResume() {
        super.onResume()

        Preferences.loadPreferences(this)
        handler.postDelayed({
            when {
                mAuth.currentUser == null -> startActivity(Intent(this, WelcomeActivity::class.java))
                Preferences.isNewUser() -> startActivity(Intent(this, AccountSetupActivity::class.java))
                else -> startActivity(Intent(this, MainActivity::class.java))
            }
//            startActivity(Intent(this, MekoItemActivity::class.java))
            finish()
        }, SPLASH_DELAY)
    }

    override fun onBackPressed() {}

    companion object {
        const val SPLASH_DELAY = 4000L

    }
}
