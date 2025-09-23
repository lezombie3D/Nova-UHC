package net.novaproject.novauhc.scenario.special.skydef;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SkyDefUi extends CustomInventory {
    private final Pattern[] pattern = {new Pattern(DyeColor.BLACK, PatternType.FLOWER)};
    SkyDef sky = ScenarioManager.get().getScenario(SkyDef.class);

    public SkyDefUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCadre(0);
        ItemCreator def_team_size = new ItemCreator(Material.DIAMOND_CHESTPLATE)
                .setLores(Arrays.asList(
                        "",
                        "  §8┃ §fVous permet de " + Common.get().getMainColor() + "modifier",
                        "  §8┃ §fle nombre de " + Common.get().getMainColor() + "joueurs§f",
                        "  §8┃ §fdans l'equipe de §bDéfenseur§f",
                        ""
                )).setName("§8┃ §f Taille de l'équipe des §bDéfenseur : " + sky.getTeam_size());
        addMenu(13, def_team_size, new ConfigVarUi(getPlayer(), 3, 2, 1, 3, 2, 1, sky.getTeam_size(), 2, 0, this) {
            @Override
            public void onChange(int newValue) {
                sky.setTeam_size(newValue);
                sky.createDefTeam();
            }
        });
    }

    @Override
    public String getTitle() {
        return "§b§l SkyDef";
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
