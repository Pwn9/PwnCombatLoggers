package com.pwn9.PwnCombatLoggers.Listener;

import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.pwn9.PwnCombatLoggers.*;

public class ConnectListener implements Listener 
{
   private PwnCombatLoggers plugin;
   private long lastLogout = System.currentTimeMillis();

   public ConnectListener(PwnCombatLoggers pt) 
   {
      this.plugin = pt;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) 
   {
	   e.getPlayer().setScoreboard(ScoreboardFeatures.getBoard()); 
	   
	   if(PvPLoggerMob.waitingToDie.contains(e.getPlayer().getName())) 
	   {
		   e.getPlayer().setHealth(0);
		   
		   PvPLoggerMob.waitingToDie.remove(e.getPlayer().getName());
	   } 	   
	   
      PvPLoggerMob pz = PvPLoggerMob.getByOwner(e.getPlayer().getName());
      
      if(pz != null) 
      {
         Location pzloc = pz.getZombie().getLocation();
         
         PwnCombatLoggers.log(Level.INFO, "Tagged player " + e.getPlayer().getName() + " logging in at " + pzloc.toString());
         
         // Teleport player on reconnect before adding back to unsafe list, otherwise the TP will be cancelled when TP disabled
         e.getPlayer().teleport(pzloc);
                 
         pz.despawnNoDrop(true, true);
         
    	 // this method is breaking
         plugin.addUnsafe(e.getPlayer());
         
         e.getPlayer().setHealth(pz.getHealthForOwner());
      }
   }
   
   @EventHandler
   public void onKick(PlayerKickEvent e) 
   {
	   // If a player is being kicked (like during shutdown),
	   // mark them "safe", so they don't get turned into a Zombie
	   if (! plugin.isSafe(e.getPlayer().getName())) 
	   {
		   plugin.callSafe(e.getPlayer());
	   }
   }    
   
   @EventHandler
   public void onQuit(PlayerQuitEvent e) 
   {
      if(! plugin.isSafe(e.getPlayer().getName()) && plugin.mobEnabled) 
      {
         lastLogout = System.currentTimeMillis();
         new PvPLoggerMob(e.getPlayer().getName());
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
            if(PvPLoggerMob.isPvPZombie(z)) 
            {
               PvPLoggerMob pz = PvPLoggerMob.getByZombie(z);
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