package net.novaproject.novauhc.scenario.role.deathnote;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Interface de configuration pour le scénario Death Note UHC
 * Permet de configurer tous les paramètres du mode de jeu
 *
 * @author NovaProject
 * @version 1.0
 */
public class DeathNoteConfigUi extends CustomInventory {

    private final DeathNote scenario;

    public DeathNoteConfigUi(Player player) {
        super(player);
        this.scenario = ScenarioManager.get().getScenario(DeathNote.class);
    }

    @Override
    public void setup() {
        // Remplir les bordures
        fillCadre(14); // Couleur rouge pour Death Note

        // Titre informatif
        addItem(new StaticItem(4, new ItemCreator(Material.WRITTEN_BOOK)
                .setName("§c§lConfiguration Death Note UHC")
                .addLore("§7Configurez tous les paramètres du mode")
                .addLore("§7Clic gauche : Augmenter")
                .addLore("§7Clic droit : Diminuer")));

        // Configuration du Death Note
        setupDeathNoteConfig();

        // Configuration des épisodes
        setupEpisodeConfig();

        // Configuration des rôles
        setupRoleConfig();

        // Configuration avancée
        setupAdvancedConfig();

        // Bouton de retour
        addReturn(49, new ScenariosUi(getPlayer(), true));
    }

