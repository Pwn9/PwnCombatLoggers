package com.pwn9.PwnCombatLoggers;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class PvPLoggerMob 
{
	
   public static Set<PvPLoggerMob> zombies = new HashSet<PvPLoggerMob>();
   public static Set<String> waitingToDie = new HashSet<String>();
   public static Set<Integer> zombieIds = new HashSet<Integer>();
   public static int HEALTH = 20;
   private double hp = 10;
   private Zombie zombie;
   private String player;
   private ItemStack[] contents;

   public PvPLoggerMob(String player) 
   {
      this.player = player;
      Player p = Bukkit.getPlayer(player);
      hp = p.getHealth();
      zombieIds.add((zombie = (Zombie)p.getWorld().spawnEntity(p.getLocation(), EntityType.valueOf(PwnCombatLoggers.mobType))).getEntityId());
      zombie.getWorld().playEffect(zombie.getLocation(), Effect.MOBSPAWNER_FLAMES, 1, 1);
      zombie.setRemoveWhenFarAway(false);
      invFromPlayer(p); // Take player's inventory and apply it to this mob.
      Iterator<PvPLoggerMob> it = zombies.iterator();
      while(it.hasNext()) 
      {
         PvPLoggerMob pz = it.next();
         if(pz.getPlayer().equalsIgnoreCase(player)) 
         {
            despawnDrop(false);
            it.remove();
         }
      }
      zombies.add(this);
   }

   public Zombie getZombie() 
   {
      return zombie;
   }

   public void setZombie(Zombie zombie) 
   {
      this.zombie = zombie;
   }

   public String getPlayer() 
   {
      return player;
   }

   public void setPlayer(String player)
   {
      this.player = player;
   }

   @SuppressWarnings("deprecation")
   public void invFromPlayer(Player p) 
   {
	  PlayerInventory pi = p.getInventory(); 	   

	  // Set max health of zombie - should it be constant from config or max of what player logger had at the time of logout?
      if (PwnCombatLoggers.allowPlayerRegenZomb) 
      {
    	  zombie.setMaxHealth(PvPLoggerMob.HEALTH);
      }
      else 
      {
    	  zombie.setMaxHealth((getHealth() == 0) ? 1 : getHealth());
      }
      
      // Set the actual health of zombie
      zombie.setHealth(getHealth());
      
      // Additional zombie settings
      zombie.setRemoveWhenFarAway(false);
      zombie.setCanPickupItems(false);
      zombie.setCustomName("�c" + p.getDisplayName());
      zombie.setCustomNameVisible(true);
      zombie.getEquipment().setArmorContents(pi.getArmorContents());
      zombie.getEquipment().setItemInHand(pi.getItemInHand());
      zombie.getEquipment().setBootsDropChance(0);
      zombie.getEquipment().setChestplateDropChance(0);
      zombie.getEquipment().setHelmetDropChance(0);
      zombie.getEquipment().setLeggingsDropChance(0);
      zombie.getEquipment().setItemInHandDropChance(0);
      pi.setArmorContents(new ItemStack[] { null, null, null, null });
      pi.setItemInHand(null);
      this.contents = pi.getContents();
      
      // We've saved the player's inventory, now let's wipe it from the player, so no dupes. -Sage905
      p.getInventory().clear();
      p.updateInventory();    
   }

   @SuppressWarnings("deprecation")
   public void invToPlayer(Player p)
   {
	   PlayerInventory pi = p.getInventory();
       // Give to Player
	   pi.setContents(this.contents);
	   pi.setArmorContents(zombie.getEquipment().getArmorContents());
	   pi.setItemInHand(zombie.getEquipment().getItemInHand());
	   p.updateInventory();
	   // Take from Zombie
       zombie.getEquipment().setBootsDropChance(0);
       zombie.getEquipment().setChestplateDropChance(0);
       zombie.getEquipment().setHelmetDropChance(0);
       zombie.getEquipment().setLeggingsDropChance(0);
       zombie.getEquipment().setItemInHandDropChance(0);
       zombie.getEquipment().setArmorContents(new ItemStack[]{});
       zombie.getEquipment().setItemInHand(null);
   } 
	
   public List<ItemStack> itemsToDrop() 
   {   
      List<ItemStack> itemsToDrop = new ArrayList<ItemStack>();
      for(ItemStack i : contents) 
      {
         if(i != null) itemsToDrop.add(i);
      }
      return itemsToDrop;
   }

   public void despawnNoDrop(boolean giveToOwner, boolean iterate) 
   {
      if(giveToOwner)
      {
         Player p = Bukkit.getPlayer(player);
         if(p == null) 
         {
            PwnCombatLoggers.log(Level.WARNING, "Player was null!");
            return;
         }
         invToPlayer(p);
      }
      zombie.getEquipment().setBootsDropChance(0);
      zombie.getEquipment().setChestplateDropChance(0);
      zombie.getEquipment().setHelmetDropChance(0);
      zombie.getEquipment().setLeggingsDropChance(0);
      zombie.getEquipment().setItemInHandDropChance(0);      
      zombie.remove();
      if(iterate)
         despawn();
   }

   public void despawn() 
   {
      Iterator<PvPLoggerMob> it = zombies.iterator();
      while(it.hasNext()) 
      {
         PvPLoggerMob pz = it.next();
         if(pz.getPlayer().equalsIgnoreCase(player)) it.remove();
      }
      zombie.remove();
   }

   public void despawnDrop(boolean iterate) 
   {
      zombie.setCanPickupItems(false);
      for(ItemStack is : contents)
      {
         if(is != null) 
         {
        	 if (is.getType() != Material.AIR) 
        	 {
        		 zombie.getWorld().dropItemNaturally(zombie.getLocation(), is);
        	 }
         }
      }
      
     // Drop armor in same condition.  Allowing it to drop by the zombie will damage it.
     for (ItemStack is: zombie.getEquipment().getArmorContents()) 
     {
        if(is != null) 
        {
        	if (is.getType() != Material.AIR) 
        	{
        		zombie.getWorld().dropItemNaturally(zombie.getLocation(), is);
        	}
        }
     }
     
     // Same with the ItemInHand
  	 if (zombie.getEquipment().getItemInHand() != null) 
  	 {
  		if (zombie.getEquipment().getItemInHand().getType() != Material.AIR) 
  		{
  			zombie.getWorld().dropItemNaturally(zombie.getLocation(), zombie.getEquipment().getItemInHand());
  		}
  	 } 
      
     zombie.getWorld().playEffect(zombie.getLocation(), Effect.ENDER_SIGNAL, 1, 1);
     zombie.setHealth(0);
     zombie.remove();
     if(iterate) 
     {
        despawn();
     }
   }

   public static PvPLoggerMob getByOwner(String owner) 
   {
      for(PvPLoggerMob pz : zombies) 
      {
         if(pz.getPlayer().equalsIgnoreCase(owner)) return pz;
      }
      return null;
   }

   public static PvPLoggerMob getByZombie(Zombie z)
   {
      for(PvPLoggerMob pz : zombies) 
      {
         if(zombieEquals(pz.getZombie(), z)) return pz;
      }
      return null;
   }

   private static boolean zombieEquals(Zombie z1, Zombie z2)
   {
      return (z1.getEntityId() == (z2.getEntityId()));
   }

   public static boolean isPvPZombie(Zombie z) 
   {
      return zombieIds.contains(z.getEntityId());
   }

   public void killOwner() 
   {
      waitingToDie.add(player);
   }

   public double getHealth() 
   {
      if(!PwnCombatLoggers.keepPlayerHealthZomb)
         return PvPLoggerMob.HEALTH;
      else
      {
         return hp;
      }
   }

   public double getHealthForOwner() 
   {
      if(!PwnCombatLoggers.keepPlayerHealthZomb)
         return hp;
      else
         return zombie.getHealth();
   }
   
}
