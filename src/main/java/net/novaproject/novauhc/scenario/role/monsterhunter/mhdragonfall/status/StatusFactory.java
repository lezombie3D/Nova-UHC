package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.ElementType;
import org.bukkit.entity.Player;

public class StatusFactory {
    public static StatusEffect create(ElementType type, Player player, int duration, DragonRole dragon) {
        switch (type) {
            case FIRE:
                return new FireBlight(player, "FireBlast", duration, dragon);
            default: return null;
        }
    }
}
