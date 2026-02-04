package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.BloodLustLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodLust extends Scenario {

    private final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();

    @ScenarioVariable(
            name = "speed_duration",
            description = "Durée de l'effet Speed en secondes",
            type = VariableType.TIME
    )
    private int speedDuration = 30;

    @ScenarioVariable(
            name = "strength_duration",
            description = "Durée de l'effet Strength en secondes",
            type = VariableType.TIME
    )
    private int strengthDuration = 30;

    @ScenarioVariable(
            name = "speed_level",
            description = "Niveau de l'effet Speed",
            type = VariableType.INTEGER
    )
    private int speedLevel = 1;

    @ScenarioVariable(
            name = "strength_level",
            description = "Niveau de l'effet Strength",
            type = VariableType.INTEGER
    )
    private int strengthLevel = 0;

    @ScenarioVariable(
            name = "countdown_10sec",
            description = "Activer le message de countdown à 10 secondes",
            type = VariableType.BOOLEAN
    )
    private boolean countdown10Sec = true;

    @ScenarioVariable(
            name = "countdown_5sec",
            description = "Activer le message de countdown à 5 secondes",
            type = VariableType.BOOLEAN
    )
    private boolean countdown5Sec = true;

    @ScenarioVariable(
            name = "countdown_end",
            description = "Activer le message à la fin de l'effet",
            type = VariableType.BOOLEAN
    )
    private boolean countdownEnd = true;

    @Override
    public String getName() {
        return "BloodLust";
    }

    @Override
    public String getDescription() {
        return "Chaque kill donne Speed " + (speedLevel + 1) + " et Strength " + (strengthLevel + 1) + " pendant " + speedDuration + " secondes.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BLAZE_POWDER);
    }

    @Override
    public String getPath() {
        return "bloodlust";
    }

    @Override
    public ScenarioLang[] getLang() {
        return BloodLustLang.values();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive() || killer == null) return;

        Player killerPlayer = killer.getPlayer();
        cancelBloodLustEffect(killerPlayer.getUniqueId());
        applyBloodLustEffect(killerPlayer);

        ScenarioLangManager.send(killer, BloodLustLang.KILL_BOOST);
        Bukkit.broadcastMessage("§c[BloodLust] §f" + killerPlayer.getName() + " §fest en état de soif de sang !");
    }

    private void applyBloodLustEffect(Player player) {
        UUID playerUuid = player.getUniqueId();

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, speedLevel));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthDuration * 20, strengthLevel));

        BukkitRunnable countdownTask = new BukkitRunnable() {
            private int timeLeft = speedDuration;

            @Override
            public void run() {
                if (!player.isOnline() || !isActive()) {
                    cancel();
                    activeEffects.remove(playerUuid);
                    return;
                }

                timeLeft--;

                if (countdown10Sec && timeLeft == 10) {
                    player.sendMessage("§c[BloodLust] §fSoif de sang se termine dans 10 secondes !");
                } else if (countdown5Sec && timeLeft == 5) {
                    player.sendMessage("§c[BloodLust] §fSoif de sang se termine dans 5 secondes !");
                } else if (countdownEnd && timeLeft <= 0) {
                    player.sendMessage("§c[BloodLust] §fVotre soif de sang s'est calmée.");
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                    activeEffects.remove(playerUuid);
                    cancel();
                }
            }
        };

        activeEffects.put(playerUuid, countdownTask);
        countdownTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void cancelBloodLustEffect(UUID playerUuid) {
        BukkitRunnable existingTask = activeEffects.get(playerUuid);
        if (existingTask != null) {
            existingTask.cancel();
            activeEffects.remove(playerUuid);

            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            }
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (!isActive()) {
            for (UUID playerUuid : activeEffects.keySet()) {
                cancelBloodLustEffect(playerUuid);
            }
            activeEffects.clear();
        }
    }
}
