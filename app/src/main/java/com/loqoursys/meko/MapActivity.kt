package com.loqoursys.meko

import android.Manifest
import android.animation.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Address
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_map.*
import utils.*
import utils.location.GetAddress
import utils.location.GetCurrentLocation
import utils.location.SearchLocation
import utils.location.interfaces.GpsOnListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GpsStatusDetector.GpsStatusDetectorCallBack, GpsOnListener {


    private lateinit var apiClient: GoogleApiClient
    private var mMap: GoogleMap? = null
    private var gpsStatusDetector: GpsStatusDetector? = null
    private var selectedLocationName: String? = null

    private lateinit var currentLocation: LatLng
    private lateinit var defaultLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var getAddress: GetAddress

    lateinit var context: Context
    private lateinit var handler: Handler
    private lateinit var dlgSearch: MaterialDialog

    private lateinit var mCircle: Circle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        setSupportActionBar(toolbar)

        context = this
        handler = Handler()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_clear_black_24dp)
        drawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        supportActionBar?.setHomeAsUpIndicator(drawable)

        fab.setOnClickListener {
            animateSearch()
            txt_search.requestFocus()
        }

        txt_search.setOnEditorActionListener { textView, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val searchText = txt_search.text.toString()
                    if (searchText.isNotEmpty()) {
                        hideKeyboard(textView)
                        searchForLocation(txt_search.text.toString())
                    }
                    true
                }
                else -> false
            }
        }
        txt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null) {
                    if (editable.isNotEmpty()) {
                        btn_clear.visibility = View.VISIBLE
                    } else {
                        btn_clear.visibility = View.INVISIBLE
                    }
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
        btn_back.setOnClickListener {
            hideKeyboard(it)
            closeSearch()
        }
        btn_clear.setOnClickListener { txt_search.setText("") }

        btn_map_layers.setOnClickListener { toggleMapType() }
        btn_my_location.setOnClickListener {
            if (checkGpsPermission()) {
                getGpsLocation()
            }
        }
        linear_location.setOnClickListener {
            selectLocation()
        }

        gpsStatusDetector = GpsStatusDetector(this)
        apiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build()
        apiClient.connect()

        defaultLocation = LatLng(-0.4167, 36.9476)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getAddress = GetAddress(this)
        resultReceiver = LocationResultReceiver(handler)

        dlgSearch = MaterialDialog.Builder(this)
                .content(getString(R.string.searching))
                .contentColor(Color.BLACK)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .build()
    }


    override fun onBackPressed() {
        if (inSearch) {
            closeSearch()
        } else {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed()
        }
    }

    private fun animateSearch() {
        val dx = ViewUtils.centerX(card_search) + fabSizePx() - ViewUtils.centerX(fab)
        val dy = (ViewUtils.getRelativeTop(card_search) - ViewUtils.getRelativeTop(fab) - (fab.height - fabSizePx())).toFloat()

        val x = (ViewUtils.centerX(fab) + dx).toInt()
        val y = (card_search.bottom - card_search.top) / 2

        val startRadius = (fabSizePx() / 2).toFloat()
        val endRadius = Math.hypot(Math.max(x, card_search.width - x).toDouble(),
                Math.max(y, card_search.height - y).toDouble()).toFloat()

        card_search.alpha = 0f
        card_search.visibility = View.VISIBLE
        val fabSlideX = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, dx))
        fabSlideX.duration = ANIMATION_DURATION / 2

        val fabSlideY = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, dy))
        fabSlideY.duration = ANIMATION_DURATION / 2

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val expandSearch = ViewAnimationUtils.createCircularReveal(card_search, x, y,
                    startRadius, endRadius)
            expandSearch.startDelay = ANIMATION_DURATION / 2
            expandSearch.duration = ANIMATION_DURATION / 2

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(fabSlideX, fabSlideY, expandSearch)
            fabSlideX.interpolator = AccelerateInterpolator(1.0f)
            fabSlideY.interpolator = DecelerateInterpolator(0.8f)
            fabSlideX.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
