package de.cndrbrbr.ReadOSM;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class Overpass {

	private Location playerpos;
	private double lat;
	private double lon;
	private double sizeMeters;

	public Overpass(Location ploc, double lat, double lon, double sizeMeters)
	{
		playerpos = ploc;
		this.lat = lat;
		this.lon = lon;
		this.sizeMeters = sizeMeters;
	}

	public void importOSMData2World()
	{
		try {
			// Calculate bounding box from center lat/lon and size in meters
			double half = sizeMeters / 2.0;
			double centerMeterX = SphericalMercator.lon2x(lon);
			double centerMeterY = SphericalMercator.lat2y(lat);

			double south = SphericalMercator.y2lat(centerMeterY - half);
			double north = SphericalMercator.y2lat(centerMeterY + half);
			double west  = SphericalMercator.x2lon(centerMeterX - half);
			double east  = SphericalMercator.x2lon(centerMeterX + half);

			// Set geo reference to the center of the imported area
			MCWCpoint.setRefLatN(lat);
			MCWCpoint.setRefLonE(lon);
			MCWCpoint.setBlockMeterScale(1.0);

			// Download OSM data to a temp file
			String tempFile = System.getProperty("java.io.tmpdir") + "/geomaptools_osm.xml";
			ReadOSMData importer = new ReadOSMData();
			boolean ok = importer.importOSM(
				String.valueOf(south), String.valueOf(north),
				String.valueOf(west),  String.valueOf(east),
				tempFile
			);
			if (!ok) {
				System.out.println("[geomaptools] Overpass import failed.");
				return;
			}

			Map<String, OSMNode> nodes = new HashMap<>();

			// First pass: collect all nodes
			ReadOSMFile of = new ReadOSMFile(tempFile);
			String line = of.readFirstLine();
			while (line != null) {
				OSMNode nod = OSMNode.OSMNodeAddLine(line);
				if (nod != null) nodes.put(nod.id, nod);
				line = of.readNextLine();
			}

			// Second pass: draw ways
			OSMway way = new OSMway(nodes, playerpos, lat, lon);
			line = of.readFirstLine();
			while (line != null) {
				way.OSMWayAddLine(line);
				line = of.readNextLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
