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
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import org.bukkit.entity.Player;

public class Fatalis extends DragonRole {
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_MAX_HP_NAME", descKey = "FATALIS_VAR_MAX_HP_DESC", type = VariableType.INTEGER)
    public int maxHP = 17500;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_FORCE_NAME", descKey = "FATALIS_VAR_FORCE_DESC", type = VariableType.INTEGER)
    public int force = 480;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_RES_NAME", descKey = "FATALIS_VAR_RES_DESC", type = VariableType.INTEGER)
    public int res = 480;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_CRIT_DAMAGE_NAME", descKey = "FATALIS_VAR_CRIT_DAMAGE_DESC", type = VariableType.PERCENTAGE)
    public int critDamage = 50;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_CRIT_CHANCE_NAME", descKey = "FATALIS_VAR_CRIT_CHANCE_DESC", type = VariableType.PERCENTAGE)
    public int critChance = 17;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_FIRE_NAME", descKey = "FATALIS_VAR_FIRE_DESC", type = VariableType.PERCENTAGE)
    public double fire = 0.3;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_DRAG_NAME", descKey = "FATALIS_VAR_DRAG_DESC", type = VariableType.PERCENTAGE)
    public double drag = 0.1;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_WATER_NAME", descKey = "FATALIS_VAR_WATER_DESC", type = VariableType.PERCENTAGE)
    public double water = -0.2;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_ICE_NAME", descKey = "FATALIS_VAR_ICE_DESC", type = VariableType.PERCENTAGE)
    public double ice = -0.2;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_THUNDER_NAME", descKey = "FATALIS_VAR_THUNDER_DESC", type = VariableType.PERCENTAGE)
    public double thunder = 0.0;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_AURA_NAME", type = VariableType.ABILITY)
    public Ability aura;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_MER_NAME", type = VariableType.ABILITY)
    public Ability mer;
    @RoleVariable(lang = ScenarioVarLang.class, nameKey = "FATALIS_VAR_FLAME_NAME", type = VariableType.ABILITY)
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
    public void sendDescription(Player player) {
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
