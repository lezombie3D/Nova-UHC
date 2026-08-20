package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AssassinForcePassive extends PassiveAbility {

    @Var(name = "Assassin Force Level", desc = "Passive Strength level.", type = VariableType.INTEGER)
    private int forceLevel = 1;

    public AssassinForcePassive() { setCooldown(0); }

    @Override public String getName() { return "Force Assassin"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (forceLevel <= 0) return false;
        if (!hasSecretBlade(player) || countSwords(player) >= 2) {
            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            return false;
        }
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, forceLevel - 1, false, false));
        return true;
    }

    private boolean hasSecretBlade(Player p) {
        for (ItemStack i : p.getInventory().getContents())
            if (i != null && i.hasItemMeta() && i.getItemMeta().hasDisplayName()
                    && i.getItemMeta().getDisplayName().equals("§8§lLame Secrète")) return true;
        return false;
    }

    private int countSwords(Player p) {
        int c = 0;
        for (ItemStack i : p.getInventory().getContents()) {
            if (i == null) continue;
            Material m = i.getType();
            if (m == Material.WOOD_SWORD || m == Material.STONE_SWORD || m == Material.IRON_SWORD
                    || m == Material.GOLD_SWORD || m == Material.DIAMOND_SWORD) c++;
        }
        return c;
    }
}

