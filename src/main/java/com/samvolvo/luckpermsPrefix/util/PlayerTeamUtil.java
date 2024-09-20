package com.samvolvo.luckpermsPrefix.util;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class PlayerTeamUtil {
    private final LuckpermsPrefix plugin;

    public PlayerTeamUtil(LuckpermsPrefix plugin){
        this.plugin = plugin;
    }

    public void setPlayerTeam(Player player){
        Team team = plugin.getScoreboard().getTeam(player.getName());

        if (team == null){
            team = plugin.getScoreboard().registerNewTeam(player.getName());
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getPrefixManager().getPlayerPrefix(player) + " &r");
        team.setPrefix(prefix);
        team.addEntry(player.getName());

        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', prefix + " &r" + player.getName()));
        player.setScoreboard(plugin.getScoreboard());
    }

}
