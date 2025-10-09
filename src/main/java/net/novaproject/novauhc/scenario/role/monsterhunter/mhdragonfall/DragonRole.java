package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.resistance.ResistanceProfile;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class DragonRole extends Role {


    public DragonRole(){
        this.resistanceProfile = new ResistanceProfile();
        initResistances();
    }


    private int currentHP;
    private int currentStrength;
    private int currentResistance;
    private int currentCritDamage;
    private int currentCritChance;
    private int Absortion = 0;

    private ResistanceProfile resistanceProfile;
    public abstract int getMaxHP();

    public abstract int getStrength();

    public abstract int getResistance();

    public abstract int getCritDamage();

    public abstract int getCritChance();

    public abstract void initResistances();

    private final Map<ElementType, Double> elementPowers = new EnumMap<>(ElementType.class);

    public Map<ElementType, Double> getElementPowers() {
        return elementPowers;
    }

    private final Map<ElementType, Double> blightChances = new EnumMap<ElementType, Double>(ElementType.class);

    public double getBlightChance(ElementType type) {
        Double val = blightChances.get(type);
        return val == null ? 0.0D : val;
    }

    public void setBlightChance(ElementType type, double chance) {
        blightChances.put(type, Math.max(0.0D, Math.min(100.0D, chance)));
    }

    public void addElement(ElementType type, double power) {
        elementPowers.put(type, power);
    }


    public DragonRole getEvolution() {
        return null;
    }

    @Override
    public String getColor() {
        return getCamp().getColor();
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        currentHP = getMaxHP();
        currentCritChance = getCritChance();
        currentStrength = getStrength();
        currentResistance = getResistance();
        currentCritDamage = getCritDamage();
        new DragonStatsDisplay(uhcPlayer.getPlayer(), this).start();
    }


    public void onGoldenHeadUse() {

    }

    public void onGoldenAppleUse() {

    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        GoldenHead goldenHead = ScenarioManager.get().getScenario(GoldenHead.class);
        if (goldenHead != null && goldenHead.isActive()) {
            if (item.isSimilar(new ItemCreator(Material.GOLDEN_APPLE).setName(ChatColor.GOLD + "Golden Head").getItemstack())) {
                onGoldenHeadUse();
            }
        }
        if (item.getType() == Material.GOLDEN_APPLE) {
            onGoldenAppleUse();
        }

    }

}
