package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.whitefatalis;

import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.Tiers;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
    public void sendDescription(Player player) {
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
        getResistanceProfile().setResistance(ElementType.FIRE, 0.4);    
        getResistanceProfile().setResistance(ElementType.WATER, -0.3);  
        getResistanceProfile().setResistance(ElementType.ICE, -0.2);    
        getResistanceProfile().setResistance(ElementType.THUNDER, 0.2); 
        getResistanceProfile().setResistance(ElementType.DRAGON, 0.0);  
    }

    @Override
    public void initElements() {

        addElement(ElementType.DRAGON, 1.2);
        addElement(ElementType.THUNDER, 1.0);

        setBlightChance(ElementType.DRAGON, 20.0); 
        setBlightChance(ElementType.THUNDER, 15.0); 

        setBlightDuration(ElementType.DRAGON, 100);
        setBlightDuration(ElementType.THUNDER, 80);
    }

    @Override
    public double getProjectileMultiplier() {
        return 1.15;
    }
}
