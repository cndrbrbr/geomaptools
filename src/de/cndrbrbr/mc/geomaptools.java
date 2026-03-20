// (c)cndrbrbr 2018 
package de.cndrbrbr.mc;


import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.cndrbrbr.ReadOSM.*;

// reserve :  player.getWorld().strikeLightning(clickpos);

// https://yivesmirror.com/downloads/craftbukkit

public class geomaptools extends JavaPlugin implements Listener, TabCompleter{
	
	playersList state = null;
	Logger log = null;
	String pathtomaps;
	mcbuildgraph gm; // graphic manager

	public void onEnable(){ 
		
		
		
		log = this.getLogger();
		try {
			pathtomaps = getConfig().getString("geo.path");
			saveDefaultConfig();
		}
		catch(Exception e)
		{
			log.info(e.toString());
			saveDefaultConfig();
		}
		
		state = new playersList(log);
		gm = new mcbuildgraph(log);
		log.info("cndrbrbr geotools have been enabled.");
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable(){ 
		this.getLogger().info("spc geotools have been disabled.");
	}
	 @EventHandler
	 public void onBlockBreak(BlockBreakEvent event) {
		 Player p =   event.getPlayer();
		 Block b= event.getBlock();
		 Material mat = b.getType();
		

		  Bukkit.broadcastMessage("Block break! "+p.getName()+" "+mat.name()
		  		+" "+ b.getLocation().getWorld().getName()
				+" "+  b.getLocation().getBlockX()
				+" "+  b.getLocation().getBlockZ()
				+" "+  b.getLocation().getBlockY());
	 }
	 
	 
	 
	 @EventHandler
	 public void onBlockPlaceEvent (BlockPlaceEvent event) {
		 
		 this.getLogger().info("onBlockPlaceEvent !!!");	
		 this.getLogger().info(".");
		 Player player = event.getPlayer(); if (player == null) return;
		 String playername = player.getName(); if (playername == null) return;
		 Location playerplace = player.getLocation(); if (playerplace == null) return;
		 double yaw = event.getPlayer().getLocation().getYaw();
		 this.getLogger().info("..");
	
		 Block clickedblock =  event.getBlockPlaced(); if (clickedblock == null) return;
		 
		 Location blockpos = clickedblock.getLocation(); if (blockpos == null) return;
		 internalCommandState istate = state.GetState(playername); if (istate == null) return;
	//	 Action action = event.getAction(); if (action== null) return;
		 Location clickpos =  clickedblock.getLocation();
		 this.getLogger().info("...");
		 
		 Material mat;
		 
//		 if ((action.equals(Action.RIGHT_CLICK_AIR)||
//			(action.equals(Action.RIGHT_CLICK_BLOCK))))
		 {
			
			this.getLogger().info("onBlockPlaceEvent !!!");	
			
			// Die Farbe des geclickten Blockes holen 
			mat =  clickedblock.getType();
			DyeColor theColor = mappingTables.WoolToDyeColor(mat);
			this.getLogger().info(mat + " " + theColor);  
				 
			  
			 if (state.IsCommandActive(playername,"gquad"))
			 { 
					 if (clickedblock == null) {
						 this.getLogger().info("Block is null");
						 return;
					 }
					 Location loc =  clickedblock.getLocation();
					 if (loc == null) {
						 this.getLogger().info("Location is null");
						 return;
					 }
					 Vector vec = playerplace.getDirection();
					 
					 float pitsch = playerplace.getPitch();
					 this.getLogger().info("direction X "+ vec.getX() + ", Y "+ vec.getY() + ",Z "+ vec.getZ()+ ", yaw "+ yaw + ", pitch " + pitsch);
					 gm.DrawBlocksDirection(mat,theColor, clickpos,istate.Getwidx(),istate.Getheiy(),istate.Getdepz(), yaw);				 

			 }
			 if (state.IsCommandActive(playername,"gforward")) 
			 {
				 this.getLogger().info("gforward selected !!!");

				 				 
				
				 if (clickpos == null) {
					 this.getLogger().info("clickpos is null");
					 return;
				 }
				 
				 gm.DrawBlocksDirection(mat,theColor, clickpos,1,istate.Getheiy(),istate.Getdepz(), yaw);				 
			 
				 						
			}
			 if (state.IsCommandActive(playername,"gdelete")) 
			 {
				 this.getLogger().info("gdelete selected !!!");
				
				 if (clickpos == null) {
					 this.getLogger().info("clickpos is null");
					 return;
				 }
				 
				 gm.RemoveNeighBlocks(mat,theColor, clickpos,istate.getRadius(),(int)clickpos.getX(),(int)clickpos.getY(),(int)clickpos.getZ());				 
			 }
			 
			 if (state.IsCommandActive(playername,"gbuildalong")) 
			 {
				 this.getLogger().info("gbuildalong selected !!!");
				
				 if (clickpos == null) {
					 this.getLogger().info("clickpos is null");
					 return;
				 }
				  
				 gm.BuildOnNeighBlocks(mat,theColor, clickpos,istate.getRadius(),istate.Getheiy(),(int)clickpos.getX(),(int)clickpos.getY(),(int)clickpos.getZ());				 
			 }
			 
			 
			 
			 if (state.IsCommandActive(playername,"gmap")) 
			 {
				 this.getLogger().info("gmap selected !!!");

				 				 
				
				 if (clickpos == null) {
					 this.getLogger().info("clickpos is null");
					 return;
				 }
				 
				 createMap m = new createMap();
				 this.getLogger().info("gmap "+istate.getImagename()+ " "+ pathtomaps);
				 m.doit( clickpos,istate.isGround(),istate.getImagename(),pathtomaps,true,gm);
				 				 
				 
				 						
			} 
			 if (state.IsCommandActive(playername,"gspawnColorSheeps")) 
			 {
				 this.getLogger().info("gspawnColorSheeps selected !!!");

				 				 
				
				 if (clickpos == null) {
					 this.getLogger().info("clickpos is null");
					 return;
				 }
				 
				
				 
				 SpawnCreatures sp = new SpawnCreatures();
				 this.getLogger().info("gspawnColorSheeps "+istate.getNumber());
				 sp.SpawnColorSheeps(clickpos,istate.getNumber(),istate.getColorname());
				 						
			} 
			
				 
				 

		 }
		 
	 }

/*	 @EventHandler
	 public void onBlockPlace(BlockPlaceEvent event) {
		 Player p = event.getPlayer();
		 Block b= event.getBlock();
		 Material mat = b.getType();

		  Bukkit.broadcastMessage("Block place! "+p.getName()+" "+mat.name()
	  		+" "+ b.getLocation().getWorld().getName()
			+" "+  b.getLocation().getBlockX()
			+" "+  b.getLocation().getBlockZ()
			+" "+  b.getLocation().getBlockY());

	 }
*/

	 
	 @EventHandler
	 public void onPlayerMove(PlayerMoveEvent event) {
		 String playername = event.getPlayer().getName();
 		 Location to = event.getTo();
		 Location from = event.getFrom();
		 internalCommandState istate = state.GetState(playername);
	//	  blockinHand = event.getPlayer().getItemInHand().getData().getItemType();
		 
			
		 if (state.IsCommandActive(playername,"gspur")) 
		 {
			 Material mat;
			 DyeColor theColor = null;
			 ItemStack items = event.getPlayer().getInventory().getItemInMainHand();
			 
			 if (items != null) {
				 mat = items.getType();
				 theColor = mappingTables.WoolToDyeColor(mat);
			 }
			 else {
				 mat = state.GetMaterial(playername);
			 }

			 double num1 = event.getFrom().getY();
			 
			 if (state.IsMaterielRedstone(playername)) 
			 {
				 from.setY(from.getY() -2);
				 from.getBlock().setType(Material.REDSTONE_BLOCK);
				 from.setY(num1-1);
				 from.getBlock().setType(Material.POWERED_RAIL);
				 
			 }
			 else {
				 from.setY(from.getY() -1);
				 gm.SetColAndMat ( mat, theColor, from);
				 
			 }
		 }
		 if (istate != null) {
			 istate.setLastx(from.getBlockX());
			 istate.setLasty(from.getBlockY());
			 istate.setLastz(from.getBlockZ());
			 
		 }
	 }
	 
	 
	 
	
	 
	

	
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

		this.getLogger().info("Activted "+ cmd.getName() + " for " + sender);
		if(internalCommandState.IsValidCommand(cmd.getName())){ 

			if (sender instanceof Player) {
				String playername = sender.getName();
				Player player = (Player)sender;
				
				String cmdname = cmd.getName();
				switch (cmdname)  {
					case "ghelp":
					{
						String topic = (args != null && args.length > 0) ? args[0] : "";
						printHelp(player, topic);
					} break;

					case "goff":
					{
						state.CommandOff(playername);
					} break;
					case "glist":				
					{
						String names = GetPicFilenames();
						this.getLogger().info(names);
						player.sendMessage(ChatColor.AQUA + names);
						
					} break;
									
					case "gspur":
					case "gquad":
					case "gmap":
					case "gdelete":
					case "gbuildalong":
					case "gforward":
					{
						String result = state.CommandOn(cmd.getName(),args,playername);
						sender.sendMessage(result);
						this.getLogger().info("Activted "+ cmd.getName() + " for " + playername + " " +result);

					} break;

					case "gspawnColorSheeps":
					{
						state.CommandOn(cmd.getName(),args,playername);
						internalCommandState istate = state.GetState(playername);
						if (istate != null) {
							SpawnCreatures sp = new SpawnCreatures();
							sp.SpawnColorSheeps(player.getLocation(), istate.getNumber(), istate.getColorname());
							this.getLogger().info("gspawnColorSheeps spawned " + istate.getNumber() + " " + istate.getColorname());
						}
						state.CommandOff(playername);
						sender.sendMessage("Spawned sheeps!");
					} break;
					
					case "gload":
					{
						try {
							BufferedImage img = ImageIO.read(new URL("https://www.paulirish.com/lovesyou/new-browser-logos/firefox-256.png"));
							
						    
							if ((img.getHeight()< 500)&&(img.getWidth()<500))
							{
								File outputfile = new File("E:\\MinecraftAG\\mapdata\\saved.png");
							    ImageIO.write(img, "png", outputfile);
							}
						}
						catch (Exception e)
						{
							this.getLogger().info(e.toString());
						}
						state.CommandOff(playername);
					} break;
					
					case "gOSMOverpass":
	
					{
						Location startBlock = ((Player) sender).getLocation();
						Overpass ov = new Overpass(startBlock);
						ov.importOSMData2World();
						
					
					}break;
				
				}// switch					

			}
			
			else {
				sender.sendMessage("this command can only be run by a player");
			}
			
			return true;

		} // true, event behandelt

		return false; 

	}

