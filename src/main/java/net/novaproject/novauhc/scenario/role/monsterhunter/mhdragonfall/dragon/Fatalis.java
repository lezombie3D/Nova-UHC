package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.dragon;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.Tiers;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;

public class Fatalis extends DragonRole {
    public Fatalis() {
        setCamp(Tiers.SS);
    }

    @Override
    public String getName() {
        return "fatlis";
    }

    @Override
    public String getDescription() {
        return "test fatalis";
    }


    @Override
    public String getColor() {
        return Tiers.SS.getColor();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.TNT);
    }

    @Override
    public int getMaxHP() {
        return 10000;
    }

    @Override
    public int getStrength() {
        return 500;
    }

    @Override
    public int getResistance() {
        return 500;
    }

    @Override
    public int getCritDamage() {
        return 50;
    }

    @Override
    public int getCritChance() {
        return 100;
    }
}
