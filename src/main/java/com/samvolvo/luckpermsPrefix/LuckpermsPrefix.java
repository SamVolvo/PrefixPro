package com.samvolvo.luckpermsPrefix;

import com.samvolvo.luckpermsPrefix.commands.MainCommand;
import com.samvolvo.luckpermsPrefix.listeners.LuckPermsListener;
import com.samvolvo.luckpermsPrefix.listeners.PlayerChatListener;
import com.samvolvo.luckpermsPrefix.listeners.PlayerJoinListener;
import com.samvolvo.luckpermsPrefix.managers.PrefixManager;
import com.samvolvo.luckpermsPrefix.util.Logger;
import com.samvolvo.luckpermsPrefix.util.PlayerTeamUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;

public final class LuckpermsPrefix extends JavaPlugin {

    // ApiStuff
    private LuckPerms luckPerms;
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;

    // Config
    private FileConfiguration config;
    private File configFile;

    // Managers
    private PrefixManager prefixManager;

    // Utils
    private PlayerTeamUtil playerTeamUtil;
    private final Logger logger = new Logger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getAPILogger().loading("Booting LuckpermsPrefix");

        if (isLuckpermsInstalled()){
            getAPILogger().info("connected to LuckPerms");
        }else{
            getAPILogger().warning("LuckPerms not found! Disabling now!");
            getServer().getPluginManager().disablePlugin(this);
        }

        saveDefaultConfig();
        loadConfig();

        luckPerms = LuckPermsProvider.get();
        scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getMainScoreboard();

        // Managers
        prefixManager = new PrefixManager(this);

        // Utils
        playerTeamUtil = new PlayerTeamUtil(this);

        // Commands
        getCommand("luckpermsprefix").setExecutor(new MainCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);

        // LuckPermsListeners
        EventBus eventBus = luckPerms.getEventBus();
        LuckPermsListener listener = new LuckPermsListener(this);
        eventBus.subscribe(this, NodeAddEvent.class, listener::onNodeAdd);
        eventBus.subscribe(this, NodeRemoveEvent.class, listener::onNodeRemove);


        getAPILogger().info("&eLuckperms&dPrefix &aEnabled");
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

    public FileConfiguration getConfig() {
        return config;
    }

    public Logger getAPILogger(){return logger;}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getAPILogger().info("&eLuckperms&dPrefix &cDisabled");
    }

    // Checks
    private boolean isLuckpermsInstalled(){
        Plugin luckperms = Bukkit.getServer().getPluginManager().getPlugin("LuckPerms");
        return luckperms != null && luckperms.isEnabled();
    }

    // Config
    public void loadConfig(){
        if (configFile == null){
            configFile = new File(getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void saveTheConfig(){
        try{
            config.save(configFile);
        } catch (IOException e){
            getAPILogger().warning("Unable to save config.yml");
        }
    }

}
