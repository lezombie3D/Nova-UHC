package net.novaproject.ultimate.nuzlocke.roles.bug;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Consuming;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class BugGappleNerfListener extends Ability implements Listener, Consuming {

    private static final long GAPPLE_WINDOW_MS = 30000L;

    private static final java.util.Map<java.util.UUID, Long> GAPPLE_UNTIL = new java.util.HashMap<>();

    @Var(name = "Nuzlocke Bug Gapple Heal", type = VariableType.DOUBLE)
    private double gappleHeartsCap = 1.5;

    @Override public String getName() { return "Gapple Nerf"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            GAPPLE_UNTIL.put(getOwner().getUniqueId(), System.currentTimeMillis() + GAPPLE_WINDOW_MS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.MAGIC) return;
        Long until = GAPPLE_UNTIL.get(p.getUniqueId());
        if (until == null || System.currentTimeMillis() > until) return;
        event.setAmount(Math.min(event.getAmount(), 2 * gappleHeartsCap));
    }
}

