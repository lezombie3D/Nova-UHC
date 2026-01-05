package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.config.StuffUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.entity.Player;

public class ChooseVerif extends CustomInventory {

    public ChooseVerif(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.game.color"));
        ItemCreator def = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiYmM1ODM2MDliNWYwMjUzN2NjM2NjMzZkNDBhNjBlMTM2NmEyMjJkYzU0ZjFlNzYxMTAwMGE4OTViMjMzNyJ9fX0=",
                        "§8┃ §fInventaire de"+ Common.get().getMainColor()+" départ", null)
                .addLore("")
                .addLore("  §8┃ §fVous permet de §5vérifier")
                .addLore("  §8┃ §fl'inventaire par défaut")
                .addLore("  §8┃ §fdonné en début de partie.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator death = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4NDQ4MTljY2YyMDM1MDQ4M2Y5NDY5YjEwNTA3MmU2ZDQ1MjE0ZDdmMjZjYjg2N2YxODkxMGJjYzFkY2RiIn19fQ==",
                        "§8┃ §fInventaire de " + Common.get().getMainColor() + "mort", null)
                .addLore("")
                .addLore("  §8┃ §fVous permet de §5vérifier")
                .addLore("  §8┃ §fl'inventaire de " + Common.get().getMainColor() + "mort")
                .addLore("  §8┃ §fdonné lors d'une " + Common.get().getMainColor() + "mort§f.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        addMenu(12,death, new StuffUi(getPlayer(), UHCManager.get().death));

        addMenu(14,def, new StuffUi(getPlayer(), UHCManager.get().start));
        addReturn(18,new GameUi(getPlayer()));
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.game.title");
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
