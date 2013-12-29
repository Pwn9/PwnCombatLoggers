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

   private static PwnCombatLoggers pwncombatloggers;
   
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
	   
      if(!pwncombatloggers.getDataFolder().exists()) 
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
      disabledCommands();
   
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

 // THIS STUFF COULD GET LUMPED INTO 1 QUICK LOAD CONFIG GUY ITS REALLY KLUDGY LIKE THIS.
   
   // get the disabled commands list
   private void disabledCommands() 
   {
	   pwncombatloggers.disabledCommands = getConfig().getStringList("disabledCommands");
   }
   
   // get the disabled worlds list
   private void disabledWorlds() 
   {
	   pwncombatloggers.disabledWorlds = getConfig().getStringList("disabledWorlds"); 
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
      return ChatColor.getByChar(getConfig().getString("tagColor"));
   }

   public boolean isPVPWorld(EntityDamageByEntityEvent e) 
   {
      return !pwncombatloggers.disabledWorlds.contains(e.getEntity().getWorld().getName());
   }

   public boolean isPVPWorld(World w)
   {
      return !pwncombatloggers.disabledWorlds.contains(w.getName());
   }
   
   public boolean isPVPWorld(Player p)
   {
	  return !pwncombatloggers.disabledWorlds.contains(p.getWorld().getName());
   }

   public boolean isDisabledCommand(String command) 
   {
      for(String s : pwncombatloggers.disabledCommands)
      {
         if(command.startsWith("/" + s)) return true;
      }
      return false;   
   }
}
