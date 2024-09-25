package com.samvolvo.prefixPro.util;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class PlayerTeamUtil {
    private final PrefixPro plugin;

    public PlayerTeamUtil(PrefixPro plugin){
        this.plugin = plugin;
    }

    public void setPlayerTeam(Player player){
        if (plugin.getConfig().getBoolean("settings.enableNameTag")){
            Team team = plugin.getScoreboard().getTeam(player.getName());

            if (team == null){
                team = plugin.getScoreboard().registerNewTeam(player.getName());
            }

            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getPrefixManager().getPlayerPrefix(player) + " &r");
            team.setPrefix(prefix);
            team.addEntry(player.getName());
            player.setScoreboard(plugin.getScoreboard());
        }else{
            Team team = plugin.getScoreboard().getTeam(player.getName());

            if (team == null){
                team = plugin.getScoreboard().registerNewTeam(player.getName());
            }

            team.setPrefix("");
            team.addEntry(player.getName());
            player.setScoreboard(plugin.getScoreboard());
        }

        if (plugin.getConfig().getBoolean("settings.enableTabName")){
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getPrefixManager().getPlayerPrefix(player));
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', prefix + " &r" + player.getName()));
        }else{
            player.setPlayerListName("§r" + player.getName());
        }
    }

}
