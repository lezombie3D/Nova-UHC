package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.game.LateJoinManager;
import net.novaproject.novauhc.lobby.FreezeManager;
import net.novaproject.novauhc.lobby.VanishManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.InvseeUi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class StaffCMD {

    private StaffCMD() {
    }

    private abstract static class StaffBase extends Command {

        protected abstract void run(Player player, CommandArguments args);

        @Override
        public void execute(CommandArguments args) {
            if (!(args.getSender() instanceof Player player)) {
                args.getSender().sendMessage(LangManager.get().get(CoreLang.CMD_PLAYERS_ONLY));
                return;
            }
            if (!isHost(player)) {
                LangManager.get().send(CoreLang.CMD_NO_PERMISSION, player);
                return;
            }
            run(player, args);
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return Collections.emptyList();
        }
    }

    private abstract static class TargetedStaffBase extends StaffBase {

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }
    }

    public static class VanishCMD extends StaffBase {

        @Override
        protected void run(Player player, CommandArguments args) {
            boolean vanished = VanishManager.toggle(player);
            LangManager.get().send(vanished ? CoreLang.CMD_VANISH_ON : CoreLang.CMD_VANISH_OFF, player);
        }
    }

    public static class FreezeCMD extends TargetedStaffBase {

        @Override
        protected void run(Player player, CommandArguments args) {
            Player target = args.getPlayer(0);
            if (target == null) {
                LangManager.get().send(CoreLang.CMD_FREEZE_USAGE, player);
                return;
            }
            boolean frozen = FreezeManager.toggle(player, target);
            LangManager.get().send(frozen ? CoreLang.CMD_FREEZE_ON : CoreLang.CMD_FREEZE_OFF, player,
                    Map.of("%player%", target.getName()));
        }
    }

    public static boolean isSpectating(Player player) {
        UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
        return up != null && (up.isSpec() || !up.isPlaying());
    }

    public static class InvseeCMD extends Command {

        @Override
        public void execute(CommandArguments args) {
            if (!(args.getSender() instanceof Player player)) {
                args.getSender().sendMessage(LangManager.get().get(CoreLang.CMD_PLAYERS_ONLY));
                return;
            }
            if (!isHost(player) && !isSpectating(player)) {
                LangManager.get().send(CoreLang.CMD_NO_PERMISSION, player);
                return;
            }
            Player target = args.getPlayer(0);
            if (target == null) {
                LangManager.get().send(CoreLang.CMD_INVSEE_USAGE, player);
                return;
            }
            new InvseeUi(player, target).open();
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }
    }

    public static class LateCMD extends TargetedStaffBase {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (args.size() < 1) {
                LangManager.get().send(CoreLang.CMD_LATE_USAGE, player);
                return;
            }
            LateJoinManager.handleLate(player, Bukkit.getPlayer(args.getArgument(0)));
        }
    }

    public static class DisperseCMD extends StaffBase {

        @Override
        protected void run(Player player, CommandArguments args) {
            int radius = Math.max(args.getInt(0, 15), 5);
            Location origin = player.getLocation();
            World world = origin.getWorld();
            ThreadLocalRandom random = ThreadLocalRandom.current();

            int dispersed = 0;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player target = uhcPlayer.getPlayer();
                if (target == null || !target.isOnline()) continue;
                if (!target.getWorld().equals(world)) continue;
                if (target.getLocation().distanceSquared(origin) > 64) continue;

                double angle = random.nextDouble() * Math.PI * 2;
                double distance = radius / 2.0 + random.nextDouble() * (radius / 2.0);
                int x = origin.getBlockX() + (int) (Math.cos(angle) * distance);
                int z = origin.getBlockZ() + (int) (Math.sin(angle) * distance);
                int y = world.getHighestBlockYAt(x, z);
                target.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));
                target.setNoDamageTicks(100);
                dispersed++;
            }

            if (dispersed > 0) {
                LangManager.get().send(CoreLang.CMD_DISPERSE_DONE, player,
                        Map.of("%count%", dispersed, "%radius%", radius));
            } else {
                LangManager.get().send(CoreLang.CMD_DISPERSE_NONE, player);
            }
        }
    }

    public static class TphereCMD extends StaffBase {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (args.size() < 1) {
                LangManager.get().send(CoreLang.CMD_TPHERE_USAGE, player);
                return;
            }

            String target = args.getArgument(0);
            if (target.equalsIgnoreCase("all") || target.equals("@a")) {
                int count = 0;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.equals(player)) continue;
                    online.teleport(player);
                    online.setNoDamageTicks(100);
                    count++;
                }
                LangManager.get().send(CoreLang.CMD_TPHERE_ALL_DONE, player, Map.of("%count%", count));
                return;
            }

            Player targetPlayer = args.getPlayer(0);
            if (targetPlayer == null) {
                LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                return;
            }
            targetPlayer.teleport(player);
            targetPlayer.setNoDamageTicks(100);
            LangManager.get().send(CoreLang.CMD_TPHERE_DONE, player, Map.of("%player%", targetPlayer.getName()));
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() != 1) return Collections.emptyList();
            String prefix = args.getArgument(0).toLowerCase(Locale.ROOT);
            List<String> options = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            options.add("all");
            return options.stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }
    }

    public static class NearCMD extends StaffBase {

        private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

        @Override
        protected void run(Player player, CommandArguments args) {
            int radius = Math.max(args.getInt(0, 50), 1);
            Location origin = player.getLocation();

            List<Player> nearby = new ArrayList<>();
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player target = uhcPlayer.getPlayer();
                if (target == null || !target.isOnline() || target.equals(player)) continue;
                if (!target.getWorld().equals(origin.getWorld())) continue;
                if (target.getLocation().distanceSquared(origin) > (double) radius * radius) continue;
                nearby.add(target);
            }

            if (nearby.isEmpty()) {
                LangManager.get().send(CoreLang.CMD_NEAR_NONE, player, Map.of("%radius%", radius));
                return;
            }

            nearby.sort(Comparator.comparingDouble(t -> t.getLocation().distanceSquared(origin)));
            LangManager.get().send(CoreLang.CMD_NEAR_HEADER, player, Map.of("%count%", nearby.size()));
            for (Player target : nearby) {
                double distance = target.getLocation().distance(origin);
                LangManager.get().send(CoreLang.CMD_NEAR_LINE, player, Map.of(
                        "%arrow%", arrowTo(player, target.getLocation()), "%player%", target.getName(),
                        "%distance%", String.format("%.0f", distance)));
            }
        }

        private String arrowTo(Player viewer, Location target) {
            Location from = viewer.getLocation();
            double dx = target.getX() - from.getX();
            double dz = target.getZ() - from.getZ();
            double angleToTarget = Math.toDegrees(Math.atan2(-dx, dz));
            double relative = angleToTarget - from.getYaw();
            relative = ((relative % 360) + 360) % 360;
            int index = (int) Math.round(relative / 45.0) % 8;
            return ARROWS[index];
        }
    }
}
