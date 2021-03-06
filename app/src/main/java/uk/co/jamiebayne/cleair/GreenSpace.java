package uk.co.jamiebayne.cleair;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;

import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GreenSpace extends FragmentActivity
        implements
            OnMapReadyCallback,
            OnMapClickListener,
            LocationListener,
            GoogleMap.OnPolygonClickListener,
            //OnMyLocationButtonClickListener,
            ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final float ZOOM_LEVEL = 9.25f;
    private static final float MY_ZOOM_LEVEL = 16f;
    private static final LatLng LONDON = new LatLng(51.5072, -0.110);
    private static final float LOCAL_RANGE = 20000; // 8km
    private static final int PARK_OUTLINE_COLOUR = 0xffcccccc;

    //Jacques' data
    private AirData mProcessedData;

    private LocationManager mLm;
    private GoogleMap mMap;
    private KmlLayer mKmlLayer;
    private Location mLocation;
    private Marker mCurrentSelectedMarker;
    private Marker mCurrentSuggestionMarker;
    private ArrayList<Park> mParks;
    private HashMap<Integer, Park> mNearbyParks;
    private HashMap<Integer, Polygon> mNearbyParkPolys;
    private boolean mParksReady = false;
    /*
    private Circle mLocationMarker;
    private Circle mAccuracyMarker;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        mProcessedData = (AirData)i.getSerializableExtra("AirData");
        System.out.println(mProcessedData.getData());
        setContentView(R.layout.activity_green_space);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @Override
    public void onMapClick(LatLng clickPos) {
    }

    /*
    @Override
    public boolean onMyLocationButtonClick()
    {
        //System.out.println("Default Behaviour");
        //Location loc = getLastBestLocation();
        //zoomCameraToLocation(new LatLng(loc.getLatitude(), loc.getLongitude()));
        return false;
    }
    */

    @Override
    public void onLocationChanged(Location l) {
        if (mParksReady && mLocation != null) {
            findNearestGreenspace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /* code modified from Slava Fir's answer to
    http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean havePosition(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /* code modified from Agarwal Shankar's answer to
    http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
     */
    private void requestPositionSettings(Context context) {
        // notify user
        System.out.println("Requesting position");
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(context.getResources().getString(R.string.gps_service_not_enabled));
        dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
        dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    /* code modified from Agarwal Shankar's answer to
    http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
     */
    private void requestAirplaneSettings(Context context) {
        System.out.println("Requesting airplane");
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(context.getResources().getString(R.string.airplane_enabled));
        dialog.setPositiveButton(context.getResources().getString(R.string.open_airplane_settings),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
        dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    /* code from Tiago's answer to
    http://stackoverflow.com/questions/4319212/how-can-one-detect-airplane-mode-on-android
    */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean airplaneMode(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    /* Code from Maxim Shoustin's answer to
    http://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android
    */
    private Location getLastBestLocation() {
        Location locationGPS = mLm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    void requireLocation() {
        if (airplaneMode(this)) {
            requestAirplaneSettings(this);
        }
        if (!havePosition(this)) {
            requestPositionSettings(this);
        }
    }

    private void zoomCameraToLocation(LatLng loc) {
        CameraPosition pos = CameraPosition.builder()
                .target(loc)
                .zoom(MY_ZOOM_LEVEL)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 2000, null);
    }

    private void setup() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("No fine location permission");
        }
        requireLocation();

        mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mLm.requestLocationUpdates(60000, 100, criteria, this, null);

        mLocation = getLastBestLocation();
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(LONDON)
                .zoom(ZOOM_LEVEL)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMap.setMyLocationEnabled(true);
        mMap.setOnPolygonClickListener(this);

        try {
            mKmlLayer = new KmlLayer(mMap, R.raw.parks_new, this);
            //mKmlLayer.addLayerToMap();
        } catch (XmlPullParserException ex) {
            System.out.println(ex.toString());
            throw new RuntimeException("Error parsing kml file");
        }

        //Read in the csv distance matrix
        Map<Integer, Map<String, Double>> distanceData = new HashMap<Integer, Map<String, Double>>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.distancematrix);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1000);
            String line = bufferedReader.readLine();
            String[] firstRowData = line.split(",");
            for (line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                String[] rowData = line.split(",");
                Map<String, Double> parkData = new HashMap<String, Double>();
                int parkID = Integer.parseInt(rowData[1]);
                distanceData.put(parkID, parkData);
                System.out.println("Reading distances for park: " + parkID);
                for (int i=1; i<rowData.length; i++) {
                    parkData.put(firstRowData[i], Double.parseDouble(rowData[i]));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Make the parks and pass them the distance data
        createParks(distanceData);
        updateNearbyParkList();


        findNearestGreenspace();

        /*
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.powered_by_google_light))
                .position(LONDON)
                .flat(true)
                .rotation(0));
                */
    }

    private void createParks(Map<Integer, Map<String, Double>> distanceData)
    {
        System.out.println("Creating parks");
        mParks = new ArrayList<Park>();
        KmlContainer container =
                mKmlLayer.getContainers().iterator().next().getContainers().iterator().next();
        for (KmlPlacemark pm : container.getPlacemarks()) {
            mParks.add(new Park(pm, distanceData));
        }
        mParksReady = true;
    }

    private void updateNearbyParkList()
    {
        System.out.println("Creating nearby parks");
        if (mNearbyParkPolys != null) {
            for (Polygon poly : mNearbyParkPolys.values()) {
                poly.remove();
            }
        }
        mNearbyParks = new HashMap<Integer, Park>();
        mNearbyParkPolys = new HashMap<Integer, Polygon>();
        for (Park p : mParks) {
            Location loc = U.latLngToLocation(p.getCentroid());
            if (mLocation.distanceTo(loc) < LOCAL_RANGE) {
                mNearbyParks.put(p.getID(), p);

                double estimate = mProcessedData.estimate("NO2", p);
                int pollutionColour = AirData.bandColor(AirData.getBand("NO2", estimate)); //0xffffffff; //0xaarrggbb, want aa = ff

                PolygonOptions polyOpts = new PolygonOptions()
                            .strokeColor(PARK_OUTLINE_COLOUR)
                            .fillColor(pollutionColour)
                            .addAll(p.getPolygon())
                            .clickable(true);

                Polygon poly = mMap.addPolygon(polyOpts);
                mNearbyParkPolys.put(new Integer(p.getID()), poly);
            }
        }
    }

    private void findNearestGreenspace()
    {
        /*
        mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lastLocation.getLatitude(),
                                                 lastLocation.getLongitude())));
                                                 */

        //lastLocation.distanceTo()
        System.out.println("HIHIHIHI");

        double minDistance = Double.POSITIVE_INFINITY;
        Park minPark = null;
        int minBand = 12;



        for (Park p : mNearbyParks.values()) {
            double distance = mLocation.distanceTo(U.latLngToLocation(p.getCentroid()));
            int band = AirData.getBand("NO2", mProcessedData.estimate("NO2", p));

            if ((distance < minDistance && band == minBand) || band < minBand) {
                System.out.println("Found a park");
                //Test: Estimate NO2 at nearby parks
                System.out.println("Park " + p.getID() + " NO2 est = " + mProcessedData.estimate("NO2", p));
                minDistance = distance;
                minPark = p;
                minBand = band;
            }
        }

        if (minPark != null) {
            System.out.println("Adding marker to park at " + minPark.getCentroid().toString());
            if (mCurrentSuggestionMarker != null) {
                mCurrentSuggestionMarker.remove();
            }
            mCurrentSuggestionMarker =
                    mMap.addMarker(new MarkerOptions()
                        .position(minPark.getCentroid())
                        .title("Current Suggestion"));
        } else {
            System.out.println("No parks found");
        }
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] perms, int[] grantResult)
    {
        boolean fail = true;
        if (perms.length == 1 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                setup();
                fail = false;
            } catch (IOException ex) { System.out.println(ex.toString()); }
        }

        if (fail) {
            System.out.println("Returning to home screen");
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        System.out.println("Polygon clicked");
        for (Map.Entry<Integer, Polygon> e : mNearbyParkPolys.entrySet()) {
            if (e.getValue().equals(polygon)) {
                Park p = mNearbyParks.get(e.getKey());

                if (mCurrentSelectedMarker != null) {
                    mCurrentSelectedMarker.remove();
                }

                StringBuilder b = new StringBuilder();
                for (String part : AirData.SPECIES) {
                    double estimate = mProcessedData.estimate(part, p);
                    DecimalFormat df = new DecimalFormat("#.0");

                    b.append(part).append(": ").append(df.format(estimate)).append(' ');
                }

                mCurrentSelectedMarker =
                        mMap.addMarker(new MarkerOptions()
                            .position(p.getCentroid())
                            .snippet(b.toString())
                            .title("Current Selection (mg / m3)"));
                mCurrentSelectedMarker.showInfoWindow();
                break;
            }
        }
    }
}
