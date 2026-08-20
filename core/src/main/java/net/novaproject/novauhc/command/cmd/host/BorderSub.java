package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.world.BorderPhase;
import net.novaproject.novauhc.world.SimpleBorder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class BorderSub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_BORDER_USAGE, player);
            return;
        }
        String action = args.get(0, "").toLowerCase();
        if (action.equals("addphase") || action.equals("phases") || action.equals("delphase")) {
            managePhases(player, args, action);
            return;
        }
        if (!UHCManager.get().isGame()) {
            LangManager.get().send(CoreLang.CMD_BORDER_INGAME_ONLY, player);
            return;
        }
        World arena = Common.get().getArena();
        WorldBorder wb = arena.getWorldBorder();
        SimpleBorder border = UHCManager.get().getActiveBorder();
        switch (action) {
            case "pause": {
                if (border == null || !border.isStart()) {
                    LangManager.get().send(CoreLang.CMD_BORDER_NOT_STARTED, player);
                    return;
                }
                if (border.isPause()) {
                    LangManager.get().send(CoreLang.CMD_BORDER_ALREADY_PAUSED, player);
                    return;
                }
                border.pause();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "La bordure a été mise en pause par l'host.");
                break;
            }
            case "play":
            case "resume":
            case "start": {
                if (border == null) {
                    border = new SimpleBorder(wb);
                    UHCManager.get().setActiveBorder(border);
                    border.startReduce(UHCManager.get().getTargetSize(), UHCManager.get().getReducSpeed());
                    Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "La bordure a été lancée manuellement.");
                    break;
                }
                if (!border.isStart()) {
                    border.startReduce(UHCManager.get().getTargetSize(), UHCManager.get().getReducSpeed());
                    Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "La bordure a été lancée manuellement.");
                    break;
                }
                if (!border.isPause()) {
                    LangManager.get().send(CoreLang.CMD_BORDER_NOT_PAUSED, player);
                    return;
                }
                border.play();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "La bordure reprend sa réduction.");
                break;
            }
            case "reset": {
                if (border != null) border.pause();
                double initial = UHCManager.get().getBorderDefaultSize();
                wb.setSize(initial);
                UHCManager.get().setActiveBorder(null);
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "La bordure a été remise à " + (int) initial + ".");
                break;
            }
            case "size": {
                if (args.size() < 2) {
                    LangManager.get().send(CoreLang.CMD_BORDER_SIZE_USAGE, player);
                    return;
                }
                try {
                    double size = Double.parseDouble(args.get(1, ""));
                    if (size < 1) size = 1;
                    wb.setSize(size);
                    Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "Taille de bordure fixée à " + (int) size + ".");
                } catch (NumberFormatException e) {
                    LangManager.get().send(CoreLang.CMD_BORDER_SIZE_INVALID, player);
                }
                break;
            }
            case "center": {
                if (args.size() < 3) {
                    LangManager.get().send(CoreLang.CMD_BORDER_CENTER_USAGE, player);
                    return;
                }
                try {
                    double x = Double.parseDouble(args.get(1, ""));
                    double z = Double.parseDouble(args.get(2, ""));
                    wb.setCenter(x, z);
                    Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "Centre de bordure fixé à " + (int) x + ", " + (int) z + ".");
                } catch (NumberFormatException e) {
                    LangManager.get().send(CoreLang.CMD_BORDER_CENTER_INVALID, player);
                }
                break;
            }
            case "status": {
                String state = LangManager.get().get(CoreLang.CMD_BORDER_STATE_IDLE);
                if (border != null && border.isStart()) state = LangManager.get().get(border.isPause() ? CoreLang.CMD_BORDER_STATE_PAUSED : CoreLang.CMD_BORDER_STATE_SHRINKING);
                LangManager.get().send(CoreLang.CMD_BORDER_STATUS, player, Map.of(
                        "%size%", (int) wb.getSize(), "%x%", (int) wb.getCenter().getX(),
                        "%z%", (int) wb.getCenter().getZ(), "%state%", state));
                break;
            }
            default:
                LangManager.get().send(CoreLang.CMD_BORDER_UNKNOWN_ACTION, player);
        }
    }

    private void managePhases(Player player, CommandArguments args, String action) {
        List<BorderPhase> phases = UHCManager.get().getBorderPhases();
        switch (action) {
            case "addphase": {
                if (args.size() < 3) {
                    LangManager.get().send(CoreLang.CMD_BORDER_ADDPHASE_USAGE, player);
                    return;
                }
                try {
                    int minutes = Integer.parseInt(args.get(1, ""));
                    double size = Double.parseDouble(args.get(2, ""));
                    Double cx = null, cz = null;
                    if (args.size() >= 5) {
                        cx = Double.parseDouble(args.get(3, ""));
                        cz = Double.parseDouble(args.get(4, ""));
                    }
                    int id = phases.size() + 1;
                    phases.add(new BorderPhase(id, minutes * 60, size, cx, cz));
                    LangManager.get().send(CoreLang.CMD_BORDER_PHASE_ADDED, player, Map.of(
                            "%id%", id, "%minutes%", minutes, "%size%", (int) size,
                            "%center%", cx != null ? LangManager.get().get(CoreLang.CMD_BORDER_PHASE_CENTER)
                                    .replace("%x%", String.valueOf(cx.intValue())).replace("%z%", String.valueOf(cz.intValue())) : ""));
                } catch (NumberFormatException e) {
                    LangManager.get().send(CoreLang.CMD_BORDER_ADDPHASE_INVALID, player);
                }
                return;
            }
            case "phases": {
                if (phases.isEmpty()) {
                    LangManager.get().send(CoreLang.CMD_BORDER_NO_PHASES, player);
                    return;
                }
                LangManager.get().send(CoreLang.CMD_BORDER_PHASES_HEADER, player, Map.of("%count%", phases.size()));
                for (BorderPhase phase : phases) {
                    LangManager.get().send(CoreLang.CMD_BORDER_PHASE_LINE, player, Map.of(
                            "%id%", phase.getId(), "%minutes%", phase.getTimer() / 60, "%size%", (int) phase.getSize(),
                            "%center%", phase.hasCenter() ? LangManager.get().get(CoreLang.CMD_BORDER_PHASE_CENTER)
                                    .replace("%x%", String.valueOf(phase.getCenterX().intValue()))
                                    .replace("%z%", String.valueOf(phase.getCenterZ().intValue())) : "",
                            "%done%", phase.isExecuted() ? LangManager.get().get(CoreLang.CMD_BORDER_PHASE_DONE) : ""));
                }
                return;
            }
            case "delphase": {
                if (args.size() < 2) {
                    LangManager.get().send(CoreLang.CMD_BORDER_DELPHASE_USAGE, player);
                    return;
                }
                try {
                    int id = Integer.parseInt(args.get(1, ""));
                    boolean removed = phases.removeIf(p -> p.getId() == id);
                    player.sendMessage(removed
                            ? ChatColor.GREEN + "Phase #" + id + " supprimée."
                            : ChatColor.RED + "Aucune phase #" + id + ".");
                } catch (NumberFormatException e) {
                    LangManager.get().send(CoreLang.CMD_BORDER_DELPHASE_USAGE, player);
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) {
            return getStrings(args, "pause", "play", "reset", "size", "center", "status", "addphase", "phases", "delphase");
        }
        return List.of();
    }
}

