package net.novaproject.ultimate.timedkings;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TimedKings extends Scenario {

    private static final DynamicLang DESC = DynamicLang.of("mode.timedkings.desc",
            "§7Un roi par équipe reçoit force, résistance, célérité et §e%health% §7points de vie. La couronne change de porteur toutes les §e%rotation% §7secondes.");
    private static final DynamicLang YOU_ARE_KING = DynamicLang.of("mode.timedkings.you_are_king",
            "§6Tu es désormais le §lRoi §6de ton équipe.");
    private static final DynamicLang TEAM_NEW_KING = DynamicLang.of("mode.timedkings.team_new_king",
            "§6%king% §7porte désormais la couronne de votre équipe.");
    private static final DynamicLang NO_LONGER_KING = DynamicLang.of("mode.timedkings.no_longer_king",
            "§7La couronne a changé de porteur, tu n'es plus le roi.");
    private static final DynamicLang KING_DIED = DynamicLang.of("mode.timedkings.king_died",
            "§cVotre roi est mort.");
    private static final DynamicLang NOTIF_CROWN_TITLE = DynamicLang.of("mode.timedkings.notif.crown_title",
            "§6La couronne est à toi");
    private static final DynamicLang NOTIF_CROWN_BODY = DynamicLang.of("mode.timedkings.notif.crown_body",
            "§7Tu portes les pouvoirs du roi jusqu'à la prochaine rotation.");
    private static final DynamicLang NOTIF_KING_DEAD_TITLE = DynamicLang.of("mode.timedkings.notif.dead_title",
            "§cVotre roi est tombé");
    private static final DynamicLang NOTIF_KING_DEAD_BODY = DynamicLang.of("mode.timedkings.notif.dead_body",
            "§7Votre équipe est affaiblie jusqu'au prochain couronnement.");

    private static final Color COURONNE = new Color(255, 170, 0);

    @Var(name = "Rotation", desc = "Délai entre deux changements de roi.", type = VariableType.TIME, min = 1)
    private int rotation = 600;

    @Var(name = "Vie du roi", desc = "Vie maximale du roi (40 = 20 coeurs).", type = VariableType.INTEGER, min = 2)
    private int kingMaxHealth = 40;

    @Var(name = "Niveau de force", desc = "Niveau de force du roi (0 = désactivé).", type = VariableType.INTEGER, min = 0)
    private int strengthLevel = 1;

    @Var(name = "Niveau de résistance", desc = "Niveau de résistance du roi (0 = désactivé).", type = VariableType.INTEGER, min = 0)
    private int resistanceLevel = 1;

    @Var(name = "Niveau de célérité", desc = "Niveau de célérité du roi (0 = désactivé).", type = VariableType.INTEGER, min = 0)
    private int hasteLevel = 1;

    @Var(name = "Durée de faiblesse", desc = "Faiblesse infligée aux survivants à la mort du roi (0 = désactivé).", type = VariableType.TIME, min = 0)
    private int weaknessDuration = 180;

    @Var(name = "Niveau de faiblesse", desc = "Niveau de la faiblesse infligée à la mort du roi (0 = désactivé).", type = VariableType.INTEGER, min = 0)
    private int weaknessLevel = 1;

    private final Set<UUID> kings = new HashSet<>();
    private final Map<UUID, Double> baseMaxHealth = new HashMap<>();
    private int dernierTour = -1;

    @Override
    public String getName() {
        return "Timed Kings";
    }

    @Override
    public String getDescription(Player player) {
        return t(DESC, player, Map.of("%health%", kingMaxHealth, "%rotation%", rotation));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WATCH);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            UHCManager.get().setTeam_size(2);
        } else {
            kings.clear();
            UHCTeamManager.get().deleteTeams();
        }
    }

    @Override
    public void onTeamUpdate() {
        if (!isActive()) return;
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void onGameStart() {
        if (!isActive()) return;
        kings.clear();
        baseMaxHealth.clear();
        dernierTour = -1;

        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            List<UHCPlayer> candidates = alivePlayers(team);
            if (candidates.isEmpty()) continue;

            UHCPlayer king = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            crown(king);
            Player kingPlayer = king.getPlayer();
            if (kingPlayer == null) continue;
            kingPlayer.setHealth(kingPlayer.getMaxHealth());
            announce(team, kingPlayer);
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() > 1 && uhcPlayer.getTeam().isPresent()) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        if (uhcPlayer.getPlayer() != null) uhcPlayer.getPlayer().teleport(location);
    }

    @Override
    public void onSec(Player player) {
        if (!isActive()) return;

        int timer = UHCManager.get().getTimer();
        if (timer > 0 && timer != dernierTour && timer % Math.max(1, rotation) == 0) {
            dernierTour = timer;
            rotate();
        }

        if (player == null) return;
        if (!kings.contains(player.getUniqueId())) {
            Double base = baseMaxHealth.remove(player.getUniqueId());
            if (base != null) player.setMaxHealth(base);
            return;
        }

        List<PotionEffect> effects = new ArrayList<>();
        if (strengthLevel > 0) {
            effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, strengthLevel - 1, false, false));
        }
        if (resistanceLevel > 0) {
            effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, resistanceLevel - 1, false, false));
        }
        if (hasteLevel > 0) {
            effects.add(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, hasteLevel - 1, false, false));
        }
        if (!effects.isEmpty()) {
            UHCUtils.applyInfiniteEffects(effects.toArray(new PotionEffect[0]), player);
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;
        if (!kings.remove(player.getUniqueId())) return;
        if (!uhcPlayer.getTeam().isPresent()) return;

        showCrown(uhcPlayer.getTeam().get(), player, false);

        for (UHCPlayer member : alivePlayers(uhcPlayer.getTeam().get())) {
            Player mp = member.getPlayer();
            if (mp == null || mp.getUniqueId().equals(player.getUniqueId())) continue;
            if (weaknessDuration > 0 && weaknessLevel > 0) {
                mp.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                        weaknessDuration * 20, weaknessLevel - 1, false, false), true);
            }
            mp.sendMessage(t(KING_DIED, mp));
            DisplayService.notification(mp, t(NOTIF_KING_DEAD_TITLE, mp), t(NOTIF_KING_DEAD_BODY, mp), 6);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for (Map.Entry<UUID, Double> entry : baseMaxHealth.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) player.setMaxHealth(entry.getValue());
        }
        baseMaxHealth.clear();
        kings.clear();
        dernierTour = -1;
    }

    private void rotate() {
        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            UUID current = null;
            List<UHCPlayer> candidates = new ArrayList<>();

            for (UHCPlayer member : team.getPlayers()) {
                if (!member.isPlaying()) continue;
                if (kings.contains(member.getUniqueId())) {
                    current = member.getUniqueId();
                } else if (member.getPlayer() != null) {
                    candidates.add(member);
                }
            }

            if (candidates.isEmpty()) continue;

            UHCPlayer next = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            if (current != null) {
                kings.remove(current);
                Player old = Bukkit.getPlayer(current);
                if (old != null) {
                    old.sendMessage(t(NO_LONGER_KING, old));
                    showCrown(team, old, false);
                }
            }
            crown(next);
            announce(team, next.getPlayer());
        }
    }

    private void crown(UHCPlayer member) {
        Player player = member.getPlayer();
        if (player == null) return;
        kings.add(player.getUniqueId());
        baseMaxHealth.putIfAbsent(player.getUniqueId(), player.getMaxHealth());
        player.setMaxHealth(Math.max(kingMaxHealth, 2));
        player.sendMessage(t(YOU_ARE_KING, player));
        DisplayService.notification(player, t(NOTIF_CROWN_TITLE, player), t(NOTIF_CROWN_BODY, player), 6);
    }

    private void announce(UHCTeam team, Player king) {
        if (king == null) return;
        for (UHCPlayer member : alivePlayers(team)) {
            Player mp = member.getPlayer();
            if (mp == null || mp.getUniqueId().equals(king.getUniqueId())) continue;
            mp.sendMessage(t(TEAM_NEW_KING, mp, Map.of("%king%", king.getName())));
        }
        showCrown(team, king, true);
    }

    private void showCrown(UHCTeam team, Player king, boolean visible) {
        if (team == null || king == null) return;
        for (UHCPlayer member : alivePlayers(team)) {
            Player mp = member.getPlayer();
            if (mp == null || mp.getUniqueId().equals(king.getUniqueId())) continue;
            if (visible) DisplayService.glow(mp, king, COURONNE);
            else DisplayService.resetGlow(mp, king);
        }
    }

    private List<UHCPlayer> alivePlayers(UHCTeam team) {
        List<UHCPlayer> alive = new ArrayList<>();
        for (UHCPlayer member : team.getPlayers()) {
            Player player = member.getPlayer();
            if (player != null && player.isOnline() && member.isPlaying()) alive.add(member);
        }
        return alive;
    }
}
