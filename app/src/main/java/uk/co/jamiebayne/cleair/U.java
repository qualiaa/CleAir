package uk.co.jamiebayne.cleair;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

public class U {
    public static LatLng locationToLatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    public static Location latLngToLocation(LatLng ll) {
        Location l = new Location(LocationManager.PASSIVE_PROVIDER);
        l.setLatitude(ll.latitude);
        l.setLongitude(ll.longitude);
        return l;
    }
}
