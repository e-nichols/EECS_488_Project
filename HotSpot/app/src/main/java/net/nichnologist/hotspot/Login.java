package net.nichnologist.hotspot;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class Login extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            View.OnClickListener
            //,ResultCallback<People.LoadPeopleResult>
            {

    // Declare object for storing local data after app destroy.
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private GoogleApiClient mLogin_GoogleApiClient;
    private static final int RC_SIGN_IN = 0;
    public static final String TAG = Login.class.getSimpleName();

    // Used for GET action on Google Sign-in data.
    private Person currentPerson;
    private String personName;
    private String personPhoto;
    private String personGooglePlusProfile;

    private Location lastLocation;
    private SqlConnector connector;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // standard oncreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Instantiate ASync machinery. This is used for running network actions on a new
        //  asynchronous thread.
        connector = new SqlConnector();

        buildGoogleApiClient();

        prefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = prefs.edit();

        // Sets a boolean whether or not this is the first time the application has been run
        try{
            Boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
            if(isFirstRun){
                //Tools.toastShort("First run.", getApplicationContext());
                //editor.clear();
                editor.putBoolean("isFirstRun", false);
                editor.apply();
            }
            if(!isFirstRun){
                //Tools.toastShort("Not first run.", getApplicationContext());
            }
        }
        catch(Exception e){
            Tools.toastShort("Error on reading shared preferences.", getApplicationContext());
        }

        final Intent menuIntent = new Intent(this, MapsActivity.class);


        // Go straight to map if already signed in.
        if(!prefs.getString(getString(R.string.GOOGLE_ID), "X").equals("X")) {
            startActivity(menuIntent);
            finish();
        }


        ////////////// BUTTONS ONLY BELOW HERE IN ONCREATE//////////////////

        FloatingActionButton mapButton = (FloatingActionButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Passing through to map", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(menuIntent);
                finish();
            }
        });

        FloatingActionButton resetButton = (FloatingActionButton) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.clear().apply();
                Snackbar.make(view, "Deleting shared preferences", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton signOutButton = (FloatingActionButton) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignOutClicked();
            }
        });

        FloatingActionButton locationSendButton = (FloatingActionButton) findViewById(R.id.locationSendButton);
        locationSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sendNewLocationPoint()){
                    Tools.toastShort("send method completed", getApplicationContext());
                }
                else{
                    Tools.toastShort("send method caught exception, returned false", getApplicationContext());
                }
            }
        });

        SignInButton googleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignInClicked();
            }
        });

    }

    private boolean sendNewLocationPoint() {
        updateLastLocation();
        try {
            connector.Connect();
            Tools.toastLong(prefs.getString(getString(R.string.FIRST_NAME), "Failed to get firstname from prefs"), getApplicationContext());
            return true;
        }
        catch(Exception e){
            Tools.toastLong(e.getMessage(), getApplicationContext());
            return false;
        }
    }

    private void updateLastLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mLogin_GoogleApiClient);
        /*
        if (lastLocation != null) {
            latLon = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            // Location update actions
        }
        */
    }

    protected synchronized void buildGoogleApiClient() {
        mLogin_GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Tools.toastShort("Connection Suspended", getApplicationContext());
    }



    @Override
    protected void onStart() {
        super.onStart();

        // This forces the googleapiclient to connect when the app starts. Disabling it forces
        //  the user to push the button first.
        if (mLogin_GoogleApiClient != null)
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mLogin_GoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Tools.toastLong(connectionResult.getErrorMessage(), getApplicationContext());
            }
        } else {
            // Show the signed-out UI
            //Tools.toastShort("Signed Out", getApplicationContext());
        }
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mLogin_GoogleApiClient.connect();

        // Show a message to the user that we are signing in.
        Tools.toastShort("Signing in...", getApplicationContext());


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mLogin_GoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        //I THINK THESE ARE REDUNDANT, BUT LEAVING IN CASE THINGS GO WONKY LATER
        //Plus.PeopleApi.loadVisible(mLogin_GoogleApiClient, null).setResultCallback(this);
        //Plus.PeopleApi.load(mLogin_GoogleApiClient, "me");

        // Show the signed-in UI
        //Tools.toastShort("Signed in", getApplicationContext());

        if (Plus.PeopleApi.getCurrentPerson(mLogin_GoogleApiClient) != null) {
            //Tools.toastShort("Current person not null (GOOD)", getApplicationContext());
            currentPerson = Plus.PeopleApi.getCurrentPerson(mLogin_GoogleApiClient);
            personName = currentPerson.getDisplayName();
            personPhoto = currentPerson.getImage().getUrl();
            personGooglePlusProfile = currentPerson.getUrl();

            //editor.clear();
            editor.putString(getString(R.string.FIRST_NAME), currentPerson.getName().getGivenName());
            editor.apply();
            editor.putString(getString(R.string.LAST_NAME), currentPerson.getName().getFamilyName());
            editor.apply();
            editor.putString(getString(R.string.GOOGLE_ID), currentPerson.getId());
            editor.apply();
            //Tools.toastShort("Applied prefs", getApplicationContext());
        }
        else{
            Tools.toastShort("Could not get Google ID data. There was an error.", getApplicationContext());
        }

        final Intent menuIntent = new Intent(this, MapsActivity.class);

        // Go to map as soon as logged in.
        if(!prefs.getString("net.nichnologist.hotspot.google_id", "X").equals("X")) {
            startActivity(menuIntent);
            finish();
        }

    }

    private void onSignOutClicked() {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        if (mLogin_GoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mLogin_GoogleApiClient);
            mLogin_GoogleApiClient.disconnect();
        }

    }

    /*
    @Override
    public void onResult(People.LoadPeopleResult loadPeopleResult) {

    }
    */

    private class SqlConnector extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                SqlSender sender = new SqlSender();
                if( sender.getID(prefs.getString("net.nichnologist.hotspot.google_id", "X")) == 0){
                    sender.addUser(
                            prefs.getString(getString(R.string.FIRST_NAME), "NULL"),
                            prefs.getString(getString(R.string.LAST_NAME), "NULL"),
                            prefs.getString(getString(R.string.GOOGLE_ID), "NULL")
                    );
                }
                sender.addLoc(lastLocation.getLatitude(), lastLocation.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        public void Connect(){
            Login.SqlConnector task = new SqlConnector();
            task.execute();

            //Tools.toastLong(task.doInBackground(), getApplicationContext());
        }


    }


}
