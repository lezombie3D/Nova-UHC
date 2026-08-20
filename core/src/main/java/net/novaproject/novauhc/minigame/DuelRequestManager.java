package net.novaproject.novauhc.minigame;

import net.md_5.bungee.api.chat.*;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelRequestManager {

    private static final Map<UUID, DuelRequest> BY_TARGET = new HashMap<>();
    private static final Map<UUID, UUID> BY_CHALLENGER = new HashMap<>();

    private static final int EXPIRE_TICKS = 30 * 20;


    public static boolean send(Player from, Player to, String game) {
        if (MiniGames.MiniGameRegistry.isPlaying(from.getUniqueId())) {
            LangManager.get().send(CoreLang.COMMON_MG_DUEL_SELF_BUSY, from);
            return false;
        }
        if (MiniGames.MiniGameRegistry.isPlaying(to.getUniqueId())) {
            LangManager.get().send(CoreLang.COMMON_MG_DUEL_TARGET_BUSY, from,
                    Map.of("%player%", to.getName()));
            return false;
        }
        if (BY_CHALLENGER.containsKey(from.getUniqueId())) {
            LangManager.get().send(CoreLang.COMMON_MG_DUEL_SELF_PENDING, from);
            return false;
        }
        if (BY_TARGET.containsKey(to.getUniqueId())) {
            LangManager.get().send(CoreLang.COMMON_MG_DUEL_TARGET_PENDING, from,
                    Map.of("%player%", to.getName()));
            return false;
        }

        DuelRequest req = new DuelRequest(from.getUniqueId(), to.getUniqueId(), game);
        BY_TARGET.put(to.getUniqueId(), req);
        BY_CHALLENGER.put(from.getUniqueId(), to.getUniqueId());

        BukkitTask task = Bukkit.getScheduler().runTaskLater(Main.get(), () -> expire(req), EXPIRE_TICKS);
        req.setExpireTask(task);

        sendInvite(from, to, game);
        LangManager.get().send(CoreLang.COMMON_MG_DUEL_SENT, from, Map.of(
                "%player%", to.getName(),
                "%seconds%", EXPIRE_TICKS / 20));
        return true;
    }

    private static void sendInvite(Player from, Player to, String game) {
        TextComponent header = new TextComponent(
                LangManager.get().get(CoreLang.COMMON_MG_DUEL_INVITE, to, Map.of(
                        "%player%", from.getName(),
                        "%game%", gameName(game))) + "\n");

        TextComponent accept = button(to,
                CoreLang.COMMON_MG_DUEL_ACCEPT_BUTTON, CoreLang.COMMON_MG_DUEL_ACCEPT_HOVER, "/mg accept");
        TextComponent space = new TextComponent("  ");
        TextComponent refuse = button(to,
                CoreLang.COMMON_MG_DUEL_REFUSE_BUTTON, CoreLang.COMMON_MG_DUEL_REFUSE_HOVER, "/mg refuse");

        to.spigot().sendMessage(header, accept, space, refuse);
    }

    private static TextComponent button(Player viewer, CoreLang label, CoreLang hover, String command) {
        TextComponent component = new TextComponent(LangManager.get().get(label, viewer));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(LangManager.get().get(hover, viewer)).create()));
        return component;
    }

    public static DuelRequest accept(Player target) {
        DuelRequest req = BY_TARGET.remove(target.getUniqueId());
        if (req == null) return null;
        BY_CHALLENGER.remove(req.getFrom());
        req.cancelExpiry();
        return req;
    }

    public static DuelRequest refuse(Player target) {
        DuelRequest req = BY_TARGET.remove(target.getUniqueId());
        if (req == null) return null;
        BY_CHALLENGER.remove(req.getFrom());
        req.cancelExpiry();
        return req;
    }

    public static void onQuit(Player player) {
        UUID uuid = player.getUniqueId();

        DuelRequest asTarget = BY_TARGET.remove(uuid);
        if (asTarget != null) {
            BY_CHALLENGER.remove(asTarget.getFrom());
            asTarget.cancelExpiry();
        }

        UUID targetUuid = BY_CHALLENGER.remove(uuid);
        if (targetUuid != null) {
            DuelRequest asChallenger = BY_TARGET.remove(targetUuid);
            if (asChallenger != null) asChallenger.cancelExpiry();
        }
    }

    public static void clearAll() {
        for (DuelRequest req : BY_TARGET.values()) {
            req.cancelExpiry();
        }
        BY_TARGET.clear();
        BY_CHALLENGER.clear();
    }

    private static void expire(DuelRequest req) {
        if (!BY_TARGET.containsKey(req.getTo())) return;
        BY_TARGET.remove(req.getTo());
        BY_CHALLENGER.remove(req.getFrom());

        Player from = Bukkit.getPlayer(req.getFrom());
        Player to   = Bukkit.getPlayer(req.getTo());
        if (from != null) {
            LangManager.get().send(CoreLang.COMMON_MG_DUEL_EXPIRED_SENDER, from,
                    Map.of("%player%", playerName(req.getTo())));
        }
        if (to != null) {
            LangManager.get().send(CoreLang.COMMON_MG_DUEL_EXPIRED_TARGET, to,
                    Map.of("%player%", playerName(req.getFrom())));
        }
    }

    public static String gameName(String key) {
        MiniGames.MiniGameType type = MiniGames.MiniGameType.fromKey(key);
        return type != null ? type.getDisplayName() : key;
    }

    private static String playerName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : uuid.toString().substring(0, 8);
    }

    public static class DuelRequest {

        private final UUID from;
        private final UUID to;
        private final String game;
        private BukkitTask expireTask;

        public DuelRequest(UUID from, UUID to, String game) {
            this.from = from;
            this.to   = to;
            this.game = game;
        }

        public UUID getFrom()  { return from; }
        public UUID getTo()    { return to;   }
        public String getGame(){ return game; }

        public void setExpireTask(BukkitTask task) { this.expireTask = task; }
        public void cancelExpiry() { if (expireTask != null) expireTask.cancel(); }
    }
}
