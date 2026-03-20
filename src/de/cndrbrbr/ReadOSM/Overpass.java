package de.cndrbrbr.ReadOSM;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

public class Overpass {

	private final double bboxLat;      // geo center of the area to download
	private final double bboxLon;
	private final double sizeMeters;

	private final double refLat;       // geo reference = world origin
	private final double refLon;
	private final int    mapOriginX;   // world block coords of the reference point
	private final int    mapOriginY;
	private final int    mapOriginZ;
	private final World  world;

	private String tempFile;

	/**
	 * @param world       Minecraft world to place blocks in
	 * @param bboxLat     geo latitude of the center of the area to download
	 * @param bboxLon     geo longitude of the center of the area to download
	 * @param sizeMeters  side length of the import area in meters
	 * @param refLat      latitude of the map's geo reference point (constant per map)
	 * @param refLon      longitude of the map's geo reference point (constant per map)
	 * @param mapOriginX  world X of the geo reference point (constant per map)
	 * @param mapOriginY  world Y for block placement
	 * @param mapOriginZ  world Z of the geo reference point (constant per map)
	 */
	public Overpass(World world, double bboxLat, double bboxLon, double sizeMeters,
			double refLat, double refLon, int mapOriginX, int mapOriginY, int mapOriginZ)
	{
		this.world       = world;
		this.bboxLat     = bboxLat;
		this.bboxLon     = bboxLon;
		this.sizeMeters  = sizeMeters;
		this.refLat      = refLat;
		this.refLon      = refLon;
		this.mapOriginX  = mapOriginX;
		this.mapOriginY  = mapOriginY;
		this.mapOriginZ  = mapOriginZ;
		this.tempFile    = System.getProperty("java.io.tmpdir") + "/geomaptools_osm.xml";
	}

	/** Download OSM data from Overpass API to temp file. Call from async thread. */
	public boolean downloadOSMData()
	{
		try {
			double half = sizeMeters / 2.0;
			double centerMeterX = SphericalMercator.lon2x(bboxLon);
			double centerMeterY = SphericalMercator.lat2y(bboxLat);

			double south = SphericalMercator.y2lat(centerMeterY - half);
			double north = SphericalMercator.y2lat(centerMeterY + half);
			double west  = SphericalMercator.x2lon(centerMeterX - half);
			double east  = SphericalMercator.x2lon(centerMeterX + half);

			System.out.println("[geomaptools] Overpass bbox: S=" + south + " N=" + north
					+ " W=" + west + " E=" + east);

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
			MCWCpoint.setRefLatN(refLat);
			MCWCpoint.setRefLonE(refLon);
			MCWCpoint.setBlockMeterScale(1.0);

			// Build a scratch location anchored at the map origin
			Location originLoc = new Location(world, mapOriginX, mapOriginY, mapOriginZ);

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
			OSMway way = new OSMway(nodes, originLoc, refLat, refLon);
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
}
