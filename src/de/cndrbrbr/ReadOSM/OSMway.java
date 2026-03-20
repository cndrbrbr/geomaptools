package de.cndrbrbr.ReadOSM;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/*
 * 	<way id="38549389">
	<nd ref="456356412"/>
	<nd ref="456356396"/>
	...
	<tag k="highway" v="service"/>
	<tag k="name" v="Hauptstraße"/>
	</way>
*/

public class OSMway {

	private Map<String, OSMNode> nodes;
	private Map<String, String> tags = new HashMap<>();

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

	// World position of the first node of the current way — used for sign placement
	private int signX = 0;
	private int signZ = 0;
	private boolean hasSignPos = false;

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
					signX = originX + (int) Math.round(pt.getBlockX());
					signZ = originZ + (int) Math.round(pt.getBlockZ());
					hasSignPos = true;
					placeBlock(pt);
				} else {
					DrawLine(lastNode, pt);
					lastNode = pt;
				}
			}
		}

		if (waystarted && line.contains("<tag")) {
			OSMtag tag = new OSMtag(line);
			tags.put(tag.k, tag.v);
		}

		if (line.contains("</way>")) {
			// Place sign at first node if the way has a name
			if (hasSignPos) {
				String name = tags.get("name");
				if (name != null && !name.isEmpty()) {
					placeSign(signX, signZ, name);
				}
			}
			// Reset for next way
			firstNode = true;
			lastNode = null;
			hasSignPos = false;
			tags.clear();
			waystarted = false;
		}
	}

	private void placeBlock(MCWCpoint pt) {
		int wx = originX + (int) Math.round(pt.getBlockX());
		int wz = originZ + (int) Math.round(pt.getBlockZ());
		playerPos.setX(wx);
		playerPos.setY(originY);
		playerPos.setZ(wz);
		playerPos.getBlock().setType(Material.GLOWSTONE);
	}

	/**
	 * Place an oak sign on top of the glowstone at (wx, originY, wz).
	 * The way name is split across sign lines (15 chars each).
	 */
	private void placeSign(int wx, int wz, String name) {
		try {
			Block signBlock = playerPos.getWorld().getBlockAt(wx, originY + 1, wz);
			signBlock.setType(Material.OAK_SIGN);
			Block base = playerPos.getWorld().getBlockAt(wx, originY, wz);
			if (base.getType() != Material.GLOWSTONE) {
				base.setType(Material.GLOWSTONE);
			}
			if (signBlock.getState() instanceof Sign) {
				Sign sign = (Sign) signBlock.getState();
				String[] lines = splitIntoLines(name, 15, 4);
				for (int i = 0; i < lines.length; i++) {
					sign.setLine(i, lines[i]);
				}
				sign.update();
			}
		} catch (Exception e) {
			System.out.println("[geomaptools] Sign error at (" + wx + "," + wz + "): " + e);
		}
	}

	/** Split a string into lines of maxLen, up to maxLines. */
	private static String[] splitIntoLines(String text, int maxLen, int maxLines) {
		String[] result = new String[maxLines];
		int pos = 0;
		for (int i = 0; i < maxLines; i++) {
			if (pos >= text.length()) {
				result[i] = "";
			} else {
				int end = Math.min(pos + maxLen, text.length());
				// Try to break at a space
				if (end < text.length() && text.charAt(end) != ' ') {
					int space = text.lastIndexOf(' ', end);
					if (space > pos) end = space;
				}
				result[i] = text.substring(pos, end).trim();
				pos = end;
			}
		}
		return result;
	}

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
		int limit = adx + adz + 2;

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

	public void DrawLine(MCWCpoint p1, MCWCpoint p2, Location loc) {
		DrawLine(p1, p2);
	}
}
