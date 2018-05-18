package com.loqoursys.meko

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_phone.*
import kotlinx.android.synthetic.main.content_verification_code.*
import kotlinx.android.synthetic.main.content_verify_phone.*
import utils.Preferences
import utils.mAuth
import utils.network.CheckConnectivity
import utils.network.ConnectionStatus
import utils.showToast
import utils.snackBar
import java.lang.*
import java.util.concurrent.TimeUnit

class VerifyPhoneActivity : AppCompatActivity() {

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mResendingToken: PhoneAuthProvider.ForceResendingToken
    lateinit var context: Context
    lateinit var verifyDlg: MaterialDialog
    var mVerificationId: String? = null

    private var heightPixels: Int = 0
    private var widthPixels: Int = 0
    private var pixelDensity: Float = 0F

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
                val errorMessage = "Check your connection and try again ${e.message}"
                Snackbar.make(parent_view, errorMessage, Snackbar.LENGTH_SHORT).show()
            }

            override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
                mVerificationId = s
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

        pixelDensity = resources.displayMetrics.density
        heightPixels = resources.displayMetrics.heightPixels
        widthPixels = resources.displayMetrics.widthPixels

        txt_code.setEditCodeListener { code -> authenticateUsersCode(code) }
        btn_resend.setOnClickListener { resendCode() }
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
            checkConnectivity()
        }
    }

    private fun checkConnectivity() {
        verifyDlg.show()
        CheckConnectivity(completeListener = object : CheckConnectivity.ConnectivityCompleteListener {
            override fun onConnectivityCheckComplete(connectionStatus: ConnectionStatus): Boolean {
                verifyDlg.dismiss()
                if (connectionStatus == ConnectionStatus.CONNECTED) {
                    requestAuthCode(phoneNumber)
                    setupVerification()
                } else {
                    fab.snackBar("Check your connection then try again")
                }
                return true
            }
        }).execute()
    }


    private fun signInWithPhoneAuth(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, { task ->
            if (task.isSuccessful) {
                showToast(context, "Verification complete")
                Preferences.setPreferenceNumber(phoneNumber)
                loginUser()
            } else {
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

    private fun authenticateUsersCode(authenticationCode: String) {
        if (mVerificationId != null) {
            val authCredential = PhoneAuthProvider.getCredential(mVerificationId!!, authenticationCode)
            signInWithPhoneAuth(authCredential)
        }
    }

    private fun loginUser() {
        startActivity(Intent(context, AccountSetupActivity::class.java))
        finishAffinity()
    }

    private lateinit var countDownTimer: CountDownTimer

    private fun setupVerification() {
        fab.hide()
        revealCode()

        countDownTimer = ResendCountDown(startTime, interval)
        countDownTimer.start()
        val num = "${phoneNumber.subSequence(0, 4)} ${phoneNumber.subSequence(4, 7)}" +
                " ${phoneNumber.subSequence(7, phoneNumber.length)}"
        verify_number.text = num
        btn_resend.isEnabled = false
        progress.visibility = View.VISIBLE

    }

    private fun resendCode() {
        countDownTimer.cancel()
        countDownTimer.start()
        btn_resend.isEnabled = false

        requestAuthCode(phoneNumber)
    }

    lateinit var animator: Animator

    private fun revealCode() {
        var x = widthPixels
        var y = heightPixels

        val startRadius = (fab.width / 2).toFloat()
        val endRadius = Math.hypot(x.toDouble(), y.toDouble()).toFloat()

        x -= (16 * pixelDensity + 28 * pixelDensity).toInt()
        y -= (16 * pixelDensity + 56 * pixelDensity).toInt()

        view_code.visibility = View.VISIBLE
        animator = ViewAnimationUtils.createCircularReveal(view_code, x, y, startRadius, endRadius)

        animator.duration = 400L

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                view_phone.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animator: Animator) {
                view_phone.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })
        animator.start()

    }

    private inner class ResendCountDown
    /**
     * @param millisInFuture    The number of millis in the future from the call
     * to [.start] until the countdown is done and [.onFinish]
     * is called.
     * @param countDownInterval The interval along the way to receive
     * [.onTick] callbacks.
     */
    internal constructor(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

        override fun onTick(millisUntilFinished: Long) {
            val minutes = (millisUntilFinished / 1000 / 60).toInt()
            val seconds = (millisUntilFinished / 1000 % 60).toInt()
            var sec = seconds.toString()
            if (sec.length <= 1) {
                sec = "0$seconds"
            }
            val tickTime = "0$minutes:$sec"
            txt_countdown.text = tickTime
        }

        override fun onFinish() {
            val finalTime = "0:00"
            txt_countdown.text = finalTime
            btn_resend.isEnabled = true
        }
    }


    companion object {
        const val startTime: Long = (65 * 1000).toLong()
        const val interval: Long = 1000
    }
}
