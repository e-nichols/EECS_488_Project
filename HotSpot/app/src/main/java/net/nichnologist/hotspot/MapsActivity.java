package net.nichnologist.hotspot;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity
            extends AppCompatActivity
            implements NavigationView.OnNavigationItemSelectedListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

    // Map object
    private GoogleMap mMap;
    TileOverlay mOverlay;
    HeatmapTileProvider mProvider;

    Button test_button;

    // Items used in location awarenesss
    Location lastLocation;
    LatLng latLon;
    List<LatLng> list;

    // ASyncTask objects for network actions
    private SqlConnector_PushLoc connector_pushLoc;

    // SqlSender helper object, contains machinery for SSL JDBC connection and queries.
    private SqlSender sender;

    // Declare object for storing local data after app destroy.
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // APIClient object and necessary connection resolution variables
    private GoogleApiClient mMap_GoogleApiClient;
    private static final int RC_SIGN_IN = 0;
    public static final String TAG = Login.class.getSimpleName();
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    /* OnCreate instantiates most of the variables. It builds the activity with Super, and connects
        any interface elements in the XML to their code here in Java. It sets up the map as well.
    PRE: None
    POST: Instantiates LatLng list, ASyncTask connectors, SqlSender helper. Builds and connects
            googleclientapi. Sets up map. Instantiates sharedpreferences and editor.
    RETURN: None
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.checkInButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Checking in...", Snackbar.LENGTH_LONG)
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

        ////// BEGIN NONSTANDARD ///////

        list = new ArrayList<>();
        // This point is a bit of a hack. Rather than catch exceptions or wait for SQL returns, I'm
        //  just forcing an origin point onto the list so it won't be empty. Probably unnecessary but w/e.
        //list.add(new LatLng(0, 0));
        connector_pushLoc = new SqlConnector_PushLoc();
        //connector_getLocs.Connect();

        setUpMapIfNeeded();

        buildGoogleApiClient();
        mMap_GoogleApiClient.connect();

        sender = new SqlSender();

        prefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.apply();


        ////////////// Define UI connectors/buttons /////////////////

        test_button = (Button) findViewById(R.id.toast_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLastLocation("animate");
                /*
                if (sendNewLocationPoint()) {
                    System.out.println("Send new location point");
                } else {
                    Tools.toastShort("send method caught exception, returned false", getApplicationContext());
                }
                */

                sendNewLocationPoint();

            }
        });
    }

    /* setList is a public method for setting values of list from external threads. It is required
        as an interface for ASyncTasks
    PRE: List list is declared (instantiation not required)
    POST: sets list equal to an argument list.
    RETURN: None
     */
    @SuppressWarnings("unchecked")
    public void setList(List l){
        list = l;
    }

    /* OnResume is called when the activity Resumes (after a Pause/Stop that does not Destroy).
    PRE: (OnCreate has always already run)
    POST: Builds map if required, ensures clientapi connected, animates the map to the user's location.
    RETURN: None
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // Connect API Client. MUST be done after client build, which is handled in onCreate.
        mMap_GoogleApiClient.connect();

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

    /* sendNewLocationPoint gets the user's location and send it to the remote database.
    PRE: GoogleAPIClient objects is connected, and API key is valid.
    POST: Stores current location into lastLocation, then calls connector_pushLoc to send.
    RETURN: TRUE if success, FALSE if fail.
     */
    private boolean sendNewLocationPoint() {
        updateLastLocation();
        try {
            connector_pushLoc.Connect();
            //Tools.toastLong(prefs.getString(getString(R.string.FIRST_NAME), "Failed to get firstname from prefs"), getApplicationContext());
            return true;
        }
        catch(Exception e){
            Tools.toastLong(e.getMessage(), getApplicationContext());
            return false;
        }
    }

    /* onBackPressed provides logic for the back button when drawers are open
    PRE: None.
    POST: Closes drawer if open, otherwise performs default back action.
    RETURN: None.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /* Provides action for settings pulldown
    PRE: None
    POST: Inflates pulldown window for settings and signout
    RETURN: Returns true on completion.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps_activity, menu);
        return true;
    }

    /* onOptionsItemSelected detects menu pulldown presses
    PRE: Pulldown is declared
    POST: None
    RETURN: TRUE if press if on defined item, else performs Suepr action.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final Intent menuIntent = new Intent(this, SettingsActivity.class);
            startActivity(menuIntent);
            return true;
        }
        if (id == R.id.action_google_sign_out) {
            onSignOutClicked();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /* onNavigationItemSelected provides actions on sidebar button presses.
    PRE: Sidebar exists, onNavigationItemSelectedListener is implemented.
    POST: Takes action by button case.
    RETURN: TRUE on completion.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.last_30) {
            try{
                new SqlConnector_GetLocs().execute(getTimePairFromNow(0,30));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        } else if (id == R.id.last_60) {
            try{
                new SqlConnector_GetLocs().execute(getTimePairFromNow(1,0));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        } else if (id == R.id.last_120) {
            try{
                new SqlConnector_GetLocs().execute(getTimePairFromNow(2,0));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        } else if (id == R.id.last_360) {
            try{
                new SqlConnector_GetLocs().execute(getTimePairFromNow(6,0));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }else if (id == R.id.last_day) {
            try{
                new SqlConnector_GetLocs().execute(getTimePairFromNow(24,0));
            }
            catch(Exception e){
                e.printStackTrace();
            }
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

    /* Does extra map actions.
    PRE: Map exists (called by setUpMapIfNeeded()).
    POST: Adds additional elements to the map.
    RETURN: None.
     */
    private void setUpMap() {
        try{
            new SqlConnector_GetLocs().execute(getTimePairFromNow(48,0));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /* Generates TimePair object based on time from past to current
    PRE: None.
    POST: None.
    RETURN: TimePair object with 2 timestamps. Uses parameters to find difference between current
             time and past time.
     */
    public TimePair getTimePairFromNow(int hours, int minutes){

            Calendar cal = Calendar.getInstance();
            java.util.Date d1 = cal.getTime();
            cal.add(Calendar.HOUR, hours * (-1));
            cal.add(Calendar.MINUTE, minutes * (-1));
            java.util.Date d2 = cal.getTime();

            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            String time1 = sdf.format(d1);
            String time2 = sdf.format(d2);

            return new TimePair(Timestamp.valueOf(time2), Timestamp.valueOf(time1));
    }

    /* buildGoogleApiClient builds the API client with location services and Plus privileges.
    PRE: mMap_GoogleApiClient is declared.
    POST: Builds API client object.
    RETURN: None.
     */
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

    /* updateLastLocation
    PRE: mMap_GoogleApiClient is connected and API key is valid.
    POST: Stores the current location as a LatLng in lastLocation.
    RETURN: None.
     */
    private void updateLastLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mMap_GoogleApiClient);
        if (lastLocation != null) {
            latLon = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
    }

    /* Helper method that is called to move the map to the user's location.
    PRE: mMap is instantiated
    POST: Moves map to the user's location, either instantly, or by animating to them.
    RETURN: None.
     */
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

    private void clearMap(){
        try {
            mOverlay.remove();
        }
        catch(Exception e){
            System.out.println("Tried to clear map overlay, but was empty.");
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
        }
        /*
        else {
            // Show the signed-out UI
            Tools.toastShort(getString(R.string.SIGNED_OUT), getApplicationContext());
        }
        */
    }

    private void addHeatMap() {


        // best solution so far has been waiting.
        try{
            Thread.sleep(5000);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .radius(20)
                .opacity(0.7)
                .gradient(gradient)
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        mOverlay.isVisible();
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
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mMap_GoogleApiClient);

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


    private class SqlConnector_PushLoc extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                if( sender.getID(prefs.getString(getString(R.string.GOOGLE_ID), "X")) == 0){
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
            MapsActivity.SqlConnector_PushLoc task = new SqlConnector_PushLoc();
            task.execute();

            //Tools.toastLong(task.doInBackground(), getApplicationContext());
        }


    }

    private class SqlConnector_GetLocs extends AsyncTask<TimePair, Void, String> {
        @SuppressWarnings("unchecked")
        @Override
        protected String doInBackground(TimePair... datepair) {
            try {
                SqlSender send = new SqlSender();
                List<LatLng> tempList = (ArrayList<LatLng>) send.getSet(datepair[0].time1, datepair[0].time2);
                setList(tempList);
                if(tempList.isEmpty()){
                    return "fail";
                }
                return "success";
            } catch (Exception e) {
                System.out.println("Caught exception getting locations:");
                e.printStackTrace();
                return "fail";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            if(result.equals("success")){
                clearMap();
                addHeatMap();
            }
            else{
                Tools.toastShort("No recent activity.", getApplicationContext());
                clearMap();
            }
        }

        @Override
        protected void onPreExecute() {
            Tools.toastShort("Retrieving HeatMap...", getApplicationContext());
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }
}
