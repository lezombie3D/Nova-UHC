package net.novaproject.ultimate.nuzlocke.roles.rock;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RockSturdyPassive extends PassiveAbility {

    @Var(name = "Nuzlocke Rock Sturdy Hearts", type = VariableType.INTEGER)
    private int absorptionHearts = 2;

    @Var(name = "Nuzlocke Rock Sturdy Regen", type = VariableType.TIME)
    private int regenSeconds = 120;

    private static final Map<UUID, Integer> LAST_REFILL = new HashMap<>();

    public static void clear() { LAST_REFILL.clear(); }

    @Override public String getName() { return "Sturdy"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getOwner() == null) return false;
        Player owner = getOwner().getPlayer();
        if (owner == null) return false;
        UUID id = owner.getUniqueId();
        int timer = UHCManager.get().getTimer();
        int last = LAST_REFILL.getOrDefault(id, -regenSeconds);
        if (timer - last >= regenSeconds) {
            PlayerUtils.setAbsorptionHearts(owner, absorptionHearts * 2);
            LAST_REFILL.put(id, timer);
        }
        return true;
    }
}

