package com.samvolvo.prefixPro.integration;

import com.samvolvo.prefixPro.PrefixPro;
import com.samvolvo.prefixPro.utils.ColorUtil;
import net.luckperms.api.*;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.*;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public final class PrefixProTest extends JavaPlugin {
    private PrefixPro prefix;
    private Player actor, viewer;
    private LuckPerms lp;
    private final List<String> passed = new ArrayList<>();
    private final List<String> failed = new ArrayList<>();
    private String originalConfig, originalMessages;
    private Path configFile, messagesFile;
    private Scoreboard custom;
    private Team foreign;
    private String initialListName;
    private int stage;
    private boolean running;

    @Override public void onEnable() {
        getCommand("prefixprotest").setExecutor((sender, command, label, args) -> {
            if (running) return true;
            prefix = (PrefixPro) Bukkit.getPluginManager().getPlugin("PrefixPro");
            if (prefix == null || !prefix.isEnabled()) {
                failed.add("Plugin not enabled"); finish(); return true;
            }
            actor = Bukkit.getPlayerExact("PrefixTester");
            viewer = Bukkit.getPlayerExact("PrefixViewer");
            if (actor == null || viewer == null) {
                failed.add("Both real protocol clients must be logged in"); finish(); return true;
            }
            try {
                running = true;
                lp = Bukkit.getServicesManager().load(LuckPerms.class);
                configFile = prefix.getDataFolder().toPath().resolve("config.yml");
                messagesFile = prefix.getDataFolder().toPath().resolve("Customization/messages.yml");
                originalConfig = Files.readString(configFile);
                originalMessages = Files.readString(messagesFile);
                actor.setOp(true);
                actor.setGameMode(GameMode.CREATIVE);
                actor.setInvulnerable(false);
                actor.setCollidable(true);
                actor.teleport(actor.getWorld().getSpawnLocation().add(0, 2, 0));
                initialListName = actor.getPlayerListName();
                getServer().getScheduler().runTaskTimer(this, () -> {
                    try { step(); } catch (Throwable error) {
                        failed.add("Stage " + stage + ": " + error);
                        error.printStackTrace();
                        finish();
                    }
                }, 10L, 40L);
            } catch (Exception e) { failed.add(e.toString()); finish(); }
            return true;
        });
    }

    private void check(String name, boolean condition) {
        if (!condition) throw new AssertionError(name);
        passed.add(name);
        getLogger().info("PASS: " + name);
    }

    private void settings(Consumer<org.bukkit.configuration.file.FileConfiguration> edit) throws Exception {
        edit.accept(prefix.getConfig());
        prefix.getConfig().save(configFile.toFile());
        prefix.reload();
    }

    private void step() throws Exception {
        switch (stage++) {
            case 0 -> {
                check("version 2.2.0", prefix.getDescription().getVersion().equals("2.2.0"));
                User user = lp.getUserManager().getUser(actor.getUniqueId());
                user.data().add(PrefixNode.builder("&#12abef[100%]", 1000).build());
                user.data().add(MetaNode.builder("name-color", "&#abcdef&l").build());
                lp.getUserManager().saveUser(user);
            }
            case 1 -> {
                getLogger().info("LIST_NAME " + actor.getPlayerListName().replace('\u00a7', '&'));
                getLogger().info("NAME_COLOR " + lp.getPlayerAdapter(Player.class).getMetaData(actor).getMetaValue("name-color"));
                check("LuckPerms prefix updates tab immediately", actor.getPlayerListName().contains("[100%]"));
                check("hex name color and bold", ColorUtil.normalizeCodes(actor.getPlayerListName()).contains(ColorUtil.colorize("&#abcdef&l")));
                String format = prefix.getPrefixManager().chatFormat(actor.getUniqueId());
                check("chat percent escaping", String.format(format, actor.getName(), "hello 50%").contains("[100%]"));
                check("chat message placeholder", String.format(format, actor.getName(), "hello 50%").endsWith("hello 50%"));
                check("main scoreboard nametag", actor.getScoreboard().getEntryTeam(actor.getName()) != null);
                Bukkit.dispatchCommand(actor, "afk");
                check("/afk enables", prefix.getAfkManager().isAfk(actor));
                check("AFK protection", actor.isInvulnerable() && !actor.isCollidable());
            }
            case 2 -> {
                check("AFK suffix preserved after prefix refresh", actor.getPlayerListName().contains("[AFK]"));
                Bukkit.dispatchCommand(actor, "afk");
                check("/afk disables and restores flags", !prefix.getAfkManager().isAfk(actor)
                        && !actor.isInvulnerable() && actor.isCollidable());
                actor.setInvulnerable(true);
                actor.setCollidable(false);
                Bukkit.dispatchCommand(actor, "afk");
                Bukkit.dispatchCommand(actor, "afk");
                check("pre-existing protection preserved", actor.isInvulnerable() && !actor.isCollidable());
                actor.setInvulnerable(false);
                actor.setCollidable(true);
                settings(c -> c.set("afk.protection.enabled", false));
                Bukkit.dispatchCommand(actor, "afk");
                check("optional protection", prefix.getAfkManager().isAfk(actor)
                        && !actor.isInvulnerable() && actor.isCollidable());
                Bukkit.dispatchCommand(actor, "afk");
                settings(c -> { c.set("afk.protection.enabled", true); c.set("afk.auto-time", 1); });
            }
            case 3 -> {
                check("automatic AFK", prefix.getAfkManager().isAfk(actor));
                settings(c -> c.set("afk.enabled", false));
                check("disable entire AFK system restores state", !prefix.getAfkManager().isAfk(actor)
                        && !actor.isInvulnerable() && actor.isCollidable());
                Bukkit.dispatchCommand(actor, "afk");
                check("/afk respects disabled system", !prefix.getAfkManager().isAfk(actor));
                settings(c -> { c.set("afk.enabled", true); c.set("afk.auto-time", 300); });
                var messages = YamlConfiguration.loadConfiguration(messagesFile.toFile());
                messages.set("afk.title.title", "Reloaded AFK");
                messages.save(messagesFile.toFile());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "prefixpro config reload");
                check("config reload refreshes messages", prefix.getMessages().AFK_TITLE.equals("Reloaded AFK"));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "prefixpro plugin reload");
                check("legacy plugin reload keeps instance enabled", prefix.isEnabled());
                var oldConfig = prefix.getConfig();
                Files.writeString(messagesFile, "afk: [unterminated");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "prefixpro config reload");
                check("invalid YAML retains active config", prefix.getConfig() == oldConfig);
                Files.writeString(messagesFile, originalMessages);
                prefix.reload();
                custom = Bukkit.getScoreboardManager().getNewScoreboard();
                foreign = custom.registerNewTeam("foreign");
                foreign.setPrefix("FOREIGN");
                foreign.addEntry(actor.getName());
                viewer.setScoreboard(custom);
                prefix.getPrefixManager().refreshAll();
                check("foreign team is preserved", custom.getEntryTeam(actor.getName()).equals(foreign)
                        && foreign.getPrefix().equals("FOREIGN"));
                check("viewer scoreboard is preserved", viewer.getScoreboard().equals(custom));
                foreign.removeEntry(actor.getName());
                prefix.getPrefixManager().refreshAll();
                check("custom viewer scoreboard gets nametag", custom.getEntryTeam(actor.getName()) != null);
                settings(c -> { c.set("display.tab", false); c.set("display.nametag", false); c.set("display.chat", false); });
                check("nametag toggle releases own teams", custom.getEntryTeam(actor.getName()) == null);
                check("foreign team survives display toggle", custom.getTeam("foreign") != null);
                check("chat toggle", prefix.getPrefixManager().chatFormat(actor.getUniqueId()) == null);
                settings(c -> { c.set("display.tab", true); c.set("display.nametag", true); c.set("display.chat", true); });
                // Exercise actual async Bukkit event dispatch, not a synchronous simulation.
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, actor, "async hello", new HashSet<>());
                        Bukkit.getPluginManager().callEvent(event);
                        if (!String.format(event.getFormat(), actor.getName(), event.getMessage()).contains("[100%]")) {
                            throw new AssertionError("async event format");
                        }
                    } catch (Throwable error) { synchronized (failed) { failed.add("async chat: " + error); } }
                });
            }
            case 4 -> {
                check("async chat main-thread activity handoff", !prefix.getAfkManager().isAfk(actor));
                Bukkit.dispatchCommand(actor, "afk");
                Bukkit.getPluginManager().callEvent(new PlayerToggleSneakEvent(actor, true));
                check("sneak exits AFK", !prefix.getAfkManager().isAfk(actor));
                Bukkit.dispatchCommand(actor, "afk");
                check("pre-disable AFK active", actor.isInvulnerable());
                Bukkit.getPluginManager().disablePlugin(prefix);
                check("disable restores player flags", !actor.isInvulnerable() && actor.isCollidable());
                check("disable removes owned nametag", custom.getEntryTeam(actor.getName()) == null);
                check("disable preserves foreign scoreboard team", custom.getTeam("foreign") != null);
                check("disable cancels scheduler tasks", Bukkit.getScheduler().getPendingTasks().stream()
                        .noneMatch(task -> task.getOwner().equals(prefix)));
                finish();
            }
        }
    }

    private void finish() {
        running = false;
        Bukkit.getScheduler().cancelTasks(this);
        try {
            if (originalConfig != null) Files.writeString(configFile, originalConfig);
            if (originalMessages != null) Files.writeString(messagesFile, originalMessages);
            String result = "{\"server\":\"" + Bukkit.getVersion().replace("\"", "'")
                    + "\",\"java\":\"" + System.getProperty("java.version") + "\",\"passed\":"
                    + passed.size() + ",\"failures\":[" + failed.stream()
                    .map(s -> "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\"")
                    .collect(java.util.stream.Collectors.joining(",")) + "]}";
            Files.writeString(Path.of("prefixpro-result.json"), result + "\n");
            getLogger().info("PREFIXPRO_TEST_COMPLETE " + result);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
