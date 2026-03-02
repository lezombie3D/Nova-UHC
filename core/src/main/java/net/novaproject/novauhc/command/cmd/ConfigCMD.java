package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.database.UHCGameConfiguration;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.Enchants;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

import static net.novaproject.novauhc.utils.UHCUtils.getFormattedTime;

public class ConfigCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) {
            args.getSender().sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
            return;
        }
        UUID playerUUID = player.getUniqueId();
        String[] arguments = args.getArguments();

        if (arguments.length == 0) {
            showHelp(player);
            return;
        }

        switch (arguments[0].toLowerCase()) {
            case "save":
                if (arguments.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /config save <nom>");
                    return;
                }
                saveConfig(player, playerUUID, arguments[1]);
                break;
            case "load":
                if (arguments.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /config load <nom>");
                    return;
                }
                loadConfig(player, playerUUID, arguments[1]);
                break;
            case "delete":
                if (arguments.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /config delete <nom>");
                    return;
                }
                deleteConfig(player, playerUUID, arguments[1]);
                break;
            case "list":
                listConfigs(player, playerUUID);
                break;
            default:
                showHelp(player);
                break;
        }
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
        for (Enchants ench : Enchants.values()) {
            limite.add(ench.getConfigValue());
        }
        int slot = UHCManager.get().getSlot();
        int diamant = UHCManager.get().getDimamondLimit();
        int limiteD = UHCManager.get().getDiamondArmor();
        int protection = UHCManager.get().getProtectionMax();
        Map<String, ItemStack[]> stuff = UHCManager.get().start;
        Map<String, ItemStack[]> death = UHCManager.get().death;
        Map<String, org.bson.Document> scenarioConfigs = new HashMap<>();
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            org.bson.Document doc = scenario.scenarioToDoc();
            if (doc != null && !doc.isEmpty()) {
                scenarioConfigs.put(scenario.getName(), doc);
            }
        }

        UHCGameConfiguration config = new UHCGameConfiguration(
                configName,
                scenarios,
                scenarioConfigs,
                teamSize,
                borderSize,
                pvpTime,
                finalsize,
                bordecactivation,
                timereduc,
                limite,
                slot,
                diamant,
                limiteD,
                protection,
                stuff,
                death,
                Main.getDatabaseManager().getConfigManager().getCurrentPotionStates()
        );

        
        Main.getDatabaseManager().saveUHCConfig(playerUUID, config);
        player.sendMessage(ChatColor.GREEN + "⏳ Sauvegarde de la configuration " + ChatColor.GOLD + configName + ChatColor.GREEN + " en cours...");

        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(ChatColor.GREEN + "✓ Configuration " + ChatColor.GOLD + configName + ChatColor.GREEN + " sauvegardée!");
            }
        }.runTaskLater(Main.get(), 20L); 
    }

    private void loadConfig(Player player, UUID playerUUID, String configName) {
        player.sendMessage(ChatColor.YELLOW + "⏳ Chargement de la configuration " + ChatColor.GOLD + configName + ChatColor.YELLOW + "...");

        
        Main.getDatabaseManager().getUHCConfig(playerUUID, configName)
                .thenAccept(config -> {
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (config == null) {
                                player.sendMessage(ChatColor.RED + "✗ Aucune configuration trouvée avec le nom " + ChatColor.GOLD + configName);
                                return;
                            }

                            applyConfig(player, config);
                        }
                    }.runTask(Main.get());
                })
                .exceptionally(ex -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.RED + "✗ Erreur lors du chargement: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }.runTask(Main.get());
                    return null;
                });
    }

    
    private void applyConfig(Player player, UHCGameConfiguration config) {
        UHCManager uhc = UHCManager.get();

        uhc.setTeam_size(config.getTeamSize());
        Common.get().getArena().getWorldBorder().setSize(config.getBorderSize());
        uhc.setTargetSize(config.getFinalsize());
        uhc.setTimerpvp(config.getPvpTime());
        uhc.setReducSpeed(config.getTimereduc());

        
        config.getScenarioConfigs().forEach((name, doc) -> {
            ScenarioManager.get().getScenarioByName(name).ifPresent(scenario -> scenario.docToScenario(doc));
        });

        
        config.getEnabledScenarios().forEach(name -> {
            ScenarioManager.get().getScenarioByName(name).ifPresent(scenario -> {
                if (!scenario.isActive()) scenario.enable();
            });
        });

        uhc.start = config.getStuff();
        uhc.setSlot(config.getSlot());
        uhc.setDimamondLimit(config.getDiamant());
        uhc.setDiamondArmor(config.getLimiteD());
        uhc.applyLimitsFromList(config.getLimite());
        uhc.setProtectionMax(config.getProtection());
        uhc.death = config.getDeath();

        if (config.getPotionStates() != null && !config.getPotionStates().isEmpty()) {
            Main.getDatabaseManager().getConfigManager().applyPotionStatesToEnum(config.getPotionStates());
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "╔══════════════════════════════╗");
        player.sendMessage(ChatColor.GREEN + "   ✓ Configuration chargée : "
                + ChatColor.GOLD + config.getName());
        player.sendMessage(ChatColor.DARK_GREEN + "╠══════════════════════════════╣");

        player.sendMessage(ChatColor.YELLOW + "Scénarios activés: "
                + ChatColor.WHITE + (config.getEnabledScenarios().isEmpty() ? "Aucun"
                : String.join(", ", config.getEnabledScenarios())));

        player.sendMessage(ChatColor.YELLOW + "Taille des équipes: " + ChatColor.WHITE + config.getTeamSize());
        player.sendMessage(ChatColor.YELLOW + "Bordure initiale: " + ChatColor.WHITE + config.getBorderSize());
        player.sendMessage(ChatColor.YELLOW + "Bordure finale: " + ChatColor.WHITE + config.getFinalsize());
        player.sendMessage(ChatColor.YELLOW + "PvP: " + ChatColor.WHITE + getFormattedTime(config.getPvpTime()));
        player.sendMessage(ChatColor.YELLOW + "Activation bordure: " + ChatColor.WHITE + config.getBordecactivation() + " min");

        player.sendMessage(ChatColor.YELLOW + "Protection max: " + ChatColor.WHITE + config.getProtection());
        player.sendMessage(ChatColor.YELLOW + "Diamants max: " + ChatColor.WHITE + config.getDiamant());
        player.sendMessage(ChatColor.YELLOW + "Limite de diamants: " + ChatColor.WHITE + config.getLimiteD());
        player.sendMessage(ChatColor.YELLOW + "Slots max: " + ChatColor.WHITE + config.getSlot());

        player.sendMessage(ChatColor.YELLOW + "Types de stuff: "
                + ChatColor.WHITE + (config.getStuff().isEmpty() ? "Aucun"
                : String.join(", ", config.getStuff().keySet())));

        player.sendMessage(ChatColor.DARK_GREEN + "╚══════════════════════════════╝");
        player.sendMessage("");
    }

    private void deleteConfig(Player player, UUID playerUUID, String configName) {
        player.sendMessage(ChatColor.YELLOW + "⏳ Suppression de la configuration " + ChatColor.GOLD + configName + ChatColor.YELLOW + "...");

        
        Main.getDatabaseManager().deleteUHCConfig(playerUUID, configName)
                .thenAccept(deleted -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (deleted) {
                                player.sendMessage(ChatColor.GREEN + "✓ Configuration " + ChatColor.GOLD + configName + ChatColor.GREEN + " supprimée avec succès!");
                            } else {
                                player.sendMessage(ChatColor.RED + "✗ Aucune configuration trouvée avec le nom " + ChatColor.GOLD + configName);
                            }
                        }
                    }.runTask(Main.get());
                })
                .exceptionally(ex -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.RED + "✗ Erreur lors de la suppression: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }.runTask(Main.get());
                    return null;
                });
    }

    private void listConfigs(Player player, UUID playerUUID) {
        player.sendMessage(ChatColor.YELLOW + "⏳ Récupération de vos configurations...");

        
        Main.getDatabaseManager().getPlayerUHCConfigNames(playerUUID)
                .thenAccept(configNames -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (configNames == null || configNames.isEmpty()) {
                                player.sendMessage(ChatColor.YELLOW + "Vous n'avez aucune configuration sauvegardée.");
                                return;
                            }

                            player.sendMessage(ChatColor.GOLD + "=== Vos configurations UHC (" + configNames.size() + ") ===");
                            for (String name : configNames) {
                                player.sendMessage(ChatColor.YELLOW + " • " + ChatColor.WHITE + name);
                            }
                        }
                    }.runTask(Main.get());
                })
                .exceptionally(ex -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.RED + "✗ Erreur lors de la récupération: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }.runTask(Main.get());
                    return null;
                });
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.getArguments().length == 1) {
            return Arrays.asList("save", "load", "delete", "list");
        }
        return Collections.emptyList();
    }
}