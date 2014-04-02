package com.pwn9.PwnCombatLoggers;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;
import java.io.IOException;

public class Config 
{

   private static PwnCombatLoggers plugin;
   
   public Config(PwnCombatLoggers pwncombatloggers) 
   {
      Config.plugin = pwncombatloggers;
   }

   public FileConfiguration getConfig() 
   {
      return plugin.getConfig();
   }

   public void enable() 
   {
	   
      if(!plugin.getDataFolder().exists()) 
      {
         plugin.getConfig();
         plugin.saveDefaultConfig();
         plugin.reloadConfig();
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
      disabledCommands();
   
   }

   private void addDefaultConfig() throws IOException 
   {
      File file = new File(plugin.getDataFolder(), "latestConfig.yml");
      if(! file.exists()) 
      {
         file.createNewFile();
      }
      plugin.getConfig().save(file);
   }

   private void disabledCommands() 
   {
	   plugin.disabledCommands = getConfig().getStringList("disabledCommands");
   }
   
   private void disabledWorlds() 
   {
	   plugin.disabledWorlds = getConfig().getStringList("disabledWorlds"); 
   }

   private void tryUpdate() 
   {
      if(! new File(plugin.getDataFolder(), "config.yml").exists()) 
      {
         plugin.saveDefaultConfig();
         plugin.saveConfig();
      }
   }

   public void reload()
   {
      plugin.reloadConfig();
      plugin.manageConfig();
   }

   public ChatColor parseNameTagColor()
   {
      return ChatColor.getByChar(getConfig().getString("tagColor"));
   }

   public boolean isPVPWorld(EntityDamageByEntityEvent e) 
   {
      return !plugin.disabledWorlds.contains(e.getEntity().getWorld().getName());
   }

   public boolean isPVPWorld(World w)
   {
      return !plugin.disabledWorlds.contains(w.getName());
   }
   
   public boolean isPVPWorld(Player p)
   {
	  return !plugin.disabledWorlds.contains(p.getWorld().getName());
   }

   public boolean isDisabledCommand(String command) 
   {
      for(String s : plugin.disabledCommands)
      {
         if(command.startsWith("/" + s)) return true;
      }
      return false;   
   }
}
