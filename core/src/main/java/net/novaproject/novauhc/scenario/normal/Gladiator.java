package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.GladiatorLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Gladiator extends Scenario {

    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final Map<UUID, UUID> fightPairs = new HashMap<>();
    private final Map<UUID, Location> arenaLocations = new HashMap<>();

    @Override
    public String getName() {
        return "Gladiator";
    }

    @Override
    public String getDescription() {
        return "Les combats 1v1 se déroulent dans une arène fermée qui apparaît automatiquement.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_SWORD);
    }

    @Override
    public String getPath() {
        return "gladiator";
    }

    @Override
    public ScenarioLang[] getLang() {
        return GladiatorLang.values();
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;

        if (!(entity instanceof Player victim) || !(damager instanceof Player attacker)) return;

        // Check if both players are playing
        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcAttacker = UHCPlayerManager.get().getPlayer(attacker);

        if (uhcVictim == null || uhcAttacker == null || !uhcVictim.isPlaying() || !uhcAttacker.isPlaying()) {
            return;
        }

        // Check if players are already in a fight
        if (fightPairs.containsKey(attacker.getUniqueId()) || fightPairs.containsKey(victim.getUniqueId())) {
            return;
        }

        // Start gladiator fight
        startGladiatorFight(attacker, victim);
    }

    private void startGladiatorFight(Player player1, Player player2) {
        UUID uuid1 = player1.getUniqueId();
        UUID uuid2 = player2.getUniqueId();

        // Store original locations
        originalLocations.put(uuid1, player1.getLocation().clone());
        originalLocations.put(uuid2, player2.getLocation().clone());

        // Mark players as fighting each other
        fightPairs.put(uuid1, uuid2);
        fightPairs.put(uuid2, uuid1);

        // Find arena location (high in the sky)
        Location arenaCenter = player1.getLocation().clone();
        arenaCenter.setY(200);

        // Create arena
        createArena(arenaCenter);

        // Teleport players to arena
        Location pos1 = arenaCenter.clone().add(5, 1, 0);
        Location pos2 = arenaCenter.clone().add(-5, 1, 0);

        arenaLocations.put(uuid1, pos1);
        arenaLocations.put(uuid2, pos2);

        player1.teleport(pos1);
        player2.teleport(pos2);

        // Send messages
        UHCPlayer uhcPlayer1 = UHCPlayerManager.get().getPlayer(player1);
        UHCPlayer uhcPlayer2 = UHCPlayerManager.get().getPlayer(player2);

        ScenarioLangManager.send(uhcPlayer1, GladiatorLang.COMBAT_STARTED);
        ScenarioLangManager.send(uhcPlayer2, GladiatorLang.COMBAT_STARTED);

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player1%", player1.getName());
        placeholders.put("%player2%", player2.getName());
        ScenarioLangManager.sendAll(GladiatorLang.ARENA_CREATED, placeholders);

        // Start arena cleanup timer (5 minutes max)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fightPairs.containsKey(uuid1) && fightPairs.containsKey(uuid2)) {
                    // Force end fight if still ongoing
                    endGladiatorFight(player1, player2, null);
                }
            }
        }.runTaskLater(Main.get(), 20 * 60 * 5); // 5 minutes
    }

    private void createArena(Location center) {
        World world = center.getWorld();
        int radius = 10;
        int height = 5;

        // Create floor
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    world.getBlockAt(center.clone().add(x, 0, z)).setType(Material.STONE);
                }
            }
        }

        // Create walls
        for (int y = 1; y <= height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z == radius * radius ||
                            Math.abs(x) == radius || Math.abs(z) == radius) {
                        world.getBlockAt(center.clone().add(x, y, z)).setType(Material.BEDROCK);
                    }
                }
            }
        }

        // Create ceiling
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    world.getBlockAt(center.clone().add(x, height + 1, z)).setType(Material.BEDROCK);
                }
            }
        }
    }

    public void endGladiatorFight(Player player1, Player player2, Player winner) {
        UUID uuid1 = player1.getUniqueId();
        UUID uuid2 = player2.getUniqueId();

        // Remove from fight pairs
        fightPairs.remove(uuid1);
        fightPairs.remove(uuid2);

        // Teleport survivors back to original locations
        if (player1.isOnline() && originalLocations.containsKey(uuid1)) {
            player1.teleport(originalLocations.get(uuid1));
        }
        if (player2.isOnline() && originalLocations.containsKey(uuid2)) {
            player2.teleport(originalLocations.get(uuid2));
        }

        // Clean up stored data
        originalLocations.remove(uuid1);
        originalLocations.remove(uuid2);
        arenaLocations.remove(uuid1);
        arenaLocations.remove(uuid2);

        // Announce winner
        if (winner != null) {
            Bukkit.broadcastMessage("§c[Gladiator] §f" + winner.getName() + " §fa remporté le combat d'arène !");
        } else {
            Bukkit.broadcastMessage("§c[Gladiator] §fLe combat d'arène s'est terminé.");
        }

        // Clean up arena (delayed to avoid issues)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arenaLocations.containsKey(uuid1)) {
                    Location center = arenaLocations.get(uuid1).clone().subtract(5, 1, 0);
                    cleanupArena(center);
                } else if (arenaLocations.containsKey(uuid2)) {
                    Location center = arenaLocations.get(uuid2).clone().add(5, 1, 0);
                    cleanupArena(center);
                }
            }
        }.runTaskLater(Main.get(), 60); // 3 seconds delay
    }

    private void cleanupArena(Location center) {
        World world = center.getWorld();
        int radius = 10;
        int height = 5;

        // Remove arena blocks
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= height + 1; y++) {
                    if (x * x + z * z <= radius * radius + 1) {
                        world.getBlockAt(center.clone().add(x, y, z)).setType(Material.AIR);
                    }
                }
            }
        }
    }

    // This method should be called when a player dies


    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        UUID deadUuid = uhcPlayer.getUniqueId();

        if (fightPairs.containsKey(deadUuid)) {
            UUID opponentUuid = fightPairs.get(deadUuid);
            Player opponent = Bukkit.getPlayer(opponentUuid);

            if (opponent != null) {
                endGladiatorFight(uhcPlayer.getPlayer(), opponent, opponent);
            }
        }
    }
}
