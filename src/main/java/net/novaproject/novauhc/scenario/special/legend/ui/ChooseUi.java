package net.novaproject.novauhc.scenario.special.legend.ui;

import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.special.legend.Legend;
import net.novaproject.novauhc.scenario.special.legend.LegendLang;
import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.core.LegendRegistry;
import net.novaproject.novauhc.scenario.special.legend.legends.Archer;
import net.novaproject.novauhc.scenario.special.legend.legends.Corne;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseUi extends CustomInventory {

    private final Legend legend;
    private final LegendRegistry registry;

    public ChooseUi(Player player) {
        super(player);
        this.legend = Legend.get();
        this.registry = legend.getLegendRegistry();
    }

    @Override
    public void setup() {
        fillCadre(0);

        setupClassItems();

        addInfoItems();

    }

    /**
     * Configure tous les items de classe dans l'interface
     */
    private void setupClassItems() {
        List<LegendClass> legends = registry.getAllLegendsSorted();

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (int i = 0; i < legends.size() && i < slots.length; i++) {
            LegendClass legendClass = legends.get(i);
            addClassItem(slots[i], legendClass);
        }
    }


    private void addInfoItems() {
        UHCPlayer uhcPlayer = getUHCPlayer();
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();
            int availableClasses = getAvailableClassCount(team);

            ItemCreator infoItem = new ItemCreator(Material.BOOK)
                    .setName("§6Informations").setLores(Arrays.asList(
                            "§7Classes disponibles : §a" + availableClasses + "§7/§a" + registry.getCount(),
                            "§7Équipe : §b" + team.getName(),
                            "",
                            "§eCliquez sur une classe pour la choisir !"
                    ));

            addItem(new StaticItem(40, infoItem));
        }
    }

    /**
     * Ajoute un item de classe à l'interface avec gestion spéciale pour certaines classes
     */
    private void addClassItem(int slot, LegendClass legendClass) {
        // Créer l'icône avec des informations sur la disponibilité
        ItemCreator icon = createClassIcon(legendClass);

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                handleClassSelection(legendClass);
            }
        });
    }

    /**
     * Crée l'icône d'une classe avec les informations de disponibilité
     */
    private ItemCreator createClassIcon(LegendClass legendClass) {
        ItemCreator icon = new ItemCreator(legendClass.getIconMaterial())
                .setName("§6" + legendClass.getName());

        // Ajouter la description
        icon.addLore("§7" + legendClass.getDescription());
        icon.addLore("");

        // Vérifier la disponibilité
        UHCPlayer uhcPlayer = getUHCPlayer();
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();

            if (legend.hasPlayerClass(uhcPlayer)) {
                icon.addLore("§c✗ Vous avez déjà une classe");
            } else if (legend.isClassTakenInTeam(team, legendClass)) {
                icon.addLore("§c✗ Déjà prise dans votre équipe");
            } else {
                icon.addLore("§a✓ Disponible");

                // Ajouter des informations spéciales pour certaines classes
                if (legendClass instanceof Archer) {
                    icon.addLore("§e⚠ Choix d'arc requis");
                } else if (legendClass.hasPower()) {
                    icon.addLore("§b⚡ Possède un pouvoir activable");
                }
            }
        }

        icon.addLore("");
        icon.addLore("§eCliquez pour choisir cette classe !");

        return icon;
    }

    /**
     * Gère la sélection d'une classe avec logique spéciale pour certaines classes
     */
    private void handleClassSelection(LegendClass legendClass) {
        // Vérifications de base
        if (!canSelectClass(legendClass)) {
            return;
        }

        // Gestion spéciale pour l'Archer (choix d'arc)
        if (legendClass instanceof Archer) {
            handleArcherSelection((Archer) legendClass);
            return;
        }

        // Sélection normale pour les autres classes
        selectClass(legendClass);
    }

    /**
     * Gère la sélection spéciale de l'Archer avec choix d'arc
     */
    private void handleArcherSelection(Archer archerClass) {
        getPlayer().sendMessage("§6[Archer] §eChoisissez votre type d'arc :");
        getPlayer().sendMessage("§71 = §fArc Infinity + Power III");
        getPlayer().sendMessage("§72 = §fArc Power IV");

        new AnvilUi(getPlayer(), event -> {
            if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                String enteredText = event.getName();
                try {
                    int bowChoice = Integer.parseInt(enteredText);

                    if (bowChoice != 1 && bowChoice != 2) {
                        getPlayer().sendMessage(ChatColor.RED + "Choix invalide : veuillez choisir 1 ou 2");
                        return;
                    }

                    // Sélectionner la classe Archer
                    if (selectClass(archerClass)) {
                        // Donner l'arc approprié
                        archerClass.giveBow(getPlayer(), bowChoice);
                    }

                } catch (NumberFormatException ex) {
                    getPlayer().sendMessage(ChatColor.RED + "Choix invalide : veuillez entrer un nombre (1 ou 2)");
                }
            }
        }).setSlot("Choix de l'arc ( 1 ou 2 )").open();
    }

    /**
     * Vérifie si un joueur peut sélectionner une classe donnée
     */
    private boolean canSelectClass(LegendClass legendClass) {
        if (!getUHCPlayer().getTeam().isPresent()) {
            getPlayer().sendMessage(ChatColor.RED + "Vous devez être dans une équipe pour choisir une classe");
            return false;
        }

        UHCTeam team = getUHCPlayer().getTeam().get();
        UHCPlayer player = getUHCPlayer();

        // Vérifier si le joueur a déjà une classe
        if (legend.hasPlayerClass(player)) {
            getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
            return false;
        }

        // Vérifier si la classe est déjà prise dans l'équipe
        if (legend.isClassTakenInTeam(team, legendClass)) {
            getPlayer().sendMessage(ChatColor.RED + "La classe " + legendClass.getName() +
                    " a déjà été choisie dans votre équipe");
            return false;
        }

        // Vérifier si la classe peut être choisie (logique spécifique à la classe)
        if (!legendClass.canBeChosen(player)) {
            getPlayer().sendMessage(ChatColor.RED + "Cette classe ne peut pas être choisie actuellement");
            return false;
        }

        return true;
    }

    /**
     * Sélectionne une classe pour le joueur
     */
    private boolean selectClass(LegendClass legendClass) {
        if (!canSelectClass(legendClass)) {
            return false;
        }

        UHCTeam team = getUHCPlayer().getTeam().get();
        UHCPlayer player = getUHCPlayer();

        // Assigner la classe au joueur
        legend.assignPlayerClass(player, legendClass, team);

        // Messages de confirmation avec style
        getPlayer().sendMessage("");
        getPlayer().sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        getPlayer().sendMessage("§6§l                    CLASSE SÉLECTIONNÉE");
        getPlayer().sendMessage("");
        getPlayer().sendMessage("§f  Classe : §6§l" + legendClass.getName());
        getPlayer().sendMessage("§f  Description : §7" + legendClass.getDescription());

        if (legendClass.hasPower()) {
            getPlayer().sendMessage("§f  Pouvoir : §a✓ Disponible §7(utilisez §e/ld pouvoir§7)");
        } else {
            getPlayer().sendMessage("§f  Pouvoir : §c✗ Aucun pouvoir activable");
        }

        if (legendClass instanceof Corne) {
            getPlayer().sendMessage("§f  Spécial : §b4 mélodies différentes");
        }

        getPlayer().sendMessage("");
        getPlayer().sendMessage("§a§l  ✓ Classe assignée avec succès !");
        getPlayer().sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        getPlayer().sendMessage("");

        // Fermer l'interface
        getPlayer().closeInventory();

        return true;
    }

    /**
     * Compte le nombre de classes disponibles pour une équipe
     */
    private int getAvailableClassCount(UHCTeam team) {
        int available = 0;
        for (LegendClass legendClass : registry.getAllLegends()) {
            if (!legend.isClassTakenInTeam(team, legendClass)) {
                available++;
            }
        }
        return available;
    }

    @Override
    public String getTitle() {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%count%", registry.getCount());
        return ScenarioLangManager.get(LegendLang.UI_CHOOSE_TITLE, getUHCPlayer(), placeholders);
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false; // Pas de rafraîchissement automatique
    }
}
