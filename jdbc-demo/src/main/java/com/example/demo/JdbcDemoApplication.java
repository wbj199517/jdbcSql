////package com.example.demo;
////
////import java.sql.Connection;
////import java.sql.DriverManager;
////import java.sql.SQLException;
////
////import org.springframework.boot.SpringApplication;
////import org.springframework.boot.autoconfigure.SpringBootApplication;
////
////@SpringBootApplication
////public class JdbcDemoApplication {
////
////	public static void main(String[] args) throws ClassNotFoundException, SQLException {
////		SpringApplication.run(JdbcDemoApplication.class, args);
////		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
////		String connectionUrl = "jdbc:sqlserver://localhost:1433;" +  
////				   "databaseName=AdventureWorks;user=MyUserName;password=*****;";  
////				Connection con = DriverManager.getConnection(connectionUrl);  
////	}
////}
//
//
//===============here copy paste========
//
//package com.example.demo;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//public class ConnectURL {
//
//	public static boolean FLAG = true;
//	public static String saved_time_stamp;
//	public static String saved_id;
//
//	public static void main(String[] args) {
//		checkTable(); // for debug
//		setIdAndTimeStamp(); // for debug
//		
//		
//		setResult(); //get result view
//		
//		checkResult(); //for debug
//		
//	
//
//		theForLoop();
//
//	}
//
//	public static void checkTable() {
//
//		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
//		String SQL = "SELECT * FROM dbo.myGpsPlan";
//		try (Connection con = DriverManager.getConnection(connectionUrl); PreparedStatement stmt = con.prepareStatement(SQL);) {
//			
//			ResultSet rs = stmt.executeQuery();
//			System.out.println("\n" + "Table -> myGpsPlan");
//
//			while (rs.next()) {
//				System.out.println(rs.getString("myGps_id") + " " + rs.getString("last_update"));
//			}
//		}
//
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void setIdAndTimeStamp() {// get info from SQL database: lastMsgUp
//
//		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
//		String SQL = "SELECT * FROM dbo.lastMsgUp";
//
//		try (Connection con = DriverManager.getConnection(connectionUrl); PreparedStatement stmt = con.prepareStatement(SQL);) {
//			ResultSet rs = stmt.executeQuery();
//			while (rs.next()) {
//				saved_time_stamp = rs.getString("time_stamp");
//				saved_id = rs.getString("myGps_id");
//			}
//
//			System.out.println("time_stamp bind to Java saved_time_stamp ->" + saved_time_stamp);
//			System.out.println("myGps_id bind to Java saved_id ->" + saved_id);
//		}
//
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void setResult() { //talk to victor for the order by in view.
//
//		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
//		String SQL = "ALTER VIEW RESULT AS SELECT myGpsPlan.myGps_id, last_update FROM dbo.myGpsPlan join dbo.lastMsgUp on last_update >= time_stamp";
//
//		try (Connection con = DriverManager.getConnection(connectionUrl); PreparedStatement stmt = con.prepareStatement(SQL);) {
//			stmt.executeUpdate();
//			System.out.println("\n");
//
//			System.out.println("VIEW -> RESULT has been updated");
//		}
//
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void checkResult() {
//
//		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
//		String SQL = "SELECT * FROM dbo.RESULT order by last_update";
//
//		try (Connection con = DriverManager.getConnection(connectionUrl); PreparedStatement stmt = con.prepareStatement(SQL);) {
//			ResultSet rs = stmt.executeQuery();
//
//			System.out.println("\n" + "Result:");
//			while (rs.next()) {
//				System.out.println(rs.getString("myGps_id") + " " + rs.getString("last_update"));
//			}
//		}
//
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void theForLoop() {
//
//		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
//		String SQL = "SELECT * FROM dbo.RESULT order by last_update" ;
//
//		try (Connection con = DriverManager.getConnection(connectionUrl); PreparedStatement stmt = con.prepareStatement(SQL);) {
//			ResultSet rs = stmt.executeQuery();
//
//			System.out.println("\n" + "For loop:");
//			while (rs.next()) {
//				setIdAndTimeStamp(); //every loop,from SQL, we update the java local static variable. saved_id and saved_time_stamp
//				
//				String row_lastUpdate = rs.getString("last_update"); // set up each rows info from SQL to java
//
//				String row_myGpsId = rs.getString("myGps_id");
//
//				// debug:
//				// System.out.println("row info: "+row_myGpsId + ' '+ row_lastUpdate);
//				// System.out.println("saved_info: " + saved_id + ' ' + saved_time_stamp);
//
//				// notice: use .equal() here instead of ==
//				if (row_lastUpdate.equals(saved_time_stamp)) {
//					FLAG = false; // for special case
//				}
//				if (row_myGpsId.equals(saved_id) && row_lastUpdate.equals(saved_time_stamp)) {
//					FLAG = true; // case: when this row is already updated to the database.lastMsgUp
//					continue; // then skip this row
//				}
//				if (!(row_myGpsId.equals(saved_id)) && row_lastUpdate.equals(saved_time_stamp)) {
//					FLAG = true; // when time is same but id is not, means it should run from here
//
//				}
//				if (FLAG) {
//					System.out.println("->Call SP here"); // going to deploy this function later
//					System.out.println("->Send to kafka broker here");// going to deploy this function later
//
//					System.out.println("======JDBC update here======");
//					updateTable(row_myGpsId,row_lastUpdate);
////					System.out.println(SQL);
////					stmt.executeUpdate(SQL);
////					System.out.println(row_myGpsId);
////					System.out.println(row_lastUpdate);
//					System.out.println("After binding:");
//					System.out.println(saved_id);
//					System.out.println(saved_time_stamp);
//					
//				}
//			}
//		}
//
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//	public static void updateTable(String r_id, String r_time) {
//
//		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
//		String SQL = String.format("UPDATE lastMsgUp SET myGps_id = '%s',time_stamp = '%s' ",r_id, r_time );
//
//		try (Connection con = DriverManager.getConnection(connectionUrl); PreparedStatement stmt = con.prepareStatement(SQL);) {
//			stmt.executeUpdate();
//			
//		}
//
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		
//	}
//
//}