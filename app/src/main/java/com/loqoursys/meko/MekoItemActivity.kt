package com.loqoursys.meko

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.loqoursys.meko.data.FoodItem
import com.loqoursys.meko.listener.OnScrollChangedCallback
import com.loqoursys.meko.ui.MekoImageView
import kotlinx.android.synthetic.main.activity_meko_item.*
import kotlinx.android.synthetic.main.content_meko_item.*
import utils.*

class MekoItemActivity : AppCompatActivity(), MekoImageView.LoadingRequestListener.Callback {

    private lateinit var dialog: MaterialDialog
    private lateinit var foodItem: FoodItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meko_item)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val iconBack = ContextCompat.getDrawable(this,
                R.drawable.awsb_ic_arrow_back_white_24dp)
        supportActionBar?.setHomeAsUpIndicator(iconBack)

        val itemPos = intent.getIntExtra("item_pos", 0)
        foodItem = mekoFoods[itemPos]

        item_image.loadURL(foodItem.photo_url, this)

        fab.setOnClickListener { _ ->
            showQuantityDialog()
        }

        item_cook.setOnClickListener {
            startActivity(Intent(this, CookProfileActivity::class.java))
        }
        scrollView.mScrollChangedCallback = onScrollChangedCallback
//        supportStartPostponedEnterTransition()
        loadItemDetails(foodItem)

        initCartDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_meko_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            R.id.action_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun loadItemDetails(item: FoodItem) {
        txt_title.text = item.item_name
        val servings = "${item.servings} Servings"
        txt_servings.text = servings
        txt_price.text = "${item.price}".toKshs()

        if (item.cook != null) {
            item_cook.visibility = View.VISIBLE
            item_divider.visibility = View.VISIBLE

            cook_name.text = item.cook!!.full_name
            cook_photo.loadURL(this, item.cook!!.photoUrl)

            cook_rating.rating = item.cook!!.rating
            rating_text.text = item.cook!!.rating.toString()
        } else {
            item_divider.visibility = View.GONE
            item_cook.visibility = View.GONE
        }
        txt_description.text = item.description
        txt_ingredients.text = item.ingredients
    }

    override fun onComplete() {
        animateActivity()
    }

    private fun animateActivity() {
        item_image.visibility = View.VISIBLE
        val animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein)

        val reveal = ViewAnimationUtils.createCircularReveal(item_image, item_image.width / 2,
                0, 0f, item_image.width.toFloat())
        reveal.interpolator = AccelerateInterpolator(1.5f)
        reveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                scrim.visibility = View.VISIBLE
                fab.show()
                toolbar.visibility = View.VISIBLE
                toolbar.startAnimation(animFadeIn)
                item_header.visibility = View.VISIBLE
                item_header.startAnimation(animFadeIn)
            }
        })
        reveal.duration = 500
        reveal.start()
        scrollView.visibility = View.VISIBLE
        scrollView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.translatedownandfadein))
        item_content.startAnimation(animFadeIn)

    }

    override fun onFailure(message: String) {
        showToast(applicationContext, "Could not load food photo check your connection")
    }

    private val onScrollChangedCallback = object : OnScrollChangedCallback {
        override fun onScroll(l: Int, t: Int) {
            onNewScroll(t)
        }
    }

    private var toolbarIconsNormal = false
    private var toolbarIconColored = true

    private fun onNewScroll(pos: Int) {
        showToast(this, pos.toString())
        if (pos > 0) {
            if (!toolbarIconColored) {
                setToolbarTint(toolbar, Color.BLACK)
                toolbarIconsNormal = false
                toolbarIconColored = true
            }
        } else {
            if (!toolbarIconsNormal) {
                setToolbarTint(toolbar, Color.WHITE)
                toolbarIconColored = false
                toolbarIconsNormal = true
            }
        }

    }

    private lateinit var txtQuantity: TextView
    private lateinit var txtServingPrice: TextView
    private var numOfServings = 1

    private fun initCartDialog() {
        dialog = MaterialDialog.Builder(this)
                .customView(R.layout.dialog_add_to_cart, false)
                .title("Item quantity")
                .positiveText("Add to cart")
                .negativeText("Cancel")
                .positiveColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .negativeColor(Color.RED)
                .onNegative { dialog, _ -> dialog.dismiss() }
                .onPositive { dialog, _ ->
                    dialog.dismiss()
                    addItemToCart()
                }
                .build()

        val dialogView = dialog.view
        val btnAdd: ImageButton = dialogView.findViewById(R.id.btn_add)
        val btnRemove: ImageButton = dialogView.findViewById(R.id.btn_remove)

        txtServingPrice = dialogView.findViewById(R.id.txt_item_price)
        txtQuantity = dialogView.findViewById(R.id.txt_item_num)

        btnAdd.setOnClickListener {
            if (numOfServings >= 10) {
                numOfServings = 10
                return@setOnClickListener
            }
            numOfServings++
            setServingNum(numOfServings.toString())
        }

        btnRemove.setOnClickListener {
            if (numOfServings <= 1) {
                numOfServings = 1
                return@setOnClickListener
            }
            numOfServings--
            setServingNum(numOfServings.toString())
        }
    }

    private fun setServingNum(num: String) {
        txtQuantity.text = num
        pop(this, txtQuantity)
    }

    private fun showQuantityDialog() {
        dialog.show()
        setServingNum(numOfServings.toString())
        txtServingPrice.text = foodItem.price.toString().toKshs()
    }

    private fun addItemToCart() {
        val newItem = foodItem.copy(servings = numOfServings)
        mekoCart.add(newItem)
        fab.snackBar("$numOfServings ${foodItem.item_name} added to cart", Snackbar.LENGTH_LONG)
    }
}
