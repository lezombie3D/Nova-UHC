package net.novaproject.novauhc.utils;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

@Getter
@Setter
public class ConfigUtils {
    private static final List<String> REGISTERED_PATHS = new ArrayList<>();
    private static final String LOBBY_CONFIG_PATH = "api/worldconfig.yml";
    private static final String GENERAL_CONFIG_PATH = "api/generalconfig.yml";
    private static final String LANG_CONFIG = "api/lang.yml";
    private static final String MENU_CONFIG = "api/menu.yml";
    private static final String SCHEM_RESOURCE_DIR = "api/schem";

    public static void setup() {
        registerConfig(LOBBY_CONFIG_PATH);
        registerConfig(GENERAL_CONFIG_PATH);
        registerConfig(LANG_CONFIG);
        registerConfig(MENU_CONFIG);
        saveAllSchems();
        createDefaultFiles();
        CommonString.loadMessages(getLangConfig());
    }

    public static void registerConfig(String path) {
        if (!REGISTERED_PATHS.contains(path)) {
            REGISTERED_PATHS.add(path);
        }
    }

    public static void createDefaultFiles(String path) {
        File file = new File(Main.get().getDataFolder(), path);
        if (!file.exists()) {
            Main.get().saveResource(path, false);
            Bukkit.getLogger().log(Level.INFO, "Created default config: {0}", path);
        }
    }

    public static void saveAllSchems() {
        try {
            URL jarUrl = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarUrl.toURI());
            if (jarFile.isFile()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(SCHEM_RESOURCE_DIR + "/") && name.endsWith(".schematic")) {
                            String fileName = name.substring(SCHEM_RESOURCE_DIR.length() + 1);
                            File dest = new File(Main.get().getDataFolder(), SCHEM_RESOURCE_DIR + "/" + fileName);
                            if (!dest.exists()) {
                                Main.get().saveResource(SCHEM_RESOURCE_DIR + "/" + fileName, false);
                                Bukkit.getLogger().log(Level.INFO, "Schematique copiée: {0}", fileName);
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Erreur lors de la copie des schems", e);
        }
    }

    private static void createDefaultFiles() {
        REGISTERED_PATHS.forEach(path -> {
            File file = new File(Main.get().getDataFolder(), path);
            if (!file.exists()) {
                Main.get().saveResource(path, false);
                Bukkit.getLogger().log(Level.INFO, "Created default config: {0}", path);
            }
        });
    }

    public static File getFile(FileConfiguration config) {
        return new File(Main.get().getDataFolder(), REGISTERED_PATHS.contains(config.getString("path")) ? config.getString("path") : "");
    }

    public static FileConfiguration getConfig(String path) {
        File file = new File(Main.get().getDataFolder(), path);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveConfig(FileConfiguration config, String path) throws IOException {
        File file = new File(Main.get().getDataFolder(), path);
        if (!file.exists()) {
            throw new IOException("Config file does not exist: " + path);
        }
        config.save(file);
    }

    public static Location getLocation(FileConfiguration config, String path) {
        if (!config.contains(path)) return null;

        String worldName = config.getString(path + ".world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            Bukkit.getLogger().warning("Invalid world specified in config: " + worldName);
            return null;
        }

        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw", 0),
                (float) config.getDouble(path + ".pitch", 0)
        );
    }

    public static void setLocation(FileConfiguration config, String path, Location location) {
        if (location == null || location.getWorld() == null) return;

        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }

    public static List<Location> getLocations(FileConfiguration config, String basePath) {
        List<Location> locations = new ArrayList<>();
        int index = 0;
        while (true) {
            String path = basePath + "." + index;
            if (!config.contains(path)) break;

            Location loc = getLocation(config, path);
            if (loc != null) {
                locations.add(loc);
            }
            index++;
        }
        return locations;
    }

    public static <T> T get(FileConfiguration config, String path, Class<T> type) {
        Object value = config.get(path);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public static void set(FileConfiguration config, String path, Object value) {
        config.set(path, value);
    }

    public static FileConfiguration getWorldConfig() {
        return getConfig(LOBBY_CONFIG_PATH);
    }

    public static FileConfiguration getGeneralConfig() {
        return getConfig(GENERAL_CONFIG_PATH);
    }

    public static FileConfiguration getLangConfig() {
        return getConfig(LANG_CONFIG);
    }

    public static void saveLangConfig(FileConfiguration config) {
        try {
            saveConfig(config, LANG_CONFIG);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileConfiguration getMenuConfig() {
        return getConfig(MENU_CONFIG);
    }

}
