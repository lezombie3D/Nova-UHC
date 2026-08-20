package net.novaproject.ultimate.king;
import net.novaproject.novauhc.utils.variable.Var;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;

import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class King extends Scenario {

    private static final Color COURONNE = new Color(255, 170, 0);

    private static final DynamicLang NOTIF_KING_TITLE =
            DynamicLang.of("mode.king.notif.king_title", "§6Tu es le Roi");
    private static final DynamicLang NOTIF_KING_BODY =
            DynamicLang.of("mode.king.notif.king_body", "§7Ta mort condamne ton équipe. Reste en vie.");
    private static final DynamicLang NOTIF_KING_DEAD_TITLE =
            DynamicLang.of("mode.king.notif.dead_title", "§cVotre Roi est tombé");
    private static final DynamicLang NOTIF_KING_DEAD_BODY =
            DynamicLang.of("mode.king.notif.dead_body", "§7La couronne est perdue, votre équipe est affaiblie.");

    @Var(name = "King Max Health", desc = "King's maximum health points (20 = 10 hearts).", type = VariableType.INTEGER)
    private int kingMaxHealth = 40;

    @Var(name = "Speed Enabled", desc = "Enable permanent Speed effect on the King.", type = VariableType.BOOLEAN)
    private boolean speedEnabled = true;

    @Var(name = "Speed Level", desc = "King's Speed effect level (0 = disabled).", type = VariableType.INTEGER)
    private int speedLevel = 1;

    @Var(name = "Strength Enabled", desc = "Enable permanent Strength effect on the King.", type = VariableType.BOOLEAN)
    private boolean strengthEnabled = true;

    @Var(name = "Strength Level", desc = "King's Strength effect level (0 = disabled).", type = VariableType.INTEGER)
    private int strengthLevel = 1;

    @Var(name = "Resistance Enabled", desc = "Enable permanent Resistance effect on the King.", type = VariableType.BOOLEAN)
    private boolean resistanceEnabled = true;

    @Var(name = "Resistance Level", desc = "King's Resistance effect level (0 = disabled).", type = VariableType.INTEGER)
    private int resistanceLevel = 1;

    @Var(name = "Poison Enabled", desc = "Enable poison on the team when the King dies.", type = VariableType.BOOLEAN)
    private boolean poisonEnabled = true;

    @Var(name = "Poison Duration", desc = "Poison duration in seconds (0 = disabled).", type = VariableType.TIME)
    private int poisonDuration = 120;

    @Var(name = "Poison Level", desc = "Poison level (0 = disabled).", type = VariableType.INTEGER)
    private int poisonLevel = 1;

    private final Set<UUID> kings = new HashSet<>();

    @Override
    public String getName() {
        return "King";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.KING, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_BLOCK);
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
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }
    }

    @Override
    public void onGameStart() {
        kings.clear();

        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            List<UHCPlayer> players = team.getPlayers();
            if (players.isEmpty()) continue;

            UHCPlayer king = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            Player kingPlayer = king.getPlayer();
            if (kingPlayer == null) continue;

            kings.add(kingPlayer.getUniqueId());

            int health = Math.max(kingMaxHealth, 2);
            kingPlayer.setMaxHealth(health);
            kingPlayer.setHealth(health);

            LangManager.get().send(KingLang.YOU_ARE_KING, kingPlayer);
            DisplayService.notification(kingPlayer,
                    t(NOTIF_KING_TITLE, kingPlayer), t(NOTIF_KING_BODY, kingPlayer), 6);

            for (UHCPlayer member : players) {
                Player mp = member.getPlayer();
                if (mp != null && !mp.getUniqueId().equals(kingPlayer.getUniqueId())) {
                    String msg = LangManager.get().get(KingLang.TEAM_KING_ANNOUNCE, mp)
                            .replace("%king%", kingPlayer.getName());
                    mp.sendMessage(msg);
                    DisplayService.glow(mp, kingPlayer, COURONNE);
                }
            }
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;

        if (!kings.remove(player.getUniqueId())) return;
        if (!uhcPlayer.getTeam().isPresent()) return;

        for (UHCPlayer member : uhcPlayer.getTeam().get().getPlayers()) {
            Player mp = member.getPlayer();
            if (mp == null || !mp.isOnline() || !member.isPlaying()) continue;

            if (poisonEnabled && poisonLevel > 0 && poisonDuration > 0) {
                mp.addPotionEffect(new PotionEffect(
                        PotionEffectType.POISON,
                        poisonDuration * 20,
                        poisonLevel - 1,
                        false, false
                ));
            }
            LangManager.get().send(KingLang.KING_DIED, mp);
            DisplayService.resetGlow(mp, player);
            DisplayService.notification(mp,
                    t(NOTIF_KING_DEAD_TITLE, mp), t(NOTIF_KING_DEAD_BODY, mp), 6);
        }
    }

    @Override
    public void onSec(Player p) {
        if (p == null || !kings.contains(p.getUniqueId())) return;

        List<PotionEffect> effects = new ArrayList<>();

        if (speedEnabled && speedLevel > 0) {
            effects.add(new PotionEffect(PotionEffectType.SPEED, 80, speedLevel - 1, false, false));
        }
        if (strengthEnabled && strengthLevel > 0) {
            effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, strengthLevel - 1, false, false));
        }
        if (resistanceEnabled && resistanceLevel > 0) {
            effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, resistanceLevel - 1, false, false));
        }

        if (!effects.isEmpty()) {
            UHCUtils.applyInfiniteEffects(effects.toArray(new PotionEffect[0]), p);
        }
    }

    public boolean isKing(Player player) {
        return player != null && kings.contains(player.getUniqueId());
    }
}

