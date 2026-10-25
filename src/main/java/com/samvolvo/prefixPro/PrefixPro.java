package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.commands.AfkCommand;
import com.samvolvo.prefixPro.commands.PrefixProCommand;
import com.samvolvo.prefixPro.listeners.*;
import com.samvolvo.prefixPro.managers.*;
import com.samvolvo.prefixPro.utils.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PrefixPro extends JavaPlugin {
    private PrefixManager prefixManager;
    private AfkManager afkManager;
    private ConfigManager configManager;
    private Messages messages;
    private Metrics metrics;
    private final com.samvolvo.prefixPro.utils.Logger logger = new com.samvolvo.prefixPro.utils.Logger();
    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();
    private final Set<UUID> refreshes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> activities = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean refreshAll = new AtomicBoolean();
    private volatile boolean running;
    private int ticks;

    @Override
    public void onEnable() {
        try {
            configManager = new ConfigManager(this);
            messages = new Messages(this);
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider == null) throw new IllegalStateException("LuckPerms service is unavailable");
            LuckPerms luckPerms = provider.getProvider();
            prefixManager = new PrefixManager(this, luckPerms);
            afkManager = new AfkManager(this);
            running = true;
            LuckPermsListener listener = new LuckPermsListener(this, prefixManager);
            subscriptions.add(luckPerms.getEventBus().subscribe(this, NodeAddEvent.class, listener::onNodeAdd));
            subscriptions.add(luckPerms.getEventBus().subscribe(this, NodeRemoveEvent.class, listener::onNodeRemove));
            subscriptions.add(luckPerms.getEventBus().subscribe(this, UserDataRecalculateEvent.class, listener::onDataRecalculate));
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
            getServer().getPluginManager().registerEvents(new CustomJoinMessage(this), this);
            getServer().getPluginManager().registerEvents(new AfkListener(this), this);
            Objects.requireNonNull(getCommand("afk")).setExecutor(new AfkCommand(this));
            PrefixProCommand command = new PrefixProCommand(this);
            Objects.requireNonNull(getCommand("prefixpro")).setExecutor(command);
            getCommand("prefixpro").setTabCompleter(command);
            getServer().getScheduler().runTaskTimer(this, this::tick, 1L, 1L);
            for (Player player : Bukkit.getOnlinePlayers()) afkManager.updateActivity(player);
            prefixManager.refreshAll();
            if (getConfig().getBoolean("update-check.enabled", true)) new UpdateChecker(this, "Do8Ixhbm").checkForUpdates();
            metrics = new Metrics(this, 23462);
            getLogger().info(messages.CONSOLE_PLUGIN_ENABLED);
        } catch (RuntimeException e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Cannot enable PrefixPro", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void tick() {
        if (!running) return;
        for (UUID id : new HashSet<>(activities)) {
            activities.remove(id);
            Player player = Bukkit.getPlayer(id);
            if (player != null) afkManager.updateActivity(player);
        }
        if (refreshAll.getAndSet(false)) {
            refreshes.clear();
            prefixManager.refreshAll();
        } else {
            for (UUID id : new HashSet<>(refreshes)) {
                refreshes.remove(id);
                Player player = Bukkit.getPlayer(id);
                if (player != null) prefixManager.updatePlayerPrefix(player);
            }
        }
        if (++ticks % 20 == 0) {
            afkManager.tick();
            prefixManager.refreshAll(); // Detect viewer scoreboard/context/display-name changes.
        }
    }

    /** Safe from async callbacks: only queue IDs, never access players or Bukkit here. */
    public void requestRefresh(UUID id) { if (running) refreshes.add(id); }
    public void requestRefreshAll() { if (running) refreshAll.set(true); }
    public void requestActivity(UUID id) { if (running) activities.add(id); }

    @Override
    public void onDisable() {
        running = false;
        subscriptions.forEach(EventSubscription::close);
        subscriptions.clear();
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        if (metrics != null) { metrics.shutdown(); metrics = null; }
        if (afkManager != null) afkManager.shutdown();
        if (prefixManager != null) prefixManager.shutdown();
        activities.clear();
        refreshes.clear();
        refreshAll.set(false);
        getLogger().info("PrefixPro has been disabled!");
    }

    public void reload() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Reload requires the server thread");
        configManager.reloadConfig(); // Throws before replacing live settings if either YAML is invalid.
        messages.updatePrefix(configManager.getConfig());
        afkManager.reload();
        prefixManager.refreshAll();
    }

    @Override
    public void reloadConfig() { reload(); }
    @Override
    public FileConfiguration getConfig() { return configManager.getConfig(); }
    public FileConfiguration getMessageConfig() { return configManager.getMessagesConfig(); }
    public PrefixManager getPrefixManager() { return prefixManager; }
    public AfkManager getAfkManager() { return afkManager; }
    public com.samvolvo.prefixPro.utils.Logger getCustomLogger() { return logger; }
    public Messages getMessages() { return messages; }
}