//                    shiftLocation(btn_my_location, dy)
                }
            })
            fabSlideY.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    fab.visibility = View.INVISIBLE
                    fab.translationX = 0f
                    fab.translationY = 0f
                    fab.alpha = 1f
                }
            })

            expandSearch.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    card_search.alpha = 1f
                }
            })
            animatorSet.start()
        }
        inSearch = true
    }

    private fun closeSearch() {
        val dx = ViewUtils.centerX(card_search) + fabSizePx() - ViewUtils.centerX(fab)
        val dy = (ViewUtils.getRelativeTop(card_search)
                - ViewUtils.getRelativeTop(fab)
                - (fab.height - fabSizePx()) / 2).toFloat()

        fab.alpha = 0f
        fab.translationX = dx
        fab.translationY = dy
        fab.visibility = View.VISIBLE

        val x = ViewUtils.centerX(fab).toInt()
        val y = (card_search.bottom - card_search.top) / 2

        val endRadius = (fabSizePx() / 2).toFloat()
        val startRadius = Math.hypot(Math.max(x, card_search.width - x).toDouble(),
                Math.max(y, card_search.height - y).toDouble()).toFloat()

        val fabSlideX = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, dx, 0f))
        fabSlideX.startDelay = ANIMATION_DURATION / 2
        fabSlideX.duration = ANIMATION_DURATION / 2

        val fabSlideY = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dy, 0f))
        fabSlideY.startDelay = ANIMATION_DURATION / 2
        fabSlideY.duration = ANIMATION_DURATION / 2

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val collapseSearch = ViewAnimationUtils.createCircularReveal(card_search, x, y, startRadius, endRadius)
            collapseSearch.duration = ANIMATION_DURATION / 2

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(collapseSearch, fabSlideX, fabSlideY)

            fabSlideX.interpolator = DecelerateInterpolator(0.8f)
            fabSlideY.interpolator = AccelerateInterpolator(1.0f)

            collapseSearch.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    fab.alpha = 1f
                    card_search.alpha = 0f
                }
            })

            fabSlideY.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
//                    shiftLocation(btn_my_location, dy)
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    card_search.visibility = View.INVISIBLE
                    card_search.alpha = 1f
                }
            })
            animatorSet.start()
        }

        inSearch = false
    }

    private var inSearch = false

    private fun shiftLocation(view: View, dy: Float) {
        if (!inSearch) {
            val locationSlideY = ObjectAnimator.ofPropertyValuesHolder(view,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, dy))
            locationSlideY.duration = ANIMATION_DURATION / 2
            locationSlideY.start()
            inSearch = true
        } else {
            val locationSlideY = ObjectAnimator.ofPropertyValuesHolder(view,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dy, 0f))
            locationSlideY.duration = ANIMATION_DURATION / 2
            locationSlideY.start()
            inSearch = false
        }

    }

    private fun fabSizePx(): Int {
        val displayMetrics = applicationContext.resources.displayMetrics
        return Math.round(56 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    private fun checkGpsPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                showToast(context, "Grant Loci permission to access your location")
            } else {
                ActivityCompat.requestPermissions(this@MapActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)

            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGpsLocation()
                } else {
                    showSnackBar()
                }
            }
        }
    }

//    GPS status

    private fun mapSnackBar(message: String, length: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(parent_view, message, length).show()
    }

    private fun showSnackBar() {
        Snackbar.make(parent_view, "Meko requires permission to access your Location",
                Snackbar.LENGTH_LONG).setAction("give access") { _ -> checkGpsPermission() }

    }

    private fun getGpsLocation() {
        if (mMap == null) {
            return
        }
        GetCurrentLocation(this@MapActivity).getCurrentLocation()
    }

    override fun gpsStatus(_status: Boolean) {
        if (!_status) {
            mapSnackBar("Enable GPS to get your current location")
            gpsStatusDetector?.checkGpsStatus()
        } else {
            getGpsLocation()
        }
    }

    override fun gpsPermissionDenied(deviceGpsStatus: Int) {
        if (deviceGpsStatus == 1) {
            mapSnackBar("Enable GPS to get your current location")
        } else {
            getGpsLocation()
        }
    }

    override fun gpsLocationFetched(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)
        updateMap(currentLocation)
        updateLocationView(location)
    }

    override fun onGpsSettingStatus(enabled: Boolean) {
        if (enabled) {
            showToast(context, "GPS enabled")
        }
    }

    override fun onGpsAlertCanceledByUser() {
        mapSnackBar("Enable location for improved accuracy")
    }


    //    Map

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        if (mMap != null) {
            initCircle(defaultLocation)
            mMap!!.setOnMapClickListener(this::onMapClickMarkerDrag)
            mMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(marker: Marker?) {
                    if (marker != null) {
                        onMapClickMarkerDrag(marker.position)
                    }
                }

                override fun onMarkerDragStart(p0: Marker?) {

                }

                override fun onMarkerDrag(p0: Marker?) {
                }
            })
        }
        updateToLastKnownLocation()
    }

    private fun onMapClickMarkerDrag(latLng: LatLng) {
        val touchLocation = Location("")
        touchLocation.latitude = latLng.latitude
        touchLocation.longitude = latLng.longitude

        updateMap(latLng)
        updateLocationView(touchLocation)
    }

    override fun onConnected(p0: Bundle?) {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onConnectionSuspended(p0: Int) {
        mapSnackBar("Connection  error. Check your Internet connection")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        showToast(context, "An error occurred: ${p0.errorMessage}")
    }

    private fun updateToLastKnownLocation() {
        if (mMap == null) {
            return
        }

        if (gpsStatusDetector != null) {
            gpsStatusDetector?.checkGpsStatus()
        }

        if (checkGpsPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,
                            location.longitude), DEFAULT_ZOOM))
                }
            }
        } else {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM))
        }
    }

    private fun updateMap(latLng: LatLng) {
        if (mMap == null) {
            return
        }

        mMap?.clear()
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
        mMap?.animateCamera(cameraUpdate)
        renderLocations(latLng)
        currentLocation = latLng
    }

    private fun updateLocationView(location: Location) {
        logData(location.toString())
        val userLocation = getAddress.fetchCurrentAddress(location)
        val locationText = userLocation ?: getString(R.string.no_location_selected)
        txt_location.text = locationText
        selectedLocation = location
        selectedLocationName = locationText
    }

    private fun toggleMapType() {

        val mapTypes = arrayListOf("Default", "Satellite", "Terrain")
        val dialog = MaterialDialog.Builder(this)
                .title(getString(R.string.map_type))
                .items(mapTypes)
                .itemsCallback { dialog, _, position, _ ->
                    when (position) {
                        0 -> {
                            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        }
                        1 -> {
                            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                        }
                        else -> {
                            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                        }
                    }
//                    showToast(this, "$position $text")
                    dialog.dismiss()
                }
                .build()

        dialog.show()
    }

    private fun initCircle(pos: LatLng) {
        val circleOption = CircleOptions().center(pos)
                .radius(DELIVERY_RADIUS).fillColor(Color.RED).visible(false)
        if (mMap != null) {
            mCircle = mMap!!.addCircle(circleOption)
        }
    }

