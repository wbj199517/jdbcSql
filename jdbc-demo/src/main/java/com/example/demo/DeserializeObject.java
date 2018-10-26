package com.example.demo;


import java.io.File;
import java.io.IOException;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

public class DeserializeObject {
	public static void deserializeObject() throws IOException {
		File file = new File("localFile.avro");
		DatumReader<mockMyGps> userDatumReader = new SpecificDatumReader<mockMyGps>(mockMyGps.class);
		DataFileReader<mockMyGps> dataFileReader = new DataFileReader<mockMyGps>(file, userDatumReader);
		mockMyGps m = null;
		while (dataFileReader.hasNext()) {
		// Reuse user object by passing it to next(). This saves us from
		// allocating and garbage collecting many objects for files with
		// many items.
		m = dataFileReader.next(m);
		
		System.out.println(m);
		
		
		}
	
	}
}
