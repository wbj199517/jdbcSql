package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;



	
	
	
	public class SerializeObject {

	


	public static void serializeObject(String input1, String input2, Long input3) throws IOException {
		
		mockMyGps mockData;
		Message<mockMyGps> message;
			
		mockMyGps m1 = new mockMyGps(input1,input2,input3);
	
//		
		DatumWriter<mockMyGps> userDatumWriter = new SpecificDatumWriter<mockMyGps>(mockMyGps.class);
		DataFileWriter<mockMyGps> dataFileWriter = new DataFileWriter<mockMyGps>(userDatumWriter);
		dataFileWriter.create(m1.getSchema(), new File("localFile.avro"));
		dataFileWriter.append(m1);

		dataFileWriter.close();
		
		
	}
	}

