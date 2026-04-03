package net.novaproject.novauhc.listener.player;

import com.lunarclient.apollo.event.player.ApolloRegisterPlayerEvent;
import com.lunarclient.apollo.event.player.ApolloUnregisterPlayerEvent;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lunar.LunarApolloManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LunarApolloListener implements Listener {

    /**
     * Quand un joueur se connecte avec Lunar Client (Apollo activé),
     * on lui envoie les TeamArrows de son équipe si la partie est en cours.
     */
    @EventHandler
    public void onApolloRegister(ApolloRegisterPlayerEvent event) {
        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) return;

        Player player = Bukkit.getPlayer(event.getApolloPlayer().getUniqueId());
        if (player == null) return;

        LunarApolloManager.get().updateTeamArrows(player);
    }

    /**
     * Quand un joueur quitte Lunar Client ou se déconnecte,
     * on nettoie les données Apollo côté serveur.
     */
    @EventHandler
    public void onApolloUnregister(ApolloUnregisterPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getApolloPlayer().getUniqueId());
        if (player == null) return;

        LunarApolloManager.get().clearPlayer(player);
    }
}
