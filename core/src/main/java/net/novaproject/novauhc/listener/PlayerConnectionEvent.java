package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.api.BlacklistManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.lobby.RankManager.Rank;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.minigame.DuelRequestManager;
import net.novaproject.novauhc.minigame.MiniGames.MiniGameRegistry;
import net.novaproject.novauhc.scenario.role.Bonds;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerConnectionEvent implements Listener {

    private static final Set<UUID> hosts = new HashSet<>();
    private static String firstPlayerUUID = null;
    private static OfflinePlayer hostPlayer;

    public static OfflinePlayer getHost() {
        return hostPlayer;
    }

    private static void setHost(Player player) {
        PermissionAttachment attachment = player.addAttachment(Main.get());
        attachment.setPermission("novauhc.host", true);
        hosts.add(player.getUniqueId());
        hostPlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        BlacklistManager.get().loadForHost(player.getUniqueId());
    }

    public static void transferHost(Player newHost) {
        OfflinePlayer previous = hostPlayer;
        if (previous != null && !previous.getUniqueId().equals(newHost.getUniqueId())) {
            hosts.remove(previous.getUniqueId());
            Player prevOnline = Bukkit.getPlayer(previous.getUniqueId());
            if (prevOnline != null && prevOnline.isOnline()) {
                prevOnline.addAttachment(Main.get(), "novauhc.host", false);
                if (UHCManager.get().isLobby()) {
                    RankManager.get().applyTag(prevOnline, Rank.PLAYER);
                    HotbarManager.get().giveHotbar(prevOnline);
                }
            }
        }
        firstPlayerUUID = newHost.getUniqueId().toString();
        setHost(newHost);
        if (UHCManager.get().isLobby()) {
            RankManager.get().applyTag(newHost, Rank.HOST);
            HotbarManager.get().giveHotbar(newHost);
        }
        LangManager.get().send(CoreLang.COMMON_WELCOME_HOST, newHost);
    }

    @EventHandler
    public void onPlayerConnectionEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        UHCPlayerManager.get().connect(player);

        Bonds.onJoin(player);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        UHCPlayerManager.get().disconnect(player);

        MiniGameRegistry.onQuit(player.getUniqueId());
        DuelRequestManager.onQuit(player);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        List<String> staffList = Main.get().getConfig().getStringList("staff");
        boolean isStaff = staffList.contains(player.getUniqueId().toString());

        if (firstPlayerUUID == null) {
            firstPlayerUUID = player.getUniqueId().toString();
            setHost(player);
            event.allow();
            return;
        }

        if (player.getUniqueId().toString().equals(firstPlayerUUID)) {
            setHost(player);
            event.allow();
            return;
        }

        if (isStaff) {
            event.allow();
            PermissionAttachment attachment = player.addAttachment(Main.get());
            attachment.setPermission("novauhc.host", true);
            autoWhitelist(player);
            return;
        }

        if (BlacklistManager.get().isBlacklisted(player.getUniqueId())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, LangManager.get().get(CoreLang.COMMON_KICK_BLACKLIST));
            return;
        }

        if (Bukkit.hasWhitelist() && !Bukkit.getWhitelistedPlayers().contains(player)) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, LangManager.get().get(CoreLang.COMMON_KICK_WHITELIST));
            return;
        }

        autoWhitelist(player);

        if (!UHCManager.get().isSpectator() && UHCManager.get().isGame()) {
            if (UHCPlayerManager.get().getPlayer(player) == null) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, LangManager.get().get(CoreLang.COMMON_KICK_SPEC));
                return;
            }
        }

        int slot = UHCManager.get().getSlot();
        int playersize = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();

        if (playersize >= slot) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, LangManager.get().get(CoreLang.COMMON_KICK_FULL));
        }
    }

    private void autoWhitelist(Player player) {
        OfflinePlayer offline = Bukkit.getOfflinePlayer(player.getUniqueId());
        if (!offline.isWhitelisted()) {
            offline.setWhitelisted(true);
        }
    }

}

