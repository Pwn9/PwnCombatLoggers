package com.pwn9.PwnCombatLoggers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Config 
{

   private static PwnCombatLoggers pwncombatloggers;
   private Set<String> disabledWorlds = new HashSet<String>();
   private Set<String> bannedCommands = new HashSet<String>();
   private Set<String> consoleCommandsSafe = new HashSet<String>();
   private Set<String> playerCommandsSafe = new HashSet<String>();
   private Set<String> consoleCommandsUnsafe = new HashSet<String>();
   private Set<String> playerCommandsUnsafe = new HashSet<String>();

   public Config(PwnCombatLoggers pwncombatloggers) 
   {
      Config.pwncombatloggers = pwncombatloggers;
   }

   public FileConfiguration getConfig() 
   {
      return pwncombatloggers.getConfig();
   }

   public void enable() 
   {
	   
      if(! pwncombatloggers.getDataFolder().exists()) 
      {
         pwncombatloggers.getConfig();
         pwncombatloggers.saveDefaultConfig();
         pwncombatloggers.reloadConfig();
      }
      
      try 
      {
         addDefaultConfig();
      } 
      catch (IOException e) 
      {
         e.printStackTrace();
      }
      
      tryUpdate();
      disabledWorlds();
      bannedCommands();
      commands();
      
   }

   private void commands() 
   {
      if(! getConfig().getBoolean("Tagging.Commands.Enabled")) return;

      String[] commands = getConfig().getString("Tagging.Commands.Console Safe").split(",");
      for(String s : commands)
         this.consoleCommandsSafe.add(s);

      commands = getConfig().getString("Tagging.Commands.Console Unsafe").split(",");
      for(String s : commands)
         this.consoleCommandsUnsafe.add(s);

      commands = getConfig().getString("Tagging.Commands.Player Safe").split(",");
      for(String s : commands)
         this.playerCommandsSafe.add(s);

      commands = getConfig().getString("Tagging.Commands.Player Unsafe").split(",");
      for(String s : commands)
         this.playerCommandsUnsafe.add(s);
   }

   public void performSafeCommands(Player player) 
   {
      for(String s : playerCommandsSafe) 
      {
         player.performCommand(s);
      }
   }

   public void performUnsafeCommands(Player player) 
   {
      for(String s : playerCommandsUnsafe) 
      {
         player.performCommand(s);
      }
   }

   public void performConsoleSafeCommands(Player player) 
   {
      for(String s : consoleCommandsSafe) 
      {
         pwncombatloggers.getServer().dispatchCommand(pwncombatloggers.getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&',s.replaceAll("PLAYERLOCATION", formatLocation(player.getLocation())).replaceAll("PLAYER", player.getName())));
      }
   }
   public void performConsoleUnsafeCommands(Player player)
   {
      for(String s: consoleCommandsUnsafe)
      {
         pwncombatloggers.getServer().dispatchCommand(pwncombatloggers.getServer().getConsoleSender(), ChatColor.translateAlternateColorCodes('&',s.replaceAll("PLAYERLOCATION", formatLocation(player.getLocation())).replaceAll("PLAYER", player.getName())));
      }
   }

   private String formatLocation(Location location) 
   {
      String original = getConfig().getString("Tagging.Commands.PLAYERLOCATION Setup");
      return original.replaceAll("X", location.getBlockX()+"").replaceAll("Y", location.getBlockY()+"").replaceAll("Z", location.getBlockZ()+"").replaceAll("WORLD", location.getWorld().getName());
   }

   private void addDefaultConfig() throws IOException 
   {
      File file = new File(pwncombatloggers.getDataFolder(), "latestConfig.yml");
      if(! file.exists()) 
      {
         file.createNewFile();
      }
      pwncombatloggers.getConfig().save(file);
   }

   private void bannedCommands() 
   {
      String[] banned = getConfig().getString("Tagging.Disabled Commands").split(",");
      for(String s : banned)
         bannedCommands.add(s);
   }

   private void disabledWorlds() 
   {
      String[] disabled = getConfig().getString("Tagging.Disabled Worlds").split(",");
      for(String s : disabled)
         disabledWorlds.add(s);
   }

   private void tryUpdate() 
   {
      if(! new File(pwncombatloggers.getDataFolder(), "config.yml").exists()) 
      {
         pwncombatloggers.saveDefaultConfig();
         pwncombatloggers.saveConfig();
      }
   }

   public void reload()
   {
      pwncombatloggers.reloadConfig();
      pwncombatloggers.manageConfig();
   }

   public ChatColor parseNameTagColor()
   {
      return ChatColor.getByChar(getConfig().getString("Tagging.NameTag Color"));
   }

   public boolean isPVPWorld(EntityDamageByEntityEvent e) 
   {
      return ! disabledWorlds.contains(e.getEntity().getWorld().getName());
   }

   public boolean isPVPWorld(World w)
   {
      return ! disabledWorlds.contains(w.getName());
   }
   
   public boolean isPVPWorld(Player p)
   {
	   return ! disabledWorlds.contains(p.getWorld().getName());
   }

   public boolean isBannedCommand(String command) 
   {
      for(String s : bannedCommands)
      {
         if(command.startsWith("/" + s)) return true;
      }
      return false;
   }
}
