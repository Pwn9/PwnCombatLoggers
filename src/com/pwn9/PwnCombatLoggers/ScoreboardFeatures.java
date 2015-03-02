package com.pwn9.PwnCombatLoggers;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class ScoreboardFeatures 
{
	private Scoreboard board;
	//private Scoreboard globalboard;
	
	public Team team;
	public Team combatants;
	
	public ScoreboardFeatures(boolean safeTime) 
	{
     
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		String teamprefix = PwnCombatLoggers.nameTagColor.toString();
		
		board = manager.getNewScoreboard();
		//globalboard = manager.getNewScoreboard();
		
		team = board.registerNewTeam("In Combat");
		team.setPrefix(teamprefix);   

		if(safeTime) 
		{
			board.registerNewObjective("displaySafeTime", "dummy");
			Objective objective = board.getObjective("displaySafeTime");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Safe Times:"); 
		}
		
		Objective ob = board.registerNewObjective("setNameColor", "dummy");
		ob.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		combatants = board.registerNewTeam("Combatants");
		combatants.setPrefix(teamprefix); 
		
	}

	public Scoreboard getBoard() 
	{
		return board;
	}
   
}
