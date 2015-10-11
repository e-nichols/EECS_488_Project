package main;
import java.sql.*;

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
	
	public SqlSender(String url, String database, String user, String pw){
		DB_URL = "jdbc:mysql://" + url + ":3306/";
		DATABASE = database;
		USER = user;
		PASS = pw;
		full_url = DB_URL + DATABASE + "?user=" + USER + "&password=" + PASS + "&ssl=true";
	}
	
	protected int getNextFreeID(){
		return 0;
	}
	
	public void addUser(String first, String last){

		
		String id = String.valueOf(getNextFreeID());
		String fn = first;
		String ln = last;
		PreparedStatement add = null;
	
	    try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Ran driver.");
			
			// STEP 3: Open a connection
			System.out.print("\nConnecting to database...");
			conn = DriverManager.getConnection(full_url);
			System.out.println(" SUCCESS!\n");
			
			
			
			// STEP 5: Execute query
			System.out.print("\nInserting records into table...");
			
			String sql = "INSERT INTO UserData (UserID, FirstName, LastName) VALUES (?, ?, ?)";	
			add = conn.prepareStatement(sql);
			add.setString(1, id);
			add.setString(2, fn);
			add.setString(3, ln);
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
	
}