package uk.co.jamiebayne.cyclear;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private AirData data;
    private boolean upToDate = false;
    private Button mainButton;

    //Get data back from asynchronous task
    protected void passData(AirData newData){
        this.data = newData;
        upToDate = true;
        mainButton.setClickable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainButton = (Button) findViewById(R.id.button);
        mainButton.setClickable(false);
        //View

        //Jacques' stuff
        new AsyncLoadDataTask().execute(this);
    }

    public void loadMap(View view) {
        Intent i = new Intent(this, GreenSpace.class);
        i.putExtra("AirData", data);
        startActivity(i);
    }
}
