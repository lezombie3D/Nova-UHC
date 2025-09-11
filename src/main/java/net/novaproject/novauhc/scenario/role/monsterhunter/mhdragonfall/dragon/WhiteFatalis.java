package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.dragon;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.Tiers;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;

public class WhiteFatalis extends DragonRole {


    public WhiteFatalis() {
        setCamp(Tiers.SS);
    }

    @Override
    public String getDescription() {
        return "test White fatalis";
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CARROT_ITEM);
    }

    @Override
    public int getMaxHP() {
        return 32000;
    }

    @Override
    public int getStrength() {
        return 820;
    }

    @Override
    public int getResistance() {
        return 440;
    }

    @Override
    public int getCritDamage() {
        return 32;
    }

    @Override
    public int getCritChance() {
        return 10;
    }

    @Override
    public String getName() {
        return "whitefatalis";
    }

}
