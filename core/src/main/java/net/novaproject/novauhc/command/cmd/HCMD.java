package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.command.cmd.host.EffectSub;
import net.novaproject.novauhc.command.cmd.host.MaxHealthSub;
import net.novaproject.novauhc.command.cmd.host.RegiveSub;
import net.novaproject.novauhc.command.cmd.host.BorderSub;
import net.novaproject.novauhc.command.cmd.host.BountySub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.BypassSub;
import net.novaproject.novauhc.command.cmd.host.CohostSub;
import net.novaproject.novauhc.command.cmd.host.ForceSub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.GiveSub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.GroupesSub;
import net.novaproject.novauhc.command.cmd.host.HostSub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.LimiteSub;
import net.novaproject.novauhc.command.cmd.host.ReviveSub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.SetHostSub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.SilenceSub;
import net.novaproject.novauhc.command.cmd.host.SpecSub;
import net.novaproject.novauhc.command.cmd.host.StuffSub;
import net.novaproject.novauhc.command.cmd.host.HostSubs.UnsilenceSub;
import net.novaproject.novauhc.command.cmd.host.WhitelistSub;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.display.apollo.ApolloConfigUi;
import net.novaproject.novauhc.game.VoteSystem;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.ConfirmMenu;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.GameUi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HCMD extends Command {

    public HCMD() {
        sub("give", new GiveSub());
        sub("config", HostSub.of((player, args) -> openConfig(player)));
        sub("bypass", new BypassSub());
        sub("say", HostSub.of(HCMD::say));
        sub("title", HostSub.of((player, args) -> {
            String message = args.join(0);
            for (Player p : Bukkit.getOnlinePlayers()) DisplayService.title(p, message, "", 10);
        }));
        sub("cohost", new CohostSub());
        sub("sethost", new SetHostSub());
        sub("revive", new ReviveSub());
        sub("force", new ForceSub());
        sub("heal", HostSub.of((player, args) -> {
            for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                up.getPlayer().setHealth(up.getPlayer().getMaxHealth());
            }
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_HEAL_BROADCAST));
        }));
        sub("limite", new LimiteSub());
        sub("whitelist", new WhitelistSub());
        sub("stuff", new StuffSub());
        sub("spec", new SpecSub());
        sub("border", new BorderSub());
        sub("groupes", new GroupesSub());
        sub("vote", HostSub.of(HCMD::vote));
        sub("bounty", new BountySub());
        sub("silence", new SilenceSub());
        sub("unsilence", new UnsilenceSub());
        sub("apollo", HostSub.of((player, args) -> new ApolloConfigUi(player).open()));
        sub("settings", HostSub.of((player, args) -> new GameUi(player).open()));
        sub("effect", new EffectSub());
        sub("maxhealth", new MaxHealthSub());
        sub("regive", new RegiveSub());
        sub("late", new StaffCMD.LateCMD());
        sub("freeze", new StaffCMD.FreezeCMD());
        sub("invsee", new StaffCMD.InvseeCMD());
        sub("near", new StaffCMD.NearCMD());
        sub("tphere", new StaffCMD.TphereCMD());
        sub("disperse", new StaffCMD.DisperseCMD());
        sub("vanish", new StaffCMD.VanishCMD());
        sub("bl", new BLCMD());
        sub("preconfig", new ConfigCMD());
    }

    @Override
    public void execute(CommandArguments args) {
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LangManager.get().get(CoreLang.CMD_PLAYERS_ONLY));
            return;
        }
        if (!isHost(player)) {
            LangManager.get().send(CoreLang.CMD_NO_PERMISSION, player);
            return;
        }
        if (args.size() == 0) {
            LangManager.get().send(CoreLang.COMMON_HOST_HELP_MESSAGE, player);
            LangManager.get().send(CoreLang.COMMON_HOST_HELP_MODERATION, player);
            return;
        }
        if (dispatchSub(args)) return;
        LangManager.get().send(CoreLang.CMD_UNKNOWN_CMD, player);
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return tabCompleteSub(args);
    }

    private static void say(Player player, CommandArguments args) {
        if (args.size() == 0) {
            LangManager.get().send(CoreLang.COMMON_HOST_SAY_USAGE, player);
            return;
        }
        Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.RED + args.join(0));
    }

    private static void vote(Player player, CommandArguments args) {
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_VOTE_USAGE, player);
            return;
        }
        if (!VoteSystem.startYesNo(args.join(0), UHCManager.get().getVoteDuration())) {
            LangManager.get().send(CoreLang.COMMON_VOTE_ALREADY_RUNNING, player);
        }
    }

    private static void openConfig(Player player) {
        UHCManager uhcManager = UHCManager.get();
        if (uhcManager.getGameState() != UHCManager.GameState.INGAME
                && uhcManager.getGameState() != UHCManager.GameState.SCATTERING) {
            HotbarManager.get().giveHotbar(player);
            new DefaultUi(player).open();
            return;
        }
        new ConfirmMenu(player, "§cEtes-vous sur de vouloir sur de prendre le risque ?",
                () -> new DefaultUi(player).open(),
                () -> LangManager.get().send(CoreLang.COMMON_CONFIG_CANNOT_INGAME, player),
                null).open();
    }
}

