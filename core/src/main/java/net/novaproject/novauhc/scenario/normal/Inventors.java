package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Inventors extends Scenario {

    @Override
    public Family getFamily() { return Family.CRAFT; }

    private final Map<Material, UUID> firstCrafters = new HashMap<>();

    @Var(name = "Diamants offerts", desc = "Diamants donnés au premier joueur à crafter un objet (0 = aucune récompense).", type = VariableType.INTEGER, min = 0)
    private int diamondReward = 0;

    @Var(name = "Lingots d'or offerts", desc = "Lingots d'or donnés au premier joueur à crafter un objet (0 = aucune récompense).", type = VariableType.INTEGER, min = 0)
    private int goldReward = 0;

    @Override public String getName() { return "Inventors"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.INVENTORS, player);
    }

    @Override public ItemCreator getItem() { return new ItemCreator(Material.WORKBENCH); }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {
        if (!isActive()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Material crafted = result.getType();
        if (firstCrafters.putIfAbsent(crafted, player.getUniqueId()) != null) return;

        Bukkit.broadcastMessage(t(ScenarioLang.INVENTORS_FIRST_CRAFT, Map.of(
                "%player%", player.getName(),
                "%item%", crafted.name().replace("_", " ").toLowerCase())));

        reward(player, Material.DIAMOND, diamondReward);
        reward(player, Material.GOLD_INGOT, goldReward);
    }

    private void reward(Player player, Material material, int amount) {
        if (amount <= 0) return;
        PlayerUtils.giveOrDrop(player, new ItemStack(material, amount));
    }

    @Override
    public void onStop() {
        firstCrafters.clear();
    }
}
