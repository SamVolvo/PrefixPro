package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.commands.*;
import com.samvolvo.prefixPro.managers.AfkManager;
import com.samvolvo.prefixPro.utils.Messages;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandTest {
    PrefixPro plugin;
    Command command;
    Messages messages;

    @BeforeEach void setup() {
        plugin = mock(PrefixPro.class);
        when(plugin.getConfig()).thenReturn(new YamlConfiguration());
        var messageConfig = new YamlConfiguration();
        messageConfig.set("permissions.no-permission", "Denied");
        messageConfig.set("commands.usage", "Usage");
        messageConfig.set("afk.chat.disabled", "Disabled");
        messageConfig.set("commands.player-only", "Player only");
        when(plugin.getMessageConfig()).thenReturn(messageConfig);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        messages = new Messages(plugin);
        when(plugin.getMessages()).thenReturn(messages);
        when(plugin.getAfkManager()).thenReturn(mock(AfkManager.class));
        command = mock(Command.class);
    }

    @Test void bothReloadCommandsUseSafeReloadOnly() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("prefixpro.admin")).thenReturn(true);
        var executor = new PrefixProCommand(plugin);
        assertTrue(executor.onCommand(sender, command, "prefixpro", new String[]{"config", "reload"}));
        assertTrue(executor.onCommand(sender, command, "prefixpro", new String[]{"plugin", "reload"}));
        verify(plugin, times(2)).reload();
        verify(plugin, never()).onEnable();
        verify(plugin, never()).onDisable();
    }

    @Test void reloadFailureIsReported() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("prefixpro.admin")).thenReturn(true);
        doThrow(new IllegalStateException("invalid YAML")).when(plugin).reload();
        new PrefixProCommand(plugin).onCommand(sender, command, "prefixpro", new String[]{"config", "reload"});
        verify(sender).sendMessage(messages.COMMAND_RELOAD_FAILED);
    }

    @Test void commandPermissionAndUsageAndCompletion() {
        CommandSender sender = mock(CommandSender.class);
        var executor = new PrefixProCommand(plugin);
        executor.onCommand(sender, command, "prefixpro", new String[]{"config", "reload"});
        verify(plugin, never()).reload();
        assertTrue(executor.onTabComplete(sender, command, "prefixpro", new String[]{""}).isEmpty());
        when(sender.hasPermission("prefixpro.admin")).thenReturn(true);
        executor.onCommand(sender, command, "prefixpro", new String[]{"wrong", "reload"});
        verify(sender).sendMessage(messages.COMMAND_USAGE);
        assertEquals(java.util.List.of("config"), executor.onTabComplete(sender, command, "prefixpro", new String[]{"co"}));
        assertEquals(java.util.List.of("reload"), executor.onTabComplete(sender, command, "prefixpro", new String[]{"plugin","r"}));
    }

    @Test void afkChecksConsolePermissionDisabledAndToggle() {
        var executor = new AfkCommand(plugin);
        CommandSender console = mock(CommandSender.class);
        executor.onCommand(console, command, "afk", new String[0]);
        verify(console).sendMessage(messages.PLAYER_CONSOLE_ONLY);
        Player player = mock(Player.class);
        executor.onCommand(player, command, "afk", new String[0]);
        verify(player).sendMessage(messages.PLAYER_NO_PERMISSION);
        when(player.hasPermission("prefixpro.afk")).thenReturn(true);
        executor.onCommand(player, command, "afk", new String[0]);
        verify(player).sendMessage(messages.AFK_DISABLED);
        when(plugin.getAfkManager().enabled()).thenReturn(true);
        executor.onCommand(player, command, "afk", new String[0]);
        verify(plugin.getAfkManager()).setAfk(player, true);
        when(plugin.getAfkManager().isAfk(player)).thenReturn(true);
        executor.onCommand(player, command, "afk", new String[0]);
        verify(plugin.getAfkManager()).setAfk(player, false);
    }
}
