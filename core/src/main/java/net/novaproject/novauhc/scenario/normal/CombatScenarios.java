package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.event.UhcGameEvents.UhcPvpEnableEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class CombatScenarios {

    private CombatScenarios() {
    }

    public static class SwitchArrow extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        private final Random random = new Random();

        @Var(name = "Chance d'échange", desc = "Chance en pourcentage d'échanger les positions quand une flèche touche.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int swapChance = 100;

        @Override public String getName() { return "SwitchArrow"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.ARROW_SWITCH, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (entity.getType() != EntityType.PLAYER) return;
            if (!(damager instanceof Arrow arrow)) return;
            if (!(arrow.getShooter() instanceof Player shooter)) return;
            if (random.nextInt(100) >= swapChance) return;
            Player victim = (Player) event.getEntity();
            Location victimLoc = victim.getLocation();
            Location shooterLoc = shooter.getLocation();
            victim.teleport(shooterLoc);
            shooter.teleport(victimLoc);
        }
    }

    public static class LongShoot extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Var(lang = ScenarioVarLang.class, nameKey = "LONGSHOOT_VAR_MIN_DISTANCE_NAME", descKey = "LONGSHOOT_VAR_MIN_DISTANCE_DESC", type = VariableType.INTEGER)
        private int minDistance = 30;

        @Var(name = "Palier 2", desc = "Distance du 2e palier de récompense (fer + or).", type = VariableType.INTEGER, min = 1)
        private int tier2Distance = 50;

        @Var(name = "Palier 3", desc = "Distance du 3e palier de récompense (fer + or + diamant).", type = VariableType.INTEGER, min = 1)
        private int tier3Distance = 100;

        @Var(name = "Palier 4", desc = "Distance du dernier palier de récompense.", type = VariableType.INTEGER, min = 1)
        private int tier4Distance = 200;

        @Override public String getName() { return "LongShot"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.LONG_SHOOT, player)
                    .replace("%distance%", String.valueOf(minDistance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (entity.getType() != EntityType.PLAYER) return;
            if (!(damager instanceof Projectile projectile)) return;
            if (!projectile.getType().equals(EntityType.ARROW)) return;
            if (!(projectile.getShooter() instanceof Player shooter)) return;
            if (shooter.equals(entity)) return;

            double distance = shooter.getLocation().distance(entity.getLocation());
            if (distance < minDistance) return;

            List<ItemStack> reward = new ArrayList<>();
            if (distance >= tier4Distance) {
                reward.add(new ItemStack(Material.DIAMOND, 5));
                reward.add(new ItemStack(Material.GOLD_INGOT, 3));
                reward.add(new ItemStack(Material.IRON_INGOT, 2));
            } else if (distance >= tier3Distance) {
                reward.add(new ItemStack(Material.IRON_INGOT, 1));
                reward.add(new ItemStack(Material.GOLD_INGOT, 1));
                reward.add(new ItemStack(Material.DIAMOND, 1));
            } else if (distance >= tier2Distance) {
                reward.add(new ItemStack(Material.IRON_INGOT, 1));
                reward.add(new ItemStack(Material.GOLD_INGOT, 1));
            } else {
                reward.add(new ItemStack(Material.IRON_INGOT, 1));
            }

            for (ItemStack item : reward) {
                PlayerUtils.giveOrDrop(shooter, item);
            }
            shooter.sendMessage(LangManager.get().get(ScenarioLang.LONGSHOOT_LONG_SHOT, shooter,
                    Map.of("%servertag%", Common.get().getServertag())));
        }
    }

    public static class Cripple extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Var(lang = ScenarioVarLang.class, nameKey = "CRIPPLE_VAR_WEAKNESS_DURATION_NAME", descKey = "CRIPPLE_VAR_WEAKNESS_DURATION_DESC", type = VariableType.TIME)
        private int weaknessDuration = 30;

        @Var(lang = ScenarioVarLang.class, nameKey = "CRIPPLE_VAR_WEAKNESS_LEVEL_NAME", descKey = "CRIPPLE_VAR_WEAKNESS_LEVEL_DESC", type = VariableType.INTEGER)
        private int weaknessLevel = 0;

        @Override public String getName() { return "Cripple"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.CRIPPLE, player)
                    .replace("%duration%", String.valueOf(weaknessDuration));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BONE); }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (entity instanceof Player player) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.WEAKNESS,
                            weaknessDuration * 20,
                            weaknessLevel,
                            false,
                            false
                    ));
                }
            }
        }
    }

    public static class SafeMiner extends Scenario implements Listener {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Var(lang = ScenarioVarLang.class, nameKey = "SAFEMINER_VAR_MAX_HEIGHT_NAME", descKey = "SAFEMINER_VAR_MAX_HEIGHT_DESC", type = VariableType.INTEGER)
        private int max_height = 32;

        @Var(lang = ScenarioVarLang.class, nameKey = "SAFEMINER_VAR_DISABLE_AT_PVP_NAME", descKey = "SAFEMINER_VAR_DISABLE_AT_PVP_DESC", type = VariableType.BOOLEAN)
        private boolean disable_at_pvp = true;

        @Var(name = "Réduction des dégâts", desc = "Part des dégâts de mobs annulée sous la hauteur max.", type = VariableType.PERCENTAGE, min = 0, max = 1)
        private double reductionPercent = 0.5;

        private boolean actived = true;

        @Override public String getName() { return "SafeMiner"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.SAFE_MINER, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_PICKAXE); }

        @Override
        public void onGameStart() {
            actived = true;
        }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!actived) return;
            if (!(entity instanceof Player player)) return;
            if (player.getLocation().getY() > max_height) return;

            if (isFireDamage(event.getCause())) {
                event.setCancelled(true);
                player.setFireTicks(0);
                return;
            }

            if (!isMobDamage(event)) return;

            event.setDamage(event.getDamage() * (1.0 - reductionPercent));
        }

        private boolean isFireDamage(DamageCause cause) {
            return cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK || cause == DamageCause.LAVA;
        }

        private boolean isMobDamage(EntityDamageEvent event) {
            if (!(event instanceof EntityDamageByEntityEvent byEntity)) return false;
            Entity damager = byEntity.getDamager();
            if (damager instanceof Player) return false;
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player) return false;
            return true;
        }

        @EventHandler
        public void onPvp(UhcPvpEnableEvent event) {
            if (!isActive()) return;
            if (!disable_at_pvp) return;
            actived = false;
        }
    }

    public static class BetaZombie extends Scenario {

        @Override
        public Family getFamily() { return Family.MOBS; }

        private final Random random = new Random();

        @Var(name = "Plumes maximum", desc = "Nombre maximum de plumes droppées par un zombie.", type = VariableType.INTEGER, min = 0)
        private int maxFeathers = 2;

        @Override public String getName() { return "BetaZombie"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.BETA_ZOMBIE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM).setDurability((short) 2); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (event.getEntityType() == EntityType.ZOMBIE) {
                event.getDrops().clear();
                int feathers = random.nextInt(maxFeathers + 1);
                if (feathers > 0) event.getDrops().add(new ItemStack(Material.FEATHER, feathers));
            }
        }
    }

    public static class BatRoulette extends Scenario {

        @Override
        public Family getFamily() { return Family.MOBS; }

        private final Random random = new Random();

        @Var(lang = ScenarioVarLang.class, nameKey = "BATROULETTE_VAR_CHANCE_NAME", descKey = "BATROULETTE_VAR_CHANCE_DESC", type = VariableType.PERCENTAGE)
        private int chance = 5;

        @Override public String getName() { return "BatRoulette"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.BAT_ROULETTE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MONSTER_EGG).setDurability((short) 65); }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            Location loc = entity.getLocation();
            if (entity instanceof Bat && killer != null) {
                if (random.nextInt(100) < chance) {
                    killer.setHealth(0);
                } else {
                    loc.getWorld().dropItemNaturally(loc, new ItemCreator(Material.GOLDEN_APPLE).getItemstack());
                }
            }
        }
    }
}
