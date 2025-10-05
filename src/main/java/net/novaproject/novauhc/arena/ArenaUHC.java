package net.novaproject.novauhc.arena;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaUHC implements Listener {

    private static ArenaUHC instance;
    private final List<ArenaZone> zones = new ArrayList<>();
    private final Map<Player, ArenaZone> players = new HashMap<>();
    public ArenaUHC() {
        instance = this;
        if (!ConfigUtils.getWorldConfig().getBoolean("arena.active")) {
            return;
        }
        System.out.println("UHC Arena enabled");
        Bukkit.getPluginManager().registerEvents(this, Main.get());

        zones.add(new ArenaZone(
                ConfigUtils.getLocation(ConfigUtils.getWorldConfig(), "arena.center"),
                ConfigUtils.getWorldConfig().getInt("arena.high"),
                ConfigUtils.getWorldConfig().getInt("arena.radius"),
                ConfigUtils.getLocation(ConfigUtils.getWorldConfig(), "arena.exit")));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (ArenaZone zone : zones) {
                    checkPlayers(zone);
                }
            }
        }.runTaskTimer(Main.get(), 1, 1);
    }

    public static ArenaUHC get() {
        return instance;
    }

    private void checkPlayers(ArenaZone zone) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() != GameMode.ADVENTURE) continue;

            if (zone.contains(player.getLocation())) {
                addPlayer(player, zone);
            } else {
                if (players.containsKey(player) && players.get(player) == zone) {
                    removePlayer(player);
                }
            }
        }
    }

    private void addPlayer(Player player, ArenaZone zone) {
        if (players.containsKey(player)) return;

        players.put(player, zone);

        CommonString.ARENA_JOIN.send(player);
        player.setFallDistance(0);

        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        inventory.setHelmet(new ItemCreator(Material.IRON_HELMET).setUnbreakable(true).getItemstack());
        inventory.setChestplate(new ItemCreator(Material.IRON_CHESTPLATE).setUnbreakable(true).getItemstack());
        inventory.setLeggings(new ItemCreator(Material.IRON_LEGGINGS).setUnbreakable(true).getItemstack());
        inventory.setBoots(new ItemCreator(Material.IRON_BOOTS).setUnbreakable(true).getItemstack());
        inventory.addItem(new ItemCreator(Material.IRON_SWORD).setUnbreakable(true).getItemstack());

        player.setFlying(false);
        player.setAllowFlight(false);
    }

    public void removePlayer(Player player) {
        if (!players.containsKey(player)) return;

        ArenaZone zone = players.get(player);

        if (player.isOnline()) {
            player.teleport(zone.getExit());

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(null);

            UHCUtils.giveLobbyItems(player);
            player.setHealth(player.getMaxHealth());
        }

        players.remove(player);
    }

    @SafeVarargs
    private final void broadcast(String message, SimpleEntry<String, Object>... variables) {
        for (Player player : players.keySet()) {
            String m = message;
            for (SimpleEntry<String, Object> variable : variables) {
                m = m.replace(variable.getKey(), String.valueOf(variable.getValue()));
            }
            player.sendMessage(m);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!players.containsKey(player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
        boolean dead = player.getHealth() - event.getFinalDamage() <= 0;

        if (dead) {
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
                if (entityEvent.getDamager() instanceof Player) {
                    Player damager = (Player) entityEvent.getDamager();
                    broadcast(CommonString.ARENA_KILL.getMessage(),
                            new SimpleEntry<>("%player_arena%", player.getName()),
                            new SimpleEntry<>("%killer_arena%", damager.getName()));

                    damager.setHealth(damager.getMaxHealth());
                }
            } else {
                broadcast(CommonString.ARENA_DEATH.getMessage(),
                        new SimpleEntry<>("%player_arena%", player.getName()));
            }

            event.setDamage(0);
            removePlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (players.containsKey(player)) {
            removePlayer(player);
        }
    }
}
