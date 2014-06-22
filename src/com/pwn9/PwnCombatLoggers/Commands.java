package com.pwn9.PwnCombatLoggers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commands 
{
   private static PwnCombatLoggers plugin;
   
   public Commands(PwnCombatLoggers pwnCombatLoggers)
   {
	   Commands.plugin = pwnCombatLoggers;
   }

   public void onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
   {
	  switch (cmd.getName().toLowerCase()) 
	  {
	  	case "callhit":
	  		callhit(sender, cmd, commandLabel, args);
	  		break;	  
	  	case "callsafe": 
	  		callsafe(sender, cmd, commandLabel, args);
	  		break;
	  	case "pwncl":
	  		pwncl(sender, cmd, commandLabel, args);
	  		break;	  		
	  }
   }
   
   public static void callsafe(CommandSender sender, Command cmd, String commandLabel, String[] args) 
   {
       if(sender.hasPermission("pwncl.callsafe") || sender instanceof ConsoleCommandSender)
       {
          if(args.length == 1) 
          {
             if(! args[0].equalsIgnoreCase("all")) 
             {
                Player p = plugin.getServer().getPlayer(args[0]);
                if(p == null) 
                {
                   sender.sendMessage("§cYou must specify an online player.");
                } 
                else 
                {
                   if(! plugin.isSafe(p.getUniqueId())) 
                   {
                      plugin.callSafe(p);
                      sender.sendMessage("§c" + p.getName() + " is no longer hittable.");
                      plugin.refresh(p);
                   } 
                   else 
                   {
                      sender.sendMessage("§c" + p.getName() + " was not hittable.");
                   }
                }
             } 
             else
             {
                plugin.callSafeAllManual();
             }
          } 
          else
          {
             sender.sendMessage("§cUsage: /callsafe [name] or /callsafe all");
          }
       } 	   
   }
   
   public static void callhit(CommandSender sender, Command cmd, String commandLabel, String[] args) 
   {
	   if(sender.hasPermission("pwncl.callhit") || sender instanceof ConsoleCommandSender) 
       { 
            Player p;
            
            if(args.length == 1) 
            {            
               p = plugin.getServer().getPlayer(args[0]);
               if(p == null)
               {
                  sender.sendMessage("§cYou must specify an online player.");
               }
               else 
               {
                   if(plugin.isSafe(p.getUniqueId())) 
                   {
                      p.damage(1);
                      plugin.addUnsafe(p);
                   }
               }
               
            }
            else
            {
               sender.sendMessage("§cUsage: /callhit [name]");
            }                    
       }
   }
   
   public static void pwncl(CommandSender sender, Command cmd, String commandLabel, String[] args) 
   {
	   if(sender.hasPermission("pwncl.pwncl") || sender instanceof ConsoleCommandSender) 
       {
          if(args.length > 0) {
             if(args[0].equalsIgnoreCase("reload")) 
             {
                plugin.configuration.reload();
                sender.sendMessage("§cSettings reloaded!");
             } 
             else if(args[0].equalsIgnoreCase("save")) 
             {
                plugin.saveConfig();
                sender.sendMessage("§cConfig saved!");
             }             
             else if(args[0].equalsIgnoreCase("setcolor")) 
             {
                if(args.length == 2) 
                {
                    Pattern pattern = Pattern.compile("^[0-9a-fA-F]$");
                    Matcher matcher = pattern.matcher(args[1]);
                    if(matcher.matches()) 
                    {
	                   plugin.setNameTagColor(ChatColor.getByChar(args[1]));
	                   plugin.configuration.getConfig().set("tagColor", plugin.getNameTagColor().getChar());
	                   sender.sendMessage("§cColor changed to: " + plugin.getNameTagColor() + "THIS.");
	                   plugin.saveConfig();
                    }
                    else 
                    {
                    	sender.sendMessage("§cUsage: /pwncl setcolor <colors: 0 - 9, a - f>");
                    }
                }
                else 
                {
                	sender.sendMessage("§cUsage: /pwncl setcolor <color>");
                }
             } 
             else if(args[0].equalsIgnoreCase("safetime")) 
             {
                if(args.length == 2) 
                {
                    Pattern pattern = Pattern.compile("^\\d{1,3}$");
                    Matcher matcher = pattern.matcher(args[1]);
                    if(matcher.matches()) 
                    {
                    	int newtime = Integer.parseInt(args[1]);
	                    plugin.configuration.getConfig().set("tagTime", newtime);
	                    sender.sendMessage("§cSafe Time changed to: " + args[1] +" seconds.");
	                    plugin.saveConfig();
	                    plugin.reloadConfig();
                    }
                    else 
                    {
                    	sender.sendMessage("§cUsage: /pwncl safetime <secs: 0 - 999>");	
                    }
                }
                else 
                {
                	sender.sendMessage("§cUsage: /pwncl safetime <secs>");
                }
             }
             else
             {
                sender.sendMessage("§cUsage: /pwncl [reload|setcolor <color>|safetime <secs>]");
             }               
          }
          else
          {
             sender.sendMessage("§cUsage: /pwncl [reload|setcolor <color>|safetime <secs>]");
          }            
       }	   
   }
   
}