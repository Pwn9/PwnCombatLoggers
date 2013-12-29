package com.pwn9.PwnCombatLoggers.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;

import com.pwn9.PwnCombatLoggers.PvPLoggerMob;
import com.pwn9.PwnCombatLoggers.PwnCombatLoggers;

public class PlayerListener implements Listener 
{
   
   private PwnCombatLoggers pwncombatloggers;
  
   public PlayerListener(PwnCombatLoggers pt) 
   {
      this.pwncombatloggers = pt;
   }

   @EventHandler
   public void onDeath(PlayerRespawnEvent e)
   {
	  if(!pwncombatloggers.configuration.isPVPWorld(e.getPlayer())) return;
	  
      pwncombatloggers.safeTimes.remove(e.getPlayer().getName());
      pwncombatloggers.clearFromBoard(e.getPlayer());

      if(pwncombatloggers.useDeathTP)
      {
         pwncombatloggers.deathTimes.put(e.getPlayer().getName(), pwncombatloggers.calcSafeTime(pwncombatloggers.DEATH_TP_DELAY));
      }
   }

   @EventHandler
   public void onTpEvent(PlayerTeleportEvent e) 
   {
	  if(!pwncombatloggers.configuration.isPVPWorld(e.getPlayer())) return;
	  
      if(!pwncombatloggers.isSafe(e.getPlayer().getName()) && pwncombatloggers.disableTeleport) 
      {
         e.setCancelled(true);
         e.getPlayer().sendMessage(ChatColor.RED + "You cannot teleport until you are safe.");
      } 
      else 
      {
         if(pwncombatloggers.deathTimes.containsKey(e.getPlayer().getName()) && pwncombatloggers.useDeathTP) 
         {
            Long deathTime = pwncombatloggers.deathTimes.get(e.getPlayer().getName());
            Long currTime = System.currentTimeMillis();
            if(deathTime > currTime) 
            {
               e.getPlayer().sendMessage("§cYou cannot teleport for " + (pwncombatloggers.DEATH_TP_DELAY / 1000) + " seconds after dying. Time left: §6" + (deathTime / 1000 - currTime / 1000));
               e.setCancelled(true);
            } 
         }
      }
   }

   @EventHandler
   public void entityDeath(EntityDeathEvent e) 
   {
	  if(!pwncombatloggers.configuration.isPVPWorld(e.getEntity().getWorld())) return;
	   
      if(e.getEntity() instanceof Zombie) 
      {
         PvPLoggerMob pz = PvPLoggerMob.getByZombie((Zombie)e.getEntity());
         if(pz != null) 
         {
            PvPLoggerMob.waitingToDie.add(pz.getPlayer());
            pz.despawnDrop(true);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onFlight(PlayerToggleFlightEvent e) 
   {
	  if(!pwncombatloggers.configuration.isPVPWorld(e.getPlayer())) return;
	   
      if(pwncombatloggers.disableFlight && !pwncombatloggers.isSafe(e.getPlayer().getName())) 
      {
         e.getPlayer().setFlying(false);
         e.getPlayer().setAllowFlight(false);
         e.setCancelled(true);
      }
   }

   @EventHandler
   public void onProject(ProjectileLaunchEvent e) 
   {
	  if(!pwncombatloggers.configuration.isPVPWorld(e.getEntity().getWorld())) return;
	  
      if(pwncombatloggers.disableEnderpearls) 
      {
         if(e.getEntity() instanceof EnderPearl) 
         {
            EnderPearl pearl = (EnderPearl)e.getEntity();
            if(pearl.getShooter() instanceof Player) 
            {
               Player p = (Player)pearl.getShooter();
               if(!pwncombatloggers.isSafe(p.getName())) e.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void command(PlayerCommandPreprocessEvent e) 
   {
	  if(!pwncombatloggers.configuration.isPVPWorld(e.getPlayer())) return; 
	  
      if(pwncombatloggers.configuration.isDisabledCommand(e.getMessage()) && !pwncombatloggers.isSafe(e.getPlayer().getName())) 
      {
    	 e.getPlayer().sendMessage("§cYou cannot use that command whilst in combat!");
         e.setCancelled(true);
      }
   }

}