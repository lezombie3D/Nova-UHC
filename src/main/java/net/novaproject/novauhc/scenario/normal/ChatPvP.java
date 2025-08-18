package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ChatPvP extends Scenario {
    @Override
    public String getName() {
        return "Chat PvP";
    }

    @Override
    public String getDescription() {
        return "Les joueurs peuvent s'infliger des dégâts via des commandes de chat.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOOK_AND_QUILL);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int timerpvp = UHCManager.get().getTimerpvp();
        if (timer == timerpvp) {
            UHCManager.get().setChatdisbale(true);
            CommonString.CHAT_DISABLED.send(p);
        }

    }
}
