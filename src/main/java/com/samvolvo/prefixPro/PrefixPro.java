package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.commands.*;
import com.samvolvo.prefixPro.commands.tabcompleter.MainCommandCompleter;
import com.samvolvo.prefixPro.listeners.*;
import com.samvolvo.prefixPro.managers.*;
import com.samvolvo.prefixPro.util.*;
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
import java.util.List;

public final class PrefixPro extends JavaPlugin {

    // ApiStuff
    private LuckPerms luckPerms;
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;

    // Config
    private final String prefix = "&ePrefix&dPro";
    private FileConfiguration config;
    private File configFile;

    // Managers
    private PrefixManager prefixManager;

    // Utils
    private PlayerTeamUtil playerTeamUtil;
    private final Logger logger = new Logger();
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getAPILogger().loading("Booting " + prefix);

        if (isLuckpermsInstalled()){
            getAPILogger().info("connected to LuckPerms");
        }else{
            getAPILogger().warning("LuckPerms not found! Disabling " + prefix + "&r!");
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
        getCommand("prefixpro").setExecutor(new MainCommand(this));
        getCommand("prefixpro").setTabCompleter(new MainCommandCompleter());

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);

        // LuckPermsListeners
        EventBus eventBus = luckPerms.getEventBus();
        LuckPermsListener listener = new LuckPermsListener(this);
        eventBus.subscribe(this, NodeAddEvent.class, listener::onNodeAdd);
        eventBus.subscribe(this, NodeRemoveEvent.class, listener::onNodeRemove);

        updateChecker = new UpdateChecker(this);
        CheckForUpdates(updateChecker);
        Metrics metrics = new Metrics(this, 23462);

        getAPILogger().info(prefix + " &aEnabled");
    }

    public void CheckForUpdates(UpdateChecker updateChecker){
        List<String> nameless = updateChecker.generateUpdateMessage(getDescription().getVersion());
        if (!nameless.isEmpty()){
            for (String message : nameless){
                getAPILogger().warning(message);
            }
        }
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

    public String getPrefix(){
        return prefix;
    }

    public UpdateChecker getUpdateChecker(){
        return updateChecker;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getAPILogger().info(prefix + " &cDisabled");
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
            getAPILogger().error("Unable to save config.yml");
        }
    }

}