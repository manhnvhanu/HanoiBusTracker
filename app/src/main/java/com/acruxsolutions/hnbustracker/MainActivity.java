package com.acruxsolutions.hnbustracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
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



        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //status of a network interface variable
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        //check if 3G is on
        boolean isMobileConn = networkInfo.isConnected();


        //if GPS is off
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            //show alert to enable GPS
            showGPSDisabledAlertToUser();

            //if 3G is off
            if(!isMobileConn){

                //show alert to turn on 3G
                show3GDisabledAlertToUser();
            }
        }
        shareLocation.setOnClickListener(this);
        viewBus.setOnClickListener(this);

    }


    /**
     * Show alert to turn on GPS
     */
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Would you like to enable GPS on your device?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * Show alert to turn on 3G
     */
    private void show3GDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Would you like to enable 3G on your device?")
                .setCancelable(false)
                .setPositiveButton("Enable 3G",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent call3GSettingIntent = new Intent(
                                        Settings.ACTION_DATA_ROAMING_SETTINGS);
                                startActivity(call3GSettingIntent);
                            }
                        });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    //Handle many buttons click events
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.shareLocation:
                i = new Intent(MainActivity.this, ShareLocationActivity.class);
                startActivity(i);
                break;

            case R.id.viewBus:
                i = new Intent(MainActivity.this, ViewBusActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }
}

