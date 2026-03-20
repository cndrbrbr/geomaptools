package de.cndrbrbr.ReadOSM;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class Overpass {

	private Location playerpos;
	private double lat;
	private double lon;
	private double sizeMeters;
	private String tempFile;

	public Overpass(Location ploc, double lat, double lon, double sizeMeters)
	{
		playerpos = ploc;
		this.lat = lat;
		this.lon = lon;
		this.sizeMeters = sizeMeters;
		tempFile = System.getProperty("java.io.tmpdir") + "/geomaptools_osm.xml";
	}

	/** Download OSM data from Overpass API to temp file. Call from async thread. */
	public boolean downloadOSMData()
	{
		try {
			double half = sizeMeters / 2.0;
			double centerMeterX = SphericalMercator.lon2x(lon);
			double centerMeterY = SphericalMercator.lat2y(lat);

			double south = SphericalMercator.y2lat(centerMeterY - half);
			double north = SphericalMercator.y2lat(centerMeterY + half);
			double west  = SphericalMercator.x2lon(centerMeterX - half);
			double east  = SphericalMercator.x2lon(centerMeterX + half);

			System.out.println("[geomaptools] Overpass bbox: S=" + south + " N=" + north + " W=" + west + " E=" + east);

			ReadOSMData importer = new ReadOSMData();
			boolean ok = importer.importOSM(
				String.valueOf(south), String.valueOf(north),
				String.valueOf(west),  String.valueOf(east),
				tempFile
			);
			if (!ok) {
				System.out.println("[geomaptools] Overpass download failed.");
				return false;
			}
			System.out.println("[geomaptools] Overpass download ok: " + tempFile);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/** Place blocks in the world from previously downloaded data. Call on main thread. */
	public int placeBlocksInWorld()
	{
		try {
			MCWCpoint.setRefLatN(lat);
			MCWCpoint.setRefLonE(lon);
			MCWCpoint.setBlockMeterScale(1.0);

			Map<String, OSMNode> nodes = new HashMap<>();

			// First pass: collect all nodes
			ReadOSMFile of = new ReadOSMFile(tempFile);
			String line = of.readFirstLine();
			while (line != null) {
				OSMNode nod = OSMNode.OSMNodeAddLine(line);
				if (nod != null) nodes.put(nod.id, nod);
				line = of.readNextLine();
			}
			System.out.println("[geomaptools] Nodes loaded: " + nodes.size());

			// Second pass: draw ways
			int[] wayCount = {0};
			OSMway way = new OSMway(nodes, playerpos, lat, lon);
			line = of.readFirstLine();
			while (line != null) {
				if (line.contains("<way")) wayCount[0]++;
				way.OSMWayAddLine(line);
				line = of.readNextLine();
			}
			System.out.println("[geomaptools] Ways drawn: " + wayCount[0]);
			return wayCount[0];

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/** Convenience method: download + place, all on the calling thread. */
	public void importOSMData2World()
	{
		if (downloadOSMData()) {
			placeBlocksInWorld();
		}
	}
}
