package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class CustomJoinMessage implements Listener {
    private final PrefixPro plugin;
    public CustomJoinMessage(PrefixPro plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(plugin.getConfig().getBoolean("messages.join-leave-message.enabled", true)
                ? plugin.getMessages().get("join").replace("{player}", event.getPlayer().getName()) : null);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage(plugin.getConfig().getBoolean("messages.join-leave-message.enabled", true)
                ? plugin.getMessages().get("leave").replace("{player}", event.getPlayer().getName()) : null);
    }
}
