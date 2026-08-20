package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class Gladiator extends Scenario {

    @Override
    public Family getFamily() { return Family.COMBAT; }

    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final Map<UUID, UUID> fightPairs = new HashMap<>();
    private final Map<UUID, Location> arenaLocations = new HashMap<>();
    private final Set<BukkitRunnable> tasks = new HashSet<>();

    @Var(lang = ScenarioVarLang.class, nameKey = "GLADIATOR_VAR_ARENA_RADIUS_NAME", descKey = "GLADIATOR_VAR_ARENA_RADIUS_DESC", type = VariableType.INTEGER)
    private int arenaRadius = 10;

    @Var(lang = ScenarioVarLang.class, nameKey = "GLADIATOR_VAR_ARENA_HEIGHT_NAME", descKey = "GLADIATOR_VAR_ARENA_HEIGHT_DESC", type = VariableType.INTEGER)
    private int arenaHeight = 5;

    @Var(lang = ScenarioVarLang.class, nameKey = "GLADIATOR_VAR_ARENA_SPAWN_Y_NAME", descKey = "GLADIATOR_VAR_ARENA_SPAWN_Y_DESC", type = VariableType.INTEGER)
    private int arenaSpawnY = 200;

    @Var(lang = ScenarioVarLang.class, nameKey = "GLADIATOR_VAR_MAX_FIGHT_DURATION_NAME", descKey = "GLADIATOR_VAR_MAX_FIGHT_DURATION_DESC", type = VariableType.INTEGER)
    private int maxFightDuration = 20 * 60 * 5;

    @Override
    public String getName() {
        return "Gladiator";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.GLADIATOR, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_SWORD);
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

        LangManager.get().send(ScenarioLang.GLADIATOR_COMBAT_STARTED, player1);
        LangManager.get().send(ScenarioLang.GLADIATOR_COMBAT_STARTED, player2);

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player1%", player1.getName());
        placeholders.put("%player2%", player2.getName());
        LangManager.get().sendAll(ScenarioLang.GLADIATOR_ARENA_CREATED, placeholders);

        BukkitRunnable durationTask = new BukkitRunnable() {
            @Override
            public void run() {
                tasks.remove(this);
                if (!isActive()) { cancel(); return; }
                if (fightPairs.containsKey(uuid1) && fightPairs.containsKey(uuid2)) {
                    endGladiatorFight(player1, player2, null);
                }
            }
        };
        tasks.add(durationTask);
        durationTask.runTaskLater(Main.get(), maxFightDuration);
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

        if (winner != null) {
            LangManager.get().sendAll(ScenarioLang.GLADIATOR_ARENA_WINNER, Map.of("%player%", winner.getName()));
        } else {
            LangManager.get().sendAll(ScenarioLang.GLADIATOR_ARENA_ENDED);
        }

        BukkitRunnable cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                tasks.remove(this);
                if (arenaLocations.containsKey(uuid1)) {
                    Location center = arenaLocations.get(uuid1).clone().subtract(5, 1, 0);
                    arenaLocations.remove(uuid1);
                    arenaLocations.remove(uuid2);
                    cleanupArena(center);
                } else if (arenaLocations.containsKey(uuid2)) {
                    Location center = arenaLocations.get(uuid2).clone().add(5, 1, 0);
                    arenaLocations.remove(uuid1);
                    arenaLocations.remove(uuid2);
                    cleanupArena(center);
                }
            }
        };
        tasks.add(cleanupTask);
        cleanupTask.runTaskLater(Main.get(), 60);
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

    @Override
    public void onStop() {
        for (BukkitRunnable task : tasks) task.cancel();
        tasks.clear();
        originalLocations.clear();
        fightPairs.clear();
        arenaLocations.clear();
    }
}

