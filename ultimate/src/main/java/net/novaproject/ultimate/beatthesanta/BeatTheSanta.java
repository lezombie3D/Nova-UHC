package net.novaproject.ultimate.beatthesanta;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.lang.special.BeatTheSantaLang;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeatTheSanta extends Scenario {

    private final Pattern[] patern = new Pattern[]{new Pattern(DyeColor.BLACK, PatternType.FLOWER)};
    
    private UHCTeam santa;
    

    @ScenarioVariable(nameKey = "BEATSANTA_TEAM_SIZE_NAME",descKey = "BEATSANTA_TEAM_SIZE_DESC",type = VariableType.INTEGER)
    private int team_size = 1;
    @ScenarioVariable(nameKey = "BEATSANTA_STRENGTH_NAME",descKey = "BEATSANTA_STRENGTH_DESC",type = VariableType.BOOLEAN)
    private boolean strength_santa = false;
    @ScenarioVariable(nameKey = "BEATSANTA_SPEED_NAME",descKey = "BEATSANTA_SPEED_DESC",type = VariableType.BOOLEAN)
    private boolean speed_santa = false;
    @ScenarioVariable(nameKey = "BEATSANTA_FIRE_RESISTANCE_NAME",descKey = "BEATSANTA_FIRE_RESISTANCE_DESC",type = VariableType.BOOLEAN)
    private boolean fire_santa = false;
    @ScenarioVariable(nameKey = "BEATSANTA_HASTE_NAME",descKey = "BEATSANTA_HASTE_DESC",type = VariableType.BOOLEAN)
    private boolean haste_santa = false;
    @ScenarioVariable(nameKey = "BEATSANTA_RESISTANCE_NAME",descKey = "BEATSANTA_RESISTANCE_DESC",type = VariableType.BOOLEAN)
    private boolean resi_santa = false;
    public PotionEffect[] santaEffect() {
        List<PotionEffect> effects = new ArrayList<>();

        if(speed_santa) effects.add(new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false));
        if(fire_santa) effects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false));
        if(haste_santa) effects.add(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0, false, false));
        if(resi_santa) effects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false));
        if(strength_santa) effects.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false));

        return effects.toArray(new PotionEffect[0]);
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
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.BEATSANTA,player, Map.of("%santa_size%",team_size,"%lutin_size%", UHCManager.get().getSlot()-team_size));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.COOKIE);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (isActive()) {
            createSantaTeam();
        } else {
            UHCTeamManager.get().removeTeam(santa);
            UHCTeamManager.get().deleteTeams();
        }
    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onStart(Player player) {

        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        if (santa.getPlayers().contains(p)) {
            p.getPlayer().setMaxHealth(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() + 20);
            p.getPlayer().setHealth(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() + 20);
            LangManager.get().send(BeatTheSantaLang.WARNING_SANTA,player);
            return;
        }
        TeamsTagsManager.setNameTag(player, "Lutin", "§a§lLUTIN §a", "");
        LangManager.get().send(BeatTheSantaLang.WARNING_LUTIN, player);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

        for (UHCPlayer p : santa.getPlayers()) {
            if (p.getPlayer().getMaxHealth() - 1 == 20) {
                return;
            }
            p.getPlayer().setMaxHealth(p.getPlayer().getMaxHealth() - 1);
        }
        if (santa.getPlayers().contains(uhcPlayer)) {
            UHCManager.get().checkVictory();
            LangManager.get().send(BeatTheSantaLang.WARNING_SANTA_DEATH, uhcPlayer.getPlayer());
        }

    }

    public void createSantaTeam(){
        if(santa != null){
            UHCTeamManager.get().removeTeam(santa);
        }
        santa = new UHCTeam(DyeColor.RED, "§c§lSANTA §c", "Santa", patern, team_size, true);
        UHCTeamManager.get().addTeams(santa);
        UHCTeamManager.get().deleteTeams();
    }

    @Override
    public void onSec(Player p) {
        UHCPlayer player = UHCPlayerManager.get().getPlayer(p);
        if (santa.getPlayers().contains(player)) {
            UHCUtils.applyInfiniteEffects(santaEffect(), p);
        }
    }

    @Override
    public boolean isWin() {
        if (santa.getPlayers().isEmpty()) {
            return UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() == 1;
        }
        return false;
    }

    @Override
    public void onTeamUpdate() {
        createSantaTeam();
    }
}
