package com.samvolvo.prefixPro;

import com.samvolvo.prefixPro.managers.ConfigManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigManagerTest {
    @TempDir Path folder;
    PrefixPro plugin;

    @BeforeEach void setup() {
        plugin = mock(PrefixPro.class);
        when(plugin.getDataFolder()).thenReturn(folder.toFile());
        when(plugin.getResource(anyString())).thenAnswer(i ->
                getClass().getClassLoader().getResourceAsStream(i.getArgument(0)));
    }

    @Test void firstInstallCreatesBothDocuments() {
        ConfigManager manager = new ConfigManager(plugin);
        assertEquals(4, manager.getConfig().getInt("config-version"));
        assertTrue(Files.exists(folder.resolve("Customization/messages.yml")));
    }

    @Test void migrationPreservesCustomUnknownAndQuotedValuesWithBackup() throws Exception {
        String original = "config-version: 3\ndisplay:\n  chat: false\naf k: custom\naf k2: \"a: b # c\"\n"
                + "messages:\n  prefix: 'He said \"hi\" &c'\naf k3:\n  list: [one, two]\n";
        Files.writeString(folder.resolve("config.yml"), original);
        ConfigManager manager = new ConfigManager(plugin);
        assertFalse(manager.getConfig().getBoolean("display.chat"));
        assertEquals("He said \"hi\" &c", manager.getConfig().getString("messages.prefix"));
        assertEquals("a: b # c", manager.getConfig().getString("af k2"));
        assertEquals(2, manager.getConfig().getStringList("af k3.list").size());
        try (var paths = Files.list(folder)) {
            Path backup = paths.filter(p -> p.toString().endsWith(".bak")).findFirst().orElseThrow();
            assertEquals(original, Files.readString(backup));
        }
        assertEquals("a: b # c", YamlConfiguration.loadConfiguration(folder.resolve("config.yml").toFile()).getString("af k2"));
    }

    @Test void rejectedMessagesDoNotReplaceLiveConfig() throws Exception {
        ConfigManager manager = new ConfigManager(plugin);
        var oldConfig = manager.getConfig();
        var oldMessages = manager.getMessagesConfig();
        Files.writeString(folder.resolve("config.yml"), "display:\n  chat: false\n");
        Files.writeString(folder.resolve("Customization/messages.yml"), "afk: [unterminated");
        assertThrows(IllegalStateException.class, manager::reloadConfig);
        assertSame(oldConfig, manager.getConfig());
        assertSame(oldMessages, manager.getMessagesConfig());
        assertTrue(manager.getConfig().getBoolean("display.chat"));
    }

    @Test void invalidTypesAndTimeoutRejectReload() throws Exception {
        ConfigManager manager = new ConfigManager(plugin);
        for (String yaml : new String[]{"afk:\n  auto-time: 0\n", "afk:\n  enabled: nope\n", "afk: false\n"}) {
            Files.writeString(folder.resolve("config.yml"), yaml);
            assertThrows(IllegalStateException.class, manager::reloadConfig);
        }
    }

    @Test void unchangedReloadDoesNotRewriteFilesAndMessagesRefresh() throws Exception {
        ConfigManager manager = new ConfigManager(plugin);
        var before = Files.getLastModifiedTime(folder.resolve("config.yml"));
        Path messages = folder.resolve("Customization/messages.yml");
        var changed = YamlConfiguration.loadConfiguration(messages.toFile());
        changed.set("afk.title.title", "Custom title");
        changed.save(messages.toFile());
        manager.reloadConfig();
        assertEquals(before, Files.getLastModifiedTime(folder.resolve("config.yml")));
        assertEquals("Custom title", manager.getMessagesConfig().getString("afk.title.title"));
    }
}
