package com.acruxsolutions.hnbustracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton shareLocation, viewBus;

    Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shareLocation = (ImageButton) findViewById(R.id.shareLocation);
        viewBus = (ImageButton) findViewById(R.id.viewBus);

        shareLocation.setOnClickListener(this);
        viewBus.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.shareLocation:
                i= new Intent(MainActivity.this,ShareLocationActivity.class);
                startActivity(i);
                break;

            case R.id.viewBus:
                i= new Intent(MainActivity.this,ViewBusActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }
}
