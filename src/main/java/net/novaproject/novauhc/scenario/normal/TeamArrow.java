package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.Titles;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamArrow extends Scenario {
    @Override
    public String getName() {
        return "TeamArrow";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ARROW);
    }

    @Override
    public void onStart(Player player) {
        UHCPlayer player1 = UHCPlayerManager.get().getPlayer(player);
        if (player1.getTeam().isPresent()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    UHCTeam team = player1.getTeam().get();
                    if (team.getPlayers().isEmpty()) {
                        cancel();
                        return;
                    }
                    for (UHCPlayer p : team.getPlayers()) {
                        if (p == player1) {
                            continue;
                        }
                        new Titles().sendActionText(player1.getPlayer(), " " + p.getPlayer().getName() + " : " + getArrowDirection(player1.getPlayer().getLocation(), p.getPlayer().getLocation(), player1.getPlayer().getLocation().getYaw()) // Added semicolon
                        );
                    }
                }
            }.runTaskTimer(Main.get(), 0, 20);
        }
    }

    public String getArrowDirection(Location from, Location to, float playerYaw) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        angle = (angle + 360) % 360;

        float normalizedYaw = (playerYaw + 360) % 360;

        double relativeAngle = (angle - normalizedYaw + 360) % 360;

        if (relativeAngle >= 337.5 || relativeAngle < 22.5) return "↑";
        if (relativeAngle >= 22.5 && relativeAngle < 67.5) return "↗";
        if (relativeAngle >= 67.5 && relativeAngle < 112.5) return "→";
        if (relativeAngle >= 112.5 && relativeAngle < 157.5) return "↘";
        if (relativeAngle >= 157.5 && relativeAngle < 202.5) return "↓";
        if (relativeAngle >= 202.5 && relativeAngle < 247.5) return "↙";
        if (relativeAngle >= 247.5 && relativeAngle < 292.5) return "←";
        if (relativeAngle >= 292.5 && relativeAngle < 337.5) return "↖";

        return "?";
    }
}
