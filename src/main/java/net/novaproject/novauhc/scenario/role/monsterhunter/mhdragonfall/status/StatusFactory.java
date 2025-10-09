package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.ElementType;
import org.bukkit.entity.Player;

public class StatusFactory {
    public static StatusEffect create(ElementType type, Player player) {
        switch (type) {
            case FIRE: return new FireBlight(player, 100);
            case ICE: return new IceBlight(player, 120);
            case THUNDER: return new ParalysisBlight(player, 80);
            default: return null;
        }
    }
}
