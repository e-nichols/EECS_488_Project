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
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    Button test_button;
    Location lastLocation;
    LatLng latLon;

    private SqlConnector connector;

    // Used for GET action on Google Sign-in data.
    private Person currentPerson;
    private String personName;
    private String personPhoto;
    private String personGooglePlusProfile;

    private SqlSender sender;

    // Declare object for storing local data after app destroy.
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private GoogleApiClient mMap_GoogleApiClient;
    private static final int RC_SIGN_IN = 0;
    public static final String TAG = Login.class.getSimpleName();

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        connector = new SqlConnector();

        ////// BEGIN NONSTANDARD ///////

        setUpMapIfNeeded();

        buildGoogleApiClient();
        mMap_GoogleApiClient.connect();

        sender = new SqlSender();

        prefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // Connect API Client. MUST be done after client build, which is handled in onCreate.
        mMap_GoogleApiClient.connect();

        test_button = (Button) findViewById(R.id.toast_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.toastShort("Have some toast text!", getApplicationContext());
                goToLastLocation("animate");
                if(sendNewLocationPoint()){
                    Tools.toastShort("send method completed", getApplicationContext());
                }
                else{
                    Tools.toastShort("send method caught exception, returned false", getApplicationContext());
                }
            }
        });

        try {
            goToLastLocation("animate");
        }
        catch(RuntimeException e){
            //Catches an error here on first start. Seems ok on subsequent "onResume"s
        }
        catch(Exception e){
            Tools.toastLong("Caught other exception (not cool): " + e.getMessage(), getApplicationContext());
        }
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_google_sign_out) {
            onSignOutClicked();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    private void setUpMap() {
        //optional map additions go here
    }

    protected synchronized void buildGoogleApiClient() {
        mMap_GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    private void updateLastLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mMap_GoogleApiClient);
        if (lastLocation != null) {
            latLon = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
    }

    private void goToLastLocation(String how){
        updateLastLocation();
        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLon, 13);
        if(how.equals("move")) {
            mMap.moveCamera(position);
        }
        else if(how.equals("animate")){
            mMap.animateCamera(position);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Tools.toastShort("Connection Suspended", getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMap_GoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                    mMap_GoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Tools.toastLong(connectionResult.getErrorMessage(), getApplicationContext());
            }
        } else {
            // Show the signed-out UI
            //Tools.toastShort(getString(R.string.SIGNED_OUT), getApplicationContext());
        }
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
            mMap_GoogleApiClient.connect();
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
        Tools.toastShort("Welcome back " + prefs.getString(getString(R.string.FIRST_NAME), ""), getApplicationContext());
        goToLastLocation("move");

        if (Plus.PeopleApi.getCurrentPerson(mMap_GoogleApiClient) != null) {
            //Tools.toastShort("Current person not null (GOOD)", getApplicationContext());
            currentPerson = Plus.PeopleApi.getCurrentPerson(mMap_GoogleApiClient);
            personName = currentPerson.getDisplayName();
            personPhoto = currentPerson.getImage().getUrl();
            personGooglePlusProfile = currentPerson.getUrl();

            //editor.clear();
            editor.putString("net.nichnologist.hotspot.first_name", currentPerson.getName().getGivenName());
            editor.apply();
            editor.putString("net.nichnologist.hotspot.last_name", currentPerson.getName().getFamilyName());
            editor.apply();
            editor.putString("net.nichnologist.hotspot.google_id", currentPerson.getId());
            editor.apply();
            //Tools.toastShort("Applied prefs", getApplicationContext());
        }
        else{
            Tools.toastShort("Current person is null (BAD)", getApplicationContext());
        }
    }

    private void onSignOutClicked() {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        if (mMap_GoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mMap_GoogleApiClient);
            mMap_GoogleApiClient.disconnect();
        }
        editor.remove(getString(R.string.GOOGLE_ID));
        editor.apply();
        final Intent intent_loginScreen = new Intent(this, Login.class);
        Tools.toastShort("Signed out", getApplicationContext());
        startActivity(intent_loginScreen);
        finish();
    }

    private class SqlConnector extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                SqlSender sender = new SqlSender();
                if( sender.getID(prefs.getString("net.nichnologist.hotspot.google_id", "X")) == 0){
                    sender.addUser(
                            prefs.getString("net.nichnologist.hotspot.first_name", "NULL"),
                            prefs.getString("net.nichnologist.hotspot.last_name", "NULL"),
                            prefs.getString("net.nichnologist.hotspot.google_id", "NULL")
                    );
                }
                sender.addLoc(lastLocation.getLatitude(), lastLocation.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        public void Connect(){
            MapsActivity.SqlConnector task = new SqlConnector();
            task.execute();

            //Tools.toastLong(task.doInBackground(), getApplicationContext());
        }


    }
}
