package de.cndrbrbr.ReadOSM;

import java.io.*;
import java.util.Properties;

/**
 * Persists the single geo↔world anchor for one map session.
 * All OSM imports of the same map use the same reference so that
 * subsequently imported tiles align with earlier ones.
 */
public class MapReference {

	public double refLat;
	public double refLon;
	public int    originX;  // world block X of the reference point
	public int    originY;
	public int    originZ;
	public double blockMeterScale = 1.0;

	private static final String FILENAME = "mapref.properties";

	public static MapReference load(File dataFolder) {
		File f = new File(dataFolder, FILENAME);
		if (!f.exists()) return null;
		try {
			Properties p = new Properties();
			try (FileReader r = new FileReader(f)) { p.load(r); }
			MapReference ref = new MapReference();
			ref.refLat          = Double.parseDouble(p.getProperty("refLat"));
			ref.refLon          = Double.parseDouble(p.getProperty("refLon"));
			ref.originX         = Integer.parseInt(p.getProperty("originX"));
			ref.originY         = Integer.parseInt(p.getProperty("originY"));
			ref.originZ         = Integer.parseInt(p.getProperty("originZ"));
			ref.blockMeterScale = Double.parseDouble(p.getProperty("blockMeterScale", "1.0"));
			System.out.println("[geomaptools] MapReference loaded: " + ref);
			return ref;
		} catch (Exception e) {
			System.out.println("[geomaptools] Failed to load mapref: " + e);
			return null;
		}
	}

	public void save(File dataFolder) {
		dataFolder.mkdirs();
		File f = new File(dataFolder, FILENAME);
		try {
			Properties p = new Properties();
			p.setProperty("refLat",          String.valueOf(refLat));
			p.setProperty("refLon",          String.valueOf(refLon));
			p.setProperty("originX",         String.valueOf(originX));
			p.setProperty("originY",         String.valueOf(originY));
			p.setProperty("originZ",         String.valueOf(originZ));
			p.setProperty("blockMeterScale", String.valueOf(blockMeterScale));
			try (FileWriter w = new FileWriter(f)) {
				p.store(w, "geomaptools map reference - do not edit manually");
			}
			System.out.println("[geomaptools] MapReference saved: " + this);
		} catch (Exception e) {
			System.out.println("[geomaptools] Failed to save mapref: " + e);
		}
	}

	public static void delete(File dataFolder) {
		new File(dataFolder, FILENAME).delete();
		System.out.println("[geomaptools] MapReference deleted.");
	}

	/**
	 * Convert a world block position back to geo coordinates using this reference.
	 * Returns [lat, lon].
	 */
	public double[] worldToGeo(int worldX, int worldZ) {
		double refMeterX = SphericalMercator.lon2x(refLon);
		double refMeterY = SphericalMercator.lat2y(refLat);

		// Invert MCWCpoint.meter2blocks():
		//   blockX =  meterXs * scale   → meterXs = blockX / scale
		//   blockZ = -meterYs * scale   → meterYs = -blockZ / scale
		double meterXs = (worldX - originX) / blockMeterScale;
		double meterYs = -(worldZ - originZ) / blockMeterScale;

		double lon = SphericalMercator.x2lon(refMeterX + meterXs);
		double lat = SphericalMercator.y2lat(refMeterY + meterYs);
		return new double[]{lat, lon};
	}

	@Override
	public String toString() {
		return String.format("refLat=%.6f refLon=%.6f origin=(%d,%d,%d) scale=%.2f",
				refLat, refLon, originX, originY, originZ, blockMeterScale);
	}
}
