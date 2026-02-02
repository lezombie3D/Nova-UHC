package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.fatalis;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.scenario.role.RoleVariable;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.Tiers;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;

public class Fatalis extends DragonRole {
    @RoleVariable(name = "Vie", description = "§fPoints de §a§lVie §fmaximum par default.", type = VariableType.INTEGER)
    public int maxHP = 17500;
    @RoleVariable(name = "Force", description = "§fPoint de §c§lForce §fpar défault.",type = VariableType.INTEGER)
    public int force = 480;
    @RoleVariable(name = "Resistance", description = "§fPoint de §b§lResistance §fpar défault.",type = VariableType.INTEGER)
    public int res = 480;
    @RoleVariable(name = "Critical Damage", description = "§fAugmentation des §6dégat §fen cas de §5§lCritique§f.",type = VariableType.PERCENTAGE)
    public int critDamage = 50;
    @RoleVariable(name = "Critical Chance", description = "§fChance de §6provoquer §fun §5§lCritique§f.",type = VariableType.PERCENTAGE)
    public int critChance = 17;
    @RoleVariable(name = "Fire Resistance", description = "§c§lAugmentation§f/§a§lDiminution §fdes §6§lRésistance Elementaire§f",type = VariableType.PERCENTAGE)
    public double fire = 0.3;
    @RoleVariable(name = "Dragon Resistance", description = "§c§lAugmentation§f/§a§lDiminution §fdes §6§lRésistance Elementaire§f",type = VariableType.PERCENTAGE)
    public double drag = 0.1;
    @RoleVariable(name = "Water Resistance", description = "§c§lAugmentation§f/§a§lDiminution §fdes §6§lRésistance Elementaire§f",type = VariableType.PERCENTAGE)
    public double water = -0.2;
    @RoleVariable(name = "Ice Resistance", description = "§c§lAugmentation§f/§a§lDiminution §fdes §6§lRésistance Elementaire§f",type = VariableType.PERCENTAGE)
    public double ice = -0.2;
    @RoleVariable(name = "Thunder Resistance", description = "§c§lAugmentation§f/§a§lDiminution §fdes §6§lRésistance Elementaire§f",type = VariableType.PERCENTAGE)
    public double thunder = 0.0;
    @RoleVariable(name = "Aura de Peur", description = "",type = VariableType.ABILITY)
    public Ability aura;
    @RoleVariable(name = "Mer de Flamme", description = "",type = VariableType.ABILITY)
    public Ability mer;
    @RoleVariable(name = "Flame Noir", description = "",type = VariableType.ABILITY)
    public Ability flame;

    public Fatalis() {
        super();
        setCamp(Tiers.SS);
        flame = new FlameNoir();
        aura = new AuraPeur();
        mer = new MerFlamme();
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
        return force;
    }

    @Override
    public int getResistance() {
        return res;
    }

    @Override
    public int getCritDamage() {
        return critDamage;
    }

    @Override
    public int getCritChance() {
        return critChance;
    }

    @Override
    public void initResistances() {
        getResistanceProfile().setResistance(ElementType.FIRE, fire);
        getResistanceProfile().setResistance(ElementType.DRAGON, drag);
        getResistanceProfile().setResistance(ElementType.WATER, water);
        getResistanceProfile().setResistance(ElementType.ICE, ice);
        getResistanceProfile().setResistance(ElementType.THUNDER, thunder);
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
        super.onGive(uhcPlayer);
    }

    @Override
    public double getProjectileMultiplier() {
        return 0.9;
    }
}