    /**
     * Configure les paramètres du Death Note
     */
    private void setupDeathNoteConfig() {
        // Timer de mort
        int deathTimer = scenario.getConfig().getInt("death_note.damage_timer", 40);
        addItem(new ActionItem(10, new ItemCreator(Material.WATCH)
                .setName("§c§lTimer de Mort")
                .addLore("§7Temps avant la mort par Death Note")
                .addLore("§7Valeur actuelle : §e" + deathTimer + " secondes")
                .addLore("")
                .addLore("§7Clic gauche : +5 secondes")
                .addLore("§7Clic droit : -5 secondes")
                .addLore("§7Range : 10-120 secondes")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("death_note.damage_timer", 40);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(120, current + 5);
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(10, current - 5);
                } else {
                    return;
                }

                scenario.getConfig().set("death_note.damage_timer", newValue);
                openAll();
            }
        });

        int cooldown = scenario.getConfig().getInt("death_note.cooldown", 600);
        addItem(new ActionItem(11, new ItemCreator(Material.WATCH)
                .setName("§6§lCooldown Death Note")
                .addLore("§7Temps d'attente entre utilisations")
                .addLore("§7Valeur actuelle : §e" + (cooldown / 60) + " minutes")
                .addLore("")
                .addLore("§7Clic gauche : +1 minute")
                .addLore("§7Clic droit : -1 minute")
                .addLore("§7Range : 1-30 minutes")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("death_note.cooldown", 600);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(1800, current + 60); // Max 30 minutes
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(60, current - 60); // Min 1 minute
                } else {
                    return;
                }

                scenario.getConfig().set("death_note.cooldown", newValue);
                openAll();
            }
        });
        int episode2Damage = scenario.getConfig().getInt("death_note.episode_2_damage", 5);
        addItem(new ActionItem(12, new ItemCreator(Material.DIAMOND_SWORD)
                .setName("§4§lDégâts Épisode 2")
                .addLore("§7Dégâts infligés à l'épisode 2")
                .addLore("§7Valeur actuelle : §c" + episode2Damage + " cœurs")
                .addLore("")
                .addLore("§7Clic gauche : +1 cœur")
                .addLore("§7Clic droit : -1 cœur")
                .addLore("§7Range : 1-10 cœurs")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("death_note.episode_2_damage", 5);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(10, current + 1);
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(1, current - 1);
                } else {
                    return;
                }

                scenario.getConfig().set("death_note.episode_2_damage", newValue);
                openAll();
            }
        });

    }

    private void setupEpisodeConfig() {
        // Durée des épisodes
        int episodeDuration = scenario.getConfig().getInt("episodes.duration", 1200);
        addItem(new ActionItem(19, new ItemCreator(Material.BEACON)
                .setName("§3§lDurée des Épisodes")
                .addLore("§7Durée de chaque épisode")
                .addLore("§7Valeur actuelle : §e" + (episodeDuration / 60) + " minutes")
                .addLore("")
                .addLore("§7Clic gauche : +5 minutes")
                .addLore("§7Clic droit : -5 minutes")
                .addLore("§7Range : 10-60 minutes")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("episodes.duration", 1200);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(3600, current + 300); // Max 60 minutes
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(600, current - 300); // Min 10 minutes
                } else {
                    return;
                }

                scenario.getConfig().set("episodes.duration", newValue);
                openAll();
            }
        });

        // Régénération de vie
        int heartRegen = scenario.getConfig().getInt("episodes.heart_regen", 1);
        addItem(new ActionItem(20, new ItemCreator(Material.GOLDEN_APPLE)
                .setName("§a§lRégénération de Vie")
                .addLore("§7Cœurs régénérés par épisode")
                .addLore("§7Valeur actuelle : §c" + heartRegen + " cœur(s)")
                .addLore("")
                .addLore("§7Clic gauche : +1 cœur")
                .addLore("§7Clic droit : -1 cœur")
                .addLore("§7Range : 0-5 cœurs")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("episodes.heart_regen", 1);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(5, current + 1);
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(0, current - 1);
                } else {
                    return;
                }

                scenario.getConfig().set("episodes.heart_regen", newValue);
                openAll();
            }
        });
    }

    /**
     * Configure les paramètres des rôles
     */
    private void setupRoleConfig() {
        // Nombre de Kira
        int kiraCount = scenario.getConfig().getInt("roles.kira_count", 2);
        addItem(new ActionItem(28, new ItemCreator(Material.BOOK)
                .setName("§c§lNombre de Kira")
                .addLore("§7Nombre de Kira par partie")
                .addLore("§7Valeur actuelle : §e" + kiraCount)
                .addLore("")
                .addLore("§7Clic gauche : +1")
                .addLore("§7Clic droit : -1")
                .addLore("§7Range : 1-5")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("roles.kira_count", 2);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(5, current + 1);
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(1, current - 1);
                } else {
                    return;
                }

                scenario.getConfig().set("roles.kira_count", newValue);
                openAll();
            }
        });

        // Nombre d'Enquêteurs
        int enqueteurCount = scenario.getConfig().getInt("roles.enqueteur_count", 1);
        addItem(new ActionItem(29, new ItemCreator(Material.COMPASS)
                .setName("§a§lNombre d'Enquêteurs")
                .addLore("§7Nombre d'Enquêteurs par partie")
                .addLore("§7Valeur actuelle : §e" + enqueteurCount)
                .addLore("")
                .addLore("§7Clic gauche : +1")
                .addLore("§7Clic droit : -1")
                .addLore("§7Range : 0-3")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = scenario.getConfig().getInt("roles.enqueteur_count", 1);
                int newValue;

                if (e.getClick() == ClickType.LEFT) {
                    newValue = Math.min(3, current + 1);
                } else if (e.getClick() == ClickType.RIGHT) {
                    newValue = Math.max(0, current - 1);
                } else {
                    return;
                }

                scenario.getConfig().set("roles.enqueteur_count", newValue);
                openAll();
            }
        });
    }

    /**
     * Configure les paramètres avancés
     */
    private void setupAdvancedConfig() {
        // Attribution automatique des rôles
        boolean autoScaling = scenario.getConfig().getBoolean("roles.auto_scaling.enabled", true);
        addItem(new ActionItem(37, new ItemCreator(autoScaling ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .setName("§e§lAttribution Automatique")
                .addLore("§7Ajuste automatiquement les rôles")
                .addLore("§7selon le nombre de joueurs")
                .addLore("§7État : " + (autoScaling ? "§aActivé" : "§cDésactivé"))
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_TOGGLE.getMessage())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                boolean current = scenario.getConfig().getBoolean("roles.auto_scaling.enabled", true);
                scenario.getConfig().set("roles.auto_scaling.enabled", !current);
                openAll();
            }
        });

        // Chat Kira
        boolean kiraChat = scenario.getConfig().getBoolean("kira_chat.enabled", true);
        addItem(new ActionItem(38, new ItemCreator(kiraChat ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .setName("§c§lChat Kira")
                .addLore("§7Active le chat privé entre Kira")
                .addLore("§7Commande : /k <message>")
                .addLore("§7État : " + (kiraChat ? "§aActivé" : "§cDésactivé"))
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_TOGGLE.getMessage())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                boolean current = scenario.getConfig().getBoolean("kira_chat.enabled", true);
                scenario.getConfig().set("kira_chat.enabled", !current);
                openAll();
            }
        });

        // Mode debug
        boolean debugMode = scenario.getConfig().getBoolean("advanced.debug_mode", false);
        addItem(new ActionItem(39, new ItemCreator(debugMode ? Material.REDSTONE_TORCH_ON : Material.REDSTONE_TORCH_OFF)
                .setName("§7§lMode Debug")
                .addLore("§7Active les messages de debug")
                .addLore("§7pour les développeurs")
                .addLore("§7État : " + (debugMode ? "§aActivé" : "§cDésactivé"))
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_TOGGLE.getMessage())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                boolean current = scenario.getConfig().getBoolean("advanced.debug_mode", false);
                scenario.getConfig().set("advanced.debug_mode", !current);
                openAll();
            }
        });
    }

    @Override
    public String getTitle() {
        return "§c§lConfiguration Death Note UHC";
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
