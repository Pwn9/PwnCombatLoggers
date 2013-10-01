package com.pwn9.PwnCombatLoggers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import com.pwn9.PwnCombatLoggers.Listener.CombatListener;
import com.pwn9.PwnCombatLoggers.Listener.ConnectListener;
import com.pwn9.PwnCombatLoggers.Listener.PlayerListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PwnCombatLoggers extends JavaPlugin implements Listener 
{
	
   private final Commands Commands = new Commands(this);
   public HashMap<String, Long> safeTimes = new HashMap<String, Long>();
   public HashMap<String, Long> deathTimes = new HashMap<String, Long>();

   private Set<String> couldFly = new HashSet<String>();
   private Set<String> hadFlight = new HashSet<String>();

   private static Logger logger;

   private TagAPEye tagApi;
   ScoreboardFeatures scoreboard;
   private static PwnCombatLoggers instance;
   public Config configuration;
   public static ChatColor nameTagColor;

   public long SAFE_DELAY = 30000;
   public long DEATH_TP_DELAY = 30000;

   public boolean useDeathTP = true;
   public boolean disableFlight = true;
   public boolean antiPilejump = false;
   public boolean unInvis = true;
   public boolean preventTeleport = true;
   public boolean pvpZombEnabled = true;
   public boolean taggingEnabled = true;
   public boolean disableEnderpearls = true;

   //public boolean healthObjective = true;
   public boolean safeTimeObjective = true;

   static boolean keepPlayerHealthZomb = true;

   public void onEnable() 
   {
      instance = this;
      configuration = new Config(this);
      logger = getLogger();
      manageConfig();
      manageInstances();
      //scoreboard = new ScoreboardFeatures(healthObjective, safeTimeObjective);
      scoreboard = new ScoreboardFeatures(safeTimeObjective);
      getServer().getPluginManager().registerEvents(new CombatListener(this), this);
      getServer().getPluginManager().registerEvents(new ConnectListener(this), this);
      getServer().getPluginManager().registerEvents(new PlayerListener(this), this);      
      task();
   }

   private void manageInstances() 
   {
      if(configuration.getConfig().getBoolean("Tagging.Use TagAPI") && getServer().getPluginManager().getPlugin("TagAPI") != null) 
      {
         this.tagApi = new TagEnabled(this);
         //getServer().getPluginManager().registerEvents(tagApi, this);
      } 
      else 
      {
         this.tagApi = new TagDisabled();
      }
      getServer().getPluginManager().registerEvents(tagApi, this);
   }

   void manageConfig() 
   {
      configuration.enable();
      this.SAFE_DELAY = configuration.getConfig().getInt("Tagging.Safe Time", 30) * 1000;
      this.DEATH_TP_DELAY = configuration.getConfig().getInt("Death.DeathTP Time", 30) * 1000;
      useDeathTP = configuration.getConfig().getBoolean("Death.DeathTP Enabled", true);
      PwnCombatLoggers.nameTagColor = configuration.parseNameTagColor();

      PvPLoggerZombie.HEALTH = configuration.getConfig().getInt("PvPLogger Zombie.Health", 50);

      this.disableFlight = configuration.getConfig().getBoolean("Tagging.Disable Flying", true);
      this.unInvis = configuration.getConfig().getBoolean("Tagging.Remove Invisible", true);
      this.taggingEnabled = configuration.getConfig().getBoolean("Tagging.Enabled", true);
      this.antiPilejump = configuration.getConfig().getBoolean("Tagging.Anti Pilejump", true);
      this.pvpZombEnabled = configuration.getConfig().getBoolean("PvPLogger Zombie.Enabled", true);
      this.disableEnderpearls = configuration.getConfig().getBoolean("Tagging.Disable Enderpearls", true);
      this.preventTeleport = configuration.getConfig().getBoolean("Tagging.Prevent Teleport", true);
      PwnCombatLoggers.keepPlayerHealthZomb = configuration.getConfig().getBoolean("PvPLogger Zombie.Keep Player Health", true);
      this.safeTimeObjective = configuration.getConfig().getBoolean("Scoreboard Display.Safe Time", true);
      //this.healthObjective = configuration.getConfig().getBoolean("Scoreboard Display.Health", true);
   }

   private void resetNameTagsAuto() 
   {
      Iterator<String> iter = safeTimes.keySet().iterator();
      
      final Objective displaySafeTime = scoreboard.getBoard().getObjective("displaySafeTime");
      //final Objective showhealth = scoreboard.getBoard().getObjective("showhealth");
           
      while(iter.hasNext()) 
      {
         String s = iter.next();
         Player player = getServer().getPlayer(s);
   
         if(player == null) 
         {
            OfflinePlayer p = getServer().getOfflinePlayer(s);
            clearFromBoard(p);
            iter.remove();
         } 
         else if(isSafe(s)) 
         {
            iter.remove();
            player.sendMessage("§cYou are now safe.");
            clearFromBoard(player);
            fixFlying(player);
            refresh(player);
         } 
         else 
         {
            if(safeTimeObjective) 
            {
               long currTime = System.currentTimeMillis();
               long safeTime = safeTimes.get(s);
               displaySafeTime.getScore(player).setScore((int)(safeTime / 1000 - currTime / 1000));
               if (!configuration.getConfig().getBoolean("Tagging.Use TagAPI")){
                   scoreboard.team.addPlayer(player);   
               }
            }
            //if(healthObjective)
            //{
               //showhealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
               //showhealth.setDisplayName("/ 20");
               //player.setScoreboard(board);
            //}
         }
      }
   }

   public void clearFromBoard(OfflinePlayer player) 
   {
	  World world = null;
	  
	  if (player.isOnline()) {
		  world = player.getPlayer().getWorld();
		  if (! this.configuration.isPVPWorld(world)) return;
	  }
	  
      //if(safeTimeObjective || healthObjective)
      if(safeTimeObjective)    	  
      {
         if(player instanceof Player)
         {
            ((Player)player).setScoreboard(getServer().getScoreboardManager().getNewScoreboard());
            scoreboard.getBoard().resetScores(player);
            if (!configuration.getConfig().getBoolean("Tagging.Use TagAPI")){
            	scoreboard.team.removePlayer(player);
            }
         }
      }
   }

   private void fixFlying(Player player) 
   {
      if(couldFly.contains(player.getName())) 
      {
         couldFly.remove(player.getName());
         player.setAllowFlight(true);
      }
      if(hadFlight.contains(player.getName())) 
      {
         hadFlight.remove(player.getName());
         player.setFlying(true);
      }
   }

   void callSafeAllManual()
   {
      Iterator<String> iter = safeTimes.keySet().iterator();
      while(iter.hasNext()) 
      {
         String s = iter.next();
         iter.remove();
         callSafe(getServer().getPlayer(s));
      }
   }

   public void onDisable() 
   {
      callSafeAllManual();
      dealWithZombies();
      instance = null;
   }

   private void dealWithZombies() 
   {
      for(PvPLoggerZombie pz: PvPLoggerZombie.zombies)
      {
         pz.despawn();
      }
   }

   public void task() 
   {
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
      {
         public void run() 
         {
            resetNameTagsAuto();
         }
      }, 40L, 40L);
   }

   public void addUnsafe(Player p) 
   {
      addToBoard(p);
      resetSafeTime(p);
      p.sendMessage("§cYou can now be hit anywhere for at least " + (SAFE_DELAY / 1000) + " seconds!");
      removeFlight(p);
      refresh(p);
      unInvis(p);
      configuration.performConsoleUnsafeCommands(p);
      configuration.performUnsafeCommands(p);
   }

   private void addToBoard(Player p) 
   {
      //if(healthObjective || safeTimeObjective) 
      if(safeTimeObjective) 
      {
         p.setScoreboard(scoreboard.getBoard());
         if (!configuration.getConfig().getBoolean("Tagging.Use TagAPI")){
        	 scoreboard.team.addPlayer(p);
         }
      }
   }

   public void resetSafeTime(Player p)
   {
      safeTimes.put(p.getName(), calcSafeTime(SAFE_DELAY));
   }

   private void unInvis(Player p) 
   {
      if(unInvis) p.removePotionEffect(PotionEffectType.INVISIBILITY);
   }

   private void removeFlight(Player p) 
   {
      if(disableFlight && p.getGameMode() != GameMode.CREATIVE) 
      {
         if(p.getAllowFlight()) couldFly.add(p.getName());
         if(p.isFlying()) hadFlight.add(p.getName());
         p.setFlying(false);
         p.setAllowFlight(false);
      }
   }

   public void callSafe(Player player) 
   {
      if(player != null) 
      {
         clearFromBoard(player);
         safeTimes.remove(player.getName());
         refresh(player);
         player.sendMessage("§cYou are now safe.");
         configuration.performConsoleSafeCommands(player);
         configuration.performSafeCommands(player);
      }
   }

   public boolean isSafe(String player) 
   {
      if(safeTimes.containsKey(player)) 
      {
         return (safeTimes.get(player) < System.currentTimeMillis());
      }
      return true;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
   {
      Commands.onCommand(sender, cmd, commandLabel, args);
      return true;
   }

   public long calcSafeTime(Long time) 
   {
      return System.currentTimeMillis() + time;
   }

   void refresh(Player p) 
   {
      tagApi.refresh(p);
   }

   public void setNameTagColor(ChatColor nameTagColor) 
   {
      PwnCombatLoggers.nameTagColor = nameTagColor;
   }

   public ChatColor getNameTagColor() 
   {
      return nameTagColor;
   }

   public static void log(Level level, String message) 
   {
      logger.log(level, message);
   }

   public static PwnCombatLoggers getInstance() 
   {
      return instance;
   }
   
}