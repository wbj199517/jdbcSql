
package com.example.demo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ConnectURL {

	public static boolean FLAG = true;
	public static String savedTimeStamp;
	public static String savedMyGpsId;
	private static String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";

	public static void main(String[] args) {

		setResultView(); // get result view

		theForLoop();

	}

	public static void checkTable() {

		String SQL = "SELECT * FROM dbo.myGpsPlan";
		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {

			ResultSet rs = stmt.executeQuery();
			System.out.println("\n" + "Table -> myGpsPlan");

			while (rs.next()) {
				System.out.println(rs.getString("myGps_id") + " " + rs.getString("last_update"));
			}
		}

		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void setJavaIdAndTimeStamp() {// get info from SQL database: lastMsgUp

		String SQL = "SELECT * FROM dbo.lastMsgUp";

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				savedTimeStamp = rs.getString("time_stamp");
				savedMyGpsId = rs.getString("myGps_id");
			}

			System.out.println("time_stamp bind to Java savedTimeStamp ->" + savedTimeStamp);
			System.out.println("myGps_id bind to Java savedMyGpsId ->" + savedMyGpsId);
		}

		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void setResultView() { // talk to victor for the order by in view.

		String SQL = "ALTER VIEW RESULT AS SELECT myGpsPlan.myGps_id, last_update FROM dbo.myGpsPlan join dbo.lastMsgUp on last_update >= time_stamp";

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			stmt.executeUpdate();
			System.out.println("\n");

			System.out.println("VIEW -> RESULT has been updated");
		}

		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void checkResult() {

		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
		String SQL = "SELECT * FROM dbo.RESULT order by last_update";

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			ResultSet rs = stmt.executeQuery();

			System.out.println("\n" + "Result:");
			while (rs.next()) {
				System.out.println(rs.getString("myGps_id") + " " + rs.getString("last_update"));
			}
		}

		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void theForLoop() {

		String SQL = "SELECT * FROM dbo.RESULT order by last_update";

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			ResultSet rs = stmt.executeQuery();

			System.out.println("\n" + "For loop:");
			while (rs.next()) {
				setJavaIdAndTimeStamp(); // every loop,from SQL, we update the java local static variable. savedMyGpsId
										// and savedTimeStamp

				String rowLastUpdate = rs.getString("last_update"); // set up each rows info from SQL to java

				String rowMyGpsId = rs.getString("myGps_id");

			

				// notice: use .equal() here instead of ==
				if (rowLastUpdate.equals(savedTimeStamp)) {
					FLAG = false; // for special case
				}
				if (rowMyGpsId.equals(savedMyGpsId) && rowLastUpdate.equals(savedTimeStamp)) {
					FLAG = true; // case: when this row is already updated to the database.lastMsgUp
					continue; // then skip this row
				}
				if (!(rowMyGpsId.equals(savedMyGpsId)) && rowLastUpdate.equals(savedTimeStamp)) {
					FLAG = true; // when time is same but id is not, means it should run from here

				}
				if (FLAG) {
					System.out.println("->Call SP here"); // going to deploy this function later
					callStoredProc(rowMyGpsId);
					System.out.println("->Send to kafka broker here");// going to deploy this function later

					System.out.println("======JDBC update here======");
//					updateLastMsgUpTable(rowMyGpsId, rowLastUpdate);


				}
			}
		}

		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void updateLastMsgUpTable(String r_id, String r_time) {

		String SQL = String.format("UPDATE lastMsgUp SET myGps_id = '%s',time_stamp = '%s' ", r_id, r_time);

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			stmt.executeUpdate();

		}

		catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	
	public static void callStoredProc(String r_id) {

		String SQL = "{CALL myGpsProc(?)}";
		
		try (Connection con = DriverManager.getConnection(connectionUrl);
				CallableStatement stmt = con.prepareCall(SQL);
				)
				{
			stmt.setString(1,r_id);
			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			if (rs.next()) {
			    System.out.println("using myGps Id : ["+ rs.getString(1) + "] to get specific user information: lastUpdate-> [" + rs.getString(2) + "]");

			}

			 // second result set returning login and pass
/////================MANUL GET RESULT SET AND SET VALUE HERE=============//////////
			if(stmt.getMoreResults()) {
			    rs = stmt.getResultSet();			   
			    while (rs.next()) {		    	
			        System.out.println(rs.getString("name"));   
			    }
			}
			if(stmt.getMoreResults()) {
			    rs = stmt.getResultSet();			   
			    while (rs.next()) {		    	
			        System.out.println(rs.getString("last_update"));   
			    }
			}
			
			////////////====================use getString(i++)get all value of every query============/
//			ResultSetMetaData rsmd = rs.getMetaData();
//		     int numberOfColumns = rsmd.getColumnCount();
//			while(stmt.getMoreResults()) {
//				
//				
//					
//				     System.out.println("Every Info->");
//				     rs = stmt.getResultSet();
//				     while(rs.next()) {
//				     for(int i=1;i <= numberOfColumns; i++) {
//				    	 System.out.println(rs.getString(i));   
//
//				     }
//				}
//			}
            
		
		////////////////////////////////////
				}
		catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
}
