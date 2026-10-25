package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.utils.ColorUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Bukkit state is confined to the server thread. Async chat reads immutable strings only. */
public class PrefixManager {
    private final PrefixPro plugin;
    private final LuckPerms luckPerms;
    private final Map<Scoreboard, Map<UUID, Team>> ownedTeams = new IdentityHashMap<>();
    private final Map<UUID, ListName> listNames = new HashMap<>();
    private final Map<UUID, String> chatFormats = new ConcurrentHashMap<>();
    private volatile boolean chatEnabled;

    public PrefixManager(PrefixPro plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public String getPrefix(Player player) {
        String prefix = metadata(player).getPrefix();
        return prefix == null || prefix.isEmpty() ? "" : ColorUtil.colorize(prefix) + " ";
    }

    private CachedMetaData metadata(Player player) {
        return luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
    }

    public void updatePlayerPrefix(Player player) {
        CachedMetaData meta = metadata(player);
        String raw = meta.getPrefix();
        String prefix = raw == null || raw.isEmpty() ? "" : ColorUtil.colorize(raw) + " ";
        String nameColor = meta.getMetaValue("name-color");
        String colors = nameColor == null
                ? (plugin.getConfig().getBoolean("display.inherit-prefix-color", true)
                    ? ColorUtil.getLastColors(prefix) : ChatColor.RESET.toString())
                : ColorUtil.getLastColors(ColorUtil.colorize(nameColor));
        String suffix = plugin.getAfkManager().isAfk(player)
                ? ColorUtil.colorize(plugin.getConfig().getString("afk.suffix", " &7[AFK]")) : "";

        chatEnabled = plugin.getConfig().getBoolean("display.chat", true);
        String displayName = ColorUtil.colorize(player.getDisplayName());
        // Player/metadata '%' characters must never become String.format directives.
        chatFormats.put(player.getUniqueId(),
                (prefix + colors + displayName + suffix + ChatColor.RESET + ": ").replace("%", "%%") + "%2$s");

        if (plugin.getConfig().getBoolean("display.tab", true)) {
            String rendered = prefix + colors + player.getName() + suffix;
            ListName previous = listNames.get(player.getUniqueId());
            String original = previous == null ? player.getPlayerListName() : previous.original;
            if (!ColorUtil.normalizeCodes(rendered).equals(ColorUtil.normalizeCodes(player.getPlayerListName()))) {
                player.setPlayerListName(rendered);
            }
            listNames.put(player.getUniqueId(), new ListName(original, player.getPlayerListName()));
        } else {
            restoreListName(player);
        }
        for (Scoreboard board : activeBoards()) updateTeam(board, player, prefix + colors, suffix);
    }

    private Set<Scoreboard> activeBoards() {
        Set<Scoreboard> boards = Collections.newSetFromMap(new IdentityHashMap<>());
        boards.add(Bukkit.getScoreboardManager().getMainScoreboard());
        for (Player viewer : Bukkit.getOnlinePlayers()) boards.add(viewer.getScoreboard());
        return boards;
    }

    private void updateTeam(Scoreboard board, Player player, String prefix, String suffix) {
        UUID id = player.getUniqueId();
        Map<UUID, Team> teams = ownedTeams.computeIfAbsent(board, ignored -> new HashMap<>());
        Team owned = teams.get(id);
        if (owned != null && !owned.equals(board.getTeam(owned.getName()))) {
            teams.remove(id);
            owned = null;
        }
        if (!plugin.getConfig().getBoolean("display.nametag", true)) {
            removeTeam(board, id, player.getName());
            return;
        }
        Team current = board.getEntryTeam(player.getName());
        // An entry can belong to only one team per board. Never steal another plugin's entry.
        if (current != null && !current.equals(owned)) {
            removeTeam(board, id, player.getName());
            return;
        }
        if (owned == null) {
            String name = "pp_" + id.toString().replace("-", "").substring(0, 13);
            if (board.getTeam(name) != null) return; // Even our namespace can be occupied.
            owned = board.registerNewTeam(name);
            teams.put(id, owned);
            owned.addEntry(player.getName());
        } else if (!owned.hasEntry(player.getName())) {
            owned.addEntry(player.getName());
        }
        String safePrefix = ColorUtil.truncate(prefix, 64);
        String safeSuffix = ColorUtil.truncate(suffix, 64);
        if (!ColorUtil.normalizeCodes(safePrefix).equals(ColorUtil.normalizeCodes(owned.getPrefix()))) owned.setPrefix(safePrefix);
        if (!ColorUtil.normalizeCodes(safeSuffix).equals(ColorUtil.normalizeCodes(owned.getSuffix()))) owned.setSuffix(safeSuffix);
    }

    private void removeTeam(Scoreboard board, UUID id, String entry) {
        Map<UUID, Team> teams = ownedTeams.get(board);
        if (teams == null) return;
        Team team = teams.remove(id);
        if (team == null || !team.equals(board.getTeam(team.getName()))) return;
        team.removeEntry(entry);
        // Preserve foreign entries if another plugin has reused this team.
        if (team.getEntries().isEmpty()) team.unregister();
    }

    public void refreshAll() {
        Set<Scoreboard> active = activeBoards();
        for (Scoreboard board : new ArrayList<>(ownedTeams.keySet())) {
            if (!active.contains(board)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    removeTeam(board, player.getUniqueId(), player.getName());
                }
                ownedTeams.remove(board);
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) updatePlayerPrefix(player);
    }

    public String chatFormat(UUID id) { return chatEnabled ? chatFormats.get(id) : null; }

    public void updatePlayerAfk(Player player, String suffix) {
        // The manager is the source of truth, so a LuckPerms update cannot erase AFK.
        updatePlayerPrefix(player);
    }

    public String applyPrefixColorToName(String prefix, String name) {
        return ColorUtil.getLastColors(prefix) + name;
    }

    private void restoreListName(Player player) {
        ListName state = listNames.remove(player.getUniqueId());
        if (state != null && Objects.equals(player.getPlayerListName(), state.rendered)) {
            player.setPlayerListName(state.original);
        }
    }

    public void cleanup(Player player) {
        restoreListName(player);
        chatFormats.remove(player.getUniqueId());
        for (Scoreboard board : new ArrayList<>(ownedTeams.keySet())) {
            removeTeam(board, player.getUniqueId(), player.getName());
        }
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) cleanup(player);
        ownedTeams.clear();
        chatFormats.clear();
        chatEnabled = false;
    }

    private record ListName(String original, String rendered) {}
}
