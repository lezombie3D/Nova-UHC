package net.novaproject.ultimate.smashbrothers;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SmashBrothers extends Scenario {

    private static final int MARIO = 0;
    private static final int SONIC = 1;
    private static final int KIRBY = 2;
    private static final int PIT = 3;
    private static final int META_KNIGHT = 4;
    private static final int DONKEY_KONG = 5;
    private static final int PIKACHU = 6;
    private static final int LINK = 7;

    private static final DynamicLang DESC = DynamicLang.of("mode.smashbrothers.desc",
            "§7Chaque joueur reçoit l'une des huit classes — §cMario§7, §9Sonic§7, §dKirby§7, §fPit§7, §5Meta Knight§7, §6Donkey Kong§7, §ePikachu §7ou §aLink §7— et le pouvoir qui va avec.");

    private static final DynamicLang ASSIGNED = DynamicLang.of("mode.smashbrothers.assigned",
            "§b★ §7Ta classe : §e%classe% §7— %pouvoir%§7.");

    private static final DynamicLang[] CLASS_NAMES = {
            DynamicLang.of("mode.smashbrothers.name.mario", "Mario"),
            DynamicLang.of("mode.smashbrothers.name.sonic", "Sonic"),
            DynamicLang.of("mode.smashbrothers.name.kirby", "Kirby"),
            DynamicLang.of("mode.smashbrothers.name.pit", "Pit"),
            DynamicLang.of("mode.smashbrothers.name.metaknight", "Meta Knight"),
            DynamicLang.of("mode.smashbrothers.name.donkeykong", "Donkey Kong"),
            DynamicLang.of("mode.smashbrothers.name.pikachu", "Pikachu"),
            DynamicLang.of("mode.smashbrothers.name.link", "Link")
    };

    private static final DynamicLang[] CLASS_POWERS = {
            DynamicLang.of("mode.smashbrothers.power.mario", "§7immunité au feu, et tes coups enflamment ta cible"),
            DynamicLang.of("mode.smashbrothers.power.sonic", "§7rapidité permanente"),
            DynamicLang.of("mode.smashbrothers.power.kirby", "§7tu absorbes une partie des dégâts que tu infliges et tu te soignes"),
            DynamicLang.of("mode.smashbrothers.power.pit", "§7saut amélioré et aucun dégât de chute"),
            DynamicLang.of("mode.smashbrothers.power.metaknight", "§7tes coups à l'épée frappent plus fort"),
            DynamicLang.of("mode.smashbrothers.power.donkeykong", "§7force permanente et tes coups projettent ta cible"),
            DynamicLang.of("mode.smashbrothers.power.pikachu", "§7tes coups peuvent déclencher un éclair foudroyant"),
            DynamicLang.of("mode.smashbrothers.power.link", "§7tes flèches infligent des dégâts accrus")
    };

    @Var(name = "Durée d'embrasement (Mario)", desc = "Durée pendant laquelle la cible frappée par Mario brûle.", type = VariableType.TIME, min = 1)
    private int marioIgniteSec = 4;

    @Var(name = "Niveau de Rapidité (Sonic)", desc = "Niveau de l'effet Rapidité permanent de Sonic.", type = VariableType.INTEGER, min = 1)
    private int sonicSpeedLevel = 2;

    @Var(name = "Absorption de vie (Kirby)", desc = "Part des dégâts infligés que Kirby récupère en points de vie.", type = VariableType.PERCENTAGE, min = 1)
    private int kirbyLifestealPercent = 25;

    @Var(name = "Niveau de Saut (Pit)", desc = "Niveau de l'effet Saut permanent de Pit.", type = VariableType.INTEGER, min = 1)
    private int pitJumpLevel = 2;

    @Var(name = "Multiplicateur d'épée (Meta Knight)", desc = "Multiplicateur appliqué aux dégâts de Meta Knight lorsqu'il frappe avec une épée.", type = VariableType.DOUBLE, min = 1)
    private double metaKnightSwordMultiplier = 1.25;

    @Var(name = "Niveau de Force (Donkey Kong)", desc = "Niveau de l'effet Force permanent de Donkey Kong.", type = VariableType.INTEGER, min = 1)
    private int donkeyKongStrengthLevel = 1;

    @Var(name = "Projection (Donkey Kong)", desc = "Distance en blocs sur laquelle Donkey Kong projette la cible qu'il frappe.", type = VariableType.DOUBLE, min = 1)
    private double donkeyKongPushBlocks = 4.0;

    @Var(name = "Élévation (Donkey Kong)", desc = "Impulsion verticale appliquée par la projection de Donkey Kong.", type = VariableType.DOUBLE, min = 0)
    private double donkeyKongVerticalBoost = 0.35;

    @Var(name = "Chance de foudre (Pikachu)", desc = "Chance qu'un coup de Pikachu déclenche un éclair.", type = VariableType.PERCENTAGE, min = 1)
    private int pikachuLightningChance = 20;

    @Var(name = "Dégâts de foudre (Pikachu)", desc = "Dégâts ajoutés au coup de Pikachu lorsque l'éclair se déclenche (2 = 1 cœur).", type = VariableType.DOUBLE, min = 1)
    private double pikachuLightningDamage = 4.0;

    @Var(name = "Recharge de foudre (Pikachu)", desc = "Temps de recharge entre deux éclairs de Pikachu.", type = VariableType.TIME, min = 1)
    private int pikachuLightningCooldownSec = 20;

    @Var(name = "Multiplicateur de flèche (Link)", desc = "Multiplicateur appliqué aux dégâts des flèches tirées par Link.", type = VariableType.DOUBLE, min = 1)
    private double linkArrowMultiplier = 1.5;

    private final Map<UUID, Integer> classes = new HashMap<>();

    @Override
    public String getName() {
        return "Super Smash Brothers";
    }

    @Override
    public String getDescription(Player player) {
        return t(DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_SWORD);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() > 1 && up.getTeam().isPresent()) {
            UHCTeamManager.get().scatterTeam(up, teamloc);
            return;
        }
        if (up.getPlayer() != null) up.getPlayer().teleport(location);
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;
        int smashClass = pickClass(player);
        classes.put(player.getUniqueId(), smashClass);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.4f);
        LangManager.get().send(ASSIGNED, player, Map.of(
                "%classe%", t(CLASS_NAMES[smashClass], player),
                "%pouvoir%", t(CLASS_POWERS[smashClass], player)
        ));
    }

    private int pickClass(Player player) {
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < CLASS_NAMES.length; i++) pool.add(i);

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        UHCTeam team = uhcPlayer == null ? null : uhcPlayer.getTeam().orElse(null);
        if (team != null) {
            List<Integer> free = new ArrayList<>(pool);
            for (UHCPlayer member : team.getPlayers()) {
                Integer taken = classes.get(member.getUuid());
                if (taken != null) free.remove(taken);
            }
            if (!free.isEmpty()) pool = free;
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;
        Integer smashClass = classes.get(p.getUniqueId());
        if (smashClass == null) return;

        PotionEffect effect = switch (smashClass) {
            case MARIO -> new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false);
            case SONIC -> new PotionEffect(PotionEffectType.SPEED, 80, Math.max(0, sonicSpeedLevel - 1), false, false);
            case PIT -> new PotionEffect(PotionEffectType.JUMP, 80, Math.max(0, pitJumpLevel - 1), false, false);
            case DONKEY_KONG -> new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, Math.max(0, donkeyKongStrengthLevel - 1), false, false);
            default -> null;
        };
        if (effect == null) return;

        UHCUtils.applyInfiniteEffects(new PotionEffect[]{effect}, p);
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        if (event.isCancelled()) return;

        if (dammager instanceof Arrow arrow) {
            if (!(arrow.getShooter() instanceof Player shooter)) return;
            Integer shooterClass = classes.get(shooter.getUniqueId());
            if (shooterClass == null || shooterClass != LINK) return;
            event.setDamage(event.getDamage() * linkArrowMultiplier);
            return;
        }
        if (dammager instanceof Projectile) return;
        if (!(dammager instanceof Player attacker)) return;

        Integer attackerClass = classes.get(attacker.getUniqueId());
        if (attackerClass == null) return;

        switch (attackerClass) {
            case MARIO -> entity.setFireTicks(Math.max(entity.getFireTicks(), marioIgniteSec * 20));
            case KIRBY -> {
                double healed = event.getDamage() * kirbyLifestealPercent / 100.0;
                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + healed));
            }
            case META_KNIGHT -> {
                if (!holdsSword(attacker)) return;
                event.setDamage(event.getDamage() * metaKnightSwordMultiplier);
            }
            case DONKEY_KONG -> {
                if (!(entity instanceof Player target)) return;
                PlayerUtils.knockbackFrom(target, attacker.getLocation(), donkeyKongPushBlocks, donkeyKongVerticalBoost);
            }
            case PIKACHU -> {
                if (ThreadLocalRandom.current().nextInt(100) >= pikachuLightningChance) return;
                if (CooldownService.get(attacker, "SmashBrothersPikachuFoudre") > 0) return;
                CooldownService.put(attacker, "SmashBrothersPikachuFoudre", pikachuLightningCooldownSec * 1000L);
                entity.getWorld().strikeLightningEffect(entity.getLocation());
                event.setDamage(event.getDamage() + pikachuLightningDamage);
            }
            default -> {
            }
        }
    }

    private boolean holdsSword(Player player) {
        ItemStack item = player.getItemInHand();
        return item != null && item.getType().name().endsWith("_SWORD");
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!isActive()) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(entity instanceof Player player)) return;
        Integer smashClass = classes.get(player.getUniqueId());
        if (smashClass == null || smashClass != PIT) return;
        event.setCancelled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        classes.clear();
    }
}
