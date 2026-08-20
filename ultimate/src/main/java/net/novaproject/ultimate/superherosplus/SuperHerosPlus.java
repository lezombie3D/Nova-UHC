package net.novaproject.ultimate.superherosplus;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SuperHerosPlus extends Scenario {

    private static final DynamicLang DESC = DynamicLang.of("mode.superherosplus.desc",
            "§7Chaque joueur reçoit §e%count% §7pouvoirs positifs aléatoires, tous différents.");

    private static final int POWER_SPEED = 0;
    private static final int POWER_STRENGTH = 1;
    private static final int POWER_RESISTANCE = 2;
    private static final int POWER_JUMP = 3;
    private static final int POWER_EXTRA_HEALTH = 4;

    private final Map<UUID, List<Integer>> powers = new HashMap<>();
    private final Map<UUID, Double> baseMaxHealth = new HashMap<>();

    @Var(name = "Nombre de pouvoirs", desc = "Pouvoirs distincts tirés par joueur.", type = VariableType.INTEGER, min = 1, max = 5)
    private int powerCount = 2;

    @Var(name = "Speed", desc = "Met la vitesse dans le pool de pouvoirs.", type = VariableType.BOOLEAN)
    private boolean enableSpeed = true;

    @Var(name = "Strength", desc = "Met la force dans le pool de pouvoirs.", type = VariableType.BOOLEAN)
    private boolean enableDamage = true;

    @Var(name = "Resistance", desc = "Met la résistance dans le pool de pouvoirs.", type = VariableType.BOOLEAN)
    private boolean enableResistance = true;

    @Var(name = "Jump", desc = "Met le boost de saut dans le pool de pouvoirs.", type = VariableType.BOOLEAN)
    private boolean enableJump = true;

    @Var(name = "Extra health", desc = "Met les coeurs bonus dans le pool de pouvoirs.", type = VariableType.BOOLEAN)
    private boolean enableExtraHealth = true;

    @Var(name = "Speed amplifier", desc = "Niveau de la potion de vitesse.", type = VariableType.INTEGER, min = 0)
    private int speedAmplifier = 1;

    @Var(name = "Strength amplifier", desc = "Niveau de la potion de force.", type = VariableType.INTEGER, min = 0)
    private int damageAmplifier = 0;

    @Var(name = "Resistance amplifier", desc = "Niveau de la potion de résistance.", type = VariableType.INTEGER, min = 0)
    private int resistanceAmplifier = 1;

    @Var(name = "Jump amplifier", desc = "Niveau de la potion de saut.", type = VariableType.INTEGER, min = 0)
    private int jumpAmplifier = 3;

    @Var(name = "Fire resistance amplifier", desc = "Niveau de la résistance au feu jointe au saut.", type = VariableType.INTEGER, min = 0)
    private int fireResistanceAmplifier = 1;

    @Var(name = "Extra max health", desc = "Vie maximale du pouvoir coeurs bonus.", type = VariableType.DOUBLE, min = 20)
    private double extraMaxHealth = 40;

    @Var(name = "Extra Absorption", desc = "Absorption bonus sur pomme d'or.", type = VariableType.INTEGER, min = 0)
    private int absorptionExtraHealth = 1;

    @Var(name = "Extra Regeneration", desc = "Régénération bonus sur pomme d'or.", type = VariableType.INTEGER, min = 0)
    private int regenerationExtraHealth = 1;

    @Var(name = "Regeneration duration", desc = "Durée de la régénération bonus.", type = VariableType.TIME, min = 1)
    private int regenerationDurationExtraHealth = 1;

    @Var(name = "Absorption duration", desc = "Durée de l'absorption bonus.", type = VariableType.TIME, min = 1)
    private int absorptionDurationExtraHealth = 1;

    @Var(name = "Multiplicateur tête d'or", desc = "Multiplie durée et niveau des bonus pomme d'or quand Golden Head est actif.", type = VariableType.INTEGER, min = 1)
    private int goldenHeadMultiplier = 2;

    @Override
    public String getName() {
        return "SuperHeros+";
    }

    @Override
    public String getDescription(Player player) {
        return t(DESC, player, Map.of("%count%", powerCount));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BEACON);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;
        if (UHCManager.get().getTeam_size() != 1 && uhcPlayer.getTeam().isPresent()) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            player.teleport(location);
        }
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        List<Integer> pool = new ArrayList<>();
        if (enableSpeed) pool.add(POWER_SPEED);
        if (enableDamage) pool.add(POWER_STRENGTH);
        if (enableResistance) pool.add(POWER_RESISTANCE);
        if (enableJump) pool.add(POWER_JUMP);
        if (enableExtraHealth) pool.add(POWER_EXTRA_HEALTH);
        Collections.shuffle(pool);

        List<Integer> drawn = new ArrayList<>(pool.subList(0, Math.min(powerCount, pool.size())));
        powers.put(player.getUniqueId(), drawn);

        if (drawn.contains(POWER_EXTRA_HEALTH)) {
            baseMaxHealth.put(player.getUniqueId(), player.getMaxHealth());
            player.setMaxHealth(extraMaxHealth);
            player.setHealth(player.getMaxHealth());
        }
    }

    @Override
    public void onSec(Player player) {
        if (!isActive()) return;

        List<Integer> drawn = powers.get(player.getUniqueId());
        if (drawn == null || drawn.isEmpty()) return;

        List<PotionEffect> effects = new ArrayList<>();
        for (int power : drawn) {
            switch (power) {
                case POWER_SPEED -> effects.add(new PotionEffect(PotionEffectType.SPEED, 80, Math.max(0, speedAmplifier), false, false));
                case POWER_STRENGTH -> effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, Math.max(0, damageAmplifier), false, false));
                case POWER_RESISTANCE -> effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, Math.max(0, resistanceAmplifier), false, false));
                case POWER_JUMP -> {
                    effects.add(new PotionEffect(PotionEffectType.JUMP, 80, Math.max(0, jumpAmplifier), false, false));
                    effects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, Math.max(0, fireResistanceAmplifier), false, false));
                }
            }
        }

        if (!effects.isEmpty()) {
            UHCUtils.applyInfiniteEffects(effects.toArray(new PotionEffect[0]), player);
        }
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!isActive()) return;
        if (!(entity instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        List<Integer> drawn = powers.get(player.getUniqueId());
        if (drawn != null && drawn.contains(POWER_JUMP)) event.setCancelled(true);
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (!isActive()) return;
        if (item.getType() != Material.GOLDEN_APPLE) return;

        List<Integer> drawn = powers.get(player.getUniqueId());
        if (drawn == null || !drawn.contains(POWER_EXTRA_HEALTH)) return;

        int multiplier = ScenarioManager.get().getScenario(GoldenHead.class).isActive() ? goldenHeadMultiplier : 1;

        for (PotionEffect e : player.getActivePotionEffects()) {
            if (e.getType().equals(PotionEffectType.REGENERATION) || e.getType().equals(PotionEffectType.ABSORPTION))
                player.removePotionEffect(e.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                20 * absorptionDurationExtraHealth * multiplier, absorptionExtraHealth * multiplier, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                20 * regenerationDurationExtraHealth * multiplier, regenerationExtraHealth * multiplier, false, true));
    }

    @Override
    public void onStop() {
        super.onStop();
        for (Map.Entry<UUID, Double> entry : baseMaxHealth.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) player.setMaxHealth(entry.getValue());
        }
        baseMaxHealth.clear();
        powers.clear();
    }
}
