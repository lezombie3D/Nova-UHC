package net.novaproject.novauhc.scenario.special.legend;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.scenario.special.legend.core.LegendRegistry;
import net.novaproject.novauhc.scenario.special.legend.ui.ChooseUi;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Legend extends Scenario {

    private static Legend instance;

    private final LegendRegistry legendRegistry = LegendRegistry.getInstance();

    private final Map<UHCPlayer, LegendData> playerData = new ConcurrentHashMap<>();

    private final Map<UHCTeam, Set<LegendClass>> teamUsedClasses = new ConcurrentHashMap<>();

    private boolean canChooseClass = true;
    

    public static Legend get() {
        return instance;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String getName() {
        return "UHC Legends";
    }

    @Override
    public String getDescription() {
        return "Scénario avec 18 classes de légendes aux capacités uniques";
    }

    @Override
    public String getPath() {
        return "special/legend";
    }

    @Override
    public LegendLang[] getLang() {
        return LegendLang.values();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DRAGON_EGG);
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        canChooseClass = true;

        legendRegistry.initialize();

        playerData.clear();
        teamUsedClasses.clear();

        Bukkit.getLogger().info("[Legend] Scénario UHC Legends initialisé avec " +
                legendRegistry.getCount() + " légendes disponibles");
    }

    @Override
    public void onStart(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();
            teamUsedClasses.putIfAbsent(team, ConcurrentHashMap.newKeySet());
        }

        ScenarioLangManager.send(player, LegendLang.CHOOSE_CLASS_WELCOME);

        new BukkitRunnable() {
            @Override
            public void run() {
                canChooseClass = false;
                assignRandomClassIfNeeded(player);
            }
        }.runTaskLater(Main.get(), 20 * 60 * 3); // 3 minutes
    }


    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        // Vérifier si le joueur est une marionnette
        if (isPuppet(uhcPlayer)) {
            String displayName = meta.getDisplayName();
            // Les marionnettes ne peuvent pas utiliser les pouvoirs ou mélodies
            if (displayName.contains("Pouvoir") || displayName.contains("Melodie")) {
                event.setCancelled(true);
                ScenarioLangManager.send(player, LegendLang.MARIONNETTISTE_PUPPET_RESTRICTION_POWER);
                return;
            }
        }

        LegendData data = playerData.get(uhcPlayer);
        if (data == null) return;

        String displayName = meta.getDisplayName();
        LegendClass legendClass = data.getLegendClass();

        // Gestion spéciale pour les mélodies de la Corne
        if (displayName.contains("Melodie") && legendClass instanceof net.novaproject.novauhc.scenario.special.legend.legends.Corne) {
            net.novaproject.novauhc.scenario.special.legend.legends.Corne corne =
                    (net.novaproject.novauhc.scenario.special.legend.legends.Corne) legendClass;
            corne.activateMelody(player, uhcPlayer, displayName);
        }
        // Gestion des pouvoirs standards
        else if (displayName.contains("Pouvoir")) {
            legendClass.onPower(player, uhcPlayer);
        }
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!(entity instanceof Player) || !(damager instanceof Player)) return;
        
        Player victim = (Player) entity;
        Player attacker = (Player) damager;
        
        UHCPlayer uhcAttacker = UHCPlayerManager.get().getPlayer(attacker);
        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);

        LegendData attackerData = playerData.get(uhcAttacker);
        if (attackerData != null) {
            // Déléguer à la légende de l'attaquant
            attackerData.getLegendClass().onHit(attacker, victim, uhcAttacker, uhcVictim);
        }
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        LegendData data = playerData.get(uhcPlayer);
        if (data != null) {
            // Gestion spéciale pour la Princesse (immunité aux dégâts de chute)
            if (data.getLegendClass() instanceof net.novaproject.novauhc.scenario.special.legend.legends.Princesse &&
                    event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }

            // Déléguer à la légende du joueur
            data.getLegendClass().onDamage(player, uhcPlayer, event.getDamage());
        }
    }

    @Override
    public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();

        LegendData data = playerData.get(uhcPlayer);
        if (data != null) {
            data.getLegendClass().onDeath(player, uhcPlayer, killer);

            data.cleanup();
            playerData.remove(uhcPlayer);
        }
        LegendData killerData = playerData.get(killer);
        if (killerData != null) {
            data.getLegendClass().onKill(player, uhcPlayer, killer);
        }

    }

    @Override
    public void onSec(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        LegendData data = playerData.get(uhcPlayer);

        if (data != null) {
            data.getLegendClass().onTick(player, uhcPlayer);
        }

        handlePuppetEffects(player, uhcPlayer);
    }


    private void handlePuppetEffects(Player player, UHCPlayer uhcPlayer) {
        for (LegendData masterData : playerData.values()) {
            if (masterData.getPuppets().contains(uhcPlayer)) {
                UHCPlayer master = masterData.getOwner();

                if (masterData.getLegendClass() instanceof
                        net.novaproject.novauhc.scenario.special.legend.legends.Marionnettiste) {

                    net.novaproject.novauhc.scenario.special.legend.legends.Marionnettiste marionnettiste =
                            (net.novaproject.novauhc.scenario.special.legend.legends.Marionnettiste) masterData.getLegendClass();

                    marionnettiste.applyPuppetEffects(uhcPlayer, player, master);
                }
                break;
            }
        }
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        LegendData data = playerData.get(uhcPlayer);

        if (data != null) {
            data.getLegendClass().onConsume(player, uhcPlayer, item);
        }
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        // Vérifier si le joueur est une marionnette
        if (isPuppet(uhcPlayer)) {
            // Les marionnettes ne peuvent pas drop d'items
            event.setCancelled(true);
            ScenarioLangManager.send(player, LegendLang.MARIONNETTISTE_PUPPET_RESTRICTION_DROP);
        }
    }

    @Override
    public void onPickUp(Player player, Item item, PlayerPickupItemEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        // Vérifier si le joueur est une marionnette
        if (isPuppet(uhcPlayer)) {
            // Les marionnettes ne peuvent ramasser que de la cobblestone
            if (item.getItemStack().getType() != Material.COBBLESTONE) {
                event.setCancelled(true);
                // Pas de message pour éviter le spam
            }
        }
    }

    @Override
    public void onLdCMD(Player player, String subCommand, String[] args) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        // Vérifier si le joueur est une marionnette
        if (isPuppet(uhcPlayer)) {
            ScenarioLangManager.send(player, LegendLang.MARIONNETTISTE_PUPPET_RESTRICTION_COMMAND);
            return;
        }

        switch (subCommand) {
            case "choose":
                if (canChooseClass && UHCManager.get().isGame()) {
                    new ChooseUi(player).open();
                } else {
                    player.sendMessage(ChatColor.RED + "Vous ne pouvez plus choisir de classe");
                }
                break;

            case "pouvoir":
                LegendData data = playerData.get(uhcPlayer);
                if (data != null && data.getLegendClass().hasPower()) {
                    // Donner l'item de pouvoir (sera géré par chaque légende)
                    player.sendMessage(ChatColor.GREEN + "Utilisez votre pouvoir avec l'item correspondant !");
                } else {
                    player.sendMessage(ChatColor.RED + "Votre classe n'a pas de pouvoir activable");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Utilisez :");
                player.sendMessage(ChatColor.YELLOW + "/ld choose - Choisir une classe");
                player.sendMessage(ChatColor.YELLOW + "/ld pouvoir - Informations sur votre pouvoir");
        }
    }

    // === MÉTHODES PUBLIQUES POUR L'INTERFACE ===

    /**
     * Vérifie si un joueur a déjà une classe
     */
    public boolean hasPlayerClass(UHCPlayer player) {
        return playerData.containsKey(player);
    }

    /**
     * Vérifie si une classe est déjà prise dans une équipe
     */
    public boolean isClassTakenInTeam(UHCTeam team, LegendClass legendClass) {
        Set<LegendClass> usedClasses = teamUsedClasses.get(team);
        return usedClasses != null && usedClasses.contains(legendClass);
    }

    /**
     * Assigne une classe à un joueur (utilisé par ChooseUi)
     */
    public void assignPlayerClass(UHCPlayer player, LegendClass legendClass, UHCTeam team) {
        // Créer les données du joueur
        LegendData data = new LegendData(player, legendClass);
        playerData.put(player, data);

        // Marquer la classe comme utilisée dans l'équipe
        teamUsedClasses.computeIfAbsent(team, k -> ConcurrentHashMap.newKeySet()).add(legendClass);

        // Appeler onChoose de la légende
        legendClass.onChoose(player.getPlayer(), player);
    }

    /**
     * Récupère les données d'un joueur
     */
    public LegendData getPlayerData(UHCPlayer player) {
        return playerData.get(player);
    }

    /**
     * Récupère le registry des légendes
     */
    public LegendRegistry getLegendRegistry() {
        return legendRegistry;
    }

    /**
     * Vérifie si un joueur est une marionnette
     */
    public boolean isPuppet(UHCPlayer player) {
        for (LegendData masterData : playerData.values()) {
            if (masterData.getPuppets().contains(player)) {
                return true;
            }
        }
        return false;
    }

    // === MÉTHODES PRIVÉES ===

    /**
     * Assigne une classe aléatoire à un joueur s'il n'en a pas encore une
     */
    private void assignRandomClassIfNeeded(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (!canChooseClass && uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();

            // Vérifier si le joueur a déjà une classe
            if (hasPlayerClass(uhcPlayer)) {
                player.sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                return;
            }

            // Obtenir une classe disponible aléatoirement
            List<LegendClass> availableClasses = getAvailableClasses(team);
            if (availableClasses.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Aucune classe disponible !");
                return;
            }

            LegendClass selectedClass = availableClasses.get(new Random().nextInt(availableClasses.size()));
            assignPlayerClass(uhcPlayer, selectedClass, team);

            player.sendMessage(ChatColor.YELLOW + "Classe assignée automatiquement : " +
                    ChatColor.GOLD + selectedClass.getName());
        }
    }

    /**
     * Obtient la liste des classes disponibles pour une équipe
     */
    private List<LegendClass> getAvailableClasses(UHCTeam team) {
        Set<LegendClass> usedClasses = teamUsedClasses.getOrDefault(team, Collections.emptySet());

        List<LegendClass> availableClasses = new ArrayList<>();
        for (LegendClass legendClass : legendRegistry.getAllLegends()) {
            if (!usedClasses.contains(legendClass)) {
                availableClasses.add(legendClass);
            }
        }

        return availableClasses;
    }
}
