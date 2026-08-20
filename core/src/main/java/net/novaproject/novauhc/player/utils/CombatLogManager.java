package net.novaproject.novauhc.player.utils;

import net.novaproject.novauhc.event.UhcPlayerEvents.UhcCombatTagEvent;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.player.PlayerSpi;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.cooldown.CooldownService.CombatTracker;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CombatLogManager implements Listener {

    private boolean enabled() {
        return UHCManager.get().isCombatLogEnabled();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvpHit(EntityDamageByEntityEvent event) {
        if (!UHCManager.get().isGame()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        Player damager = resolveDamager(event.getDamager());
        if (damager == null || damager.equals(victim)) return;

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcDamager = UHCPlayerManager.get().getPlayer(damager);
        if (uhcVictim == null || uhcDamager == null) return;
        if (!uhcVictim.isPlaying() || !uhcDamager.isPlaying()) return;

        boolean newTag = !CombatTracker.isInCombat(victim.getUniqueId());
        CombatTracker.startCombat(victim.getUniqueId(), damager.getUniqueId());
        CombatTracker.startCombat(damager.getUniqueId(), victim.getUniqueId());
        if (newTag) {
            Bukkit.getPluginManager().callEvent(
                    new UhcCombatTagEvent(uhcVictim, uhcDamager, event.getFinalDamage()));

            for(PlayerSpi.Combat combat : CombatActionRegistry.INSTANCE.getActions()) {
                combat.onTag(uhcVictim, uhcDamager);
            }
        }

        UHCManager.get().getStatsTracker().recordDamage(
                damager.getUniqueId(), victim.getUniqueId(), event.getFinalDamage());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!enabled()) return;
        if (!UHCManager.get().isGame()) return;

        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
        if (!CombatTracker.isInCombat(player.getUniqueId())) return;

        for(PlayerSpi.Combat combat : CombatActionRegistry.INSTANCE.getActions()) {
            combat.onQuit(uhcPlayer);
        }

        LangManager.get().sendAll(CoreLang.COMMON_COMBAT_LOG_DISCONNECT, Map.of("%player%", player.getName()));
        Bukkit.getLogger().info("[CombatLog] NPC posé pour " + player.getName() + " (déco en combat)");
    }

    private Player resolveDamager(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) return shooter;
        return null;
    }

    public static class CombatActionRegistry implements PlayerSpi.ICombatActionRegistry {

        public static final CombatActionRegistry INSTANCE = new CombatActionRegistry();

        private final Set<PlayerSpi.Combat> actions = new HashSet<>();

        @Override
        public void register(PlayerSpi.Combat action) {
            actions.add(action);
        }

        @Override
        public Set<PlayerSpi.Combat> getActions() {
            return Collections.unmodifiableSet(actions);
        }
    }
}