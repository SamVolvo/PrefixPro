package com.samvolvo.prefixPro.commands;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.command.*;
import java.util.*;

public class PrefixProCommand implements CommandExecutor, TabCompleter {
    private final PrefixPro plugin;
    public PrefixProCommand(PrefixPro plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prefixpro.admin")) {
            sender.sendMessage(plugin.getMessages().PLAYER_NO_PERMISSION);
            return true;
        }
        if (args.length != 2 || !args[1].equalsIgnoreCase("reload")
                || !(args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("plugin"))) {
            sender.sendMessage(plugin.getMessages().COMMAND_USAGE);
            return true;
        }
        try {
            plugin.reload();
            sender.sendMessage(args[0].equalsIgnoreCase("config") ? plugin.getMessages().COMMAND_CONFIG_RELOADED
                    : plugin.getMessages().COMMAND_PLUGIN_RELOADED);
        } catch (RuntimeException e) {
            plugin.getLogger().log(java.util.logging.Level.WARNING, "Reload rejected; previous settings remain active", e);
            sender.sendMessage(plugin.getMessages().COMMAND_RELOAD_FAILED);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("prefixpro.admin")) return List.of();
        List<String> options = args.length == 1 ? List.of("config", "plugin")
                : args.length == 2 && (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("plugin"))
                    ? List.of("reload") : List.of();
        String entered = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(entered)).toList();
    }
}
