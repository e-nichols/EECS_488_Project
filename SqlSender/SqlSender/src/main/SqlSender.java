package main;
import java.sql.*;
import java.util.Date;

public class SqlSender {
	// Define JDBC driver for MySQL Connection
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	
	String DB_URL;
	String DATABASE;
	String USER;
	String PASS;
	String full_url;
	int nextFreeID;
	Connection conn = null;
	
	SqlSender(){
		DB_URL = "jdbc:mysql://hotspot.nichnologist.net:3306/";
		DATABASE = "HotSpot";
		// This user has only INSERT permissions for this particular database. As such, 
		//  it shouldn't be able to much up anything outside that scope.
		USER = "hotspot";
		PASS = "triplicateparadox";
		// Concatenate entire SQL address using JDBC.
		full_url = DB_URL + DATABASE + "?user=" + USER + "&password=" + PASS + "&ssl=true";
	}
	
	
	/*
	 * Method 'addUser' takes a first and last name as strings and inserts them as a new
	 *  entry into the 'UserData' table. In the future, a unique identifier such as DeviceID
	 *  or MAC address might be used to tie a user to their primary key.
	 * Returns: 0 for success and 1 for error encountered.
	 * Post: Inserts a new row into the UserData table of Hotspot.*
	 */
	public int addUser(String first, String last){

		String fn = first;
		String ln = last;
		PreparedStatement add = null;
	
	    try {
			// Instantiate driver.
			Class.forName(JDBC_DRIVER);
			System.out.println("Ran driver.");
			
			// Open connection to database.
			System.out.print("\nConnecting to database...");
			conn = DriverManager.getConnection(full_url);
			System.out.println(" SUCCESS!\n");
	    }
	    catch(Exception e){
	    	System.out.println("Encountered error with JDBC connection driver.");
	    	e.printStackTrace();
	    	return 1;
	    }
	    
		try{	
			System.out.print("\nInserting records into table...");
			
			/* The PreparedStatement 'add' is a tool that helps prevent SQL Injection
			 *  attacks. By using the '?' identifier and the 'setString' method, PreparedStatements
			 *  eliminate escape characters and malicious user input. So someone putting their 
			 *  last name as 'Smith"); DROP EVERYTHING OH SHIT;' won't damage your tables.
			 */
			String sql = "INSERT INTO UserData (FirstName, LastName) VALUES (?, ?)";	
			add = conn.prepareStatement(sql);
			add.setString(1, fn);
			add.setString(2, ln);
			System.out.println(add);
		}
		catch(SQLException se){
			System.out.println("Encountered error parsing user input into query statement.");
			se.printStackTrace();
			return 1;
		}
		
		try{
			// Execute query over JDBC SQL connection on port 3306 with SSL.
			add.executeUpdate(); 
			
			System.out.println(" SUCCESS!\n");
	
	    } catch(SQLException se) {
	    	System.out.println("Query execution failed with the following errors:");
	        se.printStackTrace();
	        return 1;
	    } 
		finally {
	        try {
	            if(add != null){
	                conn.close();
	                return 0;
	            }
	        } catch(SQLException se) {
	        	return 1;
	        	// Errors here can be ignored.
	        }
	        try {
	            if(conn != null)
	                conn.close();
	        } catch(SQLException se) {
		    	System.out.println("Query failed with the following errors:");
	            se.printStackTrace();
	            return 1;
	        }
	    }
	    System.out.println("Successfully added new user to UserData.");
	    return 0;
	}
	
	public void addLoc(double latitude, double longitude){

		
		String lat = String.valueOf(latitude);
		String lon = String.valueOf(longitude);
		PreparedStatement add = null;
		Date datem = new Date();
		
		java.sql.Timestamp sqlDate = new java.sql.Timestamp(datem.getTime());
		System.out.println("Got current time " + sqlDate);
		
		
		
	    try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);
			System.out.println("Ran driver.");
			
			// STEP 3: Open a connection
			System.out.print("\nConnecting to database...");
			conn = DriverManager.getConnection(full_url);
			System.out.println(" SUCCESS!\n");
			
			
			
			// STEP 5: Execute query
			System.out.print("\nInserting records into table...");
			
			String sql = "INSERT INTO LocData (Time, Lat, Lon) VALUES (?, ?, ?)";	
			add = conn.prepareStatement(sql);
			add.setTimestamp(1, sqlDate);
			add.setString(2, lat);
			add.setString(3, lon);
			System.out.println(add);
			add.executeUpdate(); 
			
			System.out.println(" SUCCESS!\n");
	
	    } catch(SQLException se) {
	        se.printStackTrace();
	    } catch(Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if(add != null)
	                conn.close();
	        } catch(SQLException se) {
	        }
	        try {
	            if(conn != null)
	                conn.close();
	        } catch(SQLException se) {
	            se.printStackTrace();
	        }
	    }
	    System.out.println("Successfully added new location entry to LocData.");
	}
}