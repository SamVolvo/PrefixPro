package com.samvolvo.prefixPro.utils;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {
    private final PrefixPro plugin;
    public String prefix;
    public final String CONSOLE_PLUGIN_ENABLED = "PrefixPro has been enabled!";
    public final String CONSOLE_PLUGIN_DISABLED = "PrefixPro has been disabled!";
    public final String CONSOLE_LUCKPERMS_NOT_FOUND = "LuckPerms not found! Disabling plugin...";
    public String PLAYER_NOW_AFK, PLAYER_NO_LONGER_AFK, PLAYER_NO_PERMISSION, PLAYER_CONSOLE_ONLY, AFK_DISABLED;
    public String AFK_TITLE, AFK_SUBTITLE;
    public String COMMAND_USAGE, COMMAND_CONFIG_RELOADED, COMMAND_PLUGIN_RELOADED, COMMAND_RELOAD_FAILED;

    public Messages(PrefixPro plugin) {
        this.plugin = plugin;
        updatePrefix(plugin.getConfig());
    }

    public String get(String key) {
        return ColorUtil.colorize(plugin.getMessageConfig().getString(key, ""));
    }

    private String message(String key) {
        String text = get(key);
        return text.isEmpty() ? "" : prefix + text;
    }

    public void updatePrefix(FileConfiguration config) {
        prefix = ColorUtil.colorize(config.getString("messages.prefix", "&8[&6PrefixPro&8] &7"));
        PLAYER_NOW_AFK = message("afk.chat.enable");
        PLAYER_NO_LONGER_AFK = message("afk.chat.disable");
        AFK_DISABLED = message("afk.chat.disabled");
        PLAYER_NO_PERMISSION = message("permissions.no-permission");
        PLAYER_CONSOLE_ONLY = message("commands.player-only");
        COMMAND_USAGE = message("commands.usage");
        COMMAND_CONFIG_RELOADED = message("commands.config-reloaded");
        COMMAND_PLUGIN_RELOADED = message("commands.plugin-reloaded");
        COMMAND_RELOAD_FAILED = message("commands.reload-failed");
        AFK_TITLE = get("afk.title.title");
        AFK_SUBTITLE = get("afk.title.subtitle");
    }
}
