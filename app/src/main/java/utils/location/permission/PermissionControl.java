package utils.location.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by AnkurYadav on 23-09-2017
 */


public class PermissionControl {

    public PermissionControl(Activity context) {
        Activity context1 = context;
    }

    private static boolean location_permission = false;

    public boolean getOnlyLocationPermission(Activity activity) {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            location_permission = true;
        } else {
            requestOnlyLocationPermission(activity);
        }
        return location_permission;
    }


    private static void requestOnlyLocationPermission(final Context context) {
        int REQUEST_ONLY_LOCATION = 1;
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ONLY_LOCATION);
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.SYSTEM_ALERT_WINDOW}, REQUEST_ONLY_LOCATION);
        }
    }
}
