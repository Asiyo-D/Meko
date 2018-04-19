package com.loqoursys.meko

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.content_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        btn_start.setOnClickListener { _ ->
            startActivity(Intent(this, VerifyPhoneActivity::class.java))
        }
    }


}
