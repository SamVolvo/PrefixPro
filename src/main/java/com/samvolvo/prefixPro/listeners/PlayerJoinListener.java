package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PrefixPro plugin;

    public PlayerJoinListener(PrefixPro plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        plugin.getPlayerTeamUtil().setPlayerTeam(e.getPlayer());
    }

    @EventHandler
    public void onStaffJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if (player.isOp() || player.hasPermission("prefixpro.version")){
            String message = plugin.getUpdateChecker().generateUpdateMessageColored(plugin.getDescription().getVersion());
            if (message != null || !message.isEmpty()){
                player.sendMessage(message);
            }
        }
    }
}
