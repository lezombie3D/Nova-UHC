package net.novaproject.novauhc.debug;

import net.novaproject.novauhc.event.UhcAbilityEvents.UhcAbilityUseEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcBorderStartEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcCampChangeEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcDayEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcFinalHealEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameSecondEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameStartEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameStateChangeEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcGameWinEvent;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcGiveHotbarEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcItemMaxLevelEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcLateJoinEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcNewEpisodeEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcNightEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathWaitingEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerKillEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerReconnectEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerTimeoutEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcPvpEnableEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRoleAssignEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRoleDistributionEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterEndEvent;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterStartEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcSchematicPlaceEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerResurrectEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcCombatTagEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcBorderShrinkEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcDiamondMinedEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcDiamondLimitReachedEvent;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcTeamEliminatedEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class UhcEventLogger implements Listener {

    private static void log(String event, String details) {
        DebugLog.log(DebugLog.Channel.EVENTS, event, details);
    }

    private static String name(UHCPlayer up) {
        if (up == null) return "null";
        return up.getPlayer() != null ? up.getPlayer().getName() : String.valueOf(up.getUniqueId());
    }

    private static String loc(Location l) {
        if (l == null) return "null";
        return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ()
                + (l.getWorld() == null ? "" : " (" + l.getWorld().getName() + ")");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStart(UhcGameStartEvent e) {
        log("GameStart", "cancelled=" + e.isCancelled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(UhcGameStateChangeEvent e) {
        log("GameStateChange", e.getOldState() + " → " + e.getNewState());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPvpEnable(UhcPvpEnableEvent e) {
        log("PvpEnable", "-");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderStart(UhcBorderStartEvent e) {
        log("BorderStart", "target=" + e.getTargetSize() + " blocks/s=" + e.getBlocksPerSecond() + " cancelled=" + e.isCancelled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScatterStart(UhcScatterStartEvent e) {
        log("ScatterStart", "-");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScatterEnd(UhcScatterEndEvent e) {
        log("ScatterEnd", "-");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalHeal(UhcFinalHealEvent e) {
        log("FinalHeal", "timer=" + e.getTimer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameSecond(UhcGameSecondEvent e) {
        if (e.getTimer() % 60 != 0) return;
        log("GameSecond", "timer=" + e.getTimer() + " episode=" + e.getEpisode());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDay(UhcDayEvent e) {
        log("Day", "-");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNight(UhcNightEvent e) {
        log("Night", "-");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNewEpisode(UhcNewEpisodeEvent e) {
        log("NewEpisode", "episode=" + e.getEpisode());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoleDistribution(UhcRoleDistributionEvent e) {
        log("RoleDistribution", "players=" + e.getPlayers().size() + " pool=" + e.getPool().size());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRoleAssign(UhcRoleAssignEvent e) {
        log("RoleAssign", name(e.getPlayer()) + " → "
                + (e.getRole() == null ? "null" : ChatColor.stripColor(e.getRole().getName())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRolesDistributed(UhcRolesDistributedEvent e) {
        log("RolesDistributed", e.getRoles().size() + " rôle(s) distribué(s)");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCampChange(UhcCampChangeEvent e) {
        log("CampChange", name(e.getPlayer()) + " : "
                + (e.getOldCamp() == null ? "null" : e.getOldCamp().getName()) + " → "
                + (e.getNewCamp() == null ? "null" : e.getNewCamp().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAbilityUse(UhcAbilityUseEvent e) {
        log("AbilityUse", e.getPlayer().getName() + " : "
                + (e.getAbility() == null ? "null" : e.getAbility().getName())
                + " cancelled=" + e.isCancelled());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(UhcPlayerDeathEvent e) {
        log("PlayerDeath", name(e.getVictim()) + " killer=" + name(e.getKiller()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathWaiting(UhcPlayerDeathWaitingEvent e) {
        log("PlayerDeathWaiting", name(e.getVictim()) + " killer=" + name(e.getKiller()) + " waitTicks=" + e.getWaitTicks());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKill(UhcPlayerKillEvent e) {
        log("PlayerKill", name(e.getKiller()) + " a tué " + name(e.getVictim()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerReconnect(UhcPlayerReconnectEvent e) {
        log("PlayerReconnect", name(e.getPlayer()) + " remaining=" + e.getRemainingSeconds() + "s");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTimeout(UhcPlayerTimeoutEvent e) {
        log("PlayerTimeout", name(e.getPlayer()) + " uuid=" + e.getUuid());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameWin(UhcGameWinEvent e) {
        log("GameWin", "camp=" + (e.getWinningCamp() == null ? "null" : e.getWinningCamp().getName())
                + " winners=" + e.getWinners().size());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGiveHotbar(UhcGiveHotbarEvent e) {
        log("GiveHotbar", e.getPlayer().getName() + " items=" + e.getItems().size());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemMaxLevel(UhcItemMaxLevelEvent e) {
        log("ItemMaxLevel", e.getPlayer().getName() + " enchants=" + e.getEnchants().size());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLateJoin(UhcLateJoinEvent e) {
        log("LateJoin", name(e.getPlayer()) + " intégré par " + (e.getHost() == null ? "?" : e.getHost().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSchematicPlace(UhcSchematicPlaceEvent e) {
        log("SchematicPlace", e.getFileName() + " @ " + loc(e.getAnchor()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResurrect(UhcPlayerResurrectEvent e) {
        log("PlayerResurrect", name(e.getVictim()) + " priority=" + e.getPriority());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatTag(UhcCombatTagEvent e) {
        log("CombatTag", name(e.getVictim()) + " <- " + name(e.getDamager()) + " (" + e.getDamage() + ")");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderShrink(UhcBorderShrinkEvent e) {
        log("BorderShrink", "phase=" + e.getPhaseId() + " " + (int) e.getFromSize()
                + "→" + (int) e.getToSize() + " in " + e.getDurationSeconds() + "s");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDiamondMined(UhcDiamondMinedEvent e) {
        log("DiamondMined", name(e.getPlayer()) + " total=" + e.getTotalMined());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDiamondLimitReached(UhcDiamondLimitReachedEvent e) {
        log("DiamondLimitReached", name(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeamEliminated(UhcTeamEliminatedEvent e) {
        log("TeamEliminated", e.getTeam() == null ? "null" : e.getTeam().name());
    }
}

