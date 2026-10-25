package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.managers.AfkManager;
import net.luckperms.api.*;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrefixManagerTest {
    PrefixPro plugin;
    PrefixManager manager;
    Player player, viewer;
    Scoreboard main, custom;
    CachedMetaData meta;
    YamlConfiguration config;
    MockedStatic<Bukkit> bukkit;
    Map<Scoreboard, Map<String, Team>> teams;
    AtomicReference<String> listName;

    Scoreboard board() {
        Scoreboard board = mock(Scoreboard.class);
        Map<String, Team> byName = new HashMap<>();
        teams.put(board, byName);
        when(board.getTeam(anyString())).thenAnswer(i -> byName.get(i.getArgument(0)));
        when(board.getEntryTeam(anyString())).thenAnswer(i -> byName.values().stream()
                .filter(t -> t.hasEntry(i.getArgument(0))).findFirst().orElse(null));
        when(board.registerNewTeam(anyString())).thenAnswer(i -> {
            String name = i.getArgument(0);
            if (byName.containsKey(name)) throw new IllegalArgumentException("occupied");
            Team team = mock(Team.class);
            Set<String> entries = new HashSet<>();
            AtomicReference<String> prefix = new AtomicReference<>("");
            AtomicReference<String> suffix = new AtomicReference<>("");
            when(team.getName()).thenReturn(name);
            when(team.getEntries()).thenAnswer(x -> new HashSet<>(entries));
            when(team.hasEntry(anyString())).thenAnswer(x -> entries.contains(x.getArgument(0)));
            doAnswer(x -> { entries.add(x.getArgument(0)); return null; }).when(team).addEntry(anyString());
            when(team.removeEntry(anyString())).thenAnswer(x -> entries.remove(x.getArgument(0)));
            when(team.getPrefix()).thenAnswer(x -> prefix.get());
            when(team.getSuffix()).thenAnswer(x -> suffix.get());
            doAnswer(x -> { prefix.set(x.getArgument(0)); return null; }).when(team).setPrefix(anyString());
            doAnswer(x -> { suffix.set(x.getArgument(0)); return null; }).when(team).setSuffix(anyString());
            doAnswer(x -> { byName.remove(name); return null; }).when(team).unregister();
            byName.put(name, team);
            return team;
        });
        return board;
    }

    @BeforeEach void setup() {
        teams = new IdentityHashMap<>();
        main = board(); custom = board();
        plugin = mock(PrefixPro.class);
        config = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getAfkManager()).thenReturn(mock(AfkManager.class));
        player = mock(Player.class); viewer = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(viewer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("PrefixTester");
        when(viewer.getName()).thenReturn("PrefixViewer");
        when(player.getDisplayName()).thenReturn("PrefixTester");
        when(viewer.getDisplayName()).thenReturn("PrefixViewer");
        when(player.getScoreboard()).thenReturn(main);
        when(viewer.getScoreboard()).thenReturn(custom);
        listName = new AtomicReference<>("Original list name");
        when(player.getPlayerListName()).thenAnswer(i -> listName.get());
        doAnswer(i -> { listName.set(i.getArgument(0)); return null; }).when(player).setPlayerListName(anyString());
        LuckPerms lp = mock(LuckPerms.class);
        PlayerAdapter<Player> adapter = mock(PlayerAdapter.class);
        when(lp.getPlayerAdapter(Player.class)).thenReturn(adapter);
        meta = mock(CachedMetaData.class);
        when(adapter.getMetaData(any(Player.class))).thenReturn(meta);
        when(meta.getPrefix()).thenReturn("&#123456[100%]&l");
        bukkit = mockStatic(Bukkit.class);
        var scoreboardManager = mock(ScoreboardManager.class);
        when(scoreboardManager.getMainScoreboard()).thenReturn(main);
        bukkit.when(Bukkit::getScoreboardManager).thenReturn(scoreboardManager);
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(player, viewer));
        manager = new PrefixManager(plugin, lp);
    }

    @AfterEach void cleanup() { bukkit.close(); }

    @Test void preservesForeignTeamsAndNeverAssignsScoreboards() {
        Team foreign = custom.registerNewTeam("other_plugin");
        foreign.addEntry(player.getName());
        manager.updatePlayerPrefix(player);
        assertSame(foreign, custom.getEntryTeam(player.getName()));
        verify(foreign, never()).setPrefix(anyString());
        verify(player, never()).setScoreboard(any());
        verify(viewer, never()).setScoreboard(any());
        assertNotNull(main.getEntryTeam(player.getName()));
    }

    @Test void updatesTeamsInPlaceAndKeepsAfkOnPrefixChanges() {
        manager.updatePlayerPrefix(player);
        Team owned = main.getEntryTeam(player.getName());
        when(plugin.getAfkManager().isAfk(player)).thenReturn(true);
        when(meta.getPrefix()).thenReturn("&6[Admin]");
        manager.updatePlayerPrefix(player);
        assertSame(owned, main.getEntryTeam(player.getName()));
        assertTrue(owned.getPrefix().contains("[Admin]"));
        assertTrue(owned.getSuffix().contains("[AFK]"));
        assertTrue(listName.get().contains("[AFK]"));
        verify(owned, never()).unregister();
    }

    @Test void disabledDisplaysReleaseOwnedState() {
        manager.updatePlayerPrefix(player);
        config.set("display.tab", false);
        config.set("display.nametag", false);
        config.set("display.chat", false);
        manager.updatePlayerPrefix(player);
        assertEquals("Original list name", listName.get());
        assertNull(main.getEntryTeam(player.getName()));
        assertNull(manager.chatFormat(player.getUniqueId()));
    }

    @Test void shutdownDoesNotClobberAnotherPluginsTabOverride() {
        manager.updatePlayerPrefix(player);
        listName.set("Another plugin's name");
        manager.shutdown();
        assertEquals("Another plugin's name", listName.get());
        assertNull(main.getEntryTeam(player.getName()));
    }

    @Test void foreignEntryAddedToOwnedTeamSurvivesCleanup() {
        manager.updatePlayerPrefix(player);
        Team team = main.getEntryTeam(player.getName());
        team.addEntry("foreign_entry");
        manager.cleanup(player);
        assertSame(team, main.getEntryTeam("foreign_entry"));
        assertFalse(team.hasEntry(player.getName()));
        verify(team, never()).unregister();
    }

    @Test void immutableChatSnapshotEscapesPercentMetadata() {
        manager.updatePlayerPrefix(player);
        String rendered = String.format(manager.chatFormat(player.getUniqueId()), "unused", "hello 50%");
        assertTrue(rendered.contains("[100%]"));
        assertTrue(rendered.endsWith("hello 50%"));
    }
}
