package utils.location


import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import utils.addressList
import utils.logData
import java.io.IOException
import java.util.*

@Suppress("unused")
/**
 * Created by root on 3/28/18 for LoqourSys
 */

class SearchLocation(private val name: String) : IntentService(name) {
    private var receiver: ResultReceiver? = null
    private var address: List<Address> = emptyList()

    constructor() : this(name = "")

    override fun onHandleIntent(intent: Intent?) {
        val geocoder = Geocoder(this, Locale.getDefault())

        intent ?: return
        val errorMessage: String
        val locationName: String = intent.getStringExtra(Constants.LOCATION_DATA_EXTRA)
        receiver = intent.getParcelableExtra(Constants.RECEIVER)

        logData("Searching for $locationName")
        try {
            address = geocoder.getFromLocationName(locationName, 5)
        } catch (ioe: IOException) {
            errorMessage = "Could not get location. Reboot device and try again"
            deliverResults(Constants.FAILURE_RESULT, errorMessage)
            return
        } catch (iae: IllegalArgumentException) {
            errorMessage = "Oops, could not find $locationName. Try another location"
            deliverResults(Constants.FAILURE_RESULT, errorMessage)
            return
        }

        if (address.isEmpty()) {
            errorMessage = "Oops, could not find $locationName. Try another location"
            deliverResults(Constants.FAILURE_RESULT, errorMessage)
        } else {
            address.forEach {
                addressList.add(it)
                logData("${it.countryName} ${it.latitude}")
            }
            deliverResults(Constants.SUCCESS_RESULT, "")

        }
    }

    private fun deliverResults(resultCode: Int, message: String) {

        val bundle = Bundle().apply {
            putString(Constants.RESULT_DATA_KEY, message)
            putParcelableArrayList(Constants.RESULT_DATA_EXTRA, addressList)
        }
        receiver?.send(resultCode, bundle)
    }

    object Constants {
        private const val PACKAGE_NAME = "com.loqoursys.meko"
        const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"
        const val SUCCESS_RESULT = 0
        const val FAILURE_RESULT = 1
        const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
        const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
        const val RESULT_DATA_EXTRA = "$PACKAGE_NAME.RESULT_DATA_EXTRA"

    }
}
