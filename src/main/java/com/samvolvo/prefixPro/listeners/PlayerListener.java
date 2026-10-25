package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerListener implements Listener {
    private final PrefixPro plugin;
    public PlayerListener(PrefixPro plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getAfkManager().updateActivity(event.getPlayer());
        plugin.requestRefreshAll();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getAfkManager().cleanup(event.getPlayer());
        plugin.getPrefixManager().cleanup(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && plugin.getAfkManager().protectedFromDamage(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        if (to == null) return;
        boolean moved = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        boolean looked = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        Player player = event.getPlayer();
        if (moved && plugin.getAfkManager().protectedFromPushing(player)
                && !player.isSneaking() && !player.isSprinting() && !looked) {
            // Do not count water, gravity, pistons or collision as deliberate AFK activity.
            event.setTo(from);
            return;
        }
        if (moved || looked && plugin.getConfig().getBoolean("afk.detect-look", true)) {
            plugin.getAfkManager().updateActivity(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String format = plugin.getPrefixManager().chatFormat(event.getPlayer().getUniqueId());
        if (format != null) event.setFormat(format);
        plugin.requestActivity(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // /afk is a toggle; clearing AFK before executing it would immediately set AFK again.
        String command = event.getMessage().split(" ", 2)[0];
        if (!command.equalsIgnoreCase("/afk") && !command.equalsIgnoreCase("/prefixpro:afk")) {
            plugin.getAfkManager().updateActivity(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) { plugin.getAfkManager().updateActivity(event.getPlayer()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnimation(PlayerAnimationEvent event) { plugin.getAfkManager().updateActivity(event.getPlayer()); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        // Refresh context metadata, but a plugin teleport is not proof of player activity.
        plugin.requestRefresh(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) { plugin.requestRefresh(event.getPlayer().getUniqueId()); }
}
