package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.dragon;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.Tiers;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;

public class Fatalis extends DragonRole {

    public Fatalis() {
        super();
        setCamp(Tiers.SS);
    }

    @Override
    public String getName() {
        return "Fatalis";
    }

    @Override
    public String getDescription() {
        return "Le dragon légendaire du feu, maître du Black Flame.";
    }

    @Override
    public String getColor() {
        return Tiers.SS.getColor();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DRAGON_EGG);
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
        return 400;
    }

    @Override
    public int getCritDamage() {
        return 50;
    }

    @Override
    public int getCritChance() {
        return 15;
    }

    @Override
    public void initResistances() {
        getResistanceProfile().setResistance(ElementType.FIRE, 0.3);   // forte résistance feu
        getResistanceProfile().setResistance(ElementType.DRAGON, 0.1); // résistance modérée draconique
        getResistanceProfile().setResistance(ElementType.WATER, -0.2); // vulnérable à l'eau
        getResistanceProfile().setResistance(ElementType.ICE, -0.2);   // vulnérable à la glace
        getResistanceProfile().setResistance(ElementType.THUNDER, 0.0); // neutre
    }

    @Override
    public void initElements() {
        addElement(ElementType.FIRE, 1.0);
        addElement(ElementType.DRAGON, 0.8);

        setBlightChance(ElementType.FIRE, 20.0);
        setBlightChance(ElementType.DRAGON, 10.0);

        setBlightDuration(ElementType.FIRE, 10);
        setBlightDuration(ElementType.DRAGON, 25);
    }

    @Override
    public double getProjectileMultiplier() {
        return 0.9;
    }
}
