package utils.location.playservices;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by AnkurYadav on 23-09-2017
 */

public class PlayServiceAvailability {
    public static boolean isAvailable(Activity context) {

        int status = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(context, status, 0).show();
            return false;
        }
    }
}
