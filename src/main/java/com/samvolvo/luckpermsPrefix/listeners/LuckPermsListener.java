package com.samvolvo.luckpermsPrefix.listeners;

import com.samvolvo.luckpermsPrefix.LuckpermsPrefix;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Queue;

public class LuckPermsListener implements Listener {
    private final LuckpermsPrefix plugin;
    private final Queue<Player> playerQueue;

    public LuckPermsListener(LuckpermsPrefix plugin) {
        this.plugin = plugin;
        this.playerQueue = new LinkedList<>();
    }

    public void onNodeRemove(NodeRemoveEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!playerQueue.contains(player)) {
                playerQueue.add(player);
            }
        }
        processQueue();
        plugin.getAPILogger().debug("NodeRemoveEvent triggered.");
    }

    public void onNodeAdd(NodeAddEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!playerQueue.contains(player)) {
                playerQueue.add(player);
            }
        }
        processQueue();
        plugin.getAPILogger().debug("NodeAddEvent triggered.");
    }

    private void processQueue() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerQueue.isEmpty()) {
                    Player player = playerQueue.poll();
                    if (player != null && player.isOnline()) {
                        plugin.getPlayerTeamUtil().setPlayerTeam(player);
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Verwerk elke seconde (20 ticks)
    }
}
