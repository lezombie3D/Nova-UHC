package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.ui.config.DropItemRate;
import java.util.logging.Level;
import com.google.gson.JsonObject;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.api.UHCGameConfiguration;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.Enchants;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

import static net.novaproject.novauhc.utils.chat.TextUtils.getFormattedTime;
import org.bukkit.Bukkit;

public class ConfigCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {
        UUID playerUUID = player.getUniqueId();
        String[] arguments = args.getArguments();

        if (arguments.length == 0) {
            showHelp(player);
            return;
        }

        switch (arguments[0].toLowerCase()) {
            case "save":
                if (arguments.length < 2) {
                    LangManager.get().send(CoreLang.CMD_PRECONFIG_SAVE_USAGE, player);
                    return;
                }
                saveConfig(player, playerUUID, arguments[1]);
                break;
            case "load":
                if (arguments.length < 2) {
                    LangManager.get().send(CoreLang.CMD_PRECONFIG_LOAD_USAGE, player);
                    return;
                }
                loadConfig(player, playerUUID, arguments[1]);
                break;
            case "delete":
                if (arguments.length < 2) {
                    LangManager.get().send(CoreLang.CMD_PRECONFIG_DELETE_USAGE, player);
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
        LangManager.get().send(CoreLang.CMD_PRECONFIG_HELP_HEADER, player);
        LangManager.get().send(CoreLang.CMD_PRECONFIG_HELP_SAVE, player);
        LangManager.get().send(CoreLang.CMD_PRECONFIG_HELP_LOAD, player);
        LangManager.get().send(CoreLang.CMD_PRECONFIG_HELP_DELETE, player);
        LangManager.get().send(CoreLang.CMD_PRECONFIG_HELP_LIST, player);
    }

    private void saveConfig(Player player, UUID playerUUID, String configName) {
        List<String> scenarios = ScenarioManager.get().getActiveScenarios()
                .stream()
                .map(Scenario::getName)
                .collect(Collectors.toList());
        List<Integer> limite = new ArrayList<>();
        for (Enchants ench : Enchants.values()) {
            limite.add(ench.getConfigValue());
        }
        Map<String, org.bson.Document> scenarioConfigs = new HashMap<>();
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            org.bson.Document doc = scenario.scenarioToDoc();
            if (doc != null && !doc.isEmpty()) {
                scenarioConfigs.put(scenario.getName(), doc);
            }
        }

        Map<String, Integer> dropRates = new HashMap<>();
        for (DropItemRate rate : DropItemRate.values()) {
            dropRates.put(rate.name(), rate.getAmount());
        }

        UHCGameConfiguration config = new UHCGameConfiguration(
                configName,
                scenarios,
                scenarioConfigs,
                UHCManager.get().settings().snapshot(),
                (int) Common.get().getArena().getWorldBorder().getSize(),
                limite,
                UHCManager.get().start,
                UHCManager.get().death,
                Main.getConfigManager().getCurrentPotionStates(),
                dropRates
        );

        LangManager.get().send(CoreLang.CMD_PRECONFIG_SAVING, player, Map.of("%name%", configName));

        Main.getConfigManager().saveConfig(playerUUID, config).thenAccept(saved ->
                Bukkit.getScheduler().runTask(Main.get(), () -> {
                    if (!player.isOnline()) return;
                    LangManager.get().send(
                            saved ? CoreLang.CMD_PRECONFIG_SAVED : CoreLang.CMD_PRECONFIG_SAVE_ERROR,
                            player, Map.of("%name%", configName));
                }));
    }

