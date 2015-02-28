package com.pwn9.PwnCombatLoggers;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// For hooking SimpleClans
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

import com.pwn9.PwnCombatLoggers.Listener.*;

public class PwnCombatLoggers extends JavaPlugin implements Listener 
{
	// Init plugin as instance
	public static PwnCombatLoggers instance;
	
	public static File dataFolder;
	public static Boolean logEnabled;	
	private final Commands Commands = new Commands(this);
	public HashMap<String, Long> safeTimes = new HashMap<String, Long>();
	public HashMap<String, Long> deathTimes = new HashMap<String, Long>();
	private Set<String> couldFly = new HashSet<String>();
	private Set<String> hadFlight = new HashSet<String>();
	public static Logger logger;
	ScoreboardFeatures scoreboard;
	public Config configuration;
	public static ChatColor nameTagColor;
	public long SAFE_DELAY = 20000;
	public long DEATH_TP_DELAY = 20000;
	public boolean pluginEnabled = true;   
	public boolean disableTeleport = false;
	public boolean removeInvis = false;   
	public boolean useDeathTP = true;
	public boolean disableFlight = true;
	public boolean antiPilejump = false;
	public boolean mobEnabled = true; 
	public static String mobType;
	public boolean disableEnderpearls = false;
	public boolean safeTimeObjective = true;
	static boolean keepPlayerHealthZomb = true;
	static boolean allowPlayerRegenZomb = true;
	public List<String> disabledWorlds;
	public List<String> disabledCommands;
	
	// SimpleClans hook
	private SimpleClans sc;
	  
	public void onEnable() 
	{
	   //create instance of this
	   instance = this;
	   configuration = new Config(this);
	   logger = getLogger();
	   manageConfig();
	   manageInstances();
	   scoreboard = new ScoreboardFeatures(safeTimeObjective);
	   getServer().getPluginManager().registerEvents(new CombatListener(this), this);
	   getServer().getPluginManager().registerEvents(new ConnectListener(this), this);
	   getServer().getPluginManager().registerEvents(new PlayerListener(this), this);      
	   task(); 
	}

	private void manageInstances() 
	{	  
	  // Check for SimpleClans
	  Plugin plug = getServer().getPluginManager().getPlugin("SimpleClans");
	  if (plug != null)
	  {
	      sc = ((SimpleClans) plug);
	      logger.log(Level.INFO, "SimpleClans Found, Enabling Hooks");
	  }      
	}

   void manageConfig() 
   {
      configuration.enable();
      this.pluginEnabled = configuration.getConfig().getBoolean("pluginEnabled", true);
      this.disableTeleport = configuration.getConfig().getBoolean("disableTeleport", false);
      this.removeInvis = configuration.getConfig().getBoolean("removeInvisible", false);
      this.SAFE_DELAY = configuration.getConfig().getInt("tagTime", 20) * 1000;      
      this.safeTimeObjective = configuration.getConfig().getBoolean("displayTagTime", true);  
      this.disableFlight = configuration.getConfig().getBoolean("disableFlying", true);
      this.antiPilejump = configuration.getConfig().getBoolean("antiPilejump", true);
      this.disableEnderpearls = configuration.getConfig().getBoolean("disableEnderpearls", false);
      this.DEATH_TP_DELAY = configuration.getConfig().getInt("deathTPTime", 20) * 1000;
      useDeathTP = configuration.getConfig().getBoolean("deathTPEnabled", true); 
      this.mobEnabled = configuration.getConfig().getBoolean("mobEnabled", true);
      PwnCombatLoggers.mobType = configuration.getConfig().getString("mobType", "ZOMBIE");
      PvPLoggerMob.HEALTH = configuration.getConfig().getInt("maxHealth", 20);
      PwnCombatLoggers.keepPlayerHealthZomb = configuration.getConfig().getBoolean("keepPlayerHealth", true);
      PwnCombatLoggers.allowPlayerRegenZomb = configuration.getConfig().getBoolean("allowRegen", true);
      PwnCombatLoggers.nameTagColor = configuration.parseNameTagColor();   
   }

