package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class KillRewards {

    private KillRewards() {
    }

    public static class NoCleanUp extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        private static final String PROTECTION_KEY = "NoCleanUpProtection";

        @Var(name = "Durée de protection", desc = "Secondes d'invulnérabilité au PvP accordées après un kill.", type = VariableType.TIME, min = 0)
        private int protectionSeconds = 20;

        @Override public String getName() { return "NoCleanUp"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_CLEAN_UP, player)
                    .replace("%seconds%", String.valueOf(protectionSeconds));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_NUGGET); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (killer == null || killer.getPlayer() == null || protectionSeconds <= 0) return;
            Player player = killer.getPlayer();
            CooldownService.remove(player, PROTECTION_KEY);
            CooldownService.put(player, PROTECTION_KEY, protectionSeconds * 1000L);
            player.sendMessage(t(PROTECTED, player, Map.of("%seconds%", protectionSeconds)));
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            Player attacker = attackerOf(damager);

            if (attacker != null && CooldownService.get(attacker, PROTECTION_KEY) > 0) {
                CooldownService.remove(attacker, PROTECTION_KEY);
                attacker.sendMessage(t(PROTECTION_LOST, attacker));
            }

            if (attacker == null || !(entity instanceof Player victim)) return;
            if (CooldownService.get(victim, PROTECTION_KEY) > 0) event.setCancelled(true);
        }

        private Player attackerOf(Entity damager) {
            if (damager instanceof Player player) return player;
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) return shooter;
            return null;
        }

        private static final DynamicLang PROTECTED = DynamicLang.of("scenario.nocleanup.protected",
                "§6✦ §7Tu es protégé du PvP pendant §f%seconds%s§7.");

        private static final DynamicLang PROTECTION_LOST = DynamicLang.of("scenario.nocleanup.protection-lost",
                "§c✖ §7Tu as attaqué : ta protection est perdue.");
    }

    public static class Compensation extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Var(lang = ScenarioVarLang.class, nameKey = "COMPENSATION_VAR_HEARTS_PER_DEATH_NAME", descKey = "COMPENSATION_VAR_HEARTS_PER_DEATH_DESC", type = VariableType.DOUBLE)
        private double heartsPerDeath = 2.0;

        @Override public String getName() { return "Compensation"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.COMPENSATION, player)
                    .replace("%hearts%", String.valueOf(heartsPerDeath));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (uhcPlayer.getTeam().isPresent()) {
                UHCTeam team = uhcPlayer.getTeam().get();
                double healthToAdd = heartsPerDeath * 2;
                team.getPlayers().stream()
                        .filter(uhcP -> uhcP != uhcPlayer && uhcP.getPlayer() != null)
                        .forEach(uhcP -> uhcP.getPlayer().setMaxHealth(uhcP.getPlayer().getMaxHealth() + healthToAdd));
            }
        }
    }

    public static class Ninja extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Var(lang = ScenarioVarLang.class, nameKey = "NINJA_VAR_INVISIBILITY_DURATION_NAME", descKey = "NINJA_VAR_INVISIBILITY_DURATION_DESC", type = VariableType.TIME)
        private int invisibilityDuration = 10;

        @Var(lang = ScenarioVarLang.class, nameKey = "NINJA_VAR_INVISIBILITY_LEVEL_NAME", descKey = "NINJA_VAR_INVISIBILITY_LEVEL_DESC", type = VariableType.INTEGER)
        private int invisibilityLevel = 0;

        @Override public String getName() { return "Ninja"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NINJA, player)
                    .replace("%seconds%", String.valueOf(invisibilityDuration));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (killer != null) {
                Player killerPlayer = killer.getPlayer();
                killerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, invisibilityDuration * 20, invisibilityLevel));
                LangManager.get().send(ScenarioLang.NINJA_KILL_INVISIBILITY, killerPlayer);
            }
        }
    }

    public static class GoldenDrop extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENDROP_VAR_GOLDEN_APPLE_AMOUNT_NAME", descKey = "GOLDENDROP_VAR_GOLDEN_APPLE_AMOUNT_DESC", type = VariableType.INTEGER)
        private int goldenAppleAmount = 1;

        @Override public String getName() { return "Golden Drop"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.GOLDEN_DROP, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SPIDER_EYE); }

        @Override
        public void toggleActive() {
            super.toggleActive();
            GoldenHead goldenHeadScenario = ScenarioManager.get().getScenario(GoldenHead.class);
            if (isActive()) {
                if (goldenHeadScenario != null && !goldenHeadScenario.isActive()) {
                    goldenHeadScenario.toggleActive();
                }
            } else {
                if (goldenHeadScenario != null && goldenHeadScenario.isActive()) {
                    goldenHeadScenario.toggleActive();
                }
            }
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            ItemCreator golden = new ItemCreator(Material.GOLDEN_APPLE)
                    .setName(t(ScenarioLang.GOLDENHEAD_ITEM_NAME))
                    .setAmount(goldenAppleAmount);
            uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), golden.getItemstack());
        }
    }

    public static class WebCage extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Var(lang = ScenarioVarLang.class, nameKey = "WEBCAGE_VAR_SIZE_NAME", descKey = "WEBCAGE_VAR_SIZE_DESC", type = VariableType.INTEGER)
        public int size = 5;

        @Var(name = "Seulement après le PvP", desc = "Ne pose la cage qu'une fois le PvP activé.")
        private boolean onlyAfterPvp = true;

        @Var(name = "Matériau du cocon", desc = "Bloc posé autour du joueur mort.", type = VariableType.STRING)
        private String cageMaterial = "WEB";

        @Override public String getName() { return "WebCages"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.WEB_CAGE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WEB); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (onlyAfterPvp && UHCManager.get().getTimer() <= UHCManager.get().getPvpTimer()) return;
            Material cage = Material.getMaterial(cageMaterial);
            if (cage == null) cage = Material.WEB;
            for (Location block : getSphere(uhcPlayer.getPlayer().getLocation())) {
                Block b = block.getBlock();
                if (b.getType() == Material.AIR || b.getType() == Material.LONG_GRASS) {
                    b.setType(cage);
                }
            }
        }

        private List<Location> getSphere(Location centerBlock) {
            List<Location> circleBlocks = new ArrayList<>();

            int bX = centerBlock.getBlockX();
            int bY = centerBlock.getBlockY();
            int bZ = centerBlock.getBlockZ();

            for (int x = bX - size; x <= bX + size; x++) {
                for (int y = bY - size; y <= bY + size; y++) {
                    for (int z = bZ - size; z <= bZ + size; z++) {
                        double distance = ((bX - x) * (bX - x) + ((bZ - z) * (bZ - z)) + ((bY - y) * (bY - y)));
                        if (distance < size * size && !(distance < ((size - 1) * (size - 1)))) {
                            circleBlocks.add(new Location(centerBlock.getWorld(), x, y, z));
                        }
                    }
                }
            }
            return circleBlocks;
        }
    }

    public static class BuffKiller extends Scenario {

        @Override
        public Family getFamily() { return Family.COMBAT; }

        private final Random random = new Random();

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_SPEED_ACTIVE_NAME", descKey = "BUFFKILLER_VAR_SPEED_ACTIVE_DESC", type = VariableType.BOOLEAN)
        private boolean speedActive = true;

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_STRENGTH_ACTIVE_NAME", descKey = "BUFFKILLER_VAR_STRENGTH_ACTIVE_DESC", type = VariableType.BOOLEAN)
        private boolean strengthActive = true;

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_RESISTANCE_ACTIVE_NAME", descKey = "BUFFKILLER_VAR_RESISTANCE_ACTIVE_DESC", type = VariableType.BOOLEAN)
        private boolean resistanceActive = true;

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_FIRE_RESISTANCE_ACTIVE_NAME", descKey = "BUFFKILLER_VAR_FIRE_RESISTANCE_ACTIVE_DESC", type = VariableType.BOOLEAN)
        private boolean fireResistanceActive = true;

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_MIN_DURATION_NAME", descKey = "BUFFKILLER_VAR_MIN_DURATION_DESC", type = VariableType.TIME)
        private int minDuration = 5;

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_MAX_DURATION_NAME", descKey = "BUFFKILLER_VAR_MAX_DURATION_DESC", type = VariableType.TIME)
        private int maxDuration = 15;

        @Var(lang = ScenarioVarLang.class, nameKey = "BUFFKILLER_VAR_EFFECT_LEVEL_NAME", descKey = "BUFFKILLER_VAR_EFFECT_LEVEL_DESC", type = VariableType.INTEGER)
        private int effectLevel = 0;

        @Override public String getName() { return "Buff Killer"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.BUFF_KILLER, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WOOD_SWORD); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (killer == null) return;

            List<PotionEffectType> activeEffects = new ArrayList<>();
            if (speedActive) activeEffects.add(PotionEffectType.SPEED);
            if (strengthActive) activeEffects.add(PotionEffectType.INCREASE_DAMAGE);
            if (resistanceActive) activeEffects.add(PotionEffectType.DAMAGE_RESISTANCE);
            if (fireResistanceActive) activeEffects.add(PotionEffectType.FIRE_RESISTANCE);

            if (activeEffects.isEmpty()) return;

            PotionEffectType randomEffect = activeEffects.get(random.nextInt(activeEffects.size()));
            int duration = 20 * (minDuration + random.nextInt(Math.max(1, maxDuration - minDuration + 1)));
            killer.getPlayer().addPotionEffect(new PotionEffect(randomEffect, duration, effectLevel));
        }
    }
}
