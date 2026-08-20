package net.novaproject.ultimate.australia;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Australia extends Scenario {

    private static final int KANGOUROU = 0;
    private static final int EMEU = 1;
    private static final int SERPENT = 2;
    private static final int ARAIGNEE = 3;
    private static final int CROCODILE = 4;
    private static final int KOALA = 5;
    private static final int DINGO = 6;

    private static final DynamicLang DESC = DynamicLang.of("mode.australia.desc",
            "§7Chaque joueur devient un §eanimal australien §7différent — §eKangourou§7, §eÉmeu§7, §eSerpent§7, §eAraignée§7, §eCrocodile§7, §eKoala §7ou §eDingo §7— et hérite de son pouvoir.");

    private static final DynamicLang ASSIGNED = DynamicLang.of("mode.australia.assigned",
            "§6✦ §7Ton animal : §e%animal% §7— %pouvoir%§7.");

    private static final DynamicLang[] ANIMAL_NAMES = {
            DynamicLang.of("mode.australia.name.kangourou", "Kangourou"),
            DynamicLang.of("mode.australia.name.emeu", "Émeu"),
            DynamicLang.of("mode.australia.name.serpent", "Serpent"),
            DynamicLang.of("mode.australia.name.araignee", "Araignée"),
            DynamicLang.of("mode.australia.name.crocodile", "Crocodile"),
            DynamicLang.of("mode.australia.name.koala", "Koala"),
            DynamicLang.of("mode.australia.name.dingo", "Dingo")
    };

    private static final DynamicLang[] ANIMAL_POWERS = {
            DynamicLang.of("mode.australia.power.kangourou", "§7saut amélioré et aucun dégât de chute"),
            DynamicLang.of("mode.australia.power.emeu", "§7course permanente"),
            DynamicLang.of("mode.australia.power.serpent", "§7tes coups peuvent empoisonner ta cible"),
            DynamicLang.of("mode.australia.power.araignee", "§7tes coups engluent ta cible et la ralentissent"),
            DynamicLang.of("mode.australia.power.crocodile", "§7respiration aquatique, et tu frappes plus fort dans l'eau"),
            DynamicLang.of("mode.australia.power.koala", "§7tu récupères de la vie tant que tu restes immobile"),
            DynamicLang.of("mode.australia.power.dingo", "§7ta boussole piste l'adversaire le plus proche")
    };

    @Var(name = "Niveau de Saut (Kangourou)", desc = "Niveau de l'effet Saut permanent du Kangourou.", type = VariableType.INTEGER, min = 1)
    private int kangourouJumpLevel = 3;

    @Var(name = "Niveau de Rapidité (Émeu)", desc = "Niveau de l'effet Rapidité permanent de l'Émeu.", type = VariableType.INTEGER, min = 1)
    private int emeuSpeedLevel = 2;

    @Var(name = "Chance d'empoisonnement (Serpent)", desc = "Chance qu'un coup du Serpent empoisonne sa cible.", type = VariableType.PERCENTAGE, min = 0, max = 100)
    private int serpentPoisonChance = 35;

    @Var(name = "Durée de Poison (Serpent)", desc = "Durée du Poison infligé par le Serpent.", type = VariableType.TIME, min = 1)
    private int serpentPoisonSec = 5;

    @Var(name = "Niveau de Poison (Serpent)", desc = "Niveau du Poison infligé par le Serpent.", type = VariableType.INTEGER, min = 1)
    private int serpentPoisonLevel = 1;

    @Var(name = "Durée de Lenteur (Araignée)", desc = "Durée de la Lenteur infligée par l'Araignée.", type = VariableType.TIME, min = 1)
    private int araigneeSlowSec = 4;

    @Var(name = "Niveau de Lenteur (Araignée)", desc = "Niveau de la Lenteur infligée par l'Araignée.", type = VariableType.INTEGER, min = 1)
    private int araigneeSlowLevel = 2;

    @Var(name = "Multiplicateur de dégâts dans l'eau (Crocodile)", desc = "Facteur appliqué aux dégâts du Crocodile lorsqu'il attaque depuis l'eau.", type = VariableType.DOUBLE, min = 1)
    private double crocodileWaterDamageMultiplier = 1.5;

    @Var(name = "Immobilité requise (Koala)", desc = "Temps sans bouger avant que le Koala commence à récupérer de la vie.", type = VariableType.TIME, min = 1)
    private int koalaRestSec = 5;

    @Var(name = "Vie rendue par seconde (Koala)", desc = "Points de vie rendus chaque seconde au Koala immobile (2 = 1 cœur).", type = VariableType.DOUBLE, min = 0)
    private double koalaHealPerSec = 1.0;

    @Var(name = "Portée de pistage (Dingo)", desc = "Rayon en blocs dans lequel la boussole du Dingo piste l'adversaire le plus proche.", type = VariableType.DOUBLE, min = 1)
    private double dingoTrackRadius = 60.0;

    private final Map<UUID, Integer> animals = new HashMap<>();
    private final Map<UUID, Long> lastMove = new HashMap<>();
    private final List<Integer> pool = new ArrayList<>();

    @Override
    public String getName() {
        return "Australia";
    }

    @Override
    public String getDescription(Player player) {
        return t(DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.RABBIT_FOOT);
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

        if (pool.isEmpty()) {
            for (int i = 0; i < ANIMAL_NAMES.length; i++) pool.add(i);
            Collections.shuffle(pool);
        }

        int animal = pool.remove(pool.size() - 1);
        animals.put(player.getUniqueId(), animal);
        lastMove.put(player.getUniqueId(), System.currentTimeMillis());

        if (animal == DINGO) player.getInventory().addItem(new ItemStack(Material.COMPASS));

        LangManager.get().send(ASSIGNED, player, Map.of(
                "%animal%", t(ANIMAL_NAMES[animal], player),
                "%pouvoir%", t(ANIMAL_POWERS[animal], player)
        ));
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;
        Integer animal = animals.get(p.getUniqueId());
        if (animal == null) return;

        switch (animal) {
            case KANGOUROU -> UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.JUMP, 80, Math.max(0, kangourouJumpLevel - 1), false, false)
            }, p);
            case EMEU -> UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, 80, Math.max(0, emeuSpeedLevel - 1), false, false)
            }, p);
            case CROCODILE -> UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, false, false)
            }, p);
            case KOALA -> healResting(p);
            case DINGO -> trackNearest(p);
            default -> {
            }
        }
    }

    private void healResting(Player player) {
        Long since = lastMove.get(player.getUniqueId());
        if (since == null || System.currentTimeMillis() - since < koalaRestSec * 1000L) return;
        if (player.getHealth() >= player.getMaxHealth()) return;
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + koalaHealPerSec));
    }

    private void trackNearest(Player player) {
        Player nearest = nearestEnemy(player, dingoTrackRadius);
        if (nearest == null) return;
        player.setCompassTarget(nearest.getLocation());
    }

    private Player nearestEnemy(Player player, double radius) {
        UHCPlayer self = UHCPlayerManager.get().getPlayer(player);
        UHCTeam team = self == null ? null : self.getTeam().orElse(null);
        double best = radius * radius;
        Player nearest = null;

        for (UHCPlayer other : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player target = other.getPlayer();
            if (target == null || !target.isOnline()) continue;
            if (target.getUniqueId().equals(player.getUniqueId())) continue;
            if (team != null && other.getTeam().filter(team::equals).isPresent()) continue;
            if (!target.getWorld().equals(player.getWorld())) continue;
            double distance = target.getLocation().distanceSquared(player.getLocation());
            if (distance >= best) continue;
            best = distance;
            nearest = target;
        }
        return nearest;
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive()) return;
        Integer animal = animals.get(player.getUniqueId());
        if (animal == null || animal != KOALA) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;
        lastMove.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        if (!(dammager instanceof Player attacker)) return;
        Integer animal = animals.get(attacker.getUniqueId());
        if (animal == null) return;

        if (animal == CROCODILE) {
            if (inWater(attacker)) event.setDamage(event.getDamage() * crocodileWaterDamageMultiplier);
            return;
        }
        if (!(entity instanceof LivingEntity target)) return;

        if (animal == SERPENT && ThreadLocalRandom.current().nextInt(100) < serpentPoisonChance) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, serpentPoisonSec * 20, Math.max(0, serpentPoisonLevel - 1), false, true), true);
            return;
        }
        if (animal == ARAIGNEE) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, araigneeSlowSec * 20, Math.max(0, araigneeSlowLevel - 1), false, true), true);
        }
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!isActive()) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(entity instanceof Player player)) return;
        Integer animal = animals.get(player.getUniqueId());
        if (animal == null || animal != KANGOUROU) return;
        event.setCancelled(true);
    }

    private boolean inWater(Player player) {
        Material type = player.getLocation().getBlock().getType();
        return type == Material.WATER || type == Material.STATIONARY_WATER;
    }

    @Override
    public void onStop() {
        super.onStop();
        animals.clear();
        lastMove.clear();
        pool.clear();
    }
}
