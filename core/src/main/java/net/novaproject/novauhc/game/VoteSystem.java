package net.novaproject.novauhc.game;

import net.novaproject.novauhc.game.GameSpi.IVoteSystem;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcVoteEndEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.display.DisplayService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VoteSystem {

    private static final Map<String, Vote> active = new HashMap<>();
    private static Vote current;
    private static String currentQuestion;
    private static BukkitTask task;
    private static int remaining;



    private static final IVoteSystem INSTANCE = new IVoteSystem() {
        @Override public boolean isActive(String id) { return VoteSystem.isActive(id); }
        @Override public void cast(String id, UUID voter, int option) { VoteSystem.cast(id, voter, option); }
        @Override public void retract(String id, UUID voter) { VoteSystem.retract(id, voter); }
        @Override public Map<String, Integer> tally(String id) { return VoteSystem.tally(id); }
        @Override public String winner(String id) { return VoteSystem.winner(id); }
        @Override public void end(String id) { VoteSystem.end(id); }
        @Override public boolean startYesNo(String question, int durationSeconds) { return VoteSystem.startYesNo(question, durationSeconds); }
        @Override public boolean startTimed(String id, String question, List<String> options, int durationSeconds) { return VoteSystem.startTimed(id, question, options, durationSeconds); }
        @Override public void castCurrent(Player player, int option) { VoteSystem.castCurrent(player, option); }
        @Override public boolean hasCurrent() { return VoteSystem.hasCurrent(); }
        @Override public List<String> currentOptions() { return VoteSystem.currentOptions(); }
        @Override public void clearAll() { VoteSystem.clearAll(); }
    };

    public static IVoteSystem instance() { return INSTANCE; }

    public static Vote start(String id, List<String> options) {
        Vote vote = new Vote(id, options);
        active.put(id, vote);
        return vote;
    }

    public static boolean isActive(String id) {
        return active.containsKey(id);
    }

    public static void cast(String id, UUID voter, int option) {
        Vote vote = active.get(id);
        if (vote == null || voter == null) return;
        if (option < 0 || option >= vote.options.size()) return;
        vote.ballots.put(voter, option);
    }

    public static void retract(String id, UUID voter) {
        Vote vote = active.get(id);
        if (vote != null && voter != null) {
            vote.ballots.remove(voter);
        }
    }

    public static Map<String, Integer> tally(String id) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Vote vote = active.get(id);
        if (vote == null) return result;
        for (String option : vote.options) {
            result.put(option, 0);
        }
        for (int index : vote.ballots.values()) {
            String option = vote.options.get(index);
            result.merge(option, 1, Integer::sum);
        }
        return result;
    }

    public static String winner(String id) {
        String best = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : tally(id).entrySet()) {
            if (entry.getValue() > bestCount) {
                best = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return best;
    }

    public static void end(String id) {
        Vote removed = active.remove(id);
        if (removed != null && removed == current) {
            stopTimedInternals();
        }
    }

    public static void clearAll() {
        active.clear();
        stopTimedInternals();
    }


    public static boolean startYesNo(String question, int durationSeconds) {
        return startTimed("core:host", question, List.of("Oui", "Non"), durationSeconds);
    }

    public static boolean startTimed(String id, String question, List<String> options, int durationSeconds) {
        if (current != null || question == null || question.isEmpty() || options == null || options.size() < 2) {
            return false;
        }
        current = start(id, options);
        currentQuestion = question;
        remaining = Math.max(durationSeconds, 5);

        Bukkit.broadcastMessage("");
        LangManager.get().sendAll(CoreLang.COMMON_VOTE_QUESTION, Map.of("%question%", question));

        TextComponent line = new TextComponent("");
        for (int i = 0; i < options.size(); i++) {
            String label = options.get(i);
            String color = isYesNo(options) ? (i == 0 ? "§a§l" : "§c§l") : "§e§l";
            String suffix = isYesNo(options) ? (i == 0 ? " ✔" : " ✖") : "";
            TextComponent opt = new TextComponent("    " + color + "[" + label + suffix + "]");
            opt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote " + (i + 1)));
            line.addExtra(opt);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(line);
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1.5f);
        }
        Bukkit.broadcastMessage("");

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (current == null) {
                    cancel();
                    return;
                }
                if (remaining <= 0) {
                    finishTimed();
                    return;
                }
                String bar = buildActionBar();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    DisplayService.actionBar(player, bar);
                }
                remaining--;
            }
        }.runTaskTimer(Main.get(), 0L, 20L);
        return true;
    }

    public static void castCurrent(Player player, int option) {
        if (current == null) {
            LangManager.get().send(CoreLang.COMMON_VOTE_NONE, player);
            return;
        }
        if (option < 0 || option >= current.options.size()) {
            LangManager.get().send(CoreLang.COMMON_VOTE_NONE, player);
            return;
        }
        Integer previous = current.ballots.put(player.getUniqueId(), option);
        String choiceDisplay = choiceDisplay(option);
        if (previous == null) {
            LangManager.get().send(CoreLang.COMMON_VOTE_RECORDED, player, Map.of("%choice%", choiceDisplay));
        } else if (previous != option) {
            LangManager.get().send(CoreLang.COMMON_VOTE_CHANGED, player, Map.of("%choice%", choiceDisplay));
        } else {
            LangManager.get().send(CoreLang.COMMON_VOTE_SAME, player);
        }
    }

    public static boolean hasCurrent() {
        return current != null;
    }

    public static List<String> currentOptions() {
        return current == null ? List.of() : current.options();
    }

    private static void finishTimed() {
        if (current == null) return;
        Map<String, Integer> tally = tally(current.id());
        String winner = winner(current.id());
        String question = currentQuestion;
        String voteId = current.id();

        for (Player player : Bukkit.getOnlinePlayers()) {
            DisplayService.actionBar(player, "");
        }
        Bukkit.broadcastMessage("");
        if (isYesNo(current.options)) {
            LangManager.get().sendAll(CoreLang.COMMON_VOTE_RESULTS, Map.of(
                    "%question%", question,
                    "%yes%", tally.getOrDefault("Oui", 0),
                    "%no%", tally.getOrDefault("Non", 0)));
        } else {
            LangManager.get().sendAll(CoreLang.COMMON_VOTE_RESULTS_GENERIC, Map.of(
                    "%question%", question,
                    "%winner%", winner != null ? winner : "—",
                    "%votes%", current.totalVotes()));
        }
        Bukkit.broadcastMessage("");

        active.remove(voteId);
        stopTimedInternals();
        Bukkit.getPluginManager().callEvent(new UhcVoteEndEvent(voteId, question, tally, winner));
    }

    private static void stopTimedInternals() {
        current = null;
        currentQuestion = null;
        if (task != null) {
            try { task.cancel(); } catch (Exception ignored) {}
            task = null;
        }
    }

    private static String buildActionBar() {
        if (isYesNo(current.options)) {
            Map<String, Integer> tally = tally(current.id());
            return LangManager.get().get(CoreLang.COMMON_VOTE_ACTIONBAR, null, Map.of(
                    "%question%", currentQuestion,
                    "%yes%", tally.getOrDefault("Oui", 0),
                    "%no%", tally.getOrDefault("Non", 0),
                    "%time%", remaining));
        }
        StringBuilder scores = new StringBuilder();
        Map<String, Integer> tally = tally(current.id());
        for (Map.Entry<String, Integer> e : tally.entrySet()) {
            if (scores.length() > 0) scores.append(" §8| ");
            scores.append("§e").append(e.getKey()).append(" §f").append(e.getValue());
        }
        return LangManager.get().get(CoreLang.COMMON_VOTE_ACTIONBAR_GENERIC, null, Map.of(
                "%question%", currentQuestion,
                "%scores%", scores.toString(),
                "%time%", remaining));
    }

    private static String choiceDisplay(int option) {
        String label = current.options.get(option);
        if (isYesNo(current.options)) {
            return option == 0 ? "§aOui ✔" : "§cNon ✖";
        }
        return "§e" + label;
    }

    private static boolean isYesNo(List<String> options) {
        return options.size() == 2 && "Oui".equals(options.get(0)) && "Non".equals(options.get(1));
    }

    public static final class Vote {

        private final String id;
        private final List<String> options;
        private final Map<UUID, Integer> ballots = new HashMap<>();

        private Vote(String id, List<String> options) {
            this.id = id;
            this.options = options;
        }

        public String id() {
            return id;
        }

        public List<String> options() {
            return options;
        }

        public int totalVotes() {
            return ballots.size();
        }
    }
}

