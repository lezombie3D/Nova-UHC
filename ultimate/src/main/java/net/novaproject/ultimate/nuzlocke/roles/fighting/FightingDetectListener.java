package net.novaproject.ultimate.nuzlocke.roles.fighting;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Given;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FightingDetectListener extends Ability implements Given, Hurtable {

    @Var(name = "Nuzlocke Fighting Detect Threshold", type = VariableType.TIME)
    private int parryThresholdSeconds = 30;

    @Var(name = "Nuzlocke Fighting Detect Penalty", type = VariableType.TIME)
    private int releasePenaltySeconds = 5;

    @Var(name = "Nuzlocke Fighting Detect Cd", type = VariableType.TIME)
    private int cdSeconds = 300;

    private static final Map<UUID, ParryState> STATES = new HashMap<>();
    private static final String CD_KEY = "Detect Cooldown";
    private boolean ticking = false;

    private static class ParryState {
        double accumulatedSec = 0;
        boolean blocking = false;
        boolean invincibleActive = false;
    }

    public FightingDetectListener() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Detect"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onGive(net.novaproject.novauhc.player.UHCPlayer uhcPlayer) {
        if (ticking) return;
        ticking = true;
        new BukkitRunnable() {
            @Override public void run() { tick(); }
        }.runTaskTimer(net.novaproject.novauhc.Main.get(), 20L, 20L);
    }

    private void tick() {
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UUID id = owner.getUniqueId();
        ParryState s = STATES.computeIfAbsent(id, k -> new ParryState());

        boolean nowBlocking = isHoldingSword(owner) && owner.isBlocking();
        boolean onCd = CooldownService.get(id, CD_KEY) > 0;

        if (nowBlocking && !onCd) {
            s.accumulatedSec += 1.0;
            if (s.accumulatedSec >= parryThresholdSeconds && !s.invincibleActive) {
                s.invincibleActive = true;
                owner.sendMessage("§6§lDetect §8│ §7Invincibilité activée ! Continuez à parer.");
            }
            s.blocking = true;
        } else {
            if (s.blocking) {
                if (s.invincibleActive) {
                    s.invincibleActive = false;
                    s.accumulatedSec = 0;
                    CooldownService.put(id, CD_KEY, cdSeconds * 1000L);
                    owner.sendMessage("§6§lDetect §8│ §7Invincibilité terminée. Cooldown " + cdSeconds + "s.");
                } else {
                    s.accumulatedSec = Math.max(0, s.accumulatedSec - releasePenaltySeconds);
                }
                s.blocking = false;
            }
        }
    }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        ParryState s = STATES.get(p.getUniqueId());
        if (s != null && s.invincibleActive) event.setCancelled(true);
    }

    private boolean isHoldingSword(Player p) {
        ItemStack hand = p.getItemInHand();
        if (hand == null) return false;
        Material t = hand.getType();
        return t == Material.WOOD_SWORD || t == Material.STONE_SWORD
                || t == Material.IRON_SWORD || t == Material.GOLD_SWORD
                || t == Material.DIAMOND_SWORD;
    }
}

