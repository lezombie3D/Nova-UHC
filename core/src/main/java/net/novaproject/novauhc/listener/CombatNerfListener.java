package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CombatNerfListener implements Listener {

    private static final float BASE_WALK_SPEED = 0.2f;
    private static final double VANILLA_SPEED_PER_LEVEL = 0.20;
    private static final Map<UUID, Float> appliedWalkSpeeds = new HashMap<>();

    private static final Set<Material> ARMOR_MATERIALS = Set.of(
            Material.GOLD_HELMET,      Material.GOLD_CHESTPLATE,
            Material.GOLD_LEGGINGS,    Material.GOLD_BOOTS,
            Material.LEATHER_HELMET,   Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET,   Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            Material.DIAMOND_HELMET,   Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
    );

    private final Random random = new Random();

    private static Role activeRoleOf(UHCPlayer uhcPlayer) {
        ScenarioRole<?> activeScenarioRole = ScenarioManager.get()
                .getActiveSpecialScenarios()
                .stream()
                .filter(s -> s instanceof ScenarioRole)
                .map(s -> (ScenarioRole<?>) s)
                .findFirst()
                .orElse(null);
        if (activeScenarioRole == null) return null;
        return activeScenarioRole.getRoleByUHCPlayer(uhcPlayer);
    }

    private static boolean hasActiveRoleScenario() {
        return ScenarioManager.get().getActiveSpecialScenarios().stream()
                .anyMatch(s -> s instanceof ScenarioRole);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onNerfStrength(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof LivingEntity))    return;
        if (event.getDamage() <= 0.0)                        return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(damager);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;

        double strengthPercent = uhcPlayer.getForcePercent();
        double criticPercent   = uhcPlayer.getForceCriticPercent();
        boolean rolePercentDriven = false;

        if (hasActiveRoleScenario()) {
            Role role = activeRoleOf(uhcPlayer);
            if (role == null) return;

            double roleStrengthPercent = role.getStrengthPercent();
            double roleCriticPercent   = role.getStrengthCriticPercent();
            if (roleStrengthPercent == 0 && roleCriticPercent == 0) return;

            strengthPercent = roleStrengthPercent;
            criticPercent   = roleCriticPercent;
            rolePercentDriven = true;
        }

        boolean hasStrength = damager.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE);
        boolean isCritical  = DamageMath.isCriticalDamage(damager);

        if (!hasStrength && !isCritical) return;

        double itemDamage    = DamageMath.getAttackDamageItem(damager.getItemInHand());
        double enchantDamage = DamageMath.getEnchantDamageItem(damager.getItemInHand());
        double strengthLevel = rolePercentDriven && hasStrength ? 1.0 : DamageMath.getStrengthLevel(damager);

        double strengthMultiplier = hasStrength ? 1.0 + 1.3 * strengthLevel * strengthPercent : 1.0;
        double criticMultiplier   = isCritical  ? 1.0 + 0.5 * criticPercent                  : 1.0;

        double total;

        if (isCritical && isPureCritical(itemDamage, strengthLevel, event.getDamage())) {
            total = itemDamage * strengthMultiplier * 0.5 * criticPercent;
        } else {
            total = itemDamage * strengthMultiplier * criticMultiplier + enchantDamage;
        }

        event.setDamage(total);
    }

    private boolean isPureCritical(double itemDamage, double strengthLevel, double rawDamage) {
        double vanillaCriticBonus = itemDamage * (1.0 + 1.3 * strengthLevel) * 0.5;
        return Math.abs(vanillaCriticBonus - rawDamage) < 0.01;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onNerfResistance(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
        if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) return;

        double resistancePercent = uhcPlayer.getResistancePercent();
        boolean rolePercentDriven = false;

        if (hasActiveRoleScenario()) {
            Role role = activeRoleOf(uhcPlayer);
            if (role == null) return;

            double roleResistancePercent = role.getResistancePercent();
            if (roleResistancePercent == 0) return;

            resistancePercent = roleResistancePercent;
            rolePercentDriven = true;
        }

        if (rolePercentDriven) {
            double preResistance = event.getDamage() - event.getDamage(DamageModifier.RESISTANCE);
            event.setDamage(DamageModifier.RESISTANCE, -0.2 * preResistance * resistancePercent);
        } else {
            event.setDamage(DamageModifier.RESISTANCE, event.getDamage(DamageModifier.RESISTANCE) * resistancePercent);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onArmorDurabilityNerf(PlayerItemDamageEvent event) {
        if (event.getItem() == null) return;
        if (!event.getPlayer().hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) return;
        if (!ARMOR_MATERIALS.contains(event.getItem().getType())) return;

        if (random.nextBoolean()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;

        UUID uuid = player.getUniqueId();
        float current = player.getWalkSpeed();

        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            Float applied = appliedWalkSpeeds.remove(uuid);
            if (applied != null && Math.abs(current - applied) < 0.001f
                    && Math.abs(applied - BASE_WALK_SPEED) > 0.001f) {
                player.setWalkSpeed(BASE_WALK_SPEED);
            }
            return;
        }

        Float previouslyApplied = appliedWalkSpeeds.get(uuid);
        boolean managed = Math.abs(current - BASE_WALK_SPEED) < 0.001f
                || (previouslyApplied != null && Math.abs(current - previouslyApplied) < 0.001f);
        if (!managed) return;

        double speedPercent = uhcPlayer.getSpeedPercent();
        boolean rolePercentDriven = false;

        if (hasActiveRoleScenario()) {
            Role role = activeRoleOf(uhcPlayer);
            if (role != null) {
                if (role.overridesSpeedPercent()) {
                    speedPercent = role.getSpeedPercent();
                }
                rolePercentDriven = true;
            }
        }

        int vanillaLevel = 1;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.SPEED)) {
                vanillaLevel = Math.max(vanillaLevel, effect.getAmplifier() + 1);
            }
        }
        int speedLevel = rolePercentDriven ? 1 : vanillaLevel;

        double vanillaMultiplier = 1.0 + VANILLA_SPEED_PER_LEVEL * vanillaLevel;
        double targetMultiplier = 1.0 + speedPercent * speedLevel;
        float newSpeed = (float) (BASE_WALK_SPEED * targetMultiplier / vanillaMultiplier);
        newSpeed = Math.max(0.1f, Math.min(1.0f, newSpeed));

        if (Math.abs(current - newSpeed) > 0.001f) {
            player.setWalkSpeed(newSpeed);
        }
        if (Math.abs(newSpeed - BASE_WALK_SPEED) > 0.001f) {
            appliedWalkSpeeds.put(uuid, newSpeed);
        } else {
            appliedWalkSpeeds.remove(uuid);
        }
    }
}

