package uk.co.jamiebayne.cleair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AirData implements Serializable {

    //Debug flag
    private static final boolean DEBUG = true;

    //Hardcoded list of active site codes
    public static final String[] SITES = new String[]{"GN4", "BX2", "WA9", "MY7", "HR1", "LB5", "KC5", "TH2", "BQ5", "TD5", "CD1", "RI1", "GR7", "ME2", "ST3", "HV1", "BQ6", "LW1", "HG1", "BX9", "ME1", "TD0", "CR5", "KT3", "NB1", "CT6", "WM6", "BX0", "WAA", "GN2", "GB0", "BT6", "BG1", "EA6", "GR5", "GR4", "WM0", "HV3", "CT2", "LB6", "KC2", "EN7", "RB4", "WAB", "CT8", "KC3", "EI1", "LW4", "EN5", "GB6", "KC7", "IS6", "CR8", "CD9", "RB7", "TH6", "EN4", "IM1", "LW3", "ST5", "CR9", "GR9", "CT3", "WA7", "MY1", "LH0", "BQ7", "ME7", "HI0", "SK6", "HG4", "HF4", "WM8", "KC4", "EI8", "KT4", "CD3", "RI2", "BG2", "BX1", "BL0", "GR8", "KC1", "EN1", "BT4", "HR2", "HK6", "LB4", "TH4", "WA8", "WA2", "BQ8", "ST6", "ST8", "IS2", "GN0", "LW2", "SK5", "GN3", "CR7", "ST4", "EA8", "CT4", "TH5", "BT5"};

    //Hardcoded list of species codes
    public static final String[] SPECIES = new String[]{"NO2", "SO2", "O3", "PM25", "PM10", "FINE"};

    //Flag that data has been loaded
    private boolean dataLoaded = false;
    //Place to store the processed data
    private Map<String, Map<String, Double>> processedData;

    //Turn band index into a colour int
    public static int bandColor(int band) {
        switch(band) {
            case 1:
                return 0xff00ff00; //Color.GREEN;
            case 2:
                return 0xffffff00; //Color.YELLOW;
            default:
                return 0xffff0000; //Color.RED;
        }
    }

    //Returns air quality index for given species and measurement (returns -1 for species without defined bands)
    public static int getBand(String speciesCode, double measurement) {
        int band = -1;
        switch(speciesCode) {
            case "NO2":
                if (measurement <= 50) {
                    band = 1;
                } else if (measurement <= 60) {
                    band = 2;
                } else if (measurement <= 200) {
                    band = 3;
                } else if (measurement <= 267) {
                    band = 4;
                } else if (measurement <= 334) {
                    band = 5;
                } else if (measurement <= 400) {
                    band = 6;
                } else if (measurement <= 467) {
                    band = 7;
                } else if (measurement <= 534) {
                    band = 8;
                } else if (measurement <= 600) {
                    band = 9;
                } else {
                    band = 10;
                }
                break;
            case "PM10":
                if (measurement <= 16) {
                    band = 1;
                } else if (measurement <= 33) {
                    band = 2;
                } else if (measurement <= 50) {
                    band = 3;
                } else if (measurement <= 58) {
                    band = 4;
                } else if (measurement <= 66) {
                    band = 5;
                } else if (measurement <= 75) {
                    band = 6;
                } else if (measurement <= 83) {
                    band = 7;
                } else if (measurement <= 91) {
                    band = 8;
                } else if (measurement <= 100) {
                    band = 9;
                } else {
                    band = 10;
                }
                break;
            case "PM25":
                if (measurement <= 11) {
                    band = 1;
                } else if (measurement <= 23) {
                    band = 2;
                } else if (measurement <= 35) {
                    band = 3;
                } else if (measurement <= 41) {
                    band = 4;
                } else if (measurement <= 47) {
                    band = 5;
                } else if (measurement <= 53) {
                    band = 6;
                } else if (measurement <= 58) {
                    band = 7;
                } else if (measurement <= 64) {
                    band = 8;
                } else if (measurement <= 70) {
                    band = 9;
                } else {
                    band = 10;
                }
                break;
            case "O3":
                if (measurement <= 33) {
                    band = 1;
                } else if (measurement <= 66) {
                    band = 2;
                } else if (measurement <= 100) {
                    band = 3;
                } else if (measurement <= 120) {
                    band = 4;
                } else if (measurement <= 140) {
                    band = 5;
                } else if (measurement <= 160) {
                    band = 6;
                } else if (measurement <= 187) {
                    band = 7;
                } else if (measurement <= 213) {
                    band = 8;
                } else if (measurement <= 240) {
                    band = 9;
                } else {
                    band = 10;
                }
                break;
        }
        return band;
    }

    //Constructor
    public AirData() {
        //Initialise everything
        processedData = new HashMap<String, Map<String, Double>>();
    }

    //Pull out data for serialisation
    protected Map<String, Map<String, Double>> getData() {
        return processedData;
    }

    //InputStream to String
    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1000);
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            stringBuilder.append(line);
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    //Process a datum from the JSON data, extracting a measurement and adding the relevant table
    private void processObject(JSONObject datum, String siteCode) throws JSONException {
        String readingString = datum.getString("@Value");
        String speciesCode = datum.getString("@SpeciesCode");
        //Exclude empty readings
        try {
            double reading = Double.parseDouble(readingString);
            //Get observation table from map for given species
            Map<String, Double> observationTable = processedData.get(speciesCode);
            //If new species, initialise a new observation hash table and add it to the top level map
            if (observationTable == null) {
                observationTable = new HashMap<String, Double>();
                processedData.put(speciesCode, observationTable);
            }
            if (DEBUG) {
                System.out.println("@Site: " + siteCode + " | Reading: " + reading);
            }
            //Add reading to species list
            observationTable.put(siteCode, reading);
        } catch (NumberFormatException e) {
            System.err.println("Empty data point at site: " + siteCode + ", species: " + speciesCode);
            //Ignore data point
        }
    }

    //Parse the JSON in the input stream and extract the species measurements
    private void processStream(InputStream inputStream, String siteCode) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject(readStream(inputStream));
        //Should have: jsonObject.getJSONObject("AirQualityData").getString("@SiteCode") == siteCode
        //Get the data
        Object data = jsonObject.getJSONObject("AirQualityData").get("Data");
        //Process single object or array
        if (data instanceof JSONObject) {
            processObject((JSONObject)data, siteCode);
        } else if (data instanceof JSONArray) {
            for (int i=0; i<((JSONArray)data).length(); i++) {
                processObject(((JSONArray)data).getJSONObject(i), siteCode);
            }
        }
    }

    //Load in the data
    public boolean loadData() {
        //Date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmm");

        //Find most recent hour-long time band (at least 70 minutes ago)
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.MINUTE, -70);
        calendar.set(Calendar.MINUTE, 0);
        String endDate = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        String startDate = dateFormat.format(calendar.getTime());

        //Get the data from kcl.ac.uk
        HttpURLConnection urlConnection = null;
        for (String siteCode: SITES) {
            try {
                //Make a HTTP request
                URL url = new URL("http://api.erg.kcl.ac.uk/AirQuality/Data/Site/SiteCode=" + siteCode + "/StartDate=" + startDate + "/EndDate=" + endDate + "/Json");
                if (DEBUG) {
                    System.out.println("#Site: " + siteCode + " | Times: " + startDate + ", " + endDate);
                }
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                //Process the received data
                processStream(inputStream, siteCode);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
        dataLoaded = true;
        return true;
    }

    //Estimate the species measurement at the given park for the given species (micrograms per meter cubed)
    public double estimate(String speciesCode, Park park) {
        if (!dataLoaded) {
            return -1;
        }
        double interpolatedValue = 0, weight = 0;
        //Get the relevant data
        Map<String, Double> speciesData = processedData.get(speciesCode);
        //Build the measurement estimate (inverse distance weighted interpolation)
        for (String siteCode: speciesData.keySet()) {
            double inverseDistance =  (1.0 / park.getDistance(siteCode));
            weight += inverseDistance;
            interpolatedValue += inverseDistance * speciesData.get(siteCode);
        }
        interpolatedValue /= weight;
        return interpolatedValue;
    }
}