//    Search location

    private fun searchForLocation(locationName: String) {
        dlgSearch.show()
        startIntentService(locationName)
    }

    private lateinit var resultReceiver: LocationResultReceiver


    private fun startIntentService(name: String) {
        val intent = Intent(this, SearchLocation::class.java).apply {
            putExtra(SearchLocation.Constants.RECEIVER, resultReceiver)
            putExtra(SearchLocation.Constants.LOCATION_DATA_EXTRA, name)
        }

        startService(intent)
    }

    internal inner class LocationResultReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            when (resultCode) {
                SearchLocation.Constants.SUCCESS_RESULT -> {
                    if (resultData != null) {
                        logData("$resultData")
                        showLocationOnMap(resultData)
                    } else {
                        mapSnackBar("An error occurred. Try searching for the location again")
                    }
                }
                else -> {

                    val errorMessage = resultData?.getString(SearchLocation.Constants.RESULT_DATA_KEY)
                            ?: "An error occurred. Check your internet connection and try again"

                    mapSnackBar(errorMessage, Snackbar.LENGTH_INDEFINITE)
                }
            }
            dlgSearch.dismiss()
            super.onReceiveResult(resultCode, resultData)
        }
    }

    private fun showLocationOnMap(data: Bundle) {
        val addresses: ArrayList<Address> = data
                .getParcelableArrayList(SearchLocation.Constants.RESULT_DATA_EXTRA)

        mMap?.clear()
        addresses.forEach {
            val location = LatLng(it.latitude, it.longitude)
            renderLocations(location)
        }
    }

    private fun renderLocations(latLng: LatLng) {
        if (mMap == null) {
            return
        }
        val newLocal = Location("")
        newLocal.latitude = latLng.latitude
        newLocal.longitude = latLng.longitude

//        val markerColor = ContextCompat.getColor(context, R.color.primaryLight)

        val locationTitle = getAddress.fetchCurrentAddress(newLocal)
        val locationMarker = MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(locationTitle)
                .draggable(true)

        mMap?.addMarker(locationMarker)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
    }

    private fun hideKeyboard(view: View) {
        val ime = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ime.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun isMekoInLocation(location: Location): Boolean {
        val distance = floatArrayOf(0f, 0f)
        Location.distanceBetween(location.latitude, location.longitude,
                mCircle.center.latitude, mCircle.center.longitude, distance)
        return distance[0] <= mCircle.radius
    }


    private fun selectLocation() {
        if (selectedLocation != null) {
            if (isMekoInLocation(selectedLocation!!)) {
                val data = intent
                        .putExtra("location name", selectedLocationName)
                        .putExtra("location", selectedLocation)
                this.setResult(android.app.Activity.RESULT_OK, data)
                finish()
            } else {
                mapSnackBar("We currently do not deliver to the selected location")
            }
        } else {
            showToast(this, "Select location on map to add")
        }
    }

    companion object {
        const val ANIMATION_DURATION = 300L
        const val LOCATION_PERMISSION = 202
        const val DEFAULT_ZOOM = 20F

        const val DELIVERY_RADIUS = 5000.0
    }

}
