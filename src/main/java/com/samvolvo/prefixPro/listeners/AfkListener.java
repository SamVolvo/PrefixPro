package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AfkListener implements Listener {
    private final PrefixPro plugin;
    public AfkListener(PrefixPro plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneakEvent(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            // Exiting must work after permission loss, even with return-on-activity disabled.
            if (plugin.getAfkManager().isAfk(event.getPlayer())) plugin.getAfkManager().setAfk(event.getPlayer(), false);
            plugin.getAfkManager().updateActivity(event.getPlayer());
        }
    }
}
