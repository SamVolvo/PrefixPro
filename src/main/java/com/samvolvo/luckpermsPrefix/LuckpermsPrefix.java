package com.samvolvo.luckpermsPrefix;

import com.samvolvo.luckpermsPrefix.commands.ReloadCommand;
import com.samvolvo.luckpermsPrefix.listeners.PlayerChatListener;
import com.samvolvo.luckpermsPrefix.listeners.PlayerJoinListener;
import com.samvolvo.luckpermsPrefix.managers.PrefixManager;
import com.samvolvo.luckpermsPrefix.util.PlayerTeamUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class LuckpermsPrefix extends JavaPlugin {

    // ApiStuff
    private LuckPerms luckPerms;
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;

    // Managers
    private PrefixManager prefixManager;

    // Utils
    private PlayerTeamUtil playerTeamUtil;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Booting LuckpermsPrefix");

        if (isLuckpermsInstalled()){
            getLogger().info("connected to LuckPerms");
        }else{
            getLogger().warning("LuckPerms not found! Disabling now!");
            getServer().getPluginManager().disablePlugin(this);
        }

        luckPerms = LuckPermsProvider.get();
        scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getMainScoreboard();

        // Managers
        prefixManager = new PrefixManager(this);

        // Utils
        playerTeamUtil = new PlayerTeamUtil(this);

        // Commands
        getCommand("reloadprefix").setExecutor(new ReloadCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);


        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eLuckperms&dPrefix &aEnabled"));
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public PrefixManager getPrefixManager() {
        return prefixManager;
    }

    public PlayerTeamUtil getPlayerTeamUtil() {
        return playerTeamUtil;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Checks
    private boolean isLuckpermsInstalled(){
        Plugin luckperms = Bukkit.getServer().getPluginManager().getPlugin("LuckPerms");
        return luckperms != null && luckperms.isEnabled();
    }
}
