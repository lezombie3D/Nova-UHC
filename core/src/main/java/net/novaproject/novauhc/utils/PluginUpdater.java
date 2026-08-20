package net.novaproject.novauhc.utils;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.logging.Level;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginUpdater {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/lezombie3d/Nova-UHC/releases/latest";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private final JavaPlugin plugin;

    public PluginUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        if (!plugin.getConfig().getBoolean("updater.enabled", true)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                runCheck();
            }
        }.runTaskAsynchronously(plugin);
    }

    private void runCheck() {
        try {
            JsonObject releaseData = fetchReleaseData();
            if (releaseData == null) return;

            String currentVersion = plugin.getDescription().getVersion();
            JsonElement tag = releaseData.get("tag_name");
            if (tag == null) {
                plugin.getLogger().warning("Updater: réponse GitHub sans tag_name, ignorée.");
                return;
            }
            String latestVersion = tag.getAsString();

            if (latestVersion.equals(currentVersion)) {
                plugin.getLogger().info("Plugin à jour (" + currentVersion + ").");
                return;
            }

            if (!plugin.getConfig().getBoolean("updater.auto-update", false)) {
                plugin.getLogger().info("Nouvelle version disponible : " + latestVersion
                        + " (actuelle : " + currentVersion + "). Auto-update désactivé (updater.auto-update: false).");
                return;
            }

            String downloadUrl = getDownloadUrl(releaseData);
            if (downloadUrl == null) {
                plugin.getLogger().warning("Updater: URL de téléchargement introuvable pour " + latestVersion + ".");
                return;
            }

            File tempFile = new File(plugin.getDataFolder().getParent(), "plugin_temp.jar");
            downloadFile(downloadUrl, tempFile);
            applyUpdate(tempFile, latestVersion);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Updater: échec de la vérification de mise à jour: " + e.getMessage(), e);
        }
    }

    private JsonObject fetchReleaseData() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            plugin.getLogger().warning("Updater: GitHub a répondu HTTP " + code + ", vérification ignorée.");
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return new Gson().fromJson(reader, JsonObject.class);
        }
    }

    private String getDownloadUrl(JsonObject releaseData) {
        JsonArray assets = releaseData.getAsJsonArray("assets");
        if (assets == null || assets.size() == 0) return null;
        JsonElement url = assets.get(0).getAsJsonObject().get("browser_download_url");
        return url == null ? null : url.getAsString();
    }

    private void downloadFile(String url, File outputFile) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void applyUpdate(File tempFile, String latestVersion) throws Exception {
        File pluginFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Bukkit.getPluginManager().disablePlugin(plugin);
                    Files.move(tempFile.toPath(), pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Mise à jour " + latestVersion + " appliquée. Redémarrez le serveur.");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Updater: échec d'application de la mise à jour: " + e.getMessage(), e);
                }
            }
        }.runTask(plugin);
    }
}

