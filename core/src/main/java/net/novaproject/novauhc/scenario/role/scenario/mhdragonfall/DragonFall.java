package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.Fatalis;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.WhiteFatalis;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.resistance.ResistanceProfile;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusEffect;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusFactory;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DragonFall extends ScenarioRole<DragonRole> {
    private final Map<UUID, Long> lavaDamageCooldown = new HashMap<>();
    private final StatusManager manager = StatusManager.get();
    @Override
    public String getName() {
        return "Monster Hunter : DragonRole Fall";
    }

    @Override
    public String getDescription() {
        return "Ce sous-scenrio apartien a la serie Monster Hunter UHC";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(UHCUtils.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFlZjVhNDRkZjQ0MmI3OGY1YTY4OTA1Y2U0ZDJjMjRmZTkyYmE4ZjE4ZWYwMWUwMzE4YjA1Zjg1MjJkMGNkMCJ9fX0="));
    }

    @Override
    public Camps[] getCamps() {
        return Tiers.values();
    }

    @Override
    public void setup() {
        super.setup();
        addRole(WhiteFatalis.class);
        addRole(Fatalis.class);
    }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        if (getRoleByUHCPlayer(killer).getEvolution() != null) {

        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }

    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        super.onHit(entity, damager, event);
        if (!(entity instanceof Player victim)) return;
        Player attacker = null;
        boolean isProjectile = false;

        if (damager instanceof Player) {
            attacker = (Player) damager;
        } else if (damager instanceof Arrow proj) {
            if (proj.getShooter() instanceof Player) {
                attacker = (Player) proj.getShooter();
                isProjectile = true;
            }
        }

        if (attacker == null) return;

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcAttacker = UHCPlayerManager.get().getPlayer(attacker);
        if (uhcVictim == null || uhcAttacker == null) return;

        DragonRole dragonVictim = getRoleByUHCPlayer(uhcVictim);
        DragonRole dragonAttacker = getRoleByUHCPlayer(uhcAttacker);
        if (dragonVictim == null || dragonAttacker == null) return;

        int currentHP = dragonVictim.getCurrentHP();
        if (currentHP <= 0) {
            event.setDamage(Integer.MAX_VALUE);
            return;
        }

        event.setDamage(0.0);
        victim.setNoDamageTicks(10);

        double attackerForce = dragonAttacker.getCurrentStrength();
        double defenderResistance = dragonVictim.getResistance();
        double critPercent = dragonAttacker.getCritDamage();
        double critChance = dragonAttacker.getCritChance();

        double rawAttack = Math.pow(attackerForce / 100.0D, 1.3D) * 45.0D;
        double defenseReduction = defenderResistance / (defenderResistance + 1200.0D);
        long baseDamage = Math.round(rawAttack * (1.0D - defenseReduction));

        double variation = 1.0D + ((Math.random() * 0.10D) - 0.05D);
        baseDamage = Math.round(baseDamage * variation);

        if (isProjectile) {
            double projectileMult = dragonAttacker.getProjectileMultiplier();
            baseDamage = Math.round(baseDamage * projectileMult);
        }


        ResistanceProfile resistProfile = dragonVictim.getResistanceProfile();
        Map<ElementType, Double> elements = dragonAttacker.getElementPowers();

        if (resistProfile != null && elements != null && !elements.isEmpty()) {
            double totalMultiplier = 0.0D;
            int count = 0;

            for (Map.Entry<ElementType, Double> entry : elements.entrySet()) {
                ElementType element = entry.getKey();
                double power = entry.getValue();
                double resistance = resistProfile.getResistance(element);
                double multiplier = (1.0D - resistance) * power;
                totalMultiplier += multiplier;
                count++;
            }

            double averageMultiplier = totalMultiplier / (double) count;
            baseDamage = Math.round(baseDamage * averageMultiplier);
        }


        String display = "§c-" + baseDamage;
        if ((int) (Math.random() * 100.0D) < critChance) {
            baseDamage = Math.round(baseDamage * (1.0D + critPercent / 100.0D));
            display = "§c-✦" + baseDamage + "✦";
        }

        if (elements != null && !elements.isEmpty()) {
            for (Map.Entry<ElementType, Double> entry : elements.entrySet()) {
                ElementType element = entry.getKey();
                double power = entry.getValue();
                double baseChance = dragonAttacker.getBlightChance(element);
                double resistance = dragonVictim.getResistanceProfile().getResistance(element);
                int duration = dragonAttacker.getBlightDuration(element);

                double finalChance = baseChance * power * (1.0D - resistance);
                if (finalChance < 0) finalChance = 0.0D;

                if (Math.random() * 100.0D < finalChance) {
                    StatusEffect effect = StatusFactory.create(element, victim, duration, getRoleByUHCPlayer(uhcVictim));
                    if (effect != null) {
                        StatusManager.get().applyEffect(victim, effect);
                    }
                }
            }
        }

        int finalHP = Math.max(0, currentHP - (int) baseDamage);
        dragonVictim.setCurrentHP(finalHP);

        UHCUtils.spawnFloatingDamage(attacker, display);
        UHCUtils.setRealHealth(dragonVictim.getMaxHP(), finalHP, victim, dragonVictim.getAbsortion());
    }


    @Override
    public void onDamage(Player player, EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        if (uhcVictim == null || !uhcVictim.isPlaying()) {
            event.setCancelled(true);
            return;
        }


        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FALL ||
                cause == EntityDamageEvent.DamageCause.DROWNING ||
                cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true);
            return;
        }

        UUID uuid = victim.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lavaDamageCooldown.getOrDefault(uuid, 0L);

        if (cause == EntityDamageEvent.DamageCause.LAVA ||
                cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

            if (now - last >= 500) {
                DragonRole dragonVictim = getRoleByUHCPlayer(uhcVictim);
                if (dragonVictim == null) {
                    event.setCancelled(true);
                    return;
                }

                int damage = (cause == EntityDamageEvent.DamageCause.LAVA) ? 100 : 20;

                int newHP = Math.max(0, dragonVictim.getCurrentHP() - damage);
                dragonVictim.setCurrentHP(newHP);
                lavaDamageCooldown.put(uuid, now);

                UHCUtils.spawnFloatingDamage(victim, "§4✦ §c-" + damage + " §4✦");
                victim.playSound(victim.getLocation(), Sound.NOTE_PLING, 1f, 1f);

                UHCUtils.setRealHealth(dragonVictim.getMaxHP(), newHP, victim, dragonVictim.getAbsortion());

                if (newHP <= 0) {
                    event.setDamage(Integer.MAX_VALUE);
                }

            }


            return;
        }


        ShortCooldownManager.startCombat(uuid, uuid);
    }


}
