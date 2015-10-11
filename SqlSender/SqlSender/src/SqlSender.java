import java.sql.*;
import java.util.*;

public class SqlSender {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://hotspot.nichnologist.net:3306/HotSpot";
	
	// Database credentials
	static final String USER = "hotspot";
	static final String PASS = "triplicateparadox";
	
	public static void main(String[] args) {
		Connection conn = null;
		Scanner input = new Scanner(System.in);
		String id;
		String fn = null;
		String ln = null;
		PreparedStatement addUser = null;
	
	    try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Ran driver.");
			
			// STEP 3: Open a connection
			System.out.print("\nConnecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println(" SUCCESS!\n");
			
			// STEP 4: Ask for user input
			System.out.print("Enter UserID: ");
			id = input.nextLine();
			
			System.out.print("Enter First Name: ");
			fn = input.nextLine();
			
			System.out.print("Enter Last Name: ");
			ln = input.nextLine();
			
			// STEP 5: Execute query
			System.out.print("\nInserting records into table...");
			
			String sql = "INSERT INTO UserData (UserID, FirstName, LastName) VALUES (?, ?, ?)";	
			addUser = conn.prepareStatement(sql);
			addUser.setString(1, id);
			addUser.setString(2, fn);
			addUser.setString(3, ln);
			System.out.println(addUser);
			addUser.executeUpdate(); 
			
			System.out.println(" SUCCESS!\n");
	
	    } catch(SQLException se) {
	        se.printStackTrace();
	    } catch(Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if(addUser != null)
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
	    System.out.println("Thank you for your patronage!");
	    input.close();
	}
}