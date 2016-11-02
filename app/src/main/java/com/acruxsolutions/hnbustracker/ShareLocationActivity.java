package com.acruxsolutions.hnbustracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.UUID;

public class ShareLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Logcat tag
    private static final String TAG = MainActivity.class.getSimpleName();


    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // UI elements
    private TextView lblLocation;
    private EditText busNumberSelect;

    //Switch button
    Switch switchButton;
    TextView textView;
    String switchOn = "Location update is ON";
    String switchOff = "Location update is OF";
    Button btnShowLocation;

    //GeoFire Reference
    GeoFire geoFire;

    //First time run boolean variable
    private Boolean firstTime = null;


    //SharedPreferences variables
    public static final String myRefs = "HnBusTracker";
    public static final String uuidTags = "UUID";
    public static final String firstTimeTag = "firstTime";
    SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);


        // Firstly, check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }


        Firebase.setAndroidContext(this);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);


        // Show location button click listener
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });


        // Toggling the periodic location updates
        switchButton = (Switch) findViewById(R.id.switchButton);
        textView = (TextView) findViewById(R.id.textView);
        busNumberSelect = (EditText) findViewById(R.id.busNumber);


        //Check if this is the first time activity running
        isFirstTime();

        buildGeoFireClient();

        //Listening on touch event
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {

                //if switch button is on
                if (bChecked) {

                    //if bus number is not entered yet
                    if (isEmpty(busNumberSelect)) {
                        Toast.makeText(getApplication(), "Enter Bus Number", Toast.LENGTH_SHORT).show();
                        switchButton.setChecked(false);
                    } else {

                        //bus number entered
                        //start peridical location update
                        togglePeriodicLocationUpdates();

                        //show notification
                        showNotification();
                        textView.setText(switchOn);

                    }

                } else {

                    //wiped out data pushed by user if switch button is off
                    removeLocationData(busNumberSelect.getText().toString() + " - " + sharedpreferences.getString(uuidTags, ""));

                    //clear notification
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(0);

                    textView.setText(switchOff);

                }
            }
        });


    }


    /**
     * Create notification
     */
    public void showNotification() {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.marker)
                        .setContentTitle("Location Updating")
                        .setContentText("Periodical Location updates");

        Intent resultIntent = new Intent(this, ShareLocationActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, 0);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    /**
     * Save UI state changes to the savedInstanceState
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        busNumberSelect = (EditText) findViewById(R.id.busNumber);
        switchButton = (Switch) findViewById(R.id.switchButton);

        if (switchButton != null) {
            savedInstanceState.putBoolean("isChecked", switchButton.isChecked());
        }
        savedInstanceState.putString("busNumberString", busNumberSelect.getText().toString());
    }


    /**
     * Restore UI state from the savedInstanceState.
     * @param savedInstanceState
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean isChecked = savedInstanceState.getBoolean("isChecked");
        String busNumberString = savedInstanceState.getString("busNumberString");

        busNumberSelect = (EditText) findViewById(R.id.busNumber);
        switchButton = (Switch) findViewById(R.id.switchButton);

        busNumberSelect.setText(busNumberString);
        switchButton.setChecked(isChecked);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            lblLocation.setText(latitude + ", " + longitude);

        } else {

            lblLocation
                    .setText("(Couldn't get the location. Turn on GPS and 3G)");
        }
    }

    /**
     * Method to toggle periodic location updates
     */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();


            Log.d(TAG, "Periodic location updates started!");

        } else {

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        int UPDATE_INTERVAL = 10000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        int FATEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int DISPLACEMENT = 10;
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Usupported device!", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        // Assign the new location
        mLastLocation = location;

        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();

        sharedpreferences = this.getSharedPreferences(myRefs, Context.MODE_PRIVATE);

        busNumberSelect = (EditText) findViewById(R.id.busNumber);


        if (busNumberSelect != null) {
            geoFire.setLocation(busNumberSelect.getText().toString() + " - " + sharedpreferences.getString(uuidTags, ""), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, FirebaseError error) {
                    if (error != null) {
                        System.err.println("Error on saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Saved location on server successfully!");
                    }
                }
            });
        }

        // Displaying the new location on UI
        displayLocation();

    }


    //Remove Location data pushed by users
    public void removeLocationData(String keyLocation) {
        buildGeoFireClient();
        geoFire.removeLocation(keyLocation);

    }


    /**
     * Checks if the user is opening the app for the first time.
     * Note that this method should be placed inside an activity and it can be called multiple times.
     *
     * @return boolean
     */
    private boolean isFirstTime() {
        if (firstTime == null) {
            sharedpreferences = this.getSharedPreferences(myRefs, Context.MODE_PRIVATE);

            firstTime = sharedpreferences.getBoolean(firstTimeTag, true);

            if (firstTime) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(firstTimeTag, false);
                editor.putString(uuidTags, genUUID());
                editor.apply();
            }
        }
        return firstTime;
    }

    /**
     * generate unique ID
     * @return string
     */
    private static String genUUID() {
        return UUID.randomUUID().toString();

    }

    /**
     * Build GeoFire Client
     */
    public void buildGeoFireClient() {
        geoFire = new GeoFire(new Firebase("https://hanoibustracker.firebaseio.com"));
    }

    /**
     * Check if edit text is empty
     * @param edtText
     * @return boolean
     */
    private boolean isEmpty(EditText edtText) {
        if (edtText.getText().toString().trim().length() > 0)
            return false;
        return true;
    }


}