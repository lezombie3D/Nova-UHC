package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class Role {

    public abstract String getName();
    public abstract String getDescription();
    public abstract String getCamps();

    public abstract ChatColor getColor();

    public abstract List<Integer> getPowerUse();

    public abstract ItemCreator getItem();

    public void onGive(UHCPlayer uhcPlayer) {

        Player player = uhcPlayer.getPlayer();

        player.sendMessage(getDescription());
    }


    public void onSec(Player player){

    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }

    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {

    }

    public void onIteract(Player player1, PlayerInteractEvent event) {

    }

    public void onMove(UHCPlayer player1, PlayerMoveEvent event) {

    }

    public void onFfCMD(UHCPlayer player1, String subCommand, String[] args) {

    }

    public void onSetup() {

    }
}
