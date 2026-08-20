package net.novaproject.novauhc.game;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;
import net.novaproject.novauhc.scenario.ScenarioSpi.IRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GameSpi {

    private GameSpi() {
    }

    public interface Clearable {

        void clearAll();
    }

    public interface IBountySystem extends Clearable {

        void setOnClaim(BiConsumer<Player, Integer> handler);

        void add(UUID target, int amount);

        void set(UUID target, int amount);

        int get(UUID target);

        boolean has(UUID target);

        int claim(Player claimer, UUID target);

        void clear(UUID target);
    }

    public interface IDayNightCycle {

        boolean isDay();

        void tick();
    }

    public interface IDeathAnnouncer {

        void announceVanilla(IUHCPlayer victim);

        void announceRole(IUHCPlayer victim, IRole deadRole);
    }

    public interface IEpisodeLimiter extends Clearable {

        boolean tryUse(UUID uuid, String key);

        boolean canUse(UUID uuid, String key);

        void refund(UUID uuid, String key);
    }

    public interface IEpisodeManager {

        int getEpisode();

        void reset();

        boolean isEnabled();

        int getTimeSpentInEpisode();

        int getTimeLeftInEpisode();

        void tick(int timer);
    }

    public interface IGroupSizeManager {

        boolean isEnabled();

        int getCurrentSize();

        void setForcedSize(int size);
    }

    public interface ILifecycles {

        void startAll();

        void stopAll();
    }

    public interface IPendingDeathManager {

        boolean isPending(UUID uuid);

        boolean hasResurrection(UUID uuid);

        boolean hasAnyPending();

        void clearAll();

        Set<UUID> getPendingUuids();

        int getRemainingSeconds(UUID uuid);

        ItemStack[] getSavedInventory(UUID uuid);

        ItemStack[] getSavedArmor(UUID uuid);

        void markNoRevive(UUID uuid);

        boolean consumeNoRevive(UUID uuid);

        boolean isNoRevive(UUID uuid);

        UUID getKiller(UUID victimUuid);

        Location getDeathLocation(UUID victimUuid);
    }

    public interface ISilenceSystem extends Clearable {

        void silence(UUID player, int seconds);

        boolean isSilenced(UUID player);

        int remainingSeconds(UUID player);

        void unsilence(UUID player);
    }

    public interface IVictoryManager {

        boolean isVictoryEnabled();

        void setVictoryEnabled(boolean enabled);

        void requestCheck();
    }

    public interface IVoteSystem extends Clearable {

        boolean isActive(String id);

        void cast(String id, UUID voter, int option);

        void retract(String id, UUID voter);

        Map<String, Integer> tally(String id);

        String winner(String id);

        void end(String id);

        boolean startYesNo(String question, int durationSeconds);

        boolean startTimed(String id, String question, List<String> options, int durationSeconds);

        void castCurrent(Player player, int option);

        boolean hasCurrent();

        List<String> currentOptions();
    }
}