   private void resetNameTagsAuto() 
   {
      Iterator<String> iter = safeTimes.keySet().iterator();
      
      final Objective displaySafeTime = scoreboard.getBoard().getObjective("displaySafeTime");
           
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
            player.sendMessage("§0[§cCOMBAT§0]§c You are now out of combat!");
            clearFromBoard(player);
            fixFlying(player);
         } 
         else  
         {
            if(safeTimeObjective) 
            {
               long currTime = System.currentTimeMillis();
               long safeTime = safeTimes.get(s);
               displaySafeTime.getScore(player).setScore((int)(safeTime / 1000 - currTime / 1000));
               scoreboard.team.addPlayer(player);   
            }
         }
      }
   }

   public void clearFromBoard(OfflinePlayer player) 
   {
	  World world = null;
	  if (player.isOnline()) 
	  {
		  world = player.getPlayer().getWorld();
		  if (!this.configuration.isPVPWorld(world)) return;
	  }
	  
      if(safeTimeObjective) 
      {
         if(player instanceof Player) 
         {
            ((Player)player).setScoreboard(getServer().getScoreboardManager().getNewScoreboard());
            scoreboard.getBoard().resetScores(player);
           	scoreboard.team.removePlayer(player);
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
      sc = null;
   }

   private void dealWithZombies() 
   {
      for(PvPLoggerMob pz: PvPLoggerMob.zombies) 
      {
         pz.despawn();
      }
   }

   public void task() {
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
      p.sendMessage("§0[§cCOMBAT§0]§c You are now in combat for at least " + (SAFE_DELAY / 1000) + " seconds!");   
      removeFlight(p);
      unInvis(p);
   }

   private void addToBoard(Player p) 
   {
	  if(safeTimeObjective) 
      {
		 PwnCombatLoggers.log(Level.INFO, "Adding player " + p.getName() + " to the scoreboard.");
       	 scoreboard.team.addPlayer(p);
      }
   }

   public void resetSafeTime(Player p) 
   {
      safeTimes.put(p.getName(), calcSafeTime(SAFE_DELAY));
   }

   private void unInvis(Player p) 
   {
      if(removeInvis) p.removePotionEffect(PotionEffectType.INVISIBILITY);
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
         player.sendMessage("§0[§cCOMBAT§0]§c You are now out of combat!");
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
   
   public boolean isAttackAllowed(Player attacker, Player victim)
   {
	   // Is SimpleClans even installed?
       if (sc != null)
       {
    	   // Get clanplayers
           ClanPlayer acp = sc.getClanManager().getClanPlayer(attacker);
           ClanPlayer vcp = sc.getClanManager().getClanPlayer(victim);
           Clan vclan = vcp == null ? null : vcp.getClan();
           Clan aclan = acp == null ? null : acp.getClan();
           
           // are the players even players?
           if ((attacker != null) && (victim != null))
           {

               if (sc.getSettingsManager().isPvpOnlywhileInWar())
               {
                   // if one doesn't have clan then they cant be at war
                   if (aclan == null || vclan == null)
                   {
                       return false;
                   }

                   if (sc.getPermissionsManager().has(victim, "simpleclans.mod.nopvpinwar") && attacker != null && victim != null)
                   {
                       return false;
                   }

                   // if not warring no pvp
                   if (!aclan.isWarring(vclan))
                   {
                	   return false;
                   }
               }

               if (vclan != null)
               {
                   if (aclan != null)
                   {
                       // personal ff enabled, allow damage
                       if (vcp.isFriendlyFire())
                       {
                           return true;
                       }

                       // clan ff enabled, allow damage
                       if (vclan.isFriendlyFire())
                       {
                           return true;
                       }

                       // same clan, deny damage
                       if (vclan.equals(aclan))
                       {
                    	   return false;
                       }

                       // ally clan, deny damage
                       if (vclan.isAlly(aclan.getTag()))
                       {
                    	   return false;
                       }
                   } 
                   else
                   {
                       // not part of a clan - check if safeCivilians is set
                       if (sc.getSettingsManager().getSafeCivilians())
                       {
                    	   return false;
                       }
                   }
               } 
               else
               {
                   // not part of a clan - check if safeCivilians is set
                   if (sc.getSettingsManager().getSafeCivilians())
                   {
                	   return false;
                   }
               }
           }
           else
           {
               return false;
           }
       }
       // nothing has cancelled this attack so, let it be true!
       return true;
   }
   
   public static void logToFile(String message) 
   {   
    	try 
    	{   
		    if(!dataFolder.exists()) 
		    {
		    	dataFolder.mkdir();
		    }
		     
		    File saveTo = new File(dataFolder, "pwncombatloggers.log");
		    if (!saveTo.exists())  
		    {
		    	saveTo.createNewFile();
		    }
		    
		    FileWriter fw = new FileWriter(saveTo, true);
		    PrintWriter pw = new PrintWriter(fw);
		    pw.println(getDate() +" "+ message);
		    pw.flush();
		    pw.close();
	    } 
	    catch (IOException e) 
	    {
	    	e.printStackTrace();
	    }
   }
   
   public static String getDate() 
   {
   	  String s;
   	  Format formatter;
   	  Date date = new Date(); 
   	  formatter = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");
   	  s = formatter.format(date);
   	  return s;
   }
      
}