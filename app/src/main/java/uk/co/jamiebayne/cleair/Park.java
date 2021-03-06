package uk.co.jamiebayne.cleair;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import java.util.ArrayList;
import java.util.Map;

public class Park {
    private int mId;
    private LatLng mCentroid;
    private ArrayList<LatLng> mPolygon;
    private Map<String, Double> distToSites;

    Park(KmlPlacemark data, Map<Integer, Map<String, Double>> distanceData)
    {
        mId = Integer.parseInt(data.getProperty("OBJECTID"));
        distToSites = distanceData.get(mId);
        double lat = Double.parseDouble(data.getProperty("lat"));
        double lng = Double.parseDouble(data.getProperty("lon"));
        mCentroid = new LatLng(lat, lng);
        KmlPolygon poly = (KmlPolygon)data.getGeometry();
        mPolygon = poly.getOuterBoundaryCoordinates();
    }

    public int getID() { return mId; }

    protected double getDistance(String siteCode) {
        return distToSites.get(siteCode);
    }

    public LatLng getCentroid() { return mCentroid; }

    public ArrayList<LatLng> getPolygon() { return mPolygon; }

}
