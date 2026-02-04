package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Blizzard extends Scenario {

    private final Random random = new Random();
    private final Map<UUID, Integer> playerWarmth = new HashMap<>();
    private BukkitRunnable blizzardTask;
    private boolean isBlizzardActive = false;

    @ScenarioVariable(
            name = "minTimeBetweenBlizzards",
            description = "Temps minimum entre deux tempêtes (en secondes)",
            type = VariableType.TIME
    )
    private int minTimeBetweenBlizzards = 240;

    @ScenarioVariable(
            name = "maxTimeBetweenBlizzards",
            description = "Temps maximum entre deux tempêtes (en secondes)",
            type = VariableType.TIME
    )
    private int maxTimeBetweenBlizzards = 480;

    @ScenarioVariable(
            name = "minBlizzardDuration",
            description = "Durée minimale d'une tempête (en secondes)",
            type = VariableType.TIME
    )
    private int minBlizzardDuration = 90;

    @ScenarioVariable(
            name = "maxBlizzardDuration",
            description = "Durée maximale d'une tempête (en secondes)",
            type = VariableType.TIME
    )
    private int maxBlizzardDuration = 180;

    @ScenarioVariable(
            name = "coldDamageInterval",
            description = "Intervalle de tick pour appliquer les effets de froid (en secondes)",
            type = VariableType.TIME
    )
    private int coldDamageInterval = 2;

    @ScenarioVariable(
            name = "warmthDecreasePerTick",
            description = "Perte de chaleur par tick quand exposé au blizzard",
            type = VariableType.INTEGER
    )
    private int warmthDecreasePerTick = 2;

    @ScenarioVariable(
            name = "slowEffectLevel",
            description = "Niveau de l'effet Slowness lors de froid modéré",
            type = VariableType.INTEGER
    )
    private int slowEffectLevel = 1;

    @ScenarioVariable(
            name = "blindEffectLevel",
            description = "Niveau de l'effet Blindness lors de froid",
            type = VariableType.INTEGER
    )
    private int blindEffectLevel = 0;

    @ScenarioVariable(
            name = "warmthNearHeatSourceBonus",
            description = "Gain de chaleur si proche d'une source de chaleur",
            type = VariableType.INTEGER
    )
    private int warmthNearHeatSourceBonus = 5;

    @ScenarioVariable(
            name = "warmthNormalGain",
            description = "Gain de chaleur normal par tick",
            type = VariableType.INTEGER
    )
    private int warmthNormalGain = 1;

    @ScenarioVariable(
            name = "exposureCheckHeight",
            description = "Hauteur au-dessus du joueur pour vérifier l'exposition",
            type = VariableType.INTEGER
    )
    private int exposureCheckHeight = 5;

    @ScenarioVariable(
            name = "warmthMax",
            description = "Chaleur maximale d'un joueur",
            type = VariableType.INTEGER
    )
    private int warmthMax = 100;

    @Override
    public String getName() {
        return "Blizzard";
    }

    @Override
    public String getDescription() {
        return "Tempêtes de neige qui ralentissent et aveuglent. Restez près du feu !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SNOW_BALL);
    }

    @Override
    public void onGameStart() {
        startBlizzardCycle();
        initializePlayerWarmth();
    }

    private void startBlizzardCycle() {
        if (blizzardTask != null) blizzardTask.cancel();

        blizzardTask = new BukkitRunnable() {
            private int cycleTimer = 0;
            private int blizzardDuration = 0;
            private int nextBlizzardIn = minTimeBetweenBlizzards + random.nextInt(maxTimeBetweenBlizzards - minTimeBetweenBlizzards);

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                cycleTimer++;

                if (!isBlizzardActive) {
                    if (cycleTimer >= nextBlizzardIn) {
                        startBlizzard();
                        cycleTimer = 0;
                        blizzardDuration = minBlizzardDuration + random.nextInt(maxBlizzardDuration - minBlizzardDuration);
                    } else {
                        int timeLeft = nextBlizzardIn - cycleTimer;
                        if (timeLeft == 60) Bukkit.broadcastMessage("§b[Blizzard] §fTempête de neige dans 1 minute !");
                        if (timeLeft == 10) Bukkit.broadcastMessage("§b[Blizzard] §fTempête de neige dans 10 secondes !");
                    }
                } else {
                    applyBlizzardEffects();
                    blizzardDuration--;
                    if (blizzardDuration <= 0) {
                        stopBlizzard();
                        cycleTimer = 0;
                        nextBlizzardIn = minTimeBetweenBlizzards + random.nextInt(maxTimeBetweenBlizzards - minTimeBetweenBlizzards);
                    } else if (blizzardDuration == 10) {
                        Bukkit.broadcastMessage("§b[Blizzard] §fLa tempête se calme dans 10 secondes !");
                    }
                }
            }
        };

        blizzardTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void startBlizzard() {
        isBlizzardActive = true;
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }

        Bukkit.broadcastMessage("§b§l[Blizzard] §fUne tempête de neige commence ! Trouvez de la chaleur !");
    }

    private void stopBlizzard() {
        isBlizzardActive = false;
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setWeatherDuration(0);
        }
        clearPlayerEffects();
        Bukkit.broadcastMessage("§b[Blizzard] §fLa tempête de neige s'est calmée !");
    }

    private void applyBlizzardEffects() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            UUID playerUuid = player.getUniqueId();

            if (isPlayerExposedToBlizzard(player)) {
                int currentWarmth = playerWarmth.getOrDefault(playerUuid, warmthMax);
                playerWarmth.put(playerUuid, Math.max(0, currentWarmth - warmthDecreasePerTick));

                int warmth = playerWarmth.get(playerUuid);

                if (warmth < 20) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, coldDamageInterval * 20, slowEffectLevel + 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, coldDamageInterval * 20, blindEffectLevel));
                    player.damage(0.5);
                    player.sendMessage("§b[Blizzard] §fVous gelez ! Trouvez de la chaleur !");
                } else if (warmth < 50) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, coldDamageInterval * 20, slowEffectLevel));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, coldDamageInterval * 20, blindEffectLevel));
                    player.sendMessage("§b[Blizzard] §fVous avez très froid !");
                } else if (warmth < 80) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, coldDamageInterval * 20, 0));
                    if (random.nextInt(3) == 0) player.sendMessage("§b[Blizzard] §fVous commencez à avoir froid...");
                }
            } else {
                int currentWarmth = playerWarmth.getOrDefault(playerUuid, warmthMax);
                if (isPlayerNearHeatSource(player)) {
                    playerWarmth.put(playerUuid, Math.min(warmthMax, currentWarmth + warmthNearHeatSourceBonus));
                } else {
                    playerWarmth.put(playerUuid, Math.min(warmthMax, currentWarmth + warmthNormalGain));
                }
            }
        }
    }

    private boolean isPlayerExposedToBlizzard(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (!world.hasStorm() || loc.getY() < 50) return false;

        Location checkLoc = loc.clone();
        for (int y = 1; y <= exposureCheckHeight; y++) {
            checkLoc.add(0, 1, 0);
            Material type = checkLoc.getBlock().getType();
            if (type != Material.AIR && type != Material.WATER && type != Material.LAVA && !type.name().contains("LEAVES")) {
                return false;
            }
        }
        return true;
    }

    private boolean isPlayerNearHeatSource(Player player) {
        Location loc = player.getLocation();
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location check = loc.clone().add(x, y, z);
                    Material type = check.getBlock().getType();
                    if (type == Material.FIRE || type == Material.LAVA || type == Material.BURNING_FURNACE || type == Material.TORCH || type == Material.GLOWSTONE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void initializePlayerWarmth() {
        playerWarmth.clear();
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            playerWarmth.put(uhcPlayer.getPlayer().getUniqueId(), warmthMax);
        }
    }

    private void clearPlayerEffects() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        playerWarmth.clear();
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (!isActive()) {
            if (blizzardTask != null) blizzardTask.cancel();
            clearPlayerEffects();
        }
    }
}
