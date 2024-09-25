package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    private final PrefixPro plugin;

    public PlayerChatListener(PrefixPro plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if (!plugin.getConfig().getBoolean("settings.enableChatTags")){
            return;
        }

        String prefix = plugin.getPrefixManager().getPlayerPrefix(e.getPlayer());

        if (prefix.isEmpty() || prefix == null){
            return;
        }

        String format = ChatColor.translateAlternateColorCodes('&', prefix + " &r" + e.getPlayer().getName() + "&7: &f" + e.getMessage());

        e.setFormat(format);
    }

}
