package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Fallout extends Scenario {

    private final Map<UUID, Integer> playerWarnings = new HashMap<>();
    private BukkitRunnable falloutTask;
    private boolean falloutStarted = false;

    @ScenarioVariable(
            name = "Fallout Start Time",
            description = "Temps en secondes avant le début des radiations",
            type = VariableType.TIME
    )
    private int falloutStartTime = 45 * 60;

    @ScenarioVariable(
            name = "Safe Y Level",
            description = "Altitude Y en dessous de laquelle les joueurs sont en sécurité",
            type = VariableType.INTEGER
    )
    private int safeYLevel = 60;

    @ScenarioVariable(
            name = "Base Radiation Damage",
            description = "Dégâts de radiation appliqués par tick pour exposition légère",
            type = VariableType.DOUBLE
    )
    private double baseDamage = 0.5;

    @ScenarioVariable(
            name = "Moderate Radiation Threshold",
            description = "Nombre de ticks d'exposition avant radiation modérée",
            type = VariableType.INTEGER
    )
    private int moderateThreshold = 3;

    @ScenarioVariable(
            name = "Severe Radiation Threshold",
            description = "Nombre de ticks d'exposition avant radiation sévère",
            type = VariableType.INTEGER
    )
    private int severeThreshold = 5;

    @ScenarioVariable(
            name = "Severe Radiation Max Damage",
            description = "Dégâts max de radiation par tick",
            type = VariableType.DOUBLE
    )
    private double maxDamage = 2.0;

    @Override
    public String getName() {
        return "Fallout";
    }

    @Override
    public String getDescription() {
        return "Après un certain temps, rester au-dessus de Y=" + safeYLevel +
                " inflige des dégâts de radiation aux joueurs.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM);
    }

    @Override
    public void onGameStart() {
        startFalloutTask();
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;

        int currentTime = UHCManager.get().getTimer();

        if (!falloutStarted && currentTime >= falloutStartTime) {
            startFallout();
        }

        if (falloutStarted) {
            checkPlayerRadiation(p);
        } else {
            sendFalloutWarnings(currentTime);
        }
    }

    private void startFalloutTask() {
        if (falloutTask != null) falloutTask.cancel();

        falloutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                int currentTime = UHCManager.get().getTimer();

                if (!falloutStarted && currentTime >= falloutStartTime) startFallout();
                if (falloutStarted) applyRadiationToExposedPlayers();
            }
        };

        falloutTask.runTaskTimer(Main.get(), 0, 200); // 10 sec
    }

    private void startFallout() {
        falloutStarted = true;

        Bukkit.broadcastMessage("§c§l[Fallout] §fLES RADIATIONS COMMENCENT !");
        Bukkit.broadcastMessage("§c[Fallout] §fDescendez sous Y=" + safeYLevel + " pour éviter les radiations !");

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.WITHER_SPAWN, 1.0f, 0.5f);
        }
    }

    private void sendFalloutWarnings(int currentTime) {
        int timeUntilFallout = falloutStartTime - currentTime;
        if (timeUntilFallout == 300)
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations dans 5 minutes ! Préparez vos abris !");
        else if (timeUntilFallout == 60)
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations dans 1 minute ! Descendez sous Y=" + safeYLevel + " !");
        else if (timeUntilFallout == 10)
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations dans 10 secondes !");
    }

    private void checkPlayerRadiation(Player player) {
        if (!falloutStarted) return;

        Location loc = player.getLocation();
        UUID uuid = player.getUniqueId();

        if (loc.getY() > safeYLevel) {
            int warnings = playerWarnings.getOrDefault(uuid, 0);
            playerWarnings.put(uuid, warnings + 1);
            applyRadiationEffects(player, warnings);
        } else {
            playerWarnings.put(uuid, 0);
            removeRadiationEffects(player);
        }
    }

    private void applyRadiationToExposedPlayers() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            checkPlayerRadiation(uhcPlayer.getPlayer());
        }
    }

    private void applyRadiationEffects(Player player, int exposureLevel) {
        double damage = Math.min(maxDamage, baseDamage + exposureLevel * 0.1);
        player.damage(damage);

        if (exposureLevel >= severeThreshold) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));
            player.sendMessage("§c[Fallout] §fRadiation SÉVÈRE ! Descendez immédiatement !");
        } else if (exposureLevel >= moderateThreshold) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
            player.sendMessage("§c[Fallout] §fRadiation modérée ! Trouvez un abri !");
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0));
            player.sendMessage("§c[Fallout] §fVous êtes exposé aux radiations !");
        }
    }

    private void removeRadiationEffects(Player player) {
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.HUNGER);
    }
}

