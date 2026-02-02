package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.fatalis.AuraPeur;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.fatalis.FlameNoir;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AbilityManager {
    private final List<Ability> abilities = new ArrayList<>();

    public static AbilityManager get() {
        return UHCManager.get().getAbilityManager();
    }


    public void setup() {
        regesiterAbility(new AuraPeur());
        regesiterAbility(new FlameNoir());
        regesiterAbility(new FlameNoir());
    }

    public void regesiterAbility(Ability ability) {
        abilities.add(ability);
    }

    public void updateCooldown(Player player) {
        for (Ability ability : abilities) {

            ItemStack abilityItem = ability.getItemStack();
            if (abilityItem == null) continue;

            for (int slot = 0; slot < player.getInventory().getSize(); slot++) {

                ItemStack item = player.getInventory().getItem(slot);
                if (item == null) continue;

                if (item.isSimilar(abilityItem)) {

                    long remaining = ShortCooldownManager.get(
                            player,
                            ability.getName() + "Cooldown"
                    );

                    ItemCreator itemCreator = getItemCreator(ability, remaining, item);

                    player.getInventory().setItem(slot, itemCreator.getItemstack());

                    break;
                }
            }
        }
    }

    private static @NotNull ItemCreator getItemCreator(Ability ability, long remaining, ItemStack item) {
        ArrayList<String> lores = new ArrayList<>();
        lores.add("");
        if (ability.getCooldown() == 0) {
            lores.add("§7Cooldown : §d§lAucun");
        } else {
            lores.add("§7Cooldown : §e" + Math.max(0, remaining / 1000) + "s");
        }
        lores.add("");
        if (ability.getMaxUse() == -1) {
            lores.add("§7Utilisation : §6§lInfinie");
        } else {
            lores.add("§7Utilisation : §c" + ability.getMaxUse());
        }
        lores.add("");
        ItemCreator itemCreator = new ItemCreator(item);
        itemCreator.setLores(lores);
        return itemCreator;
    }
}
