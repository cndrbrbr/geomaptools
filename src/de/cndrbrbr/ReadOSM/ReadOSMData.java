package de.cndrbrbr.ReadOSM;
import java.io.*;
import java.net.*;

public class ReadOSMData {

	ReadOSMData (){}

	public boolean importOSM(String south, String north, String west, String east, String tempFilePath)
	{
		// Overpass QL: fetch all ways in bbox, then recurse down to get their nodes
		// bbox order for QL is (south,west,north,east)
		String query = "[out:xml][timeout:60];\n" +
			"(\n" +
			"  way(" + south + "," + west + "," + north + "," + east + ");\n" +
			"  >;\n" +
			");\n" +
			"out body;\n";

		System.out.println("[geomaptools] Overpass query:\n" + query);

		String[] servers = {
			"https://overpass-api.de/api/interpreter",
			"https://lz4.overpass-api.de/api/interpreter"
		};

		for (String server : servers) {
			try {
				System.out.println("[geomaptools] Trying server: " + server);
				boolean ok = doRequest(server, query, tempFilePath);
				if (ok) return true;
			} catch (Exception e) {
				System.out.println("ReadOSMData error on " + server + ": " + e.toString());
			}
		}
		return false;
	}

	private boolean doRequest(String serverUrl, String query, String tempFilePath) throws Exception {
		String postBody = "data=" + URLEncoder.encode(query, "UTF-8");
		byte[] postDataBytes = postBody.getBytes("UTF-8");

		HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl).openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(90000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);

		int code = conn.getResponseCode();
		System.out.println("[geomaptools] HTTP response: " + code + " from " + serverUrl);
		if (code != 200) {
			return false;
		}

		try (InputStream is = conn.getInputStream();
			 FileOutputStream out = new FileOutputStream(tempFilePath)) {
			byte[] buf = new byte[8192];
			int n;
			while ((n = is.read(buf)) >= 0) out.write(buf, 0, n);
		}
		return true;
	}
}
