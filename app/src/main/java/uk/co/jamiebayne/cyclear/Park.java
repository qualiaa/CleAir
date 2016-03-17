package uk.co.jamiebayne.cyclear;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import java.util.ArrayList;
import java.util.List;

public class Park {
    int mId;
    private LatLng mCentroid;
    private ArrayList<LatLng> mPolygon;

    Park(KmlPlacemark data)
    {
        mId = Integer.parseInt(data.getProperty("OBJECTID"));
        double lat = Double.parseDouble(data.getProperty("lat"));
        double lng = Double.parseDouble(data.getProperty("lon"));
        mCentroid = new LatLng(lat, lng);
        /*
        KmlPolygon poly = (KmlPolygon)data.getGeometry().getGeometryObject();
        System.out.println("SDSDSD");
        mPolygon = poly.getOuterBoundaryCoordinates();
        System.out.println("SDSDSD");
        */
    }

    public LatLng getCentroid() { return mCentroid; }
    public LatLng getCentroidAsDistance() { return mCentroid; }

    public List<LatLng> getPolygon() { return mPolygon; }
}
