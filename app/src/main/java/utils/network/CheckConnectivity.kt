package utils.network

import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import utils.logData
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by root on 4/12/18 for LoqourSys
 */
class CheckConnectivity(private val URL: String = CONNECTION_URL,
                        completeListener: ConnectivityCompleteListener)
    : AsyncTask<Void, Void, ConnectionStatus>() {

    private var okHttpClient: OkHttpClient? = null
    //    private val context: Context
    private val listener: ConnectivityCompleteListener = completeListener

    init {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                    .build()
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        logData("Checking connection")
    }

    override fun doInBackground(vararg p0: Void?): ConnectionStatus {
        val request = Request.Builder()
                .url(URL)
                .build()
        val response: Response?
        try {
            response = okHttpClient?.newCall(request)?.execute()
        } catch (ieo: IOException) {
            return ConnectionStatus.FAILED
        }
        if (response != null) {
            if (response.isSuccessful) {
                return ConnectionStatus.CONNECTED
            }
        }

        return ConnectionStatus.DISCONNECTED
    }

    override fun onPostExecute(result: ConnectionStatus) {
        super.onPostExecute(result)
        listener.onConnectivityCheckComplete(result)
    }

    companion object {
        const val CONNECTION_URL = "https://firebase.google.com/"
        const val CONNECTION_TIMEOUT = 60L
    }

    interface ConnectivityCompleteListener {
        fun onConnectivityCheckComplete(connectionStatus: ConnectionStatus): Boolean
    }
}