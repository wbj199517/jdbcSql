
package com.example.demo;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.lang.String;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;


@SpringBootApplication(scanBasePackages = {"com.example.demo"} , exclude = JpaRepositoriesAutoConfiguration.class)
public class ConnectURL implements CommandLineRunner {
	public static String input1;
	public static String input2;
	public static Long input3;	
	
	public static boolean FLAG = true;
	
	public static String savedTimeStamp;
	public static String savedMyGpsId;
	private static String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=myGPS;IntegratedSecurity=false;user=sa;password=Yt337268; Trusted_Connection=true;";
	public static Logger logger = LoggerFactory.getLogger(ConnectURL.class);
	
	
	
	
	public static void main(String[] args) {
		SpringApplication.run(ConnectURL.class, args);
	}
	
	
	
	
    @Autowired
//    private KafkaTemplate<String, mockMyGps> myGpsTemplate;
    private KafkaTemplate<String, mockMyGps> myGpsTemplate;

    private final CountDownLatch latch = new CountDownLatch(3);
    
//
	@Value("${kafka.topic}")
	private String topic;
//	

    @KafkaListener(topics = "demo")
    public void listen(ConsumerRecord<?, ?> cr) throws Exception {
        logger.info(cr.toString());
        latch.countDown();
    }
    @Override
    public void run(String... args) throws Exception {
    	//setResultView(); // get result view
    	runProducer();

    }


   
	public void setJavaIdAndTimeStamp() {// get info from SQL database: lastMsgUp

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
    

    
	public void runProducer() throws IOException, Exception {

		//String SQL = "SELECT * FROM dbo.RESULT order by last_update";
		String SQL = "SELECT myGpsPlan.myGps_id, last_update FROM dbo.myGpsPlan join dbo.lastMsgUp on last_update >= time_stamp order by last_update";

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			ResultSet rs = stmt.executeQuery();
			//savedTimeStamp 
			//savedMyGpsId
			setJavaIdAndTimeStamp();
			System.out.println("\n" + "For loop:");
			while (rs.next()) {
				
				//setJavaIdAndTimeStamp(); // every loop,from SQL, we update the java local static variable. savedMyGpsId
										// and savedTimeStamp

				String rowLastUpdate = rs.getString("last_update"); // set up each rows info from SQL to java

				String rowMyGpsId = rs.getString("myGps_id");

//				FLAG =true;

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
					System.out.println("->Send to kafka broker here, the serialized JSON file here:");
					mockMyGps mockData = new mockMyGps(input1,input2,input3) ;
					Message<mockMyGps> message = MessageBuilder.withPayload(mockData).setHeader(KafkaHeaders.TOPIC, topic).build();
					
					

				    
					this.myGpsTemplate.send(message);
					// myGpsTemplate.flush();
					
					updateLastMsgUpTable(rowMyGpsId, rowLastUpdate); // this is to update the timestamp and myGPSid for last message 
				}       

			}
		}
		
		
		
		
		
		
//	       latch.await(60, TimeUnit.SECONDS);
//        logger.info("All received");
	// going to deploy this function later
	
//			SerializeObject.serializeObject(input1,input2,input3);
//		    DeserializeObject.deserializeObject();

		catch (SQLException e) {
			e.printStackTrace();
		}
	

	}

    
	public void updateLastMsgUpTable(String r_id, String r_time) {

		String SQL = String.format("UPDATE lastMsgUp SET myGps_id = '%s',time_stamp = '%s' ", r_id, r_time);

		try (Connection con = DriverManager.getConnection(connectionUrl);
				PreparedStatement stmt = con.prepareStatement(SQL);) {
			stmt.executeUpdate();
			savedTimeStamp = r_time;
			savedMyGpsId = r_id;

		}

		catch (SQLException e) {
			e.printStackTrace();
		}

    }
	
    
	public  void callStoredProc(String r_id) {

		String SQL = "{CALL myGpsProc(?)}";
		
		try (Connection con = DriverManager.getConnection(connectionUrl);
				CallableStatement stmt = con.prepareCall(SQL);
				)
				{
			stmt.setString(1,r_id);
			stmt.executeQuery();
			ResultSet rs = stmt.getResultSet();

			if (rs.next()) {
				input1 = rs.getString(1);
			    System.out.println("using myGps Id : ["+ input1 + "] to get specific user information: lastUpdate-> [" + rs.getString(2) + "]");

			}

			 // second result set returning login and pass
/////================MANUL GET RESULT SET AND SET VALUE HERE=============//////////
			if(stmt.getMoreResults()) {
			    rs = stmt.getResultSet();			   
			    while (rs.next()) {		    	
			    	input2 = rs.getString("name");
			        System.out.println(input2);   
			    }
			}
			if(stmt.getMoreResults()) {
			    rs = stmt.getResultSet();			   
			    while (rs.next()) {		   
			    	input3 =rs.getLong("last_update");
			        System.out.println(input3);   
			    }
			}
			
			////////////====================use getString(i++)get all value of every query============/
			
//			while(stmt.getMoreResults()) {
//				System.out.println("Every column ->");
//				rs = stmt.getResultSet();
//				ResultSetMetaData rsmd = rs.getMetaData();
//				int numberOfColumns = rsmd.getColumnCount();
//				while (rs.next()) {
//					for (int i = 1; i <= numberOfColumns; i++) {
//						System.out.println(rs.getString(i));
//					}
//				}
//			}
//            
		
		////////////////////////////////////
				}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}
