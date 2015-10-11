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
		USER = "hotspot";
		PASS = "triplicateparadox";
		full_url = DB_URL + DATABASE + "?user=" + USER + "&password=" + PASS + "&ssl=true";
	}
	
	
	
	public void addUser(String first, String last){

		
		String fn = first;
		String ln = last;
		PreparedStatement add = null;
	
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
			
			String sql = "INSERT INTO UserData (FirstName, LastName) VALUES (?, ?)";	
			add = conn.prepareStatement(sql);
			add.setString(1, fn);
			add.setString(2, ln);
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
	    System.out.println("Successfully completed addUser method.");
	}
	
	public void addLoc(double latitude, double longitude){

		
		String lat = String.valueOf(latitude);
		String lon = String.valueOf(longitude);
		PreparedStatement add = null;
		Date datem = new Date();
		double time = datem.getTime();
		System.out.println(time);
		java.sql.Date sqlDate = new java.sql.Date(datem.getTime());
	
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
			add.setDate(1, sqlDate);
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
	    System.out.println("Successfully completed addLoc method.");
	}
}