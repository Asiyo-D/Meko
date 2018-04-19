package com.loqoursys.meko

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.loqoursys.meko.data.MekoUser
import kotlinx.android.synthetic.main.activity_account_setup.*
import kotlinx.android.synthetic.main.content_account_setup.*
import utils.FirebaseUtil
import utils.Preferences
import utils.mAuth
import utils.mDatabase
import utils.network.CheckConnectivity
import utils.network.CheckConnectivity.ConnectivityCompleteListener
import utils.network.ConnectionStatus

class AccountSetupActivity : AppCompatActivity() {
    private lateinit var dialog: MaterialDialog
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setup)
        setSupportActionBar(toolbar)
        context = this

        fab.setOnClickListener { view ->
            if (isNameValid()) {
                checkConnection(view)
            } else {
                Snackbar.make(view, "Enter your full name to proceed", Snackbar.LENGTH_SHORT).show()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        dialog = MaterialDialog.Builder(this)
                .content("Setting up...")
                .contentColor(Color.BLACK)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .build()
    }

    private fun isNameValid(): Boolean = txt_name.text.toString().isNotEmpty()

    private fun checkConnection(view: View) {
        dialog.show()
        CheckConnectivity(completeListener = object : ConnectivityCompleteListener {
            override fun onConnectivityCheckComplete(connectionStatus: ConnectionStatus): Boolean {
                if (connectionStatus == ConnectionStatus.CONNECTED) {
                    signUpUser()
                } else {
                    dialog.dismiss()
                    Snackbar.make(view, "Check your connection then try again",
                            Snackbar.LENGTH_SHORT).show()
                }
                return true
            }
        }).execute()
    }

    override fun onResume() {
        super.onResume()
        Preferences.loadPreferences(this)
    }

    private fun signUpUser() {
        val name = txt_name.text.toString()
        if (name.isEmpty()) {
            return
        }
        if (mAuth.currentUser != null) {
            val userDBRef = mDatabase.getReference(FirebaseUtil.USERS_REF)
            val userId = mAuth.currentUser!!.uid

            val user = MekoUser(userId, name, Preferences.getPreferenceNumber())

            userDBRef.child(userId).setValue(user)

            Preferences.setPreferenceName(name)
            finishSetup()
        } else {
            goToLogin()
        }


    }

    private fun finishSetup() {
        dialog.dismiss()
        val intent = Intent(context, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun goToLogin() {
        dialog.dismiss()
        val intent = Intent(context, VerifyPhoneActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
