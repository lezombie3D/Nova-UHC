package net.novaproject.ultimate.legend;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.core.LegendData;
import net.novaproject.ultimate.legend.core.LegendRegistry;
import net.novaproject.ultimate.legend.legends.Corne;
import net.novaproject.ultimate.legend.legends.Marionnettiste;
import net.novaproject.ultimate.legend.legends.Princesse;
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

@Getter
@Setter
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
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
        }

    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            UHCManager.get().setTeam_size(2);
        } else {
            UHCTeamManager.get().deleteTeams();
        }
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
        CommandManager.get().register("legend", new LdCMD(), "ld");
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
        }.runTaskLater(Main.get(), 20 * 60 * 3);
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
        if (displayName.contains("Melodie") && legendClass instanceof Corne corne) {
            corne.activateMelody(player, uhcPlayer, displayName);
        }
        // Gestion des pouvoirs standards
        else if (displayName.contains("Pouvoir")) {
            legendClass.onPower(player, uhcPlayer);
        }
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!(entity instanceof Player victim) || !(damager instanceof Player attacker)) return;

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
        if (!(entity instanceof Player player)) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        LegendData data = playerData.get(uhcPlayer);
        if (data != null) {
            // Gestion spéciale pour la Princesse (immunité aux dégâts de chute)
            if (data.getLegendClass() instanceof Princesse &&
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

                if (masterData.getLegendClass() instanceof Marionnettiste marionnettiste) {

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


    public boolean hasPlayerClass(UHCPlayer player) {
        return playerData.containsKey(player);
    }

    public boolean isClassTakenInTeam(UHCTeam team, LegendClass legendClass) {
        Set<LegendClass> usedClasses = teamUsedClasses.get(team);
        return usedClasses != null && usedClasses.contains(legendClass);
    }


    public void assignPlayerClass(UHCPlayer player, LegendClass legendClass, UHCTeam team) {
        LegendData data = new LegendData(player, legendClass);
        playerData.put(player, data);

        teamUsedClasses.computeIfAbsent(team, k -> ConcurrentHashMap.newKeySet()).add(legendClass);

        legendClass.onChoose(player.getPlayer(), player);
    }


    public LegendData getPlayerData(UHCPlayer player) {
        return playerData.get(player);
    }


    public LegendRegistry getLegendRegistry() {
        return legendRegistry;
    }


    public boolean isPuppet(UHCPlayer player) {
        for (LegendData masterData : playerData.values()) {
            if (masterData.getPuppets().contains(player)) {
                return true;
            }
        }
        return false;
    }

    private void assignRandomClassIfNeeded(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (!canChooseClass && uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();

            if (hasPlayerClass(uhcPlayer)) {
                player.sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                return;
            }

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
