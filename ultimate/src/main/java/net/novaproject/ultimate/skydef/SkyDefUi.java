package net.novaproject.ultimate.skydef;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.special.SkyDefLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.ScenarioVariableUi;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.config.ScenariosUi;
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
    private final SkyDef sky = ScenarioManager.get().getScenario(SkyDef.class);

    public SkyDefUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCadre(0);
        addMenu(0, new ItemCreator(Material.PRISMARINE_SHARD)
                        .setName(LangManager.get().get(CommonLang.CLICK_HERE_TO_MODIFY, getPlayer())),
                new ScenarioVariableUi(getPlayer(), sky, this));
        String lore = LangManager.get().get(SkyDefLang.SKYDEF_UI_DEF_TEAM_SIZE_LORE, getPlayer());
        ItemCreator defTeamSize = new ItemCreator(Material.DIAMOND_CHESTPLATE).setName(t(SkyDefLang.VAR_TEAM_SIZE_DESC))
                .setLores(Arrays.asList(lore.split("\n")));
        addReturn(18, new ScenariosUi(getPlayer(), true));
        addMenu(13, defTeamSize, new ConfigVarUi(getPlayer(), 3, 2, 1, 3, 2, 1, sky.getTeam_size(), 2, 0, this) {
            @Override
            public void onChange(Number newValue) {
                sky.setTeam_size((int) newValue);
                sky.createDefTeam();
            }
        });
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(SkyDefLang.SKYDEF_UI_TITLE, getPlayer());
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