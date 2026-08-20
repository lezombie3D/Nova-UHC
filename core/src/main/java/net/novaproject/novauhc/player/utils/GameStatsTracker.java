package net.novaproject.novauhc.player.utils;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.player.PlayerSpi;

import net.novaproject.novauhc.api.ApiManager;
import java.util.*;
import java.util.stream.Collectors;

public class GameStatsTracker implements PlayerSpi.IGameStatsTracker {

    private final Map<UUID, PlayerGameStats> stats = new HashMap<>();
    private long gameStartTime = 0;

    public static class PlayerGameStats {
        public final UUID uuid;
        public String name;
        public int kills = 0;
        public int deaths = 0;
        public int assists = 0;
        public double damageDealt = 0;
        public double damageTaken = 0;
        public boolean isAlive = true;
        public String camp = null;
        public long joinTime = System.currentTimeMillis();
        public long endTime = 0;

        public PlayerGameStats(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public int getPlaytime() {
            long end = (!isAlive && endTime > 0) ? endTime : System.currentTimeMillis();
            return (int) ((end - joinTime) / 1000);
        }
    }

    public void startGame() {
        stats.clear();
        recentHits.clear();
        gameStartTime = System.currentTimeMillis();
        setAssistWindowSeconds(UHCManager.get().getStatsAssistWindow());
    }

    public void addPlayer(UUID uuid, String name) {
        stats.put(uuid, new PlayerGameStats(uuid, name));
    }

    public void addKill(UUID killer) {
        PlayerGameStats stat = stats.get(killer);
        if (stat != null) {
            stat.kills++;
        }
    }

    public void addDeath(UUID victim) {
        PlayerGameStats stat = stats.get(victim);
        if (stat != null) {
            stat.deaths++;
            stat.isAlive = false;
            stat.endTime = System.currentTimeMillis();
        }
    }

    public void revive(UUID uuid) {
        PlayerGameStats stat = stats.get(uuid);
        if (stat != null) {
            if (stat.deaths > 0) stat.deaths--;
            stat.isAlive = true;
            stat.endTime = 0;
        }
    }

    public void setCamp(UUID uuid, String camp) {
        PlayerGameStats stat = stats.get(uuid);
        if (stat != null) {
            stat.camp = camp;
        }
    }

    private final Map<UUID, Map<UUID, Long>> recentHits = new HashMap<>();
    private long assistWindowMs = 30_000L;

    public void setAssistWindowSeconds(int seconds) {
        if (seconds >= 0) this.assistWindowMs = seconds * 1000L;
    }

    public void recordDamage(UUID damager, UUID victim, double amount) {
        if (amount <= 0) return;
        PlayerGameStats dealer = stats.get(damager);
        if (dealer != null) dealer.damageDealt += amount;
        PlayerGameStats taken = stats.get(victim);
        if (taken != null) taken.damageTaken += amount;
        recentHits.computeIfAbsent(victim, k -> new HashMap<>()).put(damager, System.currentTimeMillis());
    }

    public void creditAssists(UUID victim, UUID killer) {
        Map<UUID, Long> hits = recentHits.remove(victim);
        if (hits == null) return;
        if (assistWindowMs <= 0) return;
        long now = System.currentTimeMillis();
        hits.forEach((attacker, time) -> {
            if (attacker.equals(victim)) return;
            if (attacker.equals(killer)) return;
            if (now - time > assistWindowMs) return;
            PlayerGameStats stat = stats.get(attacker);
            if (stat != null) stat.assists++;
        });
    }

    public PlayerGameStats getStats(UUID uuid) {
        return stats.get(uuid);
    }

    public int getGameDuration() {
        if (gameStartTime == 0) return 0;
        return (int) ((System.currentTimeMillis() - gameStartTime) / 1000);
    }

    public List<ApiManager.PlayerStats> getPlayerStats() {
        List<ApiManager.PlayerStats> list = new ArrayList<>();

        List<PlayerGameStats> sorted = new ArrayList<>(stats.values());
        sorted.sort((a, b) -> {
            if (a.isAlive != b.isAlive) return b.isAlive ? 1 : -1;
            if (b.kills != a.kills) return Integer.compare(b.kills, a.kills);
            return Long.compare(b.endTime, a.endTime);
        });

        int placement = 1;
        for (PlayerGameStats stat : sorted) {
            list.add(new ApiManager.PlayerStats(
                    stat.uuid.toString(),
                    stat.name,
                    stat.kills,
                    stat.deaths,
                    placement++,
                    stat.camp,
                    stat.assists,
                    Math.round(stat.damageDealt * 10.0) / 10.0,
                    Math.round(stat.damageTaken * 10.0) / 10.0,
                    stat.getPlaytime()
            ));
        }

        return list;
    }

    public List<PlayerGameStats> getAlivePlayers() {
        return stats.values().stream()
                .filter(s -> s.isAlive)
                .collect(Collectors.toList());
    }

    public List<ApiManager.WinnerInfo> getWinners(List<UUID> winnerUuids) {
        List<ApiManager.WinnerInfo> winners = new ArrayList<>();
        for (UUID uuid : winnerUuids) {
            PlayerGameStats stat = stats.get(uuid);
            if (stat != null) {
                winners.add(new ApiManager.WinnerInfo(
                        "player",
                        stat.uuid.toString(),
                        stat.name,
                        stat.kills,
                        stat.camp
                ));
            }
        }
        return winners;
    }

    public List<ApiManager.WinnerInfo> getWinnersByCamp(String winnerCamp) {
        return stats.values().stream()
                .filter(s -> s.isAlive && winnerCamp.equals(s.camp))
                .map(s -> new ApiManager.WinnerInfo("player", s.uuid.toString(), s.name, s.kills, s.camp))
                .collect(Collectors.toList());
    }

    public void reset() {
        stats.clear();
        recentHits.clear();
        gameStartTime = 0;
    }
}

