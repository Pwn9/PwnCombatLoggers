package com.pwn9.PwnCombatLoggers.Listener;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.pwn9.PwnCombatLoggers.PvPLoggerZombie;
import com.pwn9.PwnCombatLoggers.PwnCombatLoggers;

public class CombatListener implements Listener 
{
   private PwnCombatLoggers pwncombatloggers;

   public CombatListener(PwnCombatLoggers pt) 
   {
      this.pwncombatloggers = pt;
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onHit(EntityDamageByEntityEvent e) 
   {

      if(! pwncombatloggers.configuration.isPVPWorld(e)) return;
      
      if(! pwncombatloggers.taggingEnabled) return;
      
      if(e.getDamager() instanceof Snowball) e.setCancelled(true);
      
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
            if(PvPLoggerZombie.isPvPZombie((Zombie)e.getDamager())) 
            {
               if(pwncombatloggers.isSafe(hitted.getName())) e.setCancelled(true);
            }
            return;
         } 
         else 
         {
            return;
         }
         
         if(! e.isCancelled()) 
         {
            
            if(pwncombatloggers.isSafe(hitted.getName())) 
            {
               pwncombatloggers.addUnsafe(hitted);
            } 
            else 
            {
               pwncombatloggers.resetSafeTime(hitted);
            }
            if(pwncombatloggers.isSafe(hitter.getName())) 
            {
               pwncombatloggers.addUnsafe(hitter);
            } 
            else 
            {
               pwncombatloggers.resetSafeTime(hitter);
            }
         } 
         else 
         {
            if(! pwncombatloggers.isSafe(hitted.getName()) && hitter.getInventory().getItemInHand() != null) 
            {
               pwncombatloggers.resetSafeTime(hitted);
               if(pwncombatloggers.isSafe(hitter.getName())) 
               {
                  if(! pwncombatloggers.antiPilejump) 
                  {
                     e.setCancelled(false);
                     pwncombatloggers.addUnsafe(hitter);
                  }
               } 
               else 
               {
                  e.setCancelled(false);
                  pwncombatloggers.resetSafeTime(hitter);
               }
            }
         }
      }
   }
}