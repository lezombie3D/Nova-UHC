package net.novaproject.ultimate.football;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FootballEdition extends Scenario {

    private static final int GARDIEN = 0;
    private static final int DEFENSEUR = 1;
    private static final int MILIEU = 2;
    private static final int ATTAQUANT = 3;

    private static final String REPEL_KEY = "FootballEditionTacle";

    private static final DynamicLang DESC = DynamicLang.of("mode.football.desc",
            "§7Chaque membre d'une équipe reçoit un poste — §agardien§7, §edéfenseur§7, §bmilieu §7ou §cattaquant §7— avec le pouvoir qui va avec. Un seul gardien par équipe.");

    private static final DynamicLang ASSIGNED = DynamicLang.of("mode.football.assigned",
            "§a⚽ §7Ton poste : §e%poste% §7— %pouvoir%§7.");

    private static final DynamicLang TACKLED = DynamicLang.of("mode.football.tackled",
            "§e⚽ §7Ton tacle repousse §f%player%§7.");

    private static final DynamicLang[] POSITION_NAMES = {
            DynamicLang.of("mode.football.name.gardien", "Gardien"),
            DynamicLang.of("mode.football.name.defenseur", "Défenseur"),
            DynamicLang.of("mode.football.name.milieu", "Milieu"),
            DynamicLang.of("mode.football.name.attaquant", "Attaquant")
    };

    private static final DynamicLang[] POSITION_POWERS = {
            DynamicLang.of("mode.football.power.gardien", "§7résistance aux dégâts permanente"),
            DynamicLang.of("mode.football.power.defenseur", "§7chaque coup reçu au corps-à-corps tacle l'attaquant et le repousse"),
            DynamicLang.of("mode.football.power.milieu", "§7rapidité permanente et endurance inépuisable"),
            DynamicLang.of("mode.football.power.attaquant", "§7force permanente")
    };

    @Var(name = "Niveau de Résistance (Gardien)", desc = "Niveau de l'effet Résistance permanent du gardien.", type = VariableType.INTEGER, min = 1)
    private int gardienResistanceLevel = 1;

    @Var(name = "Poussée du tacle (Défenseur)", desc = "Distance en blocs sur laquelle le défenseur repousse son attaquant.", type = VariableType.DOUBLE, min = 1)
    private double defenseurPushBlocks = 5.0;

    @Var(name = "Élévation du tacle (Défenseur)", desc = "Impulsion verticale appliquée par le tacle du défenseur.", type = VariableType.DOUBLE, min = 0)
    private double defenseurVerticalBoost = 0.35;

    @Var(name = "Recharge du tacle (Défenseur)", desc = "Temps de recharge entre deux tacles du défenseur.", type = VariableType.TIME, min = 1)
    private int defenseurRepelCooldownSec = 20;

    @Var(name = "Niveau de Rapidité (Milieu)", desc = "Niveau de l'effet Rapidité permanent du milieu.", type = VariableType.INTEGER, min = 1)
    private int milieuSpeedLevel = 2;

    @Var(name = "Niveau de Force (Attaquant)", desc = "Niveau de l'effet Force permanent de l'attaquant.", type = VariableType.INTEGER, min = 1)
    private int attaquantStrengthLevel = 1;

    private final Map<UUID, Integer> positions = new HashMap<>();

    @Override
    public String getName() {
        return "Football Edition";
    }

    @Override
    public String getDescription(Player player) {
        return t(DESC, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SNOW_BALL);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    private void forceTeamMode() {
        if (UHCManager.get().getTeam_size() >= 2) return;
        UHCManager.get().setTeam_size(2);
        LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
    }

    @Override
    public void enable() {
        super.enable();
        forceTeamMode();
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) forceTeamMode();
    }

    @Override
    public void onTeamUpdate() {
        if (!isActive()) return;
        forceTeamMode();
    }

    @Override
    public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() > 1 && up.getTeam().isPresent()) {
            UHCTeamManager.get().scatterTeam(up, teamloc);
            return;
        }
        if (up.getPlayer() != null) up.getPlayer().teleport(location);
    }

    @Override
    public void onGameStart() {
        if (!isActive()) return;
        positions.clear();

        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            List<UHCPlayer> members = team.getPlayers();
            Collections.shuffle(members);
            for (int i = 0; i < members.size(); i++) {
                Player member = members.get(i).getPlayer();
                if (member != null) assign(member, i == 0 ? GARDIEN : DEFENSEUR + ((i - 1) % 3));
            }
        }

        for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = up.getPlayer();
            if (player == null || positions.containsKey(player.getUniqueId())) continue;
            assign(player, GARDIEN);
        }
    }

    private void assign(Player player, int position) {
        positions.put(player.getUniqueId(), position);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.4f);
        LangManager.get().send(ASSIGNED, player, Map.of(
                "%poste%", t(POSITION_NAMES[position], player),
                "%pouvoir%", t(POSITION_POWERS[position], player)
        ));
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;
        Integer position = positions.get(p.getUniqueId());
        if (position == null) return;

        PotionEffect[] effects = switch (position) {
            case GARDIEN -> new PotionEffect[]{
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, Math.max(0, gardienResistanceLevel - 1), false, false)
            };
            case MILIEU -> new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, 80, Math.max(0, milieuSpeedLevel - 1), false, false),
                    new PotionEffect(PotionEffectType.SATURATION, 80, 0, false, false)
            };
            case ATTAQUANT -> new PotionEffect[]{
                    new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, Math.max(0, attaquantStrengthLevel - 1), false, false)
            };
            default -> new PotionEffect[]{};
        };

        if (effects.length > 0) UHCUtils.applyInfiniteEffects(effects, p);
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (!isActive()) return;
        if (event.isCancelled() || !(entity instanceof Player defender) || !(dammager instanceof Player attacker)) return;
        Integer position = positions.get(defender.getUniqueId());
        if (position == null || position != DEFENSEUR) return;
        if (CooldownService.get(defender, REPEL_KEY) > 0) return;

        CooldownService.put(defender, REPEL_KEY, defenseurRepelCooldownSec * 1000L);
        PlayerUtils.knockbackFrom(attacker, defender.getLocation(), defenseurPushBlocks, defenseurVerticalBoost);
        defender.playSound(defender.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.4f);
        LangManager.get().send(TACKLED, defender, Map.of("%player%", attacker.getName()));
    }

    @Override
    public void onStop() {
        super.onStop();
        positions.clear();
    }
}
