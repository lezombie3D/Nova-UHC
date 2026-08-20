package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class MiningScenarios {

    public static final List<Material> ORE_TYPES = List.of(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    private static final Random TOOL_RANDOM = new Random();

    private MiningScenarios() {
    }

    public static void damageTool(Player player, ItemStack tool) {
        if (tool == null || tool.getType().getMaxDurability() <= 0) return;

        int unbreaking = tool.getEnchantmentLevel(Enchantment.DURABILITY);
        if (unbreaking > 0 && TOOL_RANDOM.nextInt(unbreaking + 1) != 0) return;

        short next = (short) (tool.getDurability() + 1);
        if (next >= tool.getType().getMaxDurability()) {
            player.setItemInHand(null);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 1f, 1f);
            return;
        }
        tool.setDurability(next);
        player.setItemInHand(tool);
    }

    public static class BlackArrow extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        private final Random random = new Random();

        @Var(name = "Flèches minimum", desc = "Nombre minimum de flèches droppées par un bloc de charbon.", type = VariableType.INTEGER, min = 0)
        private int minArrows = 1;

        @Var(name = "Flèches maximum", desc = "Nombre maximum de flèches droppées par un bloc de charbon.", type = VariableType.INTEGER, min = 0)
        private int maxArrows = 1;

        @Override public String getName() { return "BlackArrow"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.BLACK_ARROW, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (block.getType() != Material.COAL_ORE) return;
            int low = Math.min(minArrows, maxArrows);
            int amount = low + random.nextInt(Math.max(1, Math.abs(maxArrows - minArrows) + 1));
            if (amount <= 0) return;
            Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.ARROW, amount));
        }
    }

    public static class BloodDiamonds extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Var(lang = ScenarioVarLang.class, nameKey = "BLOODDIAMONDS_VAR_DAMAGE_AMOUNT_NAME", descKey = "BLOODDIAMONDS_VAR_DAMAGE_AMOUNT_DESC", type = VariableType.DOUBLE)
        private double damageAmount = 1.0;

        @Var(name = "Minerai concerné", desc = "Minerai qui blesse le joueur quand il le mine.", type = VariableType.STRING)
        private String targetBlock = "DIAMOND_ORE";

        @Override public String getName() { return "BloodDiamonds"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.BLOOD_DIAMONDS, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            Material target = Material.getMaterial(targetBlock);
            if (!isActive() || target == null || block.getType() != target) return;
            player.damage(damageAmount);
        }
    }

    public static class CobbleUnified extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Override public String getName() { return "CobbleUnified"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.COBBLE_UNIFIED, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COBBLESTONE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.STONE) return;

            byte data = block.getData();
            if (data == 0) return;

            ItemStack tool = player.getItemInHand();
            if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

            event.setCancelled(true);
            block.setType(Material.AIR);
            damageTool(player, tool);

            Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.COBBLESTONE, 1));
        }
    }

    public static class DeathEmerauld extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        private final Random random = new Random();

        @Var(lang = ScenarioVarLang.class, nameKey = "DEATHEMERAULD_VAR_DAMAGE_AMOUNT_NAME", descKey = "DEATHEMERAULD_VAR_DAMAGE_AMOUNT_DESC", type = VariableType.DOUBLE)
        private double damageAmount = 4.0;

        @Var(lang = ScenarioVarLang.class, nameKey = "DEATHEMERAULD_VAR_TARGET_BLOCK_NAME", descKey = "DEATHEMERAULD_VAR_TARGET_BLOCK_DESC", type = VariableType.STRING)
        private String targetBlock = "EMERALD_ORE";

        @Override public String getName() { return "DeathEmerauld"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.DEATH_EMERAULD, player)
                    .replace("%block%", String.valueOf(targetBlock))
                    .replace("%damage%", String.valueOf(damageAmount));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EMERALD); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            Material target = Material.getMaterial(targetBlock);
            if (target == null || block.getType() != target) return;

            List<UHCPlayer> players = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
            if (players.isEmpty()) return;

            UHCPlayer chosen = players.get(random.nextInt(players.size()));
            chosen.getPlayer().damage(damageAmount);
        }
    }

    public static class Timber extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Var(name = "Abattre les feuilles", desc = "Fait aussi tomber les feuilles reliées à l'arbre.", type = VariableType.BOOLEAN)
        private boolean breakLeaves = false;

        @Var(name = "Blocs maximum", desc = "Nombre maximum de blocs abattus en chaîne par un arbre.", type = VariableType.INTEGER, min = 1)
        private int maxBlocks = 512;

        @Override public String getName() { return "Timber"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.TIMBER, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LOG); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isLog(block)) return;
            if (Common.get().getArena().getPVP()) return;
            for (Block log : collectTree(event.getBlock())) {
                log.breakNaturally();
            }
        }

        private static boolean isLog(Block block) {
            return block.getType() == Material.LOG || block.getType() == Material.LOG_2;
        }

        private static boolean isLeaves(Block block) {
            return block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2;
        }

        private boolean isChainable(Block block) {
            return isLog(block) || (breakLeaves && isLeaves(block));
        }

        // ponytail: parcours itératif borné à maxBlocks — l'ancienne version récursive sur un
        // voisinage 3x3x3 débordait la pile sur une grande forêt. Relever la borne si un arbre
        // custom dépasse 512 bûches.
        private Set<Block> collectTree(Block origin) {
            Set<Location> visited = new HashSet<>();
            Set<Block> logs = new HashSet<>();
            Deque<Block> queue = new ArrayDeque<>();
            queue.add(origin);

            while (!queue.isEmpty() && logs.size() < maxBlocks) {
                Block current = queue.poll();
                if (!visited.add(current.getLocation())) continue;
                if (!isChainable(current)) continue;
                logs.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            queue.add(current.getRelative(dx, dy, dz));
                        }
                    }
                }
            }
            return logs;
        }
    }
}
