package com.samvolvo.luckpermsPrefix.listeners;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final LuckpermsPrefix plugin;

    public PlayerJoinListener(LuckpermsPrefix plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        plugin.getPlayerTeamUtil().setPlayerTeam(e.getPlayer());
    }
}
