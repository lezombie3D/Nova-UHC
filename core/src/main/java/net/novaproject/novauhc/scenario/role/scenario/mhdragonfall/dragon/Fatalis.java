package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.power.TestAB;
import net.novaproject.novauhc.scenario.role.RoleVariable;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.Tiers;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;

public class Fatalis extends DragonRole {
    @RoleVariable(name = "maxHP", description = "Points de vie maximum de Fatalis", type = VariableType.INTEGER)
    public int maxHP = 8000;
    @RoleVariable(name = "testAB", description = "test", type = VariableType.ABILITY)
    private Ability testAB;

    public Fatalis() {
        super();
        setCamp(Tiers.SS);
        testAB = new TestAB();
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
        return maxHP;
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
    public void onGive(UHCPlayer uhcPlayer) {
        if(testAB.active()){
            getAbilities().add(testAB.clone());
        }
        super.onGive(uhcPlayer);
    }

    @Override
    public double getProjectileMultiplier() {
        return 0.9;
    }
}
