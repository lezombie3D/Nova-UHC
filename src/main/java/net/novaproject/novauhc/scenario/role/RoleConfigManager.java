package net.novaproject.novauhc.scenario.role;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class RoleConfigManager {

    private final File baseFolder;

    public RoleConfigManager(File baseFolder) {
        this.baseFolder = baseFolder;
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }
    }

    public FileConfiguration loadRoleConfig(String scenarioRole, String camp, String role) {
        File roleFile = new File(baseFolder, scenarioRole + File.separator + camp + File.separator + role + ".yml");
        if (!roleFile.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(roleFile);
    }

    public void saveRoleConfig(String scenarioRole, String camp, String role, FileConfiguration config) throws IOException {
        File roleFile = new File(baseFolder, scenarioRole + File.separator + camp + File.separator + role + ".yml");
        if (!roleFile.getParentFile().exists()) {
            roleFile.getParentFile().mkdirs();
        }
        config.save(roleFile);
    }

    public FileConfiguration createRoleConfigIfAbsent(String scenarioRole, String camp, String role) throws IOException {
        File roleFile = new File(baseFolder, scenarioRole + File.separator + camp + File.separator + role + ".yml");
        if (!roleFile.exists()) {
            if (!roleFile.getParentFile().exists()) {
                roleFile.getParentFile().mkdirs();
            }
            roleFile.createNewFile();
        }
        return YamlConfiguration.loadConfiguration(roleFile);
    }
}