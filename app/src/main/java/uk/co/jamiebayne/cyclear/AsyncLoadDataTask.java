package uk.co.jamiebayne.cyclear;

import android.os.AsyncTask;

public class AsyncLoadDataTask extends AsyncTask<MainActivity, Void, AirData> {

    MainActivity returnLocation;

    @Override
    protected AirData doInBackground(MainActivity[] params) {
        returnLocation = params[0];
        AirData parser = new AirData();
        parser.loadData();
        return parser;
    }

    @Override
    protected void onPostExecute(AirData data) {
        returnLocation.passData(data);
        System.out.println("Async task completed.");
    }
}
