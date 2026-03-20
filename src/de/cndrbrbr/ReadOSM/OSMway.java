package de.cndrbrbr.ReadOSM;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;

/*
 * 	<way id="38549389">
	<nd ref="456356412"/>
	<nd ref="456356396"/>
	...
	<tag k="highway" v="service"/>
	</way>
*/

public class OSMway {

	private Map<String, OSMNode> nodes;
	private Map<String, String> tags = new HashMap<>();

	// Player's world position is used as the map origin (block 0,0)
	private final int originX;
	private final int originY;
	private final int originZ;
	private final Location playerPos;

	public OSMway(Map<String, OSMNode> nodes2, Location playerpos2, double refLat, double refLon) {
		nodes = nodes2;
		MCWCpoint.setRefLatN(refLat);
		MCWCpoint.setRefLonE(refLon);
		playerPos = playerpos2;
		originX = playerpos2.getBlockX();
		originY = playerpos2.getBlockY();
		originZ = playerpos2.getBlockZ();
	}

	// <nd ref="456356412"/>
	private String GetNodeid(String iline) {
		String line = StringUtls.washstring(iline);
		try {
			String[] parts = line.split(" ");
			for (int i = 1; i < parts.length; i++) {
				if (parts[i] != null && parts[i].contains("=")) {
					String[] sub = parts[i].split("=");
					if ("ref".equals(sub[0])) return sub[1];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean firstNode = true;
	private boolean waystarted = false;
	private MCWCpoint lastNode = null;

	public void OSMWayAddLine(String line) {

		if (line.contains("<way")) { waystarted = true; return; }

		if (waystarted && line.contains("<nd")) {
			String nodeid = GetNodeid(line);
			if (nodeid != null && nodes.containsKey(nodeid)) {
				OSMNode node = nodes.get(nodeid);
				double la = Double.parseDouble(node.lat);
				double lo = Double.parseDouble(node.lon);
				MCWCpoint pt = new MCWCpoint(la, lo, MCWCpoint.ISDEGREE);

				if (firstNode) {
					firstNode = false;
					lastNode = pt;
					placeBlock(pt);
				} else {
					DrawLine(lastNode, pt);
					lastNode = pt;
				}
			}
		}

		if (line.contains("</way>")) {
			firstNode = true;
			lastNode = null;
			tags.clear();
			waystarted = false;
		}

		if (waystarted && line.contains("<tag")) {
			OSMtag tag = new OSMtag(line);
			tags.put(tag.k, tag.v);
		}
	}

	/** Place a single glowstone block at the given MCWCpoint offset from player origin. */
	private void placeBlock(MCWCpoint pt) {
		int wx = originX + (int) Math.round(pt.getBlockX());
		int wz = originZ + (int) Math.round(pt.getBlockZ());
		playerPos.setX(wx);
		playerPos.setY(originY);
		playerPos.setZ(wz);
		playerPos.getBlock().setType(Material.GLOWSTONE);
	}

	/**
	 * Standard Bresenham line from p1 to p2.
	 * Block coordinates are offsets from the player origin.
	 * Safety limit prevents any infinite loop.
	 */
	public void DrawLine(MCWCpoint p1, MCWCpoint p2) {
		if (p1 == null || p2 == null) return;

		int x1 = (int) Math.round(p1.getBlockX());
		int z1 = (int) Math.round(p1.getBlockZ());
		int x2 = (int) Math.round(p2.getBlockX());
		int z2 = (int) Math.round(p2.getBlockZ());

		int adx = Math.abs(x2 - x1);
		int adz = Math.abs(z2 - z1);
		int sx  = x1 < x2 ? 1 : -1;
		int sz  = z1 < z2 ? 1 : -1;
		int err = adx - adz;

		int x = x1, z = z1;
		int limit = adx + adz + 2; // max steps is manhattan distance + slack

		for (int step = 0; step <= limit; step++) {
			playerPos.setX(originX + x);
			playerPos.setY(originY);
			playerPos.setZ(originZ + z);
			playerPos.getBlock().setType(Material.GLOWSTONE);

			if (x == x2 && z == z2) break;

			int e2 = 2 * err;
			if (e2 > -adz) { err -= adz; x += sx; }
			if (e2 <  adx) { err += adx; z += sz; }
		}
	}

	// Keep old signature for any remaining internal callers
	public void DrawLine(MCWCpoint p1, MCWCpoint p2, Location loc) {
		DrawLine(p1, p2);
	}
}
