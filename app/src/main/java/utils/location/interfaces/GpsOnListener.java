package utils.location.interfaces;

import android.location.Location;

/**
 * Created by AnkurYadav on 23-09-2017
 */

public interface GpsOnListener {

    void gpsStatus(boolean _status);

    void gpsPermissionDenied(int deviceGpsStatus);

    void gpsLocationFetched(Location location);
}
