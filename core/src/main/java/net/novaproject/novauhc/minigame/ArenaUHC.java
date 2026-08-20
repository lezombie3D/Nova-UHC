package net.novaproject.novauhc.minigame;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;

import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ArenaUHC implements Listener {

    private static ArenaUHC instance;
    private final List<ArenaZone> zones = new ArrayList<>();
    private final Map<Player, ArenaZone> players = new HashMap<>();
    private final Set<UUID> ignoringTeleport = new HashSet<>();

    public ArenaUHC() {
        instance = this;
        if (!ConfigUtils.getWorldConfig().getBoolean("arena.active")) {
            return;
        }
        Bukkit.getLogger().info("UHC Arena enabled");
        Bukkit.getPluginManager().registerEvents(this, Main.get());

        zones.add(new ArenaZone(
                ConfigUtils.getLocation(ConfigUtils.getWorldConfig(), "arena.center"),
                ConfigUtils.getWorldConfig().getInt("arena.radius"),
                ConfigUtils.getWorldConfig().getInt("arena.high"),
                ConfigUtils.getLocation(ConfigUtils.getWorldConfig(), "arena.exit")));
    }

    public static void init() {
        if (instance != null) return;
        if (!ConfigUtils.getWorldConfig().getBoolean("arena.active")) return;
        new BukkitRunnable() {
            int attempts = 0;
            @Override
            public void run() {
                if (Common.get().getLobby() != null) {
                    new ArenaUHC();
                    cancel();
                    return;
                }
                if (++attempts > 200) {
                    Main.get().getLogger().warning("[Arena] monde lobby jamais chargé — arène désactivée");
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 1L, 5L);
    }

    public static ArenaUHC get() {
        return instance;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!UHCManager.get().isLobby()) return;
        Location to = event.getTo();
        if (to == null) return;
        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.ADVENTURE) return;
        for (ArenaZone zone : zones) {
            if (zone.contains(to)) {
                addPlayer(player, zone);
            } else if (players.get(player) == zone) {
                removePlayer(player);
            }
        }
    }

    private void addPlayer(Player player, ArenaZone zone) {
        if (!(UHCManager.get().getGameState() == UHCManager.GameState.LOBBY)) return;
        if (players.containsKey(player)) return;

        players.put(player, zone);

        LangManager.get().send(CoreLang.COMMON_ARENA_JOIN, player);
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
            player.teleport(zone.exit());

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(null);

            HotbarManager.get().giveHotbar(player);
            player.setHealth(player.getMaxHealth());
        }

        players.remove(player);
    }

    public void removePlayer(Player player, Location loc) {
        if (!players.containsKey(player)) return;

        UUID uuid = player.getUniqueId();
        ignoringTeleport.add(uuid);
        try {
            if (player.isOnline()) {
                player.teleport(loc);

                PlayerInventory inventory = player.getInventory();
                inventory.clear();
                inventory.setArmorContents(null);

                HotbarManager.get().giveHotbar(player);
                player.setHealth(player.getMaxHealth());
            }
        } finally {
            ignoringTeleport.remove(uuid);
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
        if (!(event.getEntity() instanceof Player player)) return;
        if (!players.containsKey(player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
        boolean dead = player.getHealth() - event.getFinalDamage() <= 0;

        if (dead) {
            if (event instanceof EntityDamageByEntityEvent entityEvent) {
                if (entityEvent.getDamager() instanceof Player damager) {
                    broadcast(LangManager.get().get(CoreLang.COMMON_ARENA_KILL),
                            new SimpleEntry<>("%player_arena%", player.getName()),
                            new SimpleEntry<>("%killer_arena%", damager.getName()));

                    damager.setHealth(damager.getMaxHealth());
                }
            } else {
                broadcast(LangManager.get().get(CoreLang.COMMON_ARENA_DEATH),
                        new SimpleEntry<>("%player_arena%", player.getName()));
            }

            event.setDamage(0);
            removePlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (ignoringTeleport.contains(player.getUniqueId())) return;

        if (!players.containsKey(player)) return;

        ArenaZone zone = players.get(player);
        Location to = event.getTo();
        if (to != null && !zone.contains(to)) {
            removePlayer(player, to);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ignoringTeleport.remove(player.getUniqueId());
        if (players.containsKey(player)) {
            removePlayer(player);
        }
    }

    public record ArenaZone(Location base, double radius, double height, Location exit) {

        public boolean contains(Location loc) {
            if (loc.getY() < base.getY() || loc.getY() > base.getY() + height) return false;
            double dx = loc.getX() - base.getX();
            double dz = loc.getZ() - base.getZ();
            return (dx * dx + dz * dz) <= (radius * radius);
        }
    }

    public static class ArenaCommand extends Command {

        @Override
        public void execute(CommandArguments commandArguments) {

            if (commandArguments.getSender() instanceof Player player) {
                ArenaUHC arenaUHC = ArenaUHC.get();

                if (arenaUHC == null) {
                    return;
                }

                arenaUHC.removePlayer(player);

            }

        }
    }
}
