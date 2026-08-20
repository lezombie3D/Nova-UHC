package net.novaproject.novauhc.utils.cooldown;

import java.util.Set;
import java.util.UUID;
import net.novaproject.novauhc.game.GameSpi.Clearable;
import org.bukkit.entity.Player;

public final class CooldownSpi {

    private CooldownSpi() {
    }

    public interface ICooldownService {

        boolean put(Player player, String key, long durationMs);

        boolean put(UUID uuid, String key, long durationMs);

        long get(Player player, String key);

        long get(UUID uuid, String key);

        long remove(Player player, String key);

        long remove(UUID uuid, String key);

        int clearAll(Player player);

        int clearAll(UUID uuid);

        Set<String> getActiveKeys(Player player);

        Set<String> getActiveKeys(UUID uuid);

        int reduceAll(Player player, long reductionMs);

        int reduceAll(UUID uuid, long reductionMs);
    }

    public interface ICombatTracker extends Clearable {

        void startCombat(UUID playerId, UUID damagerId);

        boolean isInCombat(UUID playerId);

        UUID getLastDamager(UUID playerId);
    }
}
