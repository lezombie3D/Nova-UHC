package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;

public final class PlayerBoosts {

    private PlayerBoosts() {
    }

    public static class CatEye extends Scenario {

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "CatEye"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.CAT_EYE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EYE_OF_ENDER); }

        @Override
        public void onSec(Player player) {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, false, false)
            }, player);
        }
    }

    public static class FastMiner extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        @Var(name = "Portée de visée", desc = "Distance de détection du bloc visé pour le bonus obsidienne.", type = VariableType.INTEGER, min = 1)
        private int targetRange = 6;

        @Override public String getName() { return "FastMiner"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.FAST_MINER, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_PICKAXE); }

        @Override
        public void onSec(Player player) {
            if (UHCManager.get().getTimer() >= UHCManager.get().getPvpTimer()) return;
            int haste = isFacingObsidian(player) ? 1 : 0;
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SATURATION, 80, 0, false, false),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, 80, haste, false, false),
                    new PotionEffect(PotionEffectType.SPEED, 80, 1, false, false)
            }, player);
        }

        private boolean isFacingObsidian(Player player) {
            Block target = player.getTargetBlock((HashSet<Byte>) null, targetRange);
            return target != null && target.getType() == Material.OBSIDIAN;
        }
    }

    public static class StarterTools extends Scenario {

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Starter-Tools"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.STARTER_TOOLS, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @Override
        public void onStart(Player player) {
            player.setSaturation(100);
            PlayerInventory inv = player.getInventory();
            inv.addItem(new ItemCreator(Material.GOLDEN_CARROT).setAmount(64).getItemstack());
            inv.addItem(new ItemCreator(Material.APPLE).setAmount(64).getItemstack());
            inv.addItem(new ItemCreator(Material.WATER_BUCKET).setAmount(1).getItemstack());
            inv.addItem(new ItemCreator(Material.IRON_AXE).setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3).getItemstack());
            inv.addItem(new ItemCreator(Material.IRON_PICKAXE).setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3).getItemstack());
            inv.addItem(new ItemCreator(Material.IRON_SPADE).setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3).getItemstack());
            inv.addItem(new ItemCreator(Material.BOOK).setAmount(7).getItemstack());
            inv.addItem(new ItemCreator(Material.WOOD).setAmount(256).getItemstack());
        }
    }

    public static class WoolToString extends Scenario {

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "WooltoString"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.WOOL_TO_STRING, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WOOL); }

        @Override
        public void onGameStart() {
            for (byte data = 0; data < 16; data++) {
                ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.STRING, 1));
                recipe.addIngredient(new MaterialData(Material.WOOL, data));
                Bukkit.getServer().addRecipe(recipe);
            }
        }
    }
}
