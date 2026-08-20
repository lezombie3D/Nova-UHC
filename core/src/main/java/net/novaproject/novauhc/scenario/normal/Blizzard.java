package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class Blizzard extends Scenario {

    @Override
    public Family getFamily() { return Family.MONDE; }

    private final Random random = new Random();
    private final Map<UUID, Integer> playerWarmth = new HashMap<>();
    private BukkitRunnable blizzardTask;
    private boolean isBlizzardActive = false;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_MIN_TIME_BETWEEN_BLIZZARDS_NAME", descKey = "BLIZZARD_VAR_MIN_TIME_BETWEEN_BLIZZARDS_DESC", type = VariableType.TIME)
    private int minTimeBetweenBlizzards = 240;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_MAX_TIME_BETWEEN_BLIZZARDS_NAME", descKey = "BLIZZARD_VAR_MAX_TIME_BETWEEN_BLIZZARDS_DESC", type = VariableType.TIME)
    private int maxTimeBetweenBlizzards = 480;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_MIN_BLIZZARD_DURATION_NAME", descKey = "BLIZZARD_VAR_MIN_BLIZZARD_DURATION_DESC", type = VariableType.TIME)
    private int minBlizzardDuration = 90;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_MAX_BLIZZARD_DURATION_NAME", descKey = "BLIZZARD_VAR_MAX_BLIZZARD_DURATION_DESC", type = VariableType.TIME)
    private int maxBlizzardDuration = 180;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_COLD_DAMAGE_INTERVAL_NAME", descKey = "BLIZZARD_VAR_COLD_DAMAGE_INTERVAL_DESC", type = VariableType.TIME)
    private int coldDamageInterval = 2;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_WARMTH_DECREASE_PER_TICK_NAME", descKey = "BLIZZARD_VAR_WARMTH_DECREASE_PER_TICK_DESC", type = VariableType.INTEGER)
    private int warmthDecreasePerTick = 2;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_SLOW_EFFECT_LEVEL_NAME", descKey = "BLIZZARD_VAR_SLOW_EFFECT_LEVEL_DESC", type = VariableType.INTEGER)
    private int slowEffectLevel = 1;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_BLIND_EFFECT_LEVEL_NAME", descKey = "BLIZZARD_VAR_BLIND_EFFECT_LEVEL_DESC", type = VariableType.INTEGER)
    private int blindEffectLevel = 0;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_WARMTH_NEAR_HEAT_SOURCE_BONUS_NAME", descKey = "BLIZZARD_VAR_WARMTH_NEAR_HEAT_SOURCE_BONUS_DESC", type = VariableType.INTEGER)
    private int warmthNearHeatSourceBonus = 5;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_WARMTH_NORMAL_GAIN_NAME", descKey = "BLIZZARD_VAR_WARMTH_NORMAL_GAIN_DESC", type = VariableType.INTEGER)
    private int warmthNormalGain = 1;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_EXPOSURE_CHECK_HEIGHT_NAME", descKey = "BLIZZARD_VAR_EXPOSURE_CHECK_HEIGHT_DESC", type = VariableType.INTEGER)
    private int exposureCheckHeight = 5;

    @Var(lang = ScenarioVarLang.class, nameKey = "BLIZZARD_VAR_WARMTH_MAX_NAME", descKey = "BLIZZARD_VAR_WARMTH_MAX_DESC", type = VariableType.INTEGER)
    private int warmthMax = 100;

    @Override
    public String getName() {
        return "Blizzard";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.BLIZZARD, player);
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
            private int nextBlizzardIn = minTimeBetweenBlizzards + random.nextInt(Math.max(1, maxTimeBetweenBlizzards - minTimeBetweenBlizzards));

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
                        blizzardDuration = minBlizzardDuration + random.nextInt(Math.max(1, maxBlizzardDuration - minBlizzardDuration));
                    } else {
                        int timeLeft = nextBlizzardIn - cycleTimer;
                        if (timeLeft == 60) LangManager.get().sendAll(ScenarioLang.BLIZZARD_WARNING_ONE_MINUTE);
                        if (timeLeft == 10) LangManager.get().sendAll(ScenarioLang.BLIZZARD_WARNING_TEN_SECONDS);
                    }
                } else {
                    applyBlizzardEffects();
                    blizzardDuration--;
                    if (blizzardDuration <= 0) {
                        stopBlizzard();
                        cycleTimer = 0;
                        nextBlizzardIn = minTimeBetweenBlizzards + random.nextInt(Math.max(1, maxTimeBetweenBlizzards - minTimeBetweenBlizzards));
                    } else if (blizzardDuration == 10) {
                        LangManager.get().sendAll(ScenarioLang.BLIZZARD_ENDING_SOON);
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

        LangManager.get().sendAll(ScenarioLang.BLIZZARD_STORM_START);
    }

    private void stopBlizzard() {
        isBlizzardActive = false;
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setWeatherDuration(0);
        }
        clearPlayerEffects();
        LangManager.get().sendAll(ScenarioLang.BLIZZARD_STORM_END);
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
                    LangManager.get().send(ScenarioLang.BLIZZARD_FREEZING, player);
                } else if (warmth < 50) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, coldDamageInterval * 20, slowEffectLevel));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, coldDamageInterval * 20, blindEffectLevel));
                    LangManager.get().send(ScenarioLang.BLIZZARD_VERY_COLD, player);
                } else if (warmth < 80) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, coldDamageInterval * 20, 0));
                    if (random.nextInt(3) == 0) LangManager.get().send(ScenarioLang.BLIZZARD_GETTING_COLD, player);
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

