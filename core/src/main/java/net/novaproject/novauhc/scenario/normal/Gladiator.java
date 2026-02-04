package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.GladiatorLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
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

    @ScenarioVariable(
            name = "Rayon de l'arène",
            description = "Rayon de l'arène circulaire",
            type = VariableType.INTEGER
    )
    private int arenaRadius = 10;

    @ScenarioVariable(
            name = "Hauteur de l'arène",
            description = "Hauteur des murs de l'arène",
            type = VariableType.INTEGER
    )
    private int arenaHeight = 5;

    @ScenarioVariable(
            name = "Hauteur de spawn de l'arène",
            description = "Altitude à laquelle l'arène apparaît",
            type = VariableType.INTEGER
    )
    private int arenaSpawnY = 200;

    @ScenarioVariable(
            name = "Durée max du combat (ticks)",
            description = "Durée maximale d'un combat d'arène avant fin automatique",
            type = VariableType.INTEGER
    )
    private int maxFightDuration = 20 * 60 * 5;

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

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcAttacker = UHCPlayerManager.get().getPlayer(attacker);

        if (uhcVictim == null || uhcAttacker == null || !uhcVictim.isPlaying() || !uhcAttacker.isPlaying()) return;
        if (fightPairs.containsKey(attacker.getUniqueId()) || fightPairs.containsKey(victim.getUniqueId())) return;

        startGladiatorFight(attacker, victim);
    }

    private void startGladiatorFight(Player player1, Player player2) {
        UUID uuid1 = player1.getUniqueId();
        UUID uuid2 = player2.getUniqueId();

        originalLocations.put(uuid1, player1.getLocation().clone());
        originalLocations.put(uuid2, player2.getLocation().clone());

        fightPairs.put(uuid1, uuid2);
        fightPairs.put(uuid2, uuid1);

        Location arenaCenter = player1.getLocation().clone();
        arenaCenter.setY(arenaSpawnY);

        createArena(arenaCenter);

        Location pos1 = arenaCenter.clone().add(5, 1, 0);
        Location pos2 = arenaCenter.clone().add(-5, 1, 0);

        arenaLocations.put(uuid1, pos1);
        arenaLocations.put(uuid2, pos2);

        player1.teleport(pos1);
        player2.teleport(pos2);

        UHCPlayer uhcPlayer1 = UHCPlayerManager.get().getPlayer(player1);
        UHCPlayer uhcPlayer2 = UHCPlayerManager.get().getPlayer(player2);

        ScenarioLangManager.send(uhcPlayer1, GladiatorLang.COMBAT_STARTED);
        ScenarioLangManager.send(uhcPlayer2, GladiatorLang.COMBAT_STARTED);

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player1%", player1.getName());
        placeholders.put("%player2%", player2.getName());
        ScenarioLangManager.sendAll(GladiatorLang.ARENA_CREATED, placeholders);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (fightPairs.containsKey(uuid1) && fightPairs.containsKey(uuid2)) {
                    endGladiatorFight(player1, player2, null);
                }
            }
        }.runTaskLater(Main.get(), maxFightDuration);
    }

    private void createArena(Location center) {
        World world = center.getWorld();

        for (int x = -arenaRadius; x <= arenaRadius; x++) {
            for (int z = -arenaRadius; z <= arenaRadius; z++) {
                if (x * x + z * z <= arenaRadius * arenaRadius) {
                    world.getBlockAt(center.clone().add(x, 0, z)).setType(Material.STONE);
                }
            }
        }

        for (int y = 1; y <= arenaHeight; y++) {
            for (int x = -arenaRadius; x <= arenaRadius; x++) {
                for (int z = -arenaRadius; z <= arenaRadius; z++) {
                    if (x * x + z * z == arenaRadius * arenaRadius ||
                            Math.abs(x) == arenaRadius || Math.abs(z) == arenaRadius) {
                        world.getBlockAt(center.clone().add(x, y, z)).setType(Material.BEDROCK);
                    }
                }
            }
        }

        for (int x = -arenaRadius; x <= arenaRadius; x++) {
            for (int z = -arenaRadius; z <= arenaRadius; z++) {
                if (x * x + z * z <= arenaRadius * arenaRadius) {
                    world.getBlockAt(center.clone().add(x, arenaHeight + 1, z)).setType(Material.BEDROCK);
                }
            }
        }
    }

    public void endGladiatorFight(Player player1, Player player2, Player winner) {
        UUID uuid1 = player1.getUniqueId();
        UUID uuid2 = player2.getUniqueId();

        fightPairs.remove(uuid1);
        fightPairs.remove(uuid2);

        if (player1.isOnline() && originalLocations.containsKey(uuid1)) {
            player1.teleport(originalLocations.get(uuid1));
        }
        if (player2.isOnline() && originalLocations.containsKey(uuid2)) {
            player2.teleport(originalLocations.get(uuid2));
        }

        originalLocations.remove(uuid1);
        originalLocations.remove(uuid2);
        arenaLocations.remove(uuid1);
        arenaLocations.remove(uuid2);

        if (winner != null) {
            Bukkit.broadcastMessage("§c[Gladiator] §f" + winner.getName() + " §fa remporté le combat d'arène !");
        } else {
            Bukkit.broadcastMessage("§c[Gladiator] §fLe combat d'arène s'est terminé.");
        }

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
        }.runTaskLater(Main.get(), 60);
    }

    private void cleanupArena(Location center) {
        World world = center.getWorld();

        for (int x = -arenaRadius; x <= arenaRadius; x++) {
            for (int z = -arenaRadius; z <= arenaRadius; z++) {
                for (int y = 0; y <= arenaHeight + 1; y++) {
                    if (x * x + z * z <= arenaRadius * arenaRadius + 1) {
                        world.getBlockAt(center.clone().add(x, y, z)).setType(Material.AIR);
                    }
                }
            }
        }
    }

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
