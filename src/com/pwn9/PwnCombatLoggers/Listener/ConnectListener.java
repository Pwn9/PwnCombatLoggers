package com.pwn9.PwnCombatLoggers.Listener;

import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.pwn9.PwnCombatLoggers.PvPLoggerZombie;
import com.pwn9.PwnCombatLoggers.PwnCombatLoggers;

public class ConnectListener implements Listener 
{
   private PwnCombatLoggers pwncombatloggers;
   private long lastLogout = System.currentTimeMillis();

   public ConnectListener(PwnCombatLoggers pt) 
   {
      this.pwncombatloggers = pt;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) 
   {
	   if(PvPLoggerZombie.waitingToDie.contains(e.getPlayer().getName())) 
	   {
		   e.getPlayer().setHealth(0);
		   PvPLoggerZombie.waitingToDie.remove(e.getPlayer().getName());
	   } 	   
	   
      PvPLoggerZombie pz = PvPLoggerZombie.getByOwner(e.getPlayer().getName());
      if(pz != null) 
      {
         pwncombatloggers.addUnsafe(e.getPlayer());
         e.getPlayer().teleport(pz.getZombie().getLocation());
         pz.despawnNoDrop(true, true);
         e.getPlayer().setHealth(pz.getHealthForOwner());
      }
   }
   
   @EventHandler
   public void onKick(PlayerKickEvent e) 
   {
	   // If a player is being kicked (like during shutdown),
	   // mark them "safe", so they don't get turned into a Zombie
	   if (! pwncombatloggers.isSafe(e.getPlayer().getName())) 
	   {
		   pwncombatloggers.callSafe(e.getPlayer());
	   }
   }    
   
   @EventHandler
   public void onQuit(PlayerQuitEvent e) 
   {
      if(! pwncombatloggers.isSafe(e.getPlayer().getName()) && pwncombatloggers.pvpZombEnabled) 
      {
         lastLogout = System.currentTimeMillis();
         new PvPLoggerZombie(e.getPlayer().getName());
      }
   }

   @EventHandler
   public void onChunk(ChunkUnloadEvent e) 
   {
      Chunk c = e.getChunk();
      for(Entity en : c.getEntities()) 
      {
         if(en.getType() == EntityType.ZOMBIE) 
         {
            Zombie z = (Zombie)en;
            if(PvPLoggerZombie.isPvPZombie(z)) 
            {
               PvPLoggerZombie pz = PvPLoggerZombie.getByZombie(z);
               pz.despawnDrop(true);
               pz.killOwner();
            }
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onCreature(CreatureSpawnEvent e) 
   {
      if(e.getEntity() instanceof Zombie) {
         if(System.currentTimeMillis() - lastLogout < 20) 
         {
            e.setCancelled(false);
         } 
      }
   }   
   
}