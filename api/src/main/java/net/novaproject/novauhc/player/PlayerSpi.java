package net.novaproject.novauhc.player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.novaproject.novauhc.team.TeamSpi.IUHCTeam;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerSpi {

    private PlayerSpi() {
    }

    public interface Combat {

        void onTag(IUHCPlayer attacker, IUHCPlayer victim);

        void onQuit(IUHCPlayer uhcPlayer);

        boolean hasNpc(UUID playerUuid);

    }

    public interface ICombatActionRegistry {

        void register(Combat combat);

        Collection<Combat> getActions();

    }

    public interface IGameStatsTracker {

        void startGame();

        void addPlayer(UUID uuid, String name);

        void addKill(UUID killer);

        void addDeath(UUID victim);

        void revive(UUID uuid);

        void setCamp(UUID uuid, String camp);

        void setAssistWindowSeconds(int seconds);

        void recordDamage(UUID damager, UUID victim, double amount);

        void creditAssists(UUID victim, UUID killer);

        int getGameDuration();

        void reset();
    }

    public interface IReconnectionManager {

        void eliminateNow(UUID uuid, Location dropLocation);

        void cancelReconnectionTimer(UUID uuid);

        void cleanup();

        boolean hasPendingReconnection(UUID uuid);

        boolean hasAnyPendingReconnection();
    }

    public interface IUHCPlayer {

        UUID getUniqueId();

        Player getPlayer();

        OfflinePlayer getOfflinePlayer();

        boolean isOnline();

        String getHostname();

        String getLocale();

        void setLocale(String locale);

        void applyLoadedLocale(String locale);

        boolean isPlaying();

        void setPlaying(boolean playing);

        boolean isSpec();

        void setSpec(boolean spec);

        boolean isBypassed();

        void setBypassed(boolean bypassed);

        Player getKiller();

        void setKiller(Player killer);

        List<ItemStack> getDeathItem();

        Optional<? extends IUHCTeam> getTeam();

        int getKill();

        void setKill(int kill);

        int getDiamondmined();

        int getMinedDiamond();

        void setMinedDiamond(int minedDiamond);

        int getDiamondLimit();

        void setDiamondLimit(int diamondLimit);

        int getDiamondArmor();

        void setDiamondArmor(int diamondArmor);

        int getProtectionMax();

        void setProtectionMax(int protectionMax);

        double getForcePercent();

        void setForcePercent(double forcePercent);

        double getForceCriticPercent();

        void setForceCriticPercent(double forceCriticPercent);

        double getResistancePercent();

        void setResistancePercent(double resistancePercent);

        double getSpeedPercent();

        void setSpeedPercent(double speedPercent);

        ItemStack[] getLastInventoryContents();

        void setLastInventoryContents(ItemStack[] lastInventoryContents);

        ItemStack[] getLastArmorContents();

        void setLastArmorContents(ItemStack[] lastArmorContents);

        Location getLastDeathLocation();

        void setLastDeathLocation(Location lastDeathLocation);

        int getLastXpLevel();

        void setLastXpLevel(int lastXpLevel);

        double getLastMaxHealth();

        void setLastMaxHealth(double lastMaxHealth);

        Map<UUID, ChatColor> getTabColorPrefs();

        String getDistanceToCenter(Location to);

        void connect(Player player);

        void disconnect(Player player);
    }

    public interface IUHCPlayerManager {

        List<? extends IUHCPlayer> getOnlineUHCPlayers();

        List<? extends IUHCPlayer> getPlayingOnlineUHCPlayers();

        IUHCPlayer getPlayer(Player player);

        IUHCPlayer getPlayer(UUID uuid);

        void connect(Player player);

        void disconnect(Player player);
    }
}
