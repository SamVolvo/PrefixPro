package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.listeners.*;
import com.samvolvo.prefixPro.managers.AfkManager;
import com.samvolvo.prefixPro.utils.Messages;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListenerTest {
    PrefixPro plugin;
    Player player;
    AfkManager afk;
    PlayerListener listener;
    YamlConfiguration config;

    @BeforeEach void setup() {
        plugin = mock(PrefixPro.class);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("Tester");
        afk = mock(AfkManager.class);
        when(plugin.getAfkManager()).thenReturn(afk);
        config = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(config);
        listener = new PlayerListener(plugin);
    }

    @Test void afkToggleCommandDoesNotCountAsActivityBeforeItExecutes() {
        listener.onPlayerCommand(new PlayerCommandPreprocessEvent(player, "/afk"));
        listener.onPlayerCommand(new PlayerCommandPreprocessEvent(player, "/PrefixPro:AFK"));
        verify(afk, never()).updateActivity(player);
        listener.onPlayerCommand(new PlayerCommandPreprocessEvent(player, "/help"));
        verify(afk).updateActivity(player);
    }

    @Test void damageCancellationRespectsProtectionSwitch() {
        var damage = mock(EntityDamageEvent.class);
        when(damage.getEntity()).thenReturn(player);
        listener.onDamage(damage);
        verify(damage, never()).setCancelled(true);
        when(afk.protectedFromDamage(player)).thenReturn(true);
        listener.onDamage(damage);
        verify(damage).setCancelled(true);
    }

    @Test void cameraMovementCountsWhenEnabled() {
        World world = mock(World.class);
        var event = new PlayerMoveEvent(player, new Location(world, 0, 64, 0, 0, 0),
                new Location(world, 0, 64, 0, 10, 0));
        listener.onPlayerMove(event);
        verify(afk).updateActivity(player);
        clearInvocations(afk);
        config.set("afk.detect-look", false);
        listener.onPlayerMove(event);
        verify(afk, never()).updateActivity(player);
    }

    @Test void passivePushIsNotActivityWhileProtected() {
        World world = mock(World.class);
        var event = new PlayerMoveEvent(player, new Location(world, 0, 64, 0), new Location(world, 0.1, 64, 0));
        when(afk.protectedFromPushing(player)).thenReturn(true);
        listener.onPlayerMove(event);
        assertEquals(event.getFrom(), event.getTo());
        verify(afk, never()).updateActivity(player);
    }

    @Test void asyncChatOnlyReadsSnapshotAndQueuesActivity() {
        PrefixManager prefixes = mock(PrefixManager.class);
        when(plugin.getPrefixManager()).thenReturn(prefixes);
        when(prefixes.chatFormat(player.getUniqueId())).thenReturn("Rank %1$s: %2$s");
        var event = new AsyncPlayerChatEvent(true, player, "hello", new HashSet<>());
        listener.onPlayerChat(event);
        verify(plugin).requestActivity(player.getUniqueId());
        verify(afk, never()).updateActivity(any());
        verify(plugin, never()).getConfig();
        assertEquals("Rank Tester: hello", String.format(event.getFormat(), "Tester", event.getMessage()));
    }

    @Test void pistonProtectionOnlyAppliesWhenEnabled() {
        var piston = mock(Block.class);
        var target = mock(Block.class);
        var world = mock(World.class);
        when(piston.getWorld()).thenReturn(world);
        when(piston.getRelative(BlockFace.EAST)).thenReturn(target);
        when(target.getX()).thenReturn(1);
        when(world.getNearbyEntities(any(BoundingBox.class))).thenReturn(List.of(player));
        var event = new BlockPistonExtendEvent(piston, List.of(), BlockFace.EAST);
        var listener = new AfkListener(plugin);
        listener.onPistonExtend(event);
        assertFalse(event.isCancelled());
        when(afk.enabled()).thenReturn(true);
        when(afk.protectedFromPushing(player)).thenReturn(true);
        listener.onPistonExtend(event);
        assertTrue(event.isCancelled());
    }

    @Test void joinMessagesUseCurrentConfigurationAfterReload() {
        var messageConfig = new YamlConfiguration();
        messageConfig.set("join", "Hello {player}");
        when(plugin.getMessageConfig()).thenReturn(messageConfig);
        Messages messages = new Messages(plugin);
        when(plugin.getMessages()).thenReturn(messages);
        var joinListener = new CustomJoinMessage(plugin);
        var event = new PlayerJoinEvent(player, "old");
        joinListener.onPlayerJoin(event);
        assertEquals("Hello Tester", event.getJoinMessage());
        var reloaded = new YamlConfiguration();
        reloaded.set("messages.join-leave-message.enabled", false);
        when(plugin.getConfig()).thenReturn(reloaded);
        joinListener.onPlayerJoin(event);
        assertNull(event.getJoinMessage());
    }
}
