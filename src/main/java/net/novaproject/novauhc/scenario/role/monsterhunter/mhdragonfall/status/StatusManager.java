package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status;

import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StatusManager {

    private static StatusManager instance;
    private final Map<UUID, List<StatusEffect>> activeEffects = new HashMap<UUID, List<StatusEffect>>();

    public static StatusManager getInstance() {
        if (instance == null) instance = new StatusManager();
        return instance;
    }

    private StatusManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickAll();
            }
        }.runTaskTimerAsynchronously(Main.get(), 0L, 20L);
    }

    /**
     * Applique un effet à un joueur
     */
    public void applyEffect(Player player, StatusEffect effect) {
        if (player == null || effect == null) return;

        List<StatusEffect> effects = activeEffects.get(player.getUniqueId());
        if (effects == null) {
            effects = new ArrayList<StatusEffect>();
            activeEffects.put(player.getUniqueId(), effects);
        }

        for (Iterator<StatusEffect> it = effects.iterator(); it.hasNext();) {
            StatusEffect existing = it.next();
            if (existing.getName().equalsIgnoreCase(effect.getName())) {
                existing.end();
                it.remove();
                break;
            }
        }

        effects.add(effect);
        effect.start();
    }

    /**
     * Tick tous les effets actifs
     */
    private void tickAll() {
        for (Iterator<Map.Entry<UUID, List<StatusEffect>>> playerIt = activeEffects.entrySet().iterator(); playerIt.hasNext();) {
            Map.Entry<UUID, List<StatusEffect>> entry = playerIt.next();
            UUID uuid = entry.getKey();
            List<StatusEffect> list = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                playerIt.remove();
                continue;
            }

            for (Iterator<StatusEffect> it = list.iterator(); it.hasNext();) {
                StatusEffect effect = it.next();
                if (effect.isEnded()) {
                    it.remove();
                    continue;
                }
                effect.tick();
            }

            if (list.isEmpty()) playerIt.remove();
        }
    }

    /**
     * Supprime tous les effets d'un joueur
     */
    public void clearEffects(Player player) {
        List<StatusEffect> list = activeEffects.remove(player.getUniqueId());
        if (list != null) {
            for (StatusEffect effect : list) effect.end();
        }
    }

    /**
     * Récupère les effets actifs d’un joueur
     */
    public List<StatusEffect> getEffects(Player player) {
        List<StatusEffect> list = activeEffects.get(player.getUniqueId());
        return list == null ? Collections.<StatusEffect>emptyList() : new ArrayList<StatusEffect>(list);
    }
}
