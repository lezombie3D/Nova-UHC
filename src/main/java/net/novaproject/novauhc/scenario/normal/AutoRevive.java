package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Optional;

public class AutoRevive extends Scenario {
    private boolean actived = true;

    @Override
    public String getName() {
        return "AutoRevive";
    }

    @Override
    public String getDescription() {
        return "Les joueurs ressuscitent automatiquement une fois par partie.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_SWORD);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (actived) {
            event.getDrops().clear();
            Collection<PotionEffect> effect = uhcPlayer.getPlayer().getActivePotionEffects();
            Location location = uhcPlayer.getPlayer().getLocation();
            Player player = uhcPlayer.getPlayer();

            if (UHCManager.get().getTeam_size() != 1) {
                if (uhcPlayer.getTeam().isPresent()) {
                    UHCTeam team = uhcPlayer.getTeam().get();
                    uhcPlayer.setTeam(Optional.of(team));
                    TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), team.getName(), team.getPrefix(), "");
                    uhcPlayer.setPlaying(true);
                    uhcPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
                    uhcPlayer.getPlayer().teleport(location);
                    for (PotionEffect e : effect) {
                        uhcPlayer.getPlayer().addPotionEffect(e);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + " Impossible car la game ont des équipes hors le target n'en a pas");
                    return;
                }
            } else {
                TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), "", "", "");
                uhcPlayer.setPlaying(true);
                uhcPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
                uhcPlayer.getPlayer().teleport(location);
            }

            Bukkit.broadcastMessage(Common.get().getInfoTag() + uhcPlayer.getPlayer().getDisplayName() + " a été ressusciter automatiquement ! ");
        }
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int pvptimer = UHCManager.get().getTimerpvp();

        if (timer == pvptimer) {
            actived = false;
            Common.get().getArena().setGameRuleValue("keepInventory", "false");

            Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.GOLD + "Fin des revive automatique");
        }
    }

    @Override
    public void onStart(Player player) {
        Common.get().getArena().setGameRuleValue("keepInventory", "true");
    }
}

