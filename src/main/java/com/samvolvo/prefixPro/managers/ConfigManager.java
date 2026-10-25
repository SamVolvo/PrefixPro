package com.samvolvo.prefixPro.managers;

import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;

/** Loads both documents before publishing either. Never falls back from invalid YAML. */
public class ConfigManager {
    private final PrefixPro plugin;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;

    public ConfigManager(PrefixPro plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        try {
            Prepared nextConfig = prepare("config.yml", "config-version", 4);
            Prepared nextMessages = prepare("Customization/messages.yml", "messages-version", 2);
            validate(nextConfig.yaml, defaults("config.yml"));
            validate(nextMessages.yaml, defaults("Customization/messages.yml"));
            persist(nextConfig);
            persist(nextMessages);
            config = nextConfig.yaml;
            messagesConfig = nextMessages.yaml;
        } catch (IOException | InvalidConfigurationException e) {
            throw new IllegalStateException("Cannot load PrefixPro configuration: " + e.getMessage(), e);
        }
    }

    private YamlConfiguration defaults(String resource) throws IOException, InvalidConfigurationException {
        try (InputStream stream = plugin.getResource(resource)) {
            if (stream == null) throw new IOException("Missing packaged resource: " + resource);
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.options().parseComments(true);
            yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return yaml;
        }
    }

    private Prepared prepare(String resource, String versionKey, int version)
            throws IOException, InvalidConfigurationException {
        Path path = plugin.getDataFolder().toPath().resolve(resource);
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        if (Files.exists(path)) yaml.load(path.toFile());
        YamlConfiguration defaults = defaults(resource);
        boolean changed = !Files.exists(path);
        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key)) {
                if (yaml.contains(key, true) && !yaml.isConfigurationSection(key)) {
                    throw new InvalidConfigurationException("Expected section at " + key);
                }
            } else if (!yaml.contains(key, true)) {
                yaml.set(key, defaults.get(key));
                yaml.setComments(key, defaults.getComments(key));
                changed = true;
            }
        }
        if (yaml.getInt(versionKey) < version) {
            yaml.set(versionKey, version);
            changed = true;
        }
        return new Prepared(path, yaml, changed);
    }

    private void validate(YamlConfiguration yaml, YamlConfiguration defaults) throws InvalidConfigurationException {
        for (String key : defaults.getKeys(true)) {
            Object value = defaults.get(key);
            if (value instanceof Boolean && !yaml.isBoolean(key)
                    || value instanceof Number && !yaml.isInt(key)
                    || value instanceof String && !yaml.isString(key)) {
                throw new InvalidConfigurationException("Invalid value type at " + key);
            }
        }
        if (yaml.contains("afk.auto-time") && (yaml.getInt("afk.auto-time") < 1
                || yaml.getInt("afk.auto-time") > 31536000)) {
            throw new InvalidConfigurationException("afk.auto-time must be 1..31536000 seconds");
        }
        for (String key : new String[]{"afk.title.fade-in", "afk.title.stay", "afk.title.fade-out"}) {
            if (yaml.contains(key) && (yaml.getInt(key) < 0 || yaml.getInt(key) > 12000)) {
                throw new InvalidConfigurationException(key + " must be 0..12000 ticks");
            }
        }
    }

    private void persist(Prepared document) throws IOException {
        if (!document.changed) return;
        Files.createDirectories(document.path.getParent());
        if (Files.exists(document.path)) {
            Files.copy(document.path, document.path.resolveSibling(
                    document.path.getFileName() + "." + UUID.randomUUID() + ".bak"));
        }
        Path temporary = Files.createTempFile(document.path.getParent(), "prefixpro-", ".tmp");
        try {
            Files.writeString(temporary, document.yaml.saveToString(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, document.path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, document.path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private record Prepared(Path path, YamlConfiguration yaml, boolean changed) {}
    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
}
