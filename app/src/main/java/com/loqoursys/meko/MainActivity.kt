package com.loqoursys.meko

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.loqoursys.meko.data.MekoUser
import com.loqoursys.meko.listener.OnFoodItemSelected
import com.loqoursys.meko.ui.fragments.DrinksFragment
import com.loqoursys.meko.ui.fragments.FoodFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import utils.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnFoodItemSelected {

    private val menuAnimationHandler = MenuAnimationHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.primaryLighter)

        fab.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        drawer_layout.setViewScale(Gravity.START, 0.9f) //set height scale for main view (0f to 1f)
        drawer_layout.setViewElevation(Gravity.START, 20f)//set main view elevation when drawer open (dimension)
        drawer_layout.setViewScrimColor(Gravity.START, Color.TRANSPARENT)//set drawer overlay coloe (color)

        drawer_layout.setRadius(Gravity.START, 30f)//set end container's corner radius (dimension)

        menu_drawer.setOnClickListener { drawer_layout.openDrawer(Gravity.START) }
        menu_drawer.setImageDrawable(AnimatedVectorDrawableCompat.create(this,
                R.drawable.awsb_ic_menu_animated))
        nav_view.setNavigationItemSelectedListener(this)

        val uid = mAuth.currentUser!!.uid
        val txtName: TextView = nav_view.getHeaderView(0).findViewById(R.id.username)
        val txtNumber: TextView = nav_view.getHeaderView(0).findViewById(R.id.phone_number)

        val phoneNumber = mAuth.currentUser!!.phoneNumber!!
        val num = "${phoneNumber.subSequence(0, 4)} ${phoneNumber.subSequence(4, 7)}" +
                " ${phoneNumber.subSequence(7, phoneNumber.length)}"
        txtNumber.text = num

        val userDBRef = mDatabase.getReference(FirebaseUtil.USERS_REF)
        userDBRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null) {
                    val user = snapshot.getValue(MekoUser::class.java)
                    if (user != null) {
                        val name = user.full_name

                        txtName.text = name
                    }
                }
            }
        })

        val cal = Calendar.getInstance()

        val hr = cal.get(Calendar.HOUR_OF_DAY)
        meal_photo.loadFromResources(this,
                getMealPhotoID(hr))
        meal_tag.text = getMealTag(hr)

        meko_navigation.setOnNavigationItemSelectedListener(onMekoNavigationItemSelectedListener)
        meko_navigation.selectedItemId = R.id.nav_food

        menuAnimationHandler.sendEmptyMessageDelayed(MESSAGE_ANIMATION_START,
                DELAY_BEFORE_FIRST_MENU_ANIMATION)

        action_notification.setOnClickListener {
            viewNotifications()
        }
    }

    override fun onResume() {
        super.onResume()
        nav_view.menu.getItem(0).isChecked = true
        fab.count = mekoCart.size
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            drawer_layout.isDrawerOpen(GravityCompat.END) -> drawer_layout.closeDrawer(GravityCompat.END)
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getMekoFoods() {
        val foodFragment = FoodFragment.newInstance()
        loadFragment(foodFragment)
    }

    private fun getMekoDrinks() {
        val drinksFragment = DrinksFragment.newInstance()
        loadFragment(drinksFragment)
    }

    private fun search() {

    }

    private fun loadFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.main_content, fragment, FOOD_FRAGMENT_TAG)
        transaction.setCustomAnimations(R.anim.fadein, R.anim.fade_out)
        transaction.commit()
    }

    override fun onItemSelected(pos: Int) {
        val intent = Intent(this, MekoItemActivity::class.java)
        intent.putExtra("item_pos", pos)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun viewNotifications() {
        drawer_layout.openDrawer(GravityCompat.END)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_orders -> {
                startActivity(Intent(this, OrdersActivity::class.java))
                finishAffinity()
            }
            R.id.nav_locations -> {
                startActivity(Intent(this, LocationsActivity::class.java))
                finishAffinity()
            }
            R.id.nav_payments -> {
                startActivity(Intent(this, PaymentsActivity::class.java))
                finishAffinity()
            }
            R.id.nav_settings -> {

            }
            R.id.nav_help -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private val onMekoNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_food -> {
                        getMekoFoods()
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.nav_drinks -> {
                        getMekoDrinks()
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.nav_search -> {
                        search()
                        return@OnNavigationItemSelectedListener true
                    }

                }
                true
            }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        menuAnimationHandler.removeMessages(MESSAGE_ANIMATION_START)
    }

    internal inner class MenuAnimationHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MESSAGE_ANIMATION_START -> {
                    val drawable = menu_drawer.drawable
                    if (drawable is AnimatedVectorDrawableCompat) {
                        (menu_drawer.drawable as AnimatedVectorDrawableCompat).start()
                        sendEmptyMessageDelayed(MESSAGE_ANIMATION_START, 5000)
                    }
                }
            }
        }
    }


    companion object {
        const val DELAY_BEFORE_FIRST_MENU_ANIMATION = 1000L
        const val MESSAGE_ANIMATION_START = 1
        const val FOOD_FRAGMENT_TAG = "Food Fragment"
        const val DRINKS_FRAGMENT_TAG = "Drinks Fragment"
    }
}
