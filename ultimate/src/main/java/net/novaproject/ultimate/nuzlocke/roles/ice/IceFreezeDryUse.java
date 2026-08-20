package net.novaproject.ultimate.nuzlocke.roles.ice;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IceFreezeDryUse extends UseAbility implements Listener {

    @Var(name = "Nuzlocke Ice Freeze Dur", type = VariableType.INTEGER)
    private int freezeSeconds = 15;

    public static final Map<UUID, Boolean> ICE_WALK_ON = new HashMap<>();

    public IceFreezeDryUse() {
        setCooldown(1);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Glace"; }
    @Override public Material getMaterial() { return Material.PACKED_ICE; }

    @Override
    public boolean onEnable(Player player) {

        Block under = player.getLocation().getBlock().getRelative(0, -1, 0);
        if (under.getType() == Material.SNOW_BLOCK || under.getType() == Material.SNOW) {
            Snowball sb = player.launchProjectile(Snowball.class);
            sb.setShooter(player);
            return true;
        }
        boolean now = !ICE_WALK_ON.getOrDefault(player.getUniqueId(), false);
        ICE_WALK_ON.put(player.getUniqueId(), now);
        player.sendMessage("§b§lIce Walking §7» §" + (now ? "a§lON" : "c§lOFF"));
        if (now) startFreezeTask(player);
        return true;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;

    }

    private void startFreezeTask(Player player) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline() || !ICE_WALK_ON.getOrDefault(player.getUniqueId(), false)) {
                    cancel();
                    return;
                }
                Block below = player.getLocation().getBlock().getRelative(0, -1, 0);
                if (below.getType() == Material.WATER || below.getType() == Material.STATIONARY_WATER) {
                    below.setType(Material.ICE);
                    Block b = below;
                    org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(),
                            () -> { if (b.getType() == Material.ICE) b.setType(Material.STATIONARY_WATER); },
                            20L * freezeSeconds);
                }
            }
        }.runTaskTimer(net.novaproject.novauhc.Main.get(), 0L, 5L);
    }
}

