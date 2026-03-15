package net.novaproject.jjk.pouvoir;

import net.novaproject.jjk.lang.JJKLang;
import net.novaproject.novauhc.ability.UseAbiliy;
import net.novaproject.novauhc.ability.utils.AbilityVariable;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Blue extends UseAbiliy {
    @AbilityVariable(lang = JJKLang.class,nameKey = "BLUE_VAR_TITLE", descKey = "BLUE_VAR_DESC",type = VariableType.INTEGER)
    private int level = 20;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Material.BARRIER);
    }

    @Override
    public boolean onEnable(Player player) {
        if(Objects.equals(player.getName(), "lezombie3d")){
            return false;
        }
        player.setLevel(level);
        return true;
    }
}
