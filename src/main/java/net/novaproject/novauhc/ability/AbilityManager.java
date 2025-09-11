package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import net.novaproject.novauhc.utils.Titles;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AbilityManager {
    private final List<Ability> abilities = new ArrayList<>();

    public static AbilityManager get() {
        return UHCManager.get().getAbilityManager();
    }

    private static void resetBar(Player player) {
        player.setExp(0f);
        player.setLevel(0);
    }

    public void setup() {

    }

    public void regesiterAbility(Ability ability) {
        abilities.add(ability);
    }

    public void updateAbilityBar(Player player) {
        ItemStack inHand = player.getItemInHand();
        if (!inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()) {
            resetBar(player);
            return;
        }

        String heldName = inHand.getItemMeta().getDisplayName();

        for (Ability ability : abilities) {
            if (heldName.equals("§8» §f§l" + ability.getName() + " §8«")) {
                long remaining = ShortCooldownManager.get(player, ability.getAbilityPath());
                new Titles().sendActionText(player, remaining / 1000 + "s");
                return;
            }
        }
        resetBar(player);
    }
}
