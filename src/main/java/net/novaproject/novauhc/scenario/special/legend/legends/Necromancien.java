package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.Legend;
import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

/**
 * Légende du Nécromancien
 */
public class Necromancien extends LegendClass {

    private static final String POWER_COOLDOWN = "necro_power";
    private static final long POWER_COOLDOWN_MS = 10 * 60 * 1000L; // 10 minutes

    public Necromancien() {
        super(7, "Nécromancien", "Invoque des morts-vivants pour combattre", Material.BONE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Nécromancien] §aVous êtes maintenant un Nécromancien !");
        player.sendMessage("§7Pouvoir : Invoque des monstres contre l'ennemi le plus proche");
        player.getInventory().addItem(LegendItems.createPowerItem("Nécromancien"));
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        if (!data.isCooldownReady(POWER_COOLDOWN)) {
            long remaining = data.getCooldownRemaining(POWER_COOLDOWN);
            LegendEffects.sendCooldownMessage(player, formatTime(remaining));
            return false;
        }

        // Trouver l'ennemi le plus proche
        Player target = findNearestEnemy(player, uhcPlayer);
        if (target == null) {
            player.sendMessage("§c[Nécromancien] Aucun ennemi trouvé à proximité !");
            return false;
        }

        // Invoquer des monstres
        summonMobs(player, target, data);

        data.setCooldown(POWER_COOLDOWN, POWER_COOLDOWN_MS);
        LegendEffects.sendPowerActivatedMessage(player, "Nécromancien");
        player.sendMessage("§6[Nécromancien] §aMonstres invoqués contre " + target.getName() + " !");

        return true;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Pas d'effets passifs
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);
        // Supprimer toutes les entités invoquées
        data.clearSummonedEntities();
    }

    private Player findNearestEnemy(Player player, UHCPlayer uhcPlayer) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Player other : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (other == player) continue;

            UHCPlayer otherUhc = UHCPlayerManager.get().getPlayer(other);
            if (uhcPlayer.getTeam().isPresent() && otherUhc.getTeam().isPresent() &&
                    uhcPlayer.getTeam().get().equals(otherUhc.getTeam().get())) {
                continue; // Même équipe
            }

            double distance = player.getLocation().distance(other.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = other;
            }
        }

        return nearest;
    }

    private void summonMobs(Player player, Player target, LegendData data) {
        // Invoquer 2 squelettes et 1 zombie
        for (int i = 0; i < 2; i++) {
            Skeleton skeleton = (Skeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON);
            skeleton.setTarget(target);
            data.addSummonedEntity(skeleton);
        }

        Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        zombie.setTarget(target);
        data.addSummonedEntity(zombie);
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m" + (seconds % 60 > 0 ? (seconds % 60) + "s" : "");
    }
}
