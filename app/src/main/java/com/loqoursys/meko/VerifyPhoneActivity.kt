package com.loqoursys.meko

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_phone.*
import kotlinx.android.synthetic.main.content_verify_phone.*
import utils.Preferences
import utils.mAuth
import utils.showToast
import java.lang.*
import java.util.concurrent.TimeUnit

class VerifyPhoneActivity : AppCompatActivity() {

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mResendingToken: PhoneAuthProvider.ForceResendingToken
    lateinit var context: Context
    lateinit var verifyDlg: MaterialDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        fab.setOnClickListener { _ ->
            verifyNumber()
        }
        context = this

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuth(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                verifyDlg.dismiss()

                val errorMessage = "Check your connection and try again"
                Snackbar.make(parent_view, errorMessage, Snackbar.LENGTH_SHORT).show()
            }

            override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
//                mVerificationId = s
                mResendingToken = forceResendingToken!!
            }
        }

        verifyDlg = MaterialDialog.Builder(this)
                .content("Verifying number")
                .contentColor(Color.BLACK)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .build()
    }

    override fun onResume() {
        super.onResume()
        Preferences.loadPreferences(this)
        if (mAuth.currentUser != null) {
            loginUser()
        }
    }

    private var phoneNumber = ""

    private fun verifyNumber() {
        val number = txt_number.text.toString()
        if (number.isEmpty()) {
            phone_helper.visibility = View.VISIBLE
            phone_helper.setTextColor(Color.RED)
        } else if (number.length < 9 || number.length > 10) {
            phone_helper.visibility = View.VISIBLE
            phone_helper.setTextColor(Color.RED)
        } else {
            phone_helper.visibility = View.INVISIBLE
            val numPre = Integer.parseInt(number[0].toString())

            phoneNumber = if (numPre == 0) {
                "+254${number.replaceFirst(number[0].toString(), "")}"
            } else {
                "+254$number"
            }
            verifyDlg.show()
            requestAuthCode(phoneNumber)
        }

    }

    private fun signInWithPhoneAuth(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, { task ->
            if (task.isSuccessful) {
                showToast(context, "Verification complete")
                Preferences.setPreferenceNumber(phoneNumber)
                loginUser()
            } else {
                verifyDlg.dismiss()
                showToast(context, "Invalid verification code", Toast.LENGTH_LONG)
            }
        })

    }

    private fun requestAuthCode(number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number,
                60L,
                TimeUnit.SECONDS,
                this,
                mCallbacks)
    }

    private fun loginUser() {
        verifyDlg.dismiss()
        startActivity(Intent(context, AccountSetupActivity::class.java))
        finishAffinity()
    }

}
