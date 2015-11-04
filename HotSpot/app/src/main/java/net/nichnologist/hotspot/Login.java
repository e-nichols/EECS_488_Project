package net.nichnologist.hotspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;

public class Login extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    SharedPreferences prefs;

    private GoogleApiClient mLogin_GoogleApiClient;
    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buildGoogleApiClient();

        mLogin_GoogleApiClient.connect();

        prefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        try{
            Boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
            if(isFirstRun){
                Tools.toastShort("First run.", getApplicationContext());
                prefs.edit().putBoolean("isFirstRun", false).apply();
            }
            if(!isFirstRun){
                Tools.toastShort("Not first run.", getApplicationContext());
            }
        }
        catch(Exception e){
            Tools.toastShort("Error on reading shared preferences.", getApplicationContext());
        }

        final Intent menuIntent = new Intent(this, MapsActivity.class);


        FloatingActionButton mapButton = (FloatingActionButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Passing through to map", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(menuIntent);
            }
        });

        FloatingActionButton resetButton = (FloatingActionButton) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().clear().apply();
                Snackbar.make(view, "Dumping shared preferences", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mLogin_GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();
    }

    protected synchronized void buildGoogleApiClient() {
        mLogin_GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Tools.toastShort("onConnected Method", getApplicationContext());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Tools.toastShort("Connection Suspended", getApplicationContext());
    }

    @Override
    public void onConnectionFailed(com.google.android.gms.common.ConnectionResult connectionResult) {
        Tools.toastShort("Connection Failed", getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLogin_GoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLogin_GoogleApiClient.disconnect();
    }

    public void onClick(View v) {
        // ...
    }
}
