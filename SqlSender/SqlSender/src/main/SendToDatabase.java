package main;

public class SendToDatabase {

	public static void main(String[] args) {
		
		SqlSender sender = new SqlSender();
		//sender.addUser("Nicole", "Dzenowski");
		sender.addLoc(38.971669, -95.23525);
	}

}