	private String GetPicFilenames() {
		

        final File folder = new File(pathtomaps);
       
        StringBuilder sb = new StringBuilder();
        sb.append("Pictures> ");
        
        List<String> result = new ArrayList<>();

        search(".*\\.PNG", folder, result);

        for (String s : result) {
        	sb.append(s);
        }     
		
		return sb.toString();
	}
	private  static void search(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                search(pattern, f, result);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                	result.add(",");
                    result.add(f.getName());
                }
            }

        }
    }




	private static final List<String> MATERIALS = java.util.Arrays.asList(
		"coal","gold","diamond","dirt","fence","glass","sand","grass",
		"redstoneblock","glowstone","gravel","poweredrail","potato","apple"
	);

	private static final List<String> COLORS = java.util.Arrays.asList(
		"WHITE","ORANGE","MAGENTA","LIGHT_BLUE","YELLOW","LIME","PINK",
		"GRAY","LIGHT_GRAY","CYAN","PURPLE","BLUE","BROWN","GREEN","RED","BLACK","all"
	);

	private static final List<String> COMMANDS = java.util.Arrays.asList(
		"gspur","goff","gquad","gforward","gmap","glist","gdelete",
		"gbuildalong","gOSMOverpass","gspawnColorSheeps","ghelp"
	);

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

		switch (cmd.getName().toLowerCase()) {
			case "gspur":
				if (args.length == 1) completions.addAll(MATERIALS);
				break;
			case "gquad":
			case "gforward":
				if (args.length == 1) completions.addAll(MATERIALS);
				break;
			case "gspawncolorsheeps":
				if (args.length == 2) completions.addAll(COLORS);
				break;
			case "gmap":
				if (args.length == 1) {
					if (pathtomaps != null) {
						File folder = new File(pathtomaps);
						if (folder.exists() && folder.isDirectory()) {
							for (File f : folder.listFiles()) {
								if (f.isFile() && f.getName().toLowerCase().endsWith(".png"))
									completions.add(f.getName());
							}
						}
					}
				} else if (args.length == 2) {
					completions.add("true");
					completions.add("false");
				}
				break;
			case "ghelp":
				if (args.length == 1) completions.addAll(COMMANDS);
				break;
		}

		List<String> filtered = new ArrayList<>();
		for (String s : completions) {
			if (s.toLowerCase().startsWith(partial)) filtered.add(s);
		}
		return filtered;
	}


	private void printHelp(Player player, String topic) {
		String GOLD = ChatColor.GOLD.toString();
		String AQUA = ChatColor.AQUA.toString();
		String WHITE = ChatColor.WHITE.toString();
		String GRAY = ChatColor.GRAY.toString();

		switch (topic.toLowerCase()) {
			case "gspur":
				player.sendMessage(GOLD + "/gspur [material] [height]");
				player.sendMessage(WHITE + "Leaves a trail of blocks beneath you as you walk.");
				player.sendMessage(GRAY + "Then use /goff to stop.");
				player.sendMessage(GRAY + "Materials: coal gold diamond dirt fence glass sand grass");
				player.sendMessage(GRAY + "  redstoneblock glowstone gravel poweredrail potato apple");
				break;
			case "gquad":
				player.sendMessage(GOLD + "/gquad [material] [width] [height] [depth]");
				player.sendMessage(WHITE + "Places a solid block structure at the next block you place,");
				player.sendMessage(WHITE + "oriented in the direction you face.");
				break;
			case "gforward":
				player.sendMessage(GOLD + "/gforward [material] [length] [height]");
				player.sendMessage(WHITE + "Builds a wall straight ahead from the next block you place.");
				break;
			case "gdelete":
				player.sendMessage(GOLD + "/gdelete [radius]");
				player.sendMessage(WHITE + "Removes all connected blocks of the same material within radius.");
				player.sendMessage(WHITE + "Trigger by placing a block. Default radius: 10.");
				break;
			case "gbuildalong":
				player.sendMessage(GOLD + "/gbuildalong [radius] [height]");
				player.sendMessage(WHITE + "Builds walls on top of connected same-material blocks.");
				player.sendMessage(WHITE + "Trigger by placing a block. Defaults: radius=10 height=2.");
				break;
			case "gmap":
				player.sendMessage(GOLD + "/gmap [filename.PNG] [ground:true|false]");
				player.sendMessage(WHITE + "Places an image as blocks at the next block you place.");
				player.sendMessage(WHITE + "ground=true: flat on floor. ground=false: standing wall.");
				player.sendMessage(GRAY + "Use /glist to see available files.");
				break;
			case "gosmoverpass":
				player.sendMessage(GOLD + "/gOSMOverpass [size in meters]");
				player.sendMessage(WHITE + "Imports a real-world OpenStreetMap area centered on you.");
				break;
			case "gspawncolorsheeps":
				player.sendMessage(GOLD + "/gspawnColorSheeps [count] [color|all]");
				player.sendMessage(WHITE + "Spawns colored sheep at your location. Count: 2-100.");
				player.sendMessage(GRAY + "Colors: WHITE ORANGE MAGENTA LIGHT_BLUE YELLOW LIME PINK");
				player.sendMessage(GRAY + "  GRAY LIGHT_GRAY CYAN PURPLE BLUE BROWN GREEN RED BLACK");
				player.sendMessage(GRAY + "Use \"all\" for rainbow jeb_ sheep.");
				break;
			default:
				player.sendMessage(GOLD + "--- geomaptools commands ---");
				player.sendMessage(AQUA + "/goff" + WHITE + " - stop the active command");
				player.sendMessage(AQUA + "/gspur" + WHITE + " [material] [height] - walk a trail");
				player.sendMessage(AQUA + "/gquad" + WHITE + " [material] [w] [h] [d] - place a box");
				player.sendMessage(AQUA + "/gforward" + WHITE + " [material] [length] [height] - build a wall forward");
				player.sendMessage(AQUA + "/gdelete" + WHITE + " [radius] - flood-fill delete blocks");
				player.sendMessage(AQUA + "/gbuildalong" + WHITE + " [radius] [height] - raise walls along outline");
				player.sendMessage(AQUA + "/gmap" + WHITE + " [file] [ground] - place image as blocks");
				player.sendMessage(AQUA + "/glist" + WHITE + " - list available images");
				player.sendMessage(AQUA + "/gOSMOverpass" + WHITE + " [meters] - import OSM map");
				player.sendMessage(AQUA + "/gspawnColorSheeps" + WHITE + " [count] [color] - spawn sheep");
				player.sendMessage(GRAY + "Type /ghelp <command> for details on any command.");
				break;
		}
	}


}