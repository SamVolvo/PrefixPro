package com.samvolvo.prefixPro.listeners;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.util.BoundingBox;
import java.util.List;

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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (intersectsProtectedPlayer(event.getBlock(), event.getBlocks(), event.getDirection())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (intersectsProtectedPlayer(event.getBlock(), event.getBlocks(), event.getDirection().getOppositeFace())) {
            event.setCancelled(true);
        }
    }

    private boolean intersectsProtectedPlayer(Block piston, List<Block> moved, BlockFace direction) {
        if (!plugin.getAfkManager().enabled()) return false;
        var blocks = new java.util.ArrayList<>(moved);
        blocks.add(piston);
        for (Block block : blocks) {
            BoundingBox swept = BoundingBox.of(block).union(BoundingBox.of(block.getRelative(direction)));
            for (var entity : block.getWorld().getNearbyEntities(swept)) {
                if (entity instanceof Player player && plugin.getAfkManager().protectedFromPushing(player)) return true;
            }
        }
        return false;
    }
}
