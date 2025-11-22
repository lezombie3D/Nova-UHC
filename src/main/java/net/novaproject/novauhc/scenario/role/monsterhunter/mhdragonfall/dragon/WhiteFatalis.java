package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.dragon;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.Tiers;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;

public class WhiteFatalis extends DragonRole {

    public WhiteFatalis() {
        super();
        setCamp(Tiers.SS);
    }

    @Override
    public String getName() {
        return "White Fatalis";
    }

    @Override
    public String getDescription() {
        return "Ancien dragon divin, maître de la foudre et du chaos draconique.";
    }

    @Override
    public String getColor() {
        return Tiers.SS.getColor();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR)
                .setName("§fWhite Fatalis")
                .addLore("§7Le dragon des anciens dieux §8Maître de la foudre et du chaos.");
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
    public void initResistances() {
        getResistanceProfile().setResistance(ElementType.FIRE, 0.4);    // quasi immunisé au feu
        getResistanceProfile().setResistance(ElementType.WATER, -0.3);  // faible à l'eau
        getResistanceProfile().setResistance(ElementType.ICE, -0.2);    // vulnérable à la glace
        getResistanceProfile().setResistance(ElementType.THUNDER, 0.2); // résistance modérée
        getResistanceProfile().setResistance(ElementType.DRAGON, 0.0);  // neutre
    }

    @Override
    public void initElements() {

        addElement(ElementType.DRAGON, 1.2);
        addElement(ElementType.THUNDER, 1.0);

        setBlightChance(ElementType.DRAGON, 20.0); // Dragonblight
        setBlightChance(ElementType.THUNDER, 15.0); // Paralysis

        setBlightDuration(ElementType.DRAGON, 100);
        setBlightDuration(ElementType.THUNDER, 80);
    }

    @Override
    public double getProjectileMultiplier() {
        return 1.15;
    }
}
