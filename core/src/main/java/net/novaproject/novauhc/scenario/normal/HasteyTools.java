package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class HasteyTools {

    private static final Set<Material> ENCHANTABLE_TOOLS = EnumSet.of(
            Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SPADE,
            Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SPADE,
            Material.WOOD_PICKAXE, Material.WOOD_AXE, Material.WOOD_SPADE,
            Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SPADE,
            Material.GOLD_PICKAXE, Material.GOLD_AXE, Material.GOLD_SPADE);

    private HasteyTools() {
    }

    private abstract static class HasteyBase extends Scenario {

        @Override
        public Family getFamily() { return Family.MINAGE; }

        protected abstract Material icon();

        protected abstract ScenarioDescLang descKey();

        protected abstract int digSpeed();

        protected abstract int unbreaking();

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(descKey(), player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(icon()); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (!ENCHANTABLE_TOOLS.contains(result.getType())) return;
            result.addUnsafeEnchantment(Enchantment.DIG_SPEED, digSpeed());
            if (unbreaking() > 0) {
                result.addUnsafeEnchantment(Enchantment.DURABILITY, unbreaking());
            }
            event.getInventory().setResult(result);
        }
    }

    public static class HasteyBabie extends HasteyBase {
        @Override public String getName() { return "HasteyBabie"; }
        @Override protected Material icon() { return Material.IRON_PICKAXE; }
        @Override protected ScenarioDescLang descKey() { return ScenarioDescLang.HASTEY_BABIE; }
        @Override protected int digSpeed() { return 1; }
        @Override protected int unbreaking() { return 0; }
    }

    public static class HasteyBoy extends HasteyBase {
        @Override public String getName() { return "HasteyBoy"; }
        @Override protected Material icon() { return Material.DIAMOND_PICKAXE; }
        @Override protected ScenarioDescLang descKey() { return ScenarioDescLang.HASTEY_BOY; }
        @Override protected int digSpeed() { return 3; }
        @Override protected int unbreaking() { return 1; }
    }

    public static class HasteyAlpha extends HasteyBase {
        @Override public String getName() { return "HasteyAlpha"; }
        @Override protected Material icon() { return Material.GOLD_PICKAXE; }
        @Override protected ScenarioDescLang descKey() { return ScenarioDescLang.HASTEY_ALPHA; }
        @Override protected int digSpeed() { return 5; }
        @Override protected int unbreaking() { return 3; }
    }
}
