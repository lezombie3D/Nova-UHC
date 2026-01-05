package net.novaproject.novauhc.ability.power;

import net.novaproject.novauhc.ability.AbilityVariable;
import net.novaproject.novauhc.ability.UseAbiliy;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.entity.Player;

public class TestAB extends UseAbiliy {
    @AbilityVariable(name = "Active", description = "Active la capacité",type = VariableType.BOOLEAN)
    private boolean active = true;

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public boolean active() {
        return active;
    }

    @Override
    public int getCooldown() {
        return 10;
    }

    @Override
    public boolean onEnable(Player player) {
        player.sendMessage("§aTest");
        return true;
    }


}
