package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class LagCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {

        double[] tps;
        try {
            tps = MinecraftServer.getServer().recentTps;
        } catch (Throwable t) {
            tps = new double[]{20.0, 20.0, 20.0};
        }

        int ping;
        try {
            ping = ((CraftPlayer) player).getHandle().ping;
        } catch (Throwable t) {
            ping = -1;
        }

        LangManager.get().send(CoreLang.CMD_LAG_HEADER, player);
        LangManager.get().send(CoreLang.CMD_LAG_TPS, player, Map.of("%tps%",
                tpsColor(tps[0]) + format(tps[0]) + " §8/ "
                        + tpsColor(tps[1]) + format(tps[1]) + " §8/ "
                        + tpsColor(tps[2]) + format(tps[2])));
        LangManager.get().send(CoreLang.CMD_LAG_PING, player, Map.of("%ping%", pingColor(ping) + (ping >= 0 ? ping + "ms" : "?")));
        
    }

    private String format(double tps) {
        return String.format("%.1f", Math.min(tps, 20.0));
    }

    private String tpsColor(double tps) {
        if (tps >= 18.0) return "§a";
        if (tps >= 15.0) return "§e";
        return "§c";
    }

    private String pingColor(int ping) {
        if (ping < 0) return "§7";
        if (ping <= 80) return "§a";
        if (ping <= 150) return "§e";
        return "§c";
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return Collections.emptyList();
    }
}