    private void loadConfig(Player player, UUID playerUUID, String configName) {
        LangManager.get().send(CoreLang.CMD_PRECONFIG_LOADING, player, Map.of("%name%", configName));

        Main.getConfigManager().getConfig(playerUUID, configName)
                .thenAccept(config -> {

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (config == null) {
                                LangManager.get().send(CoreLang.CMD_PRECONFIG_NOT_FOUND, player, Map.of("%name%", configName));
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
                            LangManager.get().send(CoreLang.CMD_PRECONFIG_LOAD_ERROR, player, Map.of("%error%", String.valueOf(ex.getMessage())));
                            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", ex);
                        }
                    }.runTask(Main.get());
                    return null;
                });
    }

    private void applyConfig(Player player, UHCGameConfiguration config) {
        UHCManager uhc = UHCManager.get();

        uhc.settings().restore(config.settings());
        uhc.setTeam_size(uhc.getTeam_size());
        uhc.setDiamondLimit(uhc.getDiamondLimit());
        uhc.setGlobalForcePercent(uhc.getGlobalForcePercent());
        uhc.setGlobalResistancePercent(uhc.getGlobalResistancePercent());
        uhc.setGlobalForceCriticPercent(uhc.getGlobalForceCriticPercent());
        uhc.setGlobalSpeedPercent(uhc.getGlobalSpeedPercent());
        Common.get().getArena().getWorldBorder().setSize(config.borderSize());
        uhc.applyLimitsFromList(config.limite());
        config.dropRates().forEach((name, amount) -> {
            try {
                DropItemRate.valueOf(name).setAmount(amount);
            } catch (IllegalArgumentException ignored) {
            }
        });

        config.scenarioConfigs().forEach((name, doc) -> {
            ScenarioManager.get().getScenarioByName(name).ifPresent(scenario -> scenario.docToScenario(doc));
        });

        config.enabledScenarios().forEach(name -> {
            ScenarioManager.get().getScenarioByName(name).ifPresent(scenario -> {
                if (!scenario.isActive()) scenario.enable();
            });
        });

        uhc.start = config.stuff();
        uhc.death = config.death();

        if (config.potionStates() != null && !config.potionStates().isEmpty()) {
            Main.getConfigManager().applyPotionStatesToEnum(config.potionStates());
        }

        LangManager.get().send(CoreLang.CMD_PRECONFIG_LOADED_HEADER, player, Map.of("%name%", config.name()));

        LangManager.get().send(CoreLang.CMD_PRECONFIG_SCENARIOS, player, Map.of("%value%", config.enabledScenarios().isEmpty()
                ? LangManager.get().get(CoreLang.CMD_PRECONFIG_NONE)
                : String.join(", ", config.enabledScenarios())));

        LangManager.get().send(CoreLang.CMD_PRECONFIG_TEAMSIZE, player, Map.of("%value%", uhc.getTeam_size()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_BORDER_START, player, Map.of("%value%", config.borderSize()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_BORDER_END, player, Map.of("%value%", (int) uhc.getTargetSize()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_PVP, player, Map.of("%value%", getFormattedTime(uhc.getPvpTimer())));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_BORDER_TIMER, player, Map.of("%value%", getFormattedTime(uhc.getBorderTimer())));

        LangManager.get().send(CoreLang.CMD_PRECONFIG_PROTECTION, player, Map.of("%value%", uhc.getProtectionMax()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_DIAMOND_LIMIT, player, Map.of("%value%", uhc.getDiamondLimit()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_DIAMOND_ARMOR, player, Map.of("%value%", uhc.getDiamondArmor()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_SLOTS, player, Map.of("%value%", uhc.getSlot()));
        LangManager.get().send(CoreLang.CMD_PRECONFIG_SETTINGS, player, Map.of("%value%", config.settings().size()));

        LangManager.get().send(CoreLang.CMD_PRECONFIG_STUFF, player, Map.of("%value%", config.stuff().isEmpty()
                ? LangManager.get().get(CoreLang.CMD_PRECONFIG_NONE)
                : String.join(", ", config.stuff().keySet())));

        

        try {
            JsonObject liveConfig = Main.get().buildLiveConfig();
            if (liveConfig != null) {
                ApiManager.get().pushLiveConfig(liveConfig);
            }
        } catch (Exception e) {
            Main.get().getLogger().warning("Failed to push live config: " + e.getMessage());
        }
    }

    private void deleteConfig(Player player, UUID playerUUID, String configName) {
        LangManager.get().send(CoreLang.CMD_PRECONFIG_DELETING, player, Map.of("%name%", configName));

        Main.getConfigManager().deleteConfig(playerUUID, configName)
                .thenAccept(deleted -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (deleted) {
                                LangManager.get().send(CoreLang.CMD_PRECONFIG_DELETED, player, Map.of("%name%", configName));
                            } else {
                                LangManager.get().send(CoreLang.CMD_PRECONFIG_NOT_FOUND, player, Map.of("%name%", configName));
                            }
                        }
                    }.runTask(Main.get());
                })
                .exceptionally(ex -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            LangManager.get().send(CoreLang.CMD_PRECONFIG_DELETE_ERROR, player, Map.of("%error%", String.valueOf(ex.getMessage())));
                            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", ex);
                        }
                    }.runTask(Main.get());
                    return null;
                });
    }

    private void listConfigs(Player player, UUID playerUUID) {
        LangManager.get().send(CoreLang.CMD_PRECONFIG_FETCHING, player);

        Main.getConfigManager().getPlayerConfigNames(playerUUID)
                .thenAccept(configNames -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (configNames == null || configNames.isEmpty()) {
                                LangManager.get().send(CoreLang.CMD_PRECONFIG_LIST_EMPTY, player);
                                return;
                            }

                            LangManager.get().send(CoreLang.CMD_PRECONFIG_LIST_HEADER, player, Map.of("%count%", configNames.size()));
                            for (String name : configNames) {
                                LangManager.get().send(CoreLang.CMD_PRECONFIG_LIST_LINE, player, Map.of("%name%", name));
                            }
                        }
                    }.runTask(Main.get());
                })
                .exceptionally(ex -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            LangManager.get().send(CoreLang.CMD_PRECONFIG_LIST_ERROR, player, Map.of("%error%", String.valueOf(ex.getMessage())));
                            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", ex);
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