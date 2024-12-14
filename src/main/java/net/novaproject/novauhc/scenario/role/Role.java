package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.InvocationTargetException;

public abstract class Role<T extends Role<T>> {

    public abstract String getName();
    public abstract String getDescription();

    public T duplicate() {
        try {
            // Obtenir le constructeur par défaut et créer une nouvelle instance
            return (T) getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to duplicate instance of " + getClass().getName(), e);
        }
    }


    public void onGive(UHCPlayer uhcPlayer) {

        Player player = uhcPlayer.getPlayer();

        player.sendMessage(getDescription());

    }

    public void onSec(Player player){

    }

    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {

    }

}
