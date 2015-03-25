package com.pwn9.PwnCombatLoggers;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
	  	case "callclear": 
	  		callclear(sender, cmd, commandLabel, args);
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
                   // might allow callsafe method on offline player
                   sender.sendMessage("§0[§cCOMBAT§0]§c You should specify an online player, trying anyway.");
                   OfflinePlayer po = plugin.getServer().getOfflinePlayer(args[0]);
                   plugin.clearFromBoard(po);
                   plugin.safeTimes.remove(po.getName());
                } 
                else 
                {
                   if(! plugin.isSafe(p.getName())) 
                   {
                      plugin.callSafe(p);
                      sender.sendMessage("§0[§cCOMBAT§0]§c " + p.getName() + " is no longer hittable.");
                      //plugin.refresh(p);
                   } 
                   else 
                   {
                      sender.sendMessage("§0[§cCOMBAT§0]§c " + p.getName() + " was not hittable.");
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
             sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /callsafe [name] or /callsafe all");
          }
       } 	   
   }
   

   public static void callclear(CommandSender sender, Command cmd, String commandLabel, String[] args) 
   {
       if(sender.hasPermission("pwncl.callclear") || sender instanceof ConsoleCommandSender)
       {
          if(args.length == 1) 
          {
            OfflinePlayer p = plugin.getServer().getOfflinePlayer(args[0]);
            if(p != null) 
            {
               // might allow callsafe method on offline player
               sender.sendMessage("§0[§cCOMBAT§0]§c Trying to clear player from board.");
               plugin.clearFromBoard(p);
            } 
          } 
          else
          {
             sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /callclear [name]");
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
                  sender.sendMessage("§0[§cCOMBAT§0]§c You must specify an online player.");
               }
               else 
               {
                   if(plugin.isSafe(p.getName())) 
                   {
                      p.damage(1);
                      plugin.addUnsafe(p);
                   }
               }
               
            }
            else
            {
               sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /callhit [name]");
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
                sender.sendMessage("§0[§cCOMBAT§0]§c Settings reloaded!");
             } 
             else if(args[0].equalsIgnoreCase("save")) 
             {
                plugin.saveConfig();
                sender.sendMessage("§0[§cCOMBAT§0]§c Config saved!");
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
	                   sender.sendMessage("§0[§cCOMBAT§0]§c Color changed to: " + plugin.getNameTagColor() + "THIS.");
	                   plugin.saveConfig();
                    }
                    else 
                    {
                    	sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /pwncl setcolor <colors: 0 - 9, a - f>");
                    }
                }
                else 
                {
                	sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /pwncl setcolor <color>");
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
	                    sender.sendMessage("§0[§cCOMBAT§0]§c Safe Time changed to: " + args[1] +" seconds.");
	                    plugin.saveConfig();
	                    plugin.reloadConfig();
                    }
                    else 
                    {
                    	sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /pwncl safetime <secs: 0 - 999>");	
                    }
                }
                else 
                {
                	sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /pwncl safetime <secs>");
                }
             }
             else
             {
                sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /pwncl [reload|setcolor <color>|safetime <secs>]");
             }               
          }
          else
          {
             sender.sendMessage("§0[§cCOMBAT§0]§c Usage: /pwncl [reload|setcolor <color>|safetime <secs>]");
          }            
       }	   
   }
   
}