package com.pwn9.PwnCombatLoggers;

import org.bukkit.entity.*;
import org.bukkit.event.*;

public class TagEnabled implements TagAPEye, Listener 
{
   private PwnCombatLoggers pwncombatloggers;

   @Override
   public void refresh(Player p)
   {
      org.kitteh.tag.TagAPI.refreshPlayer(p);
   }

   public TagEnabled(PwnCombatLoggers pt)
   {
      this.pwncombatloggers = pt;
   }

   @EventHandler
   public void onNameTag(org.kitteh.tag.PlayerReceiveNameTagEvent e)
   {
      if(!pwncombatloggers.isSafe(e.getNamedPlayer().getName()))
      {
         Player p = e.getNamedPlayer();
         e.setTag(PwnCombatLoggers.nameTagColor + p.getName());
      }
      else
      {
         e.setTag(e.getNamedPlayer().getName());
      }
   }
}
