package com.samvolvo.prefixPro.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.samvolvo.prefixPro.PrefixPro;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class UpdateChecker {

    private final PrefixPro plugin;
    private final Logger logger;
    private final String modrinthProjectId;
    private final String currentVersion;

    public UpdateChecker(PrefixPro plugin, String modrinthProjectId) {
        this.plugin = plugin;
        this.logger = plugin.getCustomLogger();
        this.modrinthProjectId = modrinthProjectId;
        this.currentVersion = normalizeVersion(plugin.getDescription().getVersion());
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::checkForUpdatesAsync);
    }

    private void checkForUpdatesAsync() {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                    "https://api.modrinth.com/v2/project/"
                            + modrinthProjectId
                            + "/version?include_changelog=false"
            );

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.setRequestProperty(
                    "User-Agent",
                    "samvolvo/PrefixPro/" + currentVersion
            );
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.warning("Update check failed. HTTP status: " + responseCode);
                return;
            }

            String response = readResponse(connection);

            JsonElement parsed = JsonParser.parseString(response);
            if (!parsed.isJsonArray()) {
                logger.warning("Update check failed: unexpected response format.");
                return;
            }

            JsonArray versions = parsed.getAsJsonArray();
            if (versions.isEmpty()) {
                logger.warning("Update check failed: no versions found on Modrinth.");
                return;
            }

            JsonObject latestVersionObject = findLatestVersion(versions);
            if (latestVersionObject == null) {
                logger.warning("Update check failed: could not determine latest version.");
                return;
            }

            String latestVersion = normalizeVersion(getAsString(latestVersionObject, "version_number"));
            String publishedAt = getAsString(latestVersionObject, "date_published");

            if (latestVersion == null || latestVersion.isBlank()) {
                logger.warning("Update check failed: latest version number missing.");
                return;
            }

            if (currentVersion.equalsIgnoreCase(latestVersion)) {
                logger.info("You are running the latest version of PrefixPro! (" + currentVersion + ")");
            } else if (isNewerVersion(latestVersion, currentVersion)) {
                logger.warning("A new version of PrefixPro is available!");
                logger.warning("Current version: " + currentVersion);
                logger.warning("Latest version: " + latestVersion);
                if (publishedAt != null && !publishedAt.isBlank()) {
                    logger.warning("Published at: " + publishedAt);
                }
            } else {
                logger.info("PrefixPro " + currentVersion + " is newer than the published release.");
            }

        } catch (Exception e) {
            logger.warning("Failed to check for updates: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private JsonObject findLatestVersion(JsonArray versions) {
        JsonObject latest = null;
        Instant latestDate = null;

        for (JsonElement element : versions) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject versionObject = element.getAsJsonObject();
            if (!"release".equals(getAsString(versionObject, "version_type"))) continue;
            String datePublishedRaw = getAsString(versionObject, "date_published");

            if (datePublishedRaw == null || datePublishedRaw.isBlank()) {
                continue;
            }

            try {
                Instant published = Instant.parse(datePublishedRaw);

                if (latest == null || latestDate == null || published.isAfter(latestDate)) {
                    latest = versionObject;
                    latestDate = published;
                }
            } catch (Exception ignored) {
                // Skip malformed dates
            }
        }

        return latest;
    }

    private String getAsString(JsonObject object, String memberName) {
        if (object == null || !object.has(memberName) || object.get(memberName).isJsonNull()) {
            return null;
        }

        try {
            return object.get(memberName).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }

        version = version.trim();

        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }

        return version;
    }

    public static boolean isNewerVersion(String candidate, String current) {
        try {
            String[] next = candidate.replaceFirst("^[vV]", "").split("\\.");
            String[] installed = current.replaceFirst("^[vV]", "").split("\\.");
            for (int i = 0; i < Math.max(next.length, installed.length); i++) {
                int a = i < next.length ? Integer.parseInt(next[i]) : 0;
                int b = i < installed.length ? Integer.parseInt(installed[i]) : 0;
                if (a != b) return a > b;
            }
        } catch (NumberFormatException ignored) {
            // Unknown version schemes must not generate false update notifications.
        }
        return false;
    }
}
