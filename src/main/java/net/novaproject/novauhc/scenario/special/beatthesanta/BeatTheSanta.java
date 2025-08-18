package net.novaproject.novauhc.scenario.special.beatthesanta;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class BeatTheSanta extends Scenario {

    private final Pattern[] santa = new Pattern[]{new Pattern(DyeColor.BLACK, PatternType.FLOWER)};
    private final UHCTeam santateam = new UHCTeam(DyeColor.RED, "[§cSanta§f] ", "§cSanta", santa, 1, true);

    public PotionEffect[] santaEffect() {
        return new PotionEffect[]{

                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
                new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false),
                new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0, false, false),
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false),
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false),
        };
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        uhcPlayer.getPlayer().teleport(location);
    }

    @Override
    public String getName() {
        return "Beat The Santa";
    }

    @Override
    public String getDescription() {
        return "Scénario de Noël avec des mécaniques spéciales liées au Père Noël.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.COOKIE);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (isActive()) {
            UHCTeamManager.get().addTeams(santateam);
            UHCTeamManager.get().deleteTeams();
        } else {
            UHCTeamManager.get().removeTeam(santateam);
            UHCTeamManager.get().deleteTeams();
        }
    }

    @Override
    public ScenarioLang[] getLang() {
        return BeatTheSantaLang.values();
    }

    @Override
    public String getPath() {
        return "special/bts";
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onStart(Player player) {

        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (santateam.getPlayers().contains(p)) {
            p.getPlayer().setMaxHealth(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() + 20);
            p.getPlayer().setHealth(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() + 20);
            ScenarioLangManager.send(player, BeatTheSantaLang.WARNING_SANTA);
            return;
        }
        TeamsTagsManager.setNameTag(player, "lutin", "[§aLutin§f] ", "");
        ScenarioLangManager.send(player, BeatTheSantaLang.WARNING_LUTIN);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        /*Player player = uhcPlayer.getPlayer();
        Location location = player.getLocation();
        player.setGameMode(GameMode.SPECTATOR);
        player.spigot().respawn();
        player.teleport(location);
        event.setDeathMessage(null);
        for (Player alive : Bukkit.getOnlinePlayers()) {
            alive.playSound(alive.getLocation(), Sound.WITHER_SPAWN, 1, 1);
        }
        UHCManager.get().checkVictory();*/
        for (UHCPlayer p : santateam.getPlayers()) {
            if (p.getPlayer().getMaxHealth() - 1 == 20) {
                return;
            }
            p.getPlayer().setMaxHealth(p.getPlayer().getMaxHealth() - 1);
        }
        if (santateam.getPlayers().contains(uhcPlayer)) {
            UHCManager.get().checkVictory();
            ScenarioLangManager.send(uhcPlayer.getPlayer(), BeatTheSantaLang.WARNING_SANTA_DEATH);
        }

    }

    @Override
    public void onSec(Player p) {
        UHCPlayer player = UHCPlayerManager.get().getPlayer(p);
        if (santateam.getPlayers().contains(player)) {
            UHCUtils.applyInfiniteEffects(santaEffect(), p);

        }
    }

    @Override
    public boolean isWin() {
        if (santateam.getPlayers().isEmpty()) {
            return UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() == 1;
        }
        return false;
    }
}
