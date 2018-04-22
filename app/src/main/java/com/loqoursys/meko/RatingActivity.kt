package com.loqoursys.meko

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_rating.*
import kotlinx.android.synthetic.main.content_rating.*
import utils.snackBar

class RatingActivity : AppCompatActivity() {

    private var newRating = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        new_rating.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                newRating = rating
                rating_text.text = newRating.toString()
            }
        }

        btn_submit.setOnClickListener { view ->
            val comment = rating_comment.text.toString()
            when {
                newRating < 1f -> view.snackBar("Enter a rating of at least 1")
                comment.isEmpty() -> {
                    view.snackBar("Enter a comment to submit rating")
                }
                else -> {
                    submitRating(view, comment)
                }
            }
        }

    }

    private fun submitRating(view: View, comment: String) {
        view.snackBar("$newRating rating submitted")

        btn_submit.isEnabled = false
    }

}
