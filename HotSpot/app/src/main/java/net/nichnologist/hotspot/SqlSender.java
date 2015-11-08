package net.nichnologist.hotspot;
import com.google.android.gms.maps.model.LatLng;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlSender {
    // Define JDBC driver for MySQL Connection
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    String DB_URL;
    String DATABASE;
    String USER;
    String PASS;
    String full_url;
    Connection conn = null;

    // Constructor for SqlSender. Essentially builds the string that will become the JDBC address
    //  to send the query.
    SqlSender(){
        // Base address and port of the remote database server
        DB_URL = "jdbc:mysql://hotspot.nichnologist.net:3306/";
        // Name of the MySQL database. JDBC interfaces with the database directly, so it won't call 'USE'.
        DATABASE = "HotSpot";
        // This user has only INSERT permissions for this particular database. As such,
        //  it shouldn't be able to much up anything outside that scope.
        //TODO: Get these parameters out of the git repository and into an abstract string key,
        // then call that key file in as a dependency.
        USER = "hotspot";
        PASS = "triplicateparadox";
        // Concatenate entire SQL address using JDBC.
        full_url = DB_URL + DATABASE + "?user=" + USER + "&password=" + PASS + "&ssl=true";
    }

    public List getSet(){
        PreparedStatement add;
        ResultSet result;
        List list = new ArrayList();

        try {
            // Instantiate driver.
            Class.forName(JDBC_DRIVER);
            System.out.println("Ran driver.");

            // Open connection to database.
            System.out.print("Establishing database connection...");
            conn = DriverManager.getConnection(full_url);
            System.out.println(" SUCCESS!\n");
        }
        catch(Exception e){
            System.out.println("Encountered error with JDBC connection driver.");
            e.printStackTrace(System.out);
            return null;
        }

        try{
            System.out.print("Adding user records to table...");
			
			/* The PreparedStatement 'add' is a tool that helps prevent SQL Injection
			 *  attacks. By using the '?' identifier and the 'setString' method, PreparedStatements
			 *  eliminate escape characters and malicious user input. So someone putting their 
			 *  last name as 'Smith"); DROP EVERYTHING OH SHIT;' won't damage your tables.
			 */
            String sql = "SELECT * from LocData";
            add = conn.prepareStatement(sql);
            System.out.println(add);
        }
        catch(SQLException se){
            System.out.println("Encountered error parsing user input into query statement.");
            se.printStackTrace(System.out);
            return null;
        }

        try{
            // Execute query over JDBC SQL connection on port 3306 with SSL.
            System.out.println("Requesting location data...");
            System.out.println(add);
            result = add.executeQuery();
            //System.out.println(" SUCCESS!\n");

            try{

                while (result.next()) {
                    double lat = result.getDouble("Lat");
                    double lon = result.getDouble("Lon");
                    LatLng latlon = new LatLng(lat,lon);
                    list.add(latlon);
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }

        }
        catch(SQLException se) {
            System.out.println(" FAIL!\n");
            System.out.println("Query execution failed with the following errors:");
            //se.printStackTrace();
            se.printStackTrace(System.out);
            System.out.println("Ensure user input is valid and try again.");
            return null;
        }

        try {
            if(conn != null)
                conn.close();
        } catch(SQLException se) {
            System.out.println("Error closing connection. Was connection opened?");
            se.printStackTrace(System.out);
        }
        System.out.println("Successfully added new user to UserData.");
        return list;
    }

    /*
     * Method 'addUser' takes a first name, last name, and googleID as strings and inserts them
     *  as a new entry into the 'UserData' table.
     * Returns: 0 for success and 1 for error encountered.
     * Post: Inserts a new row into the UserData table of Hotspot.*
     */
    public int addUser(String first, String last, String GoogleID){

        PreparedStatement add;

        try {
            // Instantiate driver.
            Class.forName(JDBC_DRIVER);
            System.out.println("Ran driver.");

            // Open connection to database.
            System.out.print("Establishing database connection...");
            conn = DriverManager.getConnection(full_url);
            System.out.println(" SUCCESS!\n");
        }
        catch(Exception e){
            System.out.println("Encountered error with JDBC connection driver.");
            e.printStackTrace(System.out);
            return 1;
        }

        try{
            System.out.print("Adding user records to table...");
			
			/* The PreparedStatement 'add' is a tool that helps prevent SQL Injection
			 *  attacks. By using the '?' identifier and the 'setString' method, PreparedStatements
			 *  eliminate escape characters and malicious user input. So someone putting their 
			 *  last name as 'Smith"); DROP EVERYTHING OH SHIT;' won't damage your tables.
			 */
            String sql = "INSERT INTO UserData (FirstName, LastName, GoogleID) VALUES (?, ?, ?)";
            add = conn.prepareStatement(sql);
            add.setString(1, first);
            add.setString(2, last);
            add.setString(3, GoogleID);
            System.out.println(add);
        }
        catch(SQLException se){
            System.out.println("Encountered error parsing user input into query statement.");
            se.printStackTrace(System.out);

            return 1;
        }

        try{
            // Execute query over JDBC SQL connection on port 3306 with SSL.
            System.out.println("Executing SQL query on remote database...");
            add.executeUpdate();

            System.out.println(" SUCCESS!\n");

        }
        catch(SQLException se) {
            System.out.println(" FAIL!\n");
            System.out.println("Query execution failed with the following errors:");
            //se.printStackTrace();
            se.printStackTrace(System.out);
            System.out.println("Ensure user input is valid and try again.");
            return 1;
        }

        try {
            if(conn != null)
                conn.close();
        } catch(SQLException se) {
            System.out.println("Error closing connection. Was connection opened?");
            se.printStackTrace(System.out);
        }
        System.out.println("Successfully added new user to UserData.");
        return 0;
    }

    /*
     * Method 'addLoc' takes a latitude and longitude as doubles. Google gives these to 7 decimal
     *  places, so that's what the table has room for.
     * Returns: 0 for success and 1 for error encountered.
     * Post: Inserts a new row into the LocData table of Hotspot.*
     */
    public int addLoc(double latitude, double longitude){


        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);
        PreparedStatement add;
        Date datem = new Date();

        java.sql.Timestamp sqlDate = new java.sql.Timestamp(datem.getTime());
        System.out.println("Got current time " + sqlDate);



        try {
            // Instantiate driver.
            Class.forName(JDBC_DRIVER);
            System.out.println("Ran driver.");
        }
        catch(Exception e){
            System.out.println("Encountered error with JDBC connection driver.");
            e.printStackTrace(System.out);
            return 1;
        }
        try{
            // Open connection to database
            System.out.print("Establishing database connection...");
            conn = DriverManager.getConnection(full_url);
            System.out.println(" SUCCESS!\n");
        }
        catch(SQLException se){
            System.out.println("Error encountered connecting to SQL database.");
            se.printStackTrace(System.out);
            return 1;
        }


        try{
            System.out.print("Adding location records to table...");

            String sql = "INSERT INTO LocData (Time, Lat, Lon) VALUES (?, ?, ?)";
            add = conn.prepareStatement(sql);
            add.setTimestamp(1, sqlDate);
            add.setString(2, lat);
            add.setString(3, lon);
            System.out.println(add);
            add.executeUpdate();

            System.out.println(" SUCCESS!\n");
        }
        catch(SQLException se){
            System.out.println("Encountered error parsing user input into query statement.");
            se.printStackTrace(System.out);
            return 1;
        }

        try {
            if(conn != null)
                conn.close();
        } catch(SQLException se) {
            System.out.println("Error closing connection. Was connection opened?");
            se.printStackTrace(System.out);
        }
        System.out.println("Successfully added new location entry to LocData.");
        return 0;
    }

    /*
     * Method 'getID' takes a string googleID, then returns the user's UserID primary key. It returns
     *  a value of 0 if the user's GoogleID is not found in the table.
     * Returns:
     *          0 for 'not in database'
     *          -1 for error
     *          valid int(12) value if they exist
     * Post: none
     */
    public int getID (String GoogleID){

        PreparedStatement add;
        ResultSet result;

        try {
            // Instantiate driver.
            Class.forName(JDBC_DRIVER);
            System.out.println("Ran driver.");
        }
        catch(Exception e){
            System.out.println("Encountered error with JDBC connection driver.");
            e.printStackTrace(System.out);
            return -1;
        }
        try{
            // Open connection to database
            System.out.print("Establishing database connection...");
            conn = DriverManager.getConnection(full_url);
            System.out.println(" SUCCESS!\n");
        }
        catch(SQLException se){
            System.out.println("Error encountered connecting to SQL database.");
            se.printStackTrace(System.out);
            return -1;
        }

        try{
            System.out.print("Querying table...");

            String sql = "SELECT UserID from UserData where GoogleID=?";
            add = conn.prepareStatement(sql);
            add.setString(1, GoogleID);
            System.out.println(add);
            result = add.executeQuery();
            System.out.println(" SUCCESS!\n");
        }
        catch(SQLException se){
            System.out.println("Encountered error parsing user input into query statement.");
            se.printStackTrace(System.out);
            return -1;
        }

        try {
            result.first();
            int returnValue = result.getInt(1);
            System.out.println(returnValue);

            if(conn != null)
                conn.close();
            return returnValue;
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
            return 0;
        }
        catch(SQLException se) {
            System.out.println("Error: GoogleID does not exist.");
            se.printStackTrace(System.out);
            return 0;
        }
    }
}