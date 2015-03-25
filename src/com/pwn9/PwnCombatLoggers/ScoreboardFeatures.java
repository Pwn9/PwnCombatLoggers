package com.pwn9.PwnCombatLoggers;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class ScoreboardFeatures 
{
	private static Scoreboard board;
	//private Scoreboard globalboard;
	
	public Team team;
	//public Team combatants;
	//public Team safeteam;
	
	public ScoreboardFeatures(boolean safeTime) 
	{
     
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		String teamprefix = PwnCombatLoggers.nameTagColor.toString();
		
		board = manager.getNewScoreboard();
		//globalboard = manager.getNewScoreboard();
		
		team = board.registerNewTeam("In Combat");
		team.setPrefix(teamprefix);   
		//safeteam = board.registerNewTeam("Safe");
		//safeteam.setPrefix("WHITE");   
		
		if(safeTime) 
		{
			board.registerNewObjective("displaySafeTime", "dummy");
			Objective objective = board.getObjective("displaySafeTime");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("[In Combat : Time]"); 
		}
		
		//Objective ob = board.registerNewObjective("setNameColor", "dummy");
		//ob.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		//combatants = board.registerNewTeam("Combatants");
		//combatants.setPrefix(teamprefix); 

	}

	public static Scoreboard getBoard() 
	{
		return board;
	}

	//public Scoreboard getGlobalBoard() 
	//{
	//	return globalboard;
	//}
}
