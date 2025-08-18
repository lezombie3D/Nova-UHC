package net.novaproject.novauhc.scenario.special.superheros;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;

public class SuperHeros extends Scenario {
    private final HashMap<Player, Integer> superHeros = new HashMap<>();

    @Override
    public String getName() {
        return "SuperHeros";
    }

    @Override
    public String getDescription() {
        return "Les joueurs reçoivent des pouvoirs de super-héros aléatoires.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }
    }

    @Override
    public void onStart(Player player) {
        superHeros.clear();
        Random ran = new Random();
        int aleatoire = ran.nextInt(5);
        superHeros.put(player, aleatoire);

        if (aleatoire == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        }
        if (aleatoire == 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false));
        }
        if (aleatoire == 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        }
        if (aleatoire == 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        }
        if (aleatoire == 4) {
            player.setMaxHealth(40);
            player.setHealth(player.getMaxHealth());
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onChatSpeek(Player player, String message, AsyncPlayerChatEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.DARK_GRAY + "❯ "
                        + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de parler !");
            return;
        }

        if (UHCManager.get().isChatdisbale()) return;

        if (message.startsWith("!")) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                String formattedMessage = CommonString.CHAT_GLOBAL_SCENARIO_FORMAT.getMessage();
                formattedMessage = formattedMessage.replace("%player%", player.getName());
                formattedMessage = formattedMessage.replace("%message%", message.substring(1));
                lobby.getPlayer().sendMessage(formattedMessage);
            }
            return;
        }

        if (UHCManager.get().getTeam_size() == 1) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                String formattedMessage = CommonString.CHAT_SOLO_SCENARIO_FORMAT.getMessage();
                formattedMessage = formattedMessage.replace("%player%", player.getName());
                formattedMessage = formattedMessage.replace("%message%", message);
                lobby.getPlayer().sendMessage(formattedMessage);
            }
            return;
        }

        if (!uhcPlayer.getTeam().isPresent()) return;

        UHCTeam team = uhcPlayer.getTeam().get();
        for (UHCPlayer teamPlayer : team.getPlayers()) {
            teamPlayer.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "❖ Team ❖ "
                    + ChatColor.DARK_GRAY + player.getName() + " » "
                    + ChatColor.WHITE + message);
        }


    }


    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        if (superHeros.containsKey(player)) {
            int i = superHeros.get(player);

            if (i == 3 && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

}
