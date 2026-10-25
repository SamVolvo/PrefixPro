package com.samvolvo.prefixPro.managers;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.LongSupplier;

/** All mutations and player API calls happen on the main thread; no per-player tasks. */
public class AfkManager {
    private final PrefixPro plugin;
    private final LongSupplier clock;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Protection> afkPlayers = new HashMap<>();
    private final Set<UUID> titles = new HashSet<>();

    public AfkManager(PrefixPro plugin) {
        this(plugin, () -> System.nanoTime() / 1_000_000);
    }

    public AfkManager(PrefixPro plugin, LongSupplier clock) {
        this.plugin = plugin;
        this.clock = clock;
    }

    public boolean enabled() { return plugin.getConfig().getBoolean("afk.enabled", true); }

    public boolean protectedFromDamage(Player player) {
        return enabled() && isAfk(player)
                && plugin.getConfig().getBoolean("afk.protection.enabled", true)
                && plugin.getConfig().getBoolean("afk.protection.invulnerable", true);
    }

    public boolean protectedFromPushing(Player player) {
        return enabled() && isAfk(player)
                && plugin.getConfig().getBoolean("afk.protection.enabled", true)
                && plugin.getConfig().getBoolean("afk.protection.prevent-pushing", true);
    }

    public void updateActivity(Player player) {
        if (!enabled()) return;
        lastActivity.put(player.getUniqueId(), clock.getAsLong());
        if (isAfk(player) && plugin.getConfig().getBoolean("afk.return-on-activity", true)) setAfk(player, false);
    }

    public void tick() {
        if (!enabled()) return;
        long now = clock.getAsLong();
        long timeout = plugin.getConfig().getInt("afk.auto-time", 300) * 1000L;
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            if (isAfk(player)) {
                // Losing AFK permission must not leave a player protected indefinitely.
                if (!player.hasPermission("prefixpro.afk")) {
                    setAfk(player, false);
                } else {
                    showTitle(player);
                }
            } else if (plugin.getConfig().getBoolean("afk.auto-enabled", true)
                    && player.hasPermission("prefixpro.afk")
                    && now - lastActivity.computeIfAbsent(id, ignored -> now) >= timeout) {
                setAfk(player, true);
            }
        }
    }

    public void setAfk(Player player, boolean afk) {
        UUID id = player.getUniqueId();
        if (afk == isAfk(player)) return;
        if (afk) {
            if (!enabled() || !player.isOnline() || !player.hasPermission("prefixpro.afk")) return;
            boolean protect = plugin.getConfig().getBoolean("afk.protection.enabled", true);
            boolean invulnerable = protect && plugin.getConfig().getBoolean("afk.protection.invulnerable", true);
            boolean pushing = protect && plugin.getConfig().getBoolean("afk.protection.prevent-pushing", true);
            afkPlayers.put(id, new Protection(player.isInvulnerable(), player.isCollidable(), invulnerable, pushing));
            if (invulnerable) player.setInvulnerable(true);
            if (pushing) player.setCollidable(false);
            send(player, plugin.getMessages().PLAYER_NOW_AFK);
            showTitle(player);
        } else {
            restore(player);
            lastActivity.put(id, clock.getAsLong());
            send(player, plugin.getMessages().PLAYER_NO_LONGER_AFK);
        }
        plugin.getPrefixManager().updatePlayerPrefix(player);
    }

    private void send(Player player, String message) {
        if (plugin.getConfig().getBoolean("afk.chat-messages", true) && !message.isEmpty()) player.sendMessage(message);
    }

    private void showTitle(Player player) {
        if (!plugin.getConfig().getBoolean("afk.title.enabled", true)) return;
        if (plugin.getMessages().AFK_TITLE.isEmpty() && plugin.getMessages().AFK_SUBTITLE.isEmpty()) return;
        player.sendTitle(plugin.getMessages().AFK_TITLE, plugin.getMessages().AFK_SUBTITLE,
                plugin.getConfig().getInt("afk.title.fade-in", 0),
                plugin.getConfig().getInt("afk.title.stay", 40),
                plugin.getConfig().getInt("afk.title.fade-out", 0));
        titles.add(player.getUniqueId());
    }

    private void restore(Player player) {
        Protection saved = afkPlayers.remove(player.getUniqueId());
        if (saved != null) {
            if (saved.changedInvulnerability && player.isInvulnerable()) player.setInvulnerable(saved.invulnerable);
            if (saved.changedCollision && !player.isCollidable()) player.setCollidable(saved.collidable);
        }
        if (titles.remove(player.getUniqueId())) player.resetTitle();
    }

    public boolean isAfk(Player player) { return afkPlayers.containsKey(player.getUniqueId()); }

    public void cleanup(Player player) {
        restore(player);
        lastActivity.remove(player.getUniqueId());
    }

    /** Reload clears AFK safely and starts inactivity tracking under the new settings. */
    public void reload() {
        shutdown();
        for (Player player : Bukkit.getOnlinePlayers()) updateActivity(player);
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) restore(player);
        afkPlayers.clear();
        titles.clear();
        lastActivity.clear();
    }

    private record Protection(boolean invulnerable, boolean collidable,
                              boolean changedInvulnerability, boolean changedCollision) {}
}
