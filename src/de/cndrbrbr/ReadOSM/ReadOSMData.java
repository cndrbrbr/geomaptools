package de.cndrbrbr.ReadOSM;
import java.io.*;
import java.net.*;
import java.util.*;

public class ReadOSMData {

	ReadOSMData (){}

	public boolean importOSM(String south, String north, String west, String east, String tempFilePath)
	{
		try {
	        URL url = new URL("https://lz4.overpass-api.de/api/interpreter");

	        String query =
	        	"<osm-script>" +
	        	"<union>" +
	        	"<bbox-query s=\"" + south + "\" n=\"" + north + "\" w=\"" + west + "\" e=\"" + east + "\"/>" +
	        	"<recurse type=\"node-way\"/>" +
	        	"</union>" +
	        	"<union>" +
	        	"<item/>" +
	        	"<recurse type=\"down\"/>" +
	        	"</union>" +
	        	"<print/>" +
	        	"</osm-script>";

	        String postBody = "data=" + URLEncoder.encode(query, "UTF-8");
	        byte[] postDataBytes = postBody.getBytes("UTF-8");

	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);

	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        FileOutputStream outputStream = new FileOutputStream(tempFilePath);
	        for (int c; (c = in.read()) >= 0;) {
	            outputStream.write(c);
	        }
	        outputStream.close();
	        return true;
		}
		catch (Exception e) {
			System.out.println("ReadOSMData error: " + e.toString());
			return false;
		}
	}
}
