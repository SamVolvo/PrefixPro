package com.samvolvo.prefixPro.commands;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    private final PrefixPro plugin;
    public AfkCommand(PrefixPro plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessages().PLAYER_CONSOLE_ONLY);
        } else if (!player.hasPermission("prefixpro.afk")) {
            player.sendMessage(plugin.getMessages().PLAYER_NO_PERMISSION);
        } else if (!plugin.getAfkManager().enabled()) {
            player.sendMessage(plugin.getMessages().AFK_DISABLED);
        } else {
            plugin.getAfkManager().setAfk(player, !plugin.getAfkManager().isAfk(player));
        }
        return true;
    }
}
