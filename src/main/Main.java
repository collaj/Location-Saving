package main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor {
	
	FileConfiguration config;
	
	
	public void onEnable() {
		config = getConfig();
		
		getCommand("savePOI").setExecutor(this);
		getCommand("getPOI").setExecutor(this);
		getCommand("getAllPOI").setExecutor(this);
		getCommand("getClosestPOI").setExecutor(this);
		getCommand("removePOI").setExecutor(this);
	}

	public void onDisable() {
		
	}

	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {

		Player player = null;
		if (sender instanceof Player)
			player = (Player) sender;
		else
			return false;
		
		Environment env = player.getWorld().getEnvironment();
		String environment = null;
		switch (env) {
		case NETHER:
			environment = "nether";
			break;
		case THE_END:
			environment = "end";
			break;
		default:
			environment = "overworld";
		}
		
		
		if(cmd.getName().equalsIgnoreCase("savePOI")) {
			if (args.length != 1) {
				player.sendMessage(ChatColor.RED + "Please enter a name for the location.");
				return false;
			}
			
			if (config.contains(environment + "." + args[0].toLowerCase())) {
				player.sendMessage(ChatColor.RED + "location name already exists, please choose a different name.");
				return false;
			}
			
			String path = environment + "." + args[0].toLowerCase();
			Location location = player.getLocation();
			
			config.createSection(path);
			config.set(path + ".X", location.getBlockX());
			config.set(path + ".Y", location.getBlockY());
			config.set(path + ".Z", location.getBlockZ());
			saveConfig();
			
			Bukkit.broadcastMessage(ChatColor.GREEN + "New location, " + ChatColor.DARK_PURPLE + args[0] + ChatColor.GREEN + ", has been saved for the " + ChatColor.DARK_PURPLE + environment);
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("getPOI")) {
			if (args.length != 1) {
				player.sendMessage(ChatColor.RED + "Please enter a name for the location to find.");
				return false;
			}
			
			String path = environment + "." + args[0].toLowerCase();
			if (!config.contains(path)) {
				player.sendMessage(ChatColor.RED + "this location has not been saved.");
				return false;
			}
			
			player.sendMessage(ChatColor.GREEN + "Location: " + args[0]);
			player.sendMessage(ChatColor.DARK_PURPLE + "X: " + config.getInt(path + ".X"));
			player.sendMessage(ChatColor.DARK_PURPLE + "Y: " + config.getInt(path + ".Y"));
			player.sendMessage(ChatColor.DARK_PURPLE + "Z: " + config.getInt(path + ".Z"));
			
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("getAllPOI")) {
			if (!config.contains(environment)) {
				player.sendMessage(ChatColor.GREEN + "Locations: 0");
				return true;
			}
			Set<String> locations = config.getConfigurationSection(environment).getKeys(false);
			player.sendMessage(ChatColor.GREEN + "Locations: " + locations.size());
			for (String s : locations) {
				player.sendMessage(ChatColor.DARK_PURPLE + s);
			}
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("getClosestPOI")) {
			Location playerLocation = player.getLocation();
			Set<String> locations = config.getConfigurationSection(environment).getKeys(false);
			
			if (locations.isEmpty()) {
				player.sendMessage(ChatColor.RED + "There are no saved locations.");
				return false;
			}
			
			HashMap<String, Double> distances = new HashMap<String, Double>();
			
			for (String place : locations) {
				distances.put(place, playerLocation.distance(new Location(player.getWorld(), 
																		  config.getInt(environment + "." + place + ".X"), 
																		  config.getInt(environment + "." + place + ".Y"), 
																		  config.getInt(environment + "." + place + ".Z"))));
			}
			
			Iterator<String> iterator = distances.keySet().iterator();
			String shortestLocation = null;
			double shortestDistance = Double.MAX_VALUE;
			while (iterator.hasNext()) {
				String key = iterator.next();
				double distance = distances.get(key);
				if (distance < shortestDistance) {
					shortestDistance = distance;
					shortestLocation = key;
				}
			}
			
			player.sendMessage(ChatColor.GREEN + "Location: " + shortestLocation);
			player.sendMessage(ChatColor.DARK_PURPLE + "Distance: " + shortestDistance);
			player.sendMessage(ChatColor.DARK_PURPLE + "X: " + config.getInt(environment + "." + shortestLocation + ".X"));
			player.sendMessage(ChatColor.DARK_PURPLE + "Y: " + config.getInt(environment + "." + shortestLocation + ".Y"));
			player.sendMessage(ChatColor.DARK_PURPLE + "Z: " + config.getInt(environment + "." + shortestLocation + ".Z"));
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("removePOI")) {
			if (args.length != 1) {
				player.sendMessage(ChatColor.RED + "Please enter a name for the location.");
				return false;
			}
			
			if (!config.contains(environment + "." + args[0].toLowerCase())) {
				player.sendMessage(ChatColor.RED + "location is not in the records.");
				return false;
			}
			
			config.getConfigurationSection(environment).set(args[0].toLowerCase(), null);
			saveConfig();
			player.sendMessage(ChatColor.GREEN + "The location has been removed.");
			return true;
		}
		
		else
			return false;
	}
}
