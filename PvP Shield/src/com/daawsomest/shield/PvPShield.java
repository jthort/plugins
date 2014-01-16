package com.daawsomest.shield;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PvPShield extends JavaPlugin implements Listener {

	HashMap<String, Long> protections = new HashMap<String, Long>();
	List<String> commandList;

	public void onEnable() {
		getLogger().info("PvP Shield was enabled!");

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);

		getConfig().options().copyDefaults(true);
		this.saveConfig();

		commandList = getConfig().getStringList("blockedCommands");

	}

	public void onDisable() {
		// this.saveDefaultConfig();
		getLogger().info("PvP Shield was disabled!");
	}

	public boolean isProtected(Player player) {
		if (protections.containsKey(player.getName())) {
			if ((protections.get(player.getName()) < (System.currentTimeMillis() - (getConfig().getInt("protectionTime")) * 1000))) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public void activateProtection(Player player) {
		protections.put(player.getName(), System.currentTimeMillis());
	}

	@EventHandler
	public void onPvp(EntityDamageByEntityEvent event) {
		if (getConfig().getBoolean("protectionEnabled")) {
			if (event.getEntity() instanceof Player) {
				if (event.getDamager() instanceof Player) {
					Player damager = (Player) event.getDamager();
					Player damaged = (Player) event.getEntity();

					if (isProtected(damaged)) {
						event.setCancelled(true);
						damager.sendMessage(ChatColor.RED + damaged.getName() + " is protected");
					}

					if (isProtected(damager)) {
						event.setCancelled(true);
						damager.sendMessage(ChatColor.RED + "You can not attack while protected");
					}
				} else if (event.getDamager() instanceof Projectile) {
					LivingEntity shooter = ((Projectile) event.getDamager()).getShooter();
					if (shooter instanceof Player && event.getEntity() instanceof Player) {
						Player pShooter = (Player) shooter;
						if (isProtected(pShooter)) {
							event.setCancelled(true);
							pShooter.sendMessage(ChatColor.RED + "You can not attack while protected");
						} else {
							Player hurtPlayer = (Player) event.getEntity();
							if (isProtected(hurtPlayer)) {
								event.setCancelled(true);
								pShooter.sendMessage(ChatColor.RED + hurtPlayer.getName() + " is protected");
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		activateProtection(event.getPlayer());
	}

	@EventHandler
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String cmd = event.getMessage();
		String[] cmdArgs = cmd.split(" ");

		if (isProtected(player)) {
			if (commandList.contains(cmdArgs[0])) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "You can not use this command while protected");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("shield")) {
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("on")) {
					if (player.hasPermission("shield.admin")) {
						activateProtection(player);
						player.sendMessage(ChatColor.GREEN + "You have been protected");
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permission to do that");
					}
				}

				else if (args[0].equalsIgnoreCase("time")) {
					if (player.hasPermission("shield.admin")) {
						player.sendMessage(ChatColor.YELLOW + "Protection time is set to " + Integer.toString(getConfig().getInt("protectionTime")) + " seconds");
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permission to do that");
					}
				}

				else if (args[0].equalsIgnoreCase("cancel")) {
					if (isProtected(player)) {
						protections.put(player.getName(), new Long(0));
						player.sendMessage(ChatColor.GREEN + "Protection was canceled");
					} else {
						player.sendMessage(ChatColor.RED + "You have no current protections!");
					}
				}

				else if (args[0].equalsIgnoreCase("enable")) {
					if (player.hasPermission("shield.admin")) {
						getConfig().set("protectionEnabled", true);
						saveConfig();
						player.sendMessage(ChatColor.GREEN + "Shields enabled");
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permission to do that");
					}
				}

				else if (args[0].equalsIgnoreCase("disable")) {
					if (player.hasPermission("shield.admin")) {
						getConfig().set("protectionEnabled", false);
						saveConfig();
						player.sendMessage(ChatColor.GREEN + "Shields disabled");
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permission to do that");
					}
				}
			}

			else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("on")) {
					if (player.hasPermission("shield.admin")) {
						Player whoToProtect = Bukkit.getServer().getPlayer(args[1]);
						activateProtection(whoToProtect);
						player.sendMessage(ChatColor.GREEN + whoToProtect.getName() + " has been protected");
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permission to do that");
					}
				}

				else if (args[0].equalsIgnoreCase("add")) {
					if (player.hasPermission("shield.admin")) {
						if (args[1].contains("/")) {
							((List<String>) getConfig().getList("blockedCommands")).add(args[1]);
							this.saveConfig();
							player.sendMessage(ChatColor.YELLOW + args[1] + ChatColor.GREEN + " was add to command list");
						} else {
							player.sendMessage(ChatColor.RED + "Please use a valiad command");
						}
					}

					else {
						player.sendMessage(ChatColor.RED + "You do not have permission to do that");
					}
				}

				else if (args[0].equalsIgnoreCase("remove")) {
					if (player.hasPermission("shield.admin")) {
						if (args[1].contains("/")) {
							((List<String>) getConfig().getList("blockedCommands")).remove(args[1]);
							this.saveConfig();
							player.sendMessage(ChatColor.YELLOW + args[1] + ChatColor.GREEN + " was removed from command list");
						} else {
							player.sendMessage(ChatColor.RED + "Please use a valid command");
						}
					}
				}
			}

			else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("time") && args[1].equalsIgnoreCase("set")) {
					if (player.hasPermission("shield.admin"))
						;
					int seconds;

					try {
						seconds = Integer.parseInt(args[2]);
						getConfig().set("protectionTime", seconds);
						saveConfig();
						player.sendMessage(ChatColor.GREEN + "Protection time set to " + Integer.toString(seconds) + " seconds");
					} catch (NumberFormatException ex) {
						player.sendMessage(ChatColor.RED + "Invalid time");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You do not have permission to do that");
				}
			} else {
				if (player.hasPermission("shield.admin")) {
					player.sendMessage(ChatColor.RED + "/shield on - turn you shield on");
					player.sendMessage(ChatColor.RED + "/shield on <player> - turn shield on for player");
					player.sendMessage(ChatColor.RED + "/shield time - view shield time");
					player.sendMessage(ChatColor.RED + "/shield time set <seconds> - set shield time");
					player.sendMessage(ChatColor.RED + "/shield enable - enable shields");
					player.sendMessage(ChatColor.RED + "/shield disable - disable shields");
					player.sendMessage(ChatColor.RED + "/shield cancel - cancel current shield");
				} else {
					player.sendMessage(ChatColor.RED + "/shield cancel - cancel current shield");
				}
			}
		}
		return true;
	}
}
