package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class DragonRole extends Role {


    private int currentHP;
    private int currentStrength;
    private int currentResistance;
    private int currentCritDamage;
    private int currentCritChance;
    private int Absortion = 0;

    public abstract int getMaxHP();

    public abstract int getStrength();

    public abstract int getResistance();

    public abstract int getCritDamage();

    public abstract int getCritChance();

    public List<Element> getWeakness() {
        return new ArrayList<>();
    }

    public List<Element> getImmunity() {
        return new ArrayList<>();
    }

    public List<Element> getElement() {
        return new ArrayList<>();
    }

    public List<Status> getPossibleStatus() {
        return new ArrayList<>();
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
