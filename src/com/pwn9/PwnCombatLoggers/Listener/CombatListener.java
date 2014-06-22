package com.pwn9.PwnCombatLoggers.Listener;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.pwn9.PwnCombatLoggers.*;

public class CombatListener implements Listener 
{
   private PwnCombatLoggers plugin;

   public CombatListener(PwnCombatLoggers pt) 
   {
      this.plugin = pt;
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onHit(EntityDamageByEntityEvent e) 
   {

      if(!plugin.configuration.isPVPWorld(e)) return;
      
      if(!plugin.pluginEnabled) return;

      if(e.getDamager() instanceof Snowball) 
      {
    	  e.setCancelled(true);
      }
      
      if(e.getEntity() instanceof Player) 
      {

         Player hitter;
         Player hitted = (Player)e.getEntity();
         
         if(e.getDamager() instanceof Arrow) 
         {
            Arrow arrow = (Arrow)e.getDamager();
            if(arrow.getShooter() instanceof Player) 
            {
               hitter = (Player)arrow.getShooter();
            } 
            else 
            {
               return;
            }
         } 
         else if(e.getDamager() instanceof Player) 
         {
            hitter = (Player)e.getDamager();
         } 
         else if(e.getDamager() instanceof Zombie) 
         {
            if(PvPLoggerMob.isPvPZombie((Zombie)e.getDamager())) 
            {
               if(plugin.isSafe(hitted.getName())) {
            	   e.setCancelled(true);
               }
            }
            return;
         } 
         else 
         {
            return;
         }
         
         // Check here for reasons why not to allow attack
         if (!plugin.isAttackAllowed(hitter, hitted)) 
         {
      	   //PwnCombatLoggers.log(Level.INFO, "Player: " + hitter + " is not allowed to hit " + hitted);
      	   e.setCancelled(true);
         }       
         else if(!e.isCancelled() && plugin.isAttackAllowed(hitter, hitted)) 
         {  
        	//PwnCombatLoggers.log(Level.INFO, "(normal) Player: " + hitter + " has hit " + hitted);
        	
            if(plugin.isSafe(hitted.getName())) 
            {
               plugin.addUnsafe(hitted);
            } 
            else 
            {
               plugin.resetSafeTime(hitted);
            }
            
            if(plugin.isSafe(hitter.getName())) 
            {
               plugin.addUnsafe(hitter);
            } 
            else 
            {
               plugin.resetSafeTime(hitter);
            }	  
         } 
         else 
         {
        	//PwnCombatLoggers.log(Level.INFO, "(cancel) Player: " + hitter + " has hit " + hitted);
        	
            if(!plugin.isSafe(hitted.getName()) && hitter.getInventory().getItemInHand() != null) 
            {
               plugin.resetSafeTime(hitted);
               
               if(plugin.isSafe(hitter.getName())) 
               {
                  if(!plugin.antiPilejump) 
                  {
                     e.setCancelled(false);
                     plugin.addUnsafe(hitter);
                  }
               } 
               else 
               {
                  e.setCancelled(false);
                  plugin.resetSafeTime(hitter);
               }
            }
         }
         
      }
   }
}