package com.daawsomest.randomwarp;

import java.util.Random;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomWarp extends JavaPlugin implements Listener {
	
	public static Permission perms = null;
	public static Economy econ = null;
	
	public int radius;
	
	public void onEnable() {
		getLogger().info("Random Warp was enabled!");

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);

		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		radius = getConfig().getInt("radius");
		
		setupPermissions();
		try{
		setupEconomy();
		}catch(Exception e){
			System.out.println("Could not find the Vault file!");
		}
	}

	public void onDisable() {
		// this.saveDefaultConfig();
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	public int generateNum() {
		int x;
		
		int i = new Random().nextInt(2);
		if(i == 0)
			x = (-1)*(new Random().nextInt(radius));
		else
			x = (1)*(new Random().nextInt(radius));
		
		return x;
	}
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		
		if(event.getClickedBlock() == null) {
			return;
		}
		
		else if(event.getClickedBlock().getType().equals(Material.WALL_SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST)) {
			
			Sign sign = (Sign) event.getClickedBlock().getState();
			if(sign.getLine(0).contains("[RandomWarp]")) {
				
				int price = Integer.parseInt((sign.getLine(3).substring(10)));
				
				EconomyResponse r = econ.withdrawPlayer(player.getName(), price);
				if (r.transactionSuccess()) {
					int x = generateNum();
					int z = generateNum();
					int y = 50;
					
					World world = player.getWorld();
					
					while(!(new Location(world, x, y, z).getBlock().isEmpty()) || 
							!(new Location(world, x, y + 1, z).getBlock().isEmpty()) || 
							!(new Location(world, x, y - 1, z).getBlock().getType().isSolid())
							) {
						if(y > 200) {
							x = generateNum();
							z = generateNum();
						}
						
						y++;
					}
					
					player.teleport(new Location(world, x, y, z));
					
					player.sendMessage(ChatColor.YELLOW + "You have warped to a random location");
					player.sendMessage(ChatColor.YELLOW + "$" + price + " was remove from your account");
				} else {
					player.sendMessage(ChatColor.RED + "You do not have $" + price);
				}
				
			}
			
		}
		
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if(event.getLine(0).contains("[RandomWarp]")) {
			if(perms.has(event.getPlayer(), "randomwarp.create")) {
				event.setLine(0, ChatColor.DARK_BLUE + "[RandomWarp]");
				event.setLine(1, "Teleport to a");
				event.setLine(2, "random location");
				event.setLine(3, ChatColor.YELLOW + "Price: " + event.getLine(3));
			} else {
				event.getBlock().breakNaturally();
				event.getPlayer().sendMessage(ChatColor.RED + "You may not place a sign containing" + ChatColor.YELLOW + "[RandomWarp]");
			}
		}
	}
	
}
