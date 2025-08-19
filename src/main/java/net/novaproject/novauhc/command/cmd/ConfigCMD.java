package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.database.UHCGameConfiguration;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.Enchants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.novaproject.novauhc.utils.UHCUtils.getFormattedTime;

public class ConfigCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande ne peut être utilisée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "save":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /config save <nom>");
                    return true;
                }
                saveConfig(player, playerUUID, args[1]);
                break;
            case "load":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /config load <nom>");
                    return true;
                }
                loadConfig(player, playerUUID, args[1]);
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /config delete <nom>");
                    return true;
                }
                deleteConfig(player, playerUUID, args[1]);
                break;
            case "list":
                listConfigs(player, playerUUID);
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Commandes de configuration UHC ===");
        player.sendMessage(ChatColor.YELLOW + "/config save <nom> " + ChatColor.WHITE + "- Sauvegarde la configuration actuelle");
        player.sendMessage(ChatColor.YELLOW + "/config load <nom> " + ChatColor.WHITE + "- Charge une configuration sauvegardée");
        player.sendMessage(ChatColor.YELLOW + "/config delete <nom> " + ChatColor.WHITE + "- Supprime une configuration");
        player.sendMessage(ChatColor.YELLOW + "/config list " + ChatColor.WHITE + "- Liste toutes vos configurations");
    }

    private void saveConfig(Player player, UUID playerUUID, String configName) {
        List<String> scenarios = ScenarioManager.get().getActiveScenarios()
                .stream()
                .map(Scenario::getName)
                .collect(Collectors.toList());
        int teamSize = UHCManager.get().getTeam_size();
        int borderSize = (int) Common.get().getArena().getWorldBorder().getSize();
        int pvpTime = UHCManager.get().getTimerpvp();
        int bordecactivation = UHCManager.get().getTimerborder();
        int finalsize = (int) UHCManager.get().getTargetSize();
        int timereduc = (int) UHCManager.get().getReducSpeed();
        List<Integer> limite = new ArrayList<>();
        for (int i = 0; i < Enchants.values().length; i++) {
            limite.add(Enchants.values()[i].getConfigValue());
        }
        int slot = UHCManager.get().getSlot();
        int diamant = UHCManager.get().getDimamondLimit();
        int limiteD = UHCManager.get().getDiamondArmor();
        int protection = UHCManager.get().getProtectionMax();
        Map<String, ItemStack[]> stuff = UHCManager.get().start;

        UHCGameConfiguration config = new UHCGameConfiguration(
                configName,
                scenarios,
                teamSize,
                borderSize,
                timereduc,
                pvpTime,
                finalsize,
                bordecactivation,
                limite,
                slot,
                diamant,
                limiteD,
                protection,
                stuff,
                null // potionStates will be handled by the UHCConfigManager
        );

        Main.getDatabaseManager().saveUHCConfig(playerUUID, config);
        player.sendMessage(ChatColor.GREEN + "Configuration " + ChatColor.GOLD + configName + ChatColor.GREEN + " sauvegardée avec succès!");
    }

    private void loadConfig(Player player, UUID playerUUID, String configName) {
        UHCGameConfiguration config = Main.getDatabaseManager().getUHCConfig(playerUUID, configName);

        if (config == null) {
            player.sendMessage(ChatColor.RED + "Aucune configuration trouvée avec le nom " + ChatColor.GOLD + configName);
            return;
        }
        UHCManager uhc = UHCManager.get();

        uhc.setTeam_size(config.getTeamSize());
        Common.get().getArena().getWorldBorder().setSize(config.getBorderSize());
        uhc.setTargetSize(config.getFinalsize());
        uhc.setTimerpvp(config.getPvpTime());
        uhc.setReducSpeed(config.getTimereduc());

        config.getScenarios().forEach(name -> {
            ScenarioManager.get().getScenarioByName(name).ifPresent(scenario -> {
                if (!scenario.isActive()) {
                    scenario.enable();
                }
            });
        });

        uhc.start = config.getStuff();
        uhc.setSlot(config.getSlot());
        uhc.setDimamondLimit(config.getDiamant());
        uhc.setDiamondArmor(config.getLimiteD());
        uhc.applyLimitsFromList(config.getLimite());
        uhc.setProtectionMax(config.getProtection());

        // Apply potion states from the configuration
        if (config.getPotionStates() != null && !config.getPotionStates().isEmpty()) {
            Main.getDatabaseManager().getConfigManager().applyPotionStatesToEnum(config.getPotionStates());
        }

        player.sendMessage(ChatColor.GREEN + "Configuration " + ChatColor.GOLD + configName + ChatColor.GREEN + " chargée!");
        System.out.println();
        String scenariosStr = String.join(", ", config.getEnabledScenarios());
        player.sendMessage(ChatColor.YELLOW + "Scénarios: " + ChatColor.WHITE + scenariosStr);

        player.sendMessage(ChatColor.YELLOW + "Taille des équipes: " + ChatColor.WHITE + config.getTeamSize());
        player.sendMessage(ChatColor.YELLOW + "Taille initiale de la bordure: " + ChatColor.WHITE + config.getBorderSize());
        player.sendMessage(ChatColor.YELLOW + "Taille finale de la bordure: " + ChatColor.WHITE + config.getFinalsize());
        player.sendMessage(ChatColor.YELLOW + "Temps PvP: " + ChatColor.WHITE + getFormattedTime(config.getPvpTime()));
        player.sendMessage(ChatColor.YELLOW + "Activation bordure: " + ChatColor.WHITE + config.getBordecactivation() + " minutes");

        player.sendMessage(ChatColor.YELLOW + "Protection max: " + ChatColor.WHITE + config.getProtection());
        player.sendMessage(ChatColor.YELLOW + "Diamants max: " + ChatColor.WHITE + config.getDiamant());
        player.sendMessage(ChatColor.YELLOW + "Limite de diamants: " + ChatColor.WHITE + config.getLimiteD());
        player.sendMessage(ChatColor.YELLOW + "Slots max: " + ChatColor.WHITE + config.getSlot());

        player.sendMessage(ChatColor.YELLOW + "Types de stuff: " + ChatColor.WHITE + String.join(", ", config.getStuff().keySet()));

    }

    private void deleteConfig(Player player, UUID playerUUID, String configName) {
        boolean deleted = Main.getDatabaseManager().deleteUHCConfig(playerUUID, configName);

        if (deleted) {
            player.sendMessage(ChatColor.GREEN + "Configuration " + ChatColor.GOLD + configName + ChatColor.GREEN + " supprimée avec succès!");
        } else {
            player.sendMessage(ChatColor.RED + "Aucune configuration trouvée avec le nom " + ChatColor.GOLD + configName);
        }
    }

    private void listConfigs(Player player, UUID playerUUID) {
        List<String> configNames = Main.getDatabaseManager().getPlayerUHCConfigNames(playerUUID);

        if (configNames.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Vous n'avez aucune configuration sauvegardée.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Vos configurations UHC ===");
        for (String name : configNames) {
            player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + name);
        }
    }
}
