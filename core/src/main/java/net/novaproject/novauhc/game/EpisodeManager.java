package net.novaproject.novauhc.game;

import java.util.HashMap;

import java.util.UUID;

import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcNewEpisodeEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public class EpisodeManager implements GameSpi.IEpisodeManager {

    private static final EpisodeManager INSTANCE = new EpisodeManager();

    private int episode = 1;


    public static EpisodeManager get() {
        return INSTANCE;
    }

    public int getEpisode() {
        return episode;
    }

    public void reset() {
        episode = 1;
        EpisodeLimiter.clearAll();
    }

    public boolean isEnabled() {
        return UHCManager.get().getEpisodeDuration() > 0;
    }

    public int getTimeSpentInEpisode() {
        int duration = UHCManager.get().getEpisodeDuration();
        if (duration <= 0) return Math.max(UHCManager.get().getTimer(), 0);
        return Math.max(UHCManager.get().getTimer(), 0) % duration;
    }

    public int getTimeLeftInEpisode() {
        int duration = UHCManager.get().getEpisodeDuration();
        if (duration <= 0) return -1;
        return duration - getTimeSpentInEpisode();
    }

    public void tick(int timer) {
        int duration = UHCManager.get().getEpisodeDuration();
        if (duration <= 0 || timer <= 0) return;
        if (timer % duration != 0) return;

        episode++;
        Bukkit.getPluginManager().callEvent(new UhcNewEpisodeEvent(episode));

        String title = LangManager.get().get(CoreLang.COMMON_EPISODE_START, null, Map.of("%episode%", episode));
        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(uhcPlayer -> {
            Player player = uhcPlayer.getPlayer();
            if (player == null || !player.isOnline()) return;
            DisplayService.title(player, "", title, 40);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1.2f);
        });
    }

    public static final class EpisodeLimiter {

        private static final Map<String, Integer> LAST_USE = new HashMap<>();

        private static final GameSpi.IEpisodeLimiter INSTANCE = new GameSpi.IEpisodeLimiter() {
            @Override public boolean tryUse(UUID uuid, String key) { return EpisodeLimiter.tryUse(uuid, key); }
            @Override public boolean canUse(UUID uuid, String key) { return EpisodeLimiter.canUse(uuid, key); }
            @Override public void refund(UUID uuid, String key) { EpisodeLimiter.refund(uuid, key); }
            @Override public void clearAll() { EpisodeLimiter.clearAll(); }
        };

        public static GameSpi.IEpisodeLimiter instance() { return INSTANCE; }

        public static boolean tryUse(UUID uuid, String key) {
            if (!canUse(uuid, key)) return false;
            LAST_USE.put(mapKey(uuid, key), EpisodeManager.get().getEpisode());
            return true;
        }

        public static boolean canUse(UUID uuid, String key) {
            if (uuid == null || key == null) return false;
            Integer last = LAST_USE.get(mapKey(uuid, key));
            return last == null || last != EpisodeManager.get().getEpisode();
        }

        public static void refund(UUID uuid, String key) {
            if (uuid == null || key == null) return;
            LAST_USE.remove(mapKey(uuid, key));
        }

        public static void clearAll() {
            LAST_USE.clear();
        }

        private static String mapKey(UUID uuid, String key) {
            return uuid + "|" + key;
        }
    }
}
