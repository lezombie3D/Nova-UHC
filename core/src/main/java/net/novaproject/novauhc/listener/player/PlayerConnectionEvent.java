package net.novaproject.novauhc.listener.player;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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

    private void setHost(Player player) {
        PermissionAttachment attachment = player.addAttachment(Main.get());
        attachment.setPermission("novauhc.host", true);
        hosts.add(player.getUniqueId());
        hostPlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerConnectionEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        UHCPlayerManager.get().connect(player);

    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        UHCPlayerManager.get().disconnect(player);
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
            return;
        }

        if (Bukkit.hasWhitelist() && !Bukkit.getWhitelistedPlayers().contains(player)) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, LangManager.get().get(CommonLang.KICK_WHITELIST));
            return;
        }

        if (!UHCManager.get().isSpectator() && UHCManager.get().isGame()) {
            if (UHCPlayerManager.get().getPlayer(player) == null) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, LangManager.get().get(CommonLang.KICK_SPEC));
                return;
            }
        }

        int slot = UHCManager.get().getSlot();
        int playersize = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();

        if (playersize >= slot) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, LangManager.get().get(CommonLang.KICK_FULL));
        }
    }


}
