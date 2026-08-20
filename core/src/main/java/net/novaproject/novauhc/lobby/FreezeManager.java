package net.novaproject.novauhc.lobby;

import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.game.SpectatorNotifier;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager implements Listener {

    private static final Set<UUID> FROZEN = ConcurrentHashMap.newKeySet();

    public static boolean isFrozen(UUID uuid) {
        return FROZEN.contains(uuid);
    }

    private static final Lang MOD_FROZEN = DynamicLang.of("ui.specnotify.frozen",
            "§b▸ §7%staff% §7a §bgelé §f%player%");
    private static final Lang MOD_UNFROZEN = DynamicLang.of("ui.specnotify.unfrozen",
            "§b▸ §7%staff% §7a §bdégelé §f%player%");

    public static boolean toggle(Player target) {
        return toggle(null, target);
    }

    public static boolean toggle(Player staff, Player target) {
        boolean frozen;
        if (FROZEN.remove(target.getUniqueId())) {
            LangManager.get().send(CoreLang.COMMON_UNFROZEN, target);
            DisplayService.resetVignette(target);
            frozen = false;
        } else {
            FROZEN.add(target.getUniqueId());
            LangManager.get().send(CoreLang.COMMON_FROZEN, target);
            DisplayService.vignette(target, "textures/misc/pumpkinblur.png", 0.5f);
            DisplayService.notification(target,
                    LangManager.get().get(CoreLang.COMMON_FROZEN, target), "", 5);
            frozen = true;
        }
        SpectatorNotifier.notifySpectators(frozen ? MOD_FROZEN : MOD_UNFROZEN, Map.of(
                "%staff%", staff == null ? "?" : staff.getName(),
                "%player%", target.getName()), target);
        return frozen;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!FROZEN.contains(event.getPlayer().getUniqueId())) return;
        if (event.getTo() == null || event.getFrom() == null) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) return;
        event.setTo(event.getFrom());
        DisplayService.actionBar(event.getPlayer(),
                LangManager.get().get(CoreLang.COMMON_FROZEN_ACTIONBAR, event.getPlayer()));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && FROZEN.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && FROZEN.contains(damager.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FROZEN.remove(event.getPlayer().getUniqueId());
    }
}

