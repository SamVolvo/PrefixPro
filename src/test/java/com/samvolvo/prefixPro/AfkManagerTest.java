package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.managers.AfkManager;
import com.samvolvo.prefixPro.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AfkManagerTest {
    PrefixPro plugin;
    Player player;
    AfkManager afk;
    YamlConfiguration config;
    AtomicLong clock;
    AtomicBoolean invulnerable, collidable;
    MockedStatic<Bukkit> bukkit;

    @BeforeEach void setup() {
        plugin = mock(PrefixPro.class);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.isOnline()).thenReturn(true);
        when(player.hasPermission("prefixpro.afk")).thenReturn(true);
        config = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getPrefixManager()).thenReturn(mock(PrefixManager.class));
        var messagesConfig = new YamlConfiguration();
        when(plugin.getMessageConfig()).thenReturn(messagesConfig);
        Messages messages = new Messages(plugin);
        when(plugin.getMessages()).thenReturn(messages);
        invulnerable = new AtomicBoolean(false);
        collidable = new AtomicBoolean(true);
        when(player.isInvulnerable()).thenAnswer(i -> invulnerable.get());
        when(player.isCollidable()).thenAnswer(i -> collidable.get());
        doAnswer(i -> { invulnerable.set(i.getArgument(0)); return null; }).when(player).setInvulnerable(anyBoolean());
        doAnswer(i -> { collidable.set(i.getArgument(0)); return null; }).when(player).setCollidable(anyBoolean());
        clock = new AtomicLong(0);
        afk = new AfkManager(plugin, clock::get);
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(player));
    }

    @AfterEach void close() { bukkit.close(); }

    @Test void protectionRestoresPreviousStateOnQuit() {
        invulnerable.set(true);
        collidable.set(false);
        afk.setAfk(player, true);
        assertTrue(afk.isAfk(player));
        afk.cleanup(player);
        assertTrue(invulnerable.get());
        assertFalse(collidable.get());
        assertFalse(afk.isAfk(player));
    }

    @Test void defaultStateRestoresOnShutdown() {
        afk.setAfk(player, true);
        assertTrue(invulnerable.get());
        assertFalse(collidable.get());
        afk.shutdown();
        assertFalse(invulnerable.get());
        assertTrue(collidable.get());
    }

    @Test void optionalProtectionNeverChangesPlayerFlags() {
        config.set("afk.protection.enabled", false);
        afk.setAfk(player, true);
        assertTrue(afk.isAfk(player));
        assertFalse(afk.protectedFromPushing(player));
        afk.setAfk(player, false);
        verify(player, never()).setInvulnerable(anyBoolean());
        verify(player, never()).setCollidable(anyBoolean());
    }

    @Test void permissionLossCannotPreventRestoration() {
        afk.setAfk(player, true);
        when(player.hasPermission("prefixpro.afk")).thenReturn(false);
        afk.tick();
        assertFalse(afk.isAfk(player));
        assertFalse(invulnerable.get());
        assertTrue(collidable.get());
    }

    @Test void inactivityUsesElapsedTimeAndActivityResetsIt() {
        config.set("afk.auto-time", 3);
        afk.updateActivity(player);
        clock.set(2999);
        afk.tick();
        assertFalse(afk.isAfk(player));
        afk.updateActivity(player);
        clock.set(5998);
        afk.tick();
        assertFalse(afk.isAfk(player));
        clock.set(5999);
        afk.tick();
        assertTrue(afk.isAfk(player));
    }

    @Test void disablingFullSystemOnReloadRestoresProtection() {
        afk.setAfk(player, true);
        config.set("afk.enabled", false);
        afk.reload();
        assertFalse(afk.isAfk(player));
        assertFalse(invulnerable.get());
        assertTrue(collidable.get());
        afk.setAfk(player, true);
        assertFalse(afk.isAfk(player));
    }

    @Test void manualAfkCanRemainUntilSneakWhenConfigured() {
        config.set("afk.return-on-activity", false);
        afk.setAfk(player, true);
        afk.updateActivity(player);
        assertTrue(afk.isAfk(player));
        afk.setAfk(player, false);
        assertFalse(afk.isAfk(player));
    }

    @Test void autoAfkCanBeDisabledIndependently() {
        config.set("afk.auto-enabled", false);
        afk.updateActivity(player);
        clock.set(1000000);
        afk.tick();
        assertFalse(afk.isAfk(player));
        afk.setAfk(player, true);
        assertTrue(afk.isAfk(player));
    }
}
