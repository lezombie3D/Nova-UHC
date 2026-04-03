package net.novaproject.novauhc.lunar;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.icon.ItemStackIcon;
import com.lunarclient.apollo.module.cooldown.Cooldown;
import com.lunarclient.apollo.module.cooldown.CooldownModule;
import com.lunarclient.apollo.module.teamarrow.TeamArrow;
import com.lunarclient.apollo.module.teamarrow.TeamArrowModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.time.Duration;
import java.util.Optional;

public class LunarApolloManager {

    private static LunarApolloManager instance;
    private boolean available = false;

    public static LunarApolloManager get() {
        return instance;
    }

    public LunarApolloManager() {
        instance = this;
    }

    public void setup() {
        if (Bukkit.getPluginManager().getPlugin("Apollo-Bukkit") != null) {
            available = true;
            Bukkit.getLogger().info("[Nova-UHC] Lunar Apollo détecté - compatibilité activable via le menu host.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    private boolean isEnabled() {
        return available && UHCManager.get().isLunarApollo();
    }

    /**
     * Met à jour les TeamArrows pour tous les joueurs Lunar Client en jeu.
     * À appeler au début de la partie ou après un changement d'équipe.
     */
    public void updateAllTeamArrows() {
        if (!isEnabled()) return;
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (player != null) {
                updateTeamArrows(player);
            }
        }
    }

    /**
     * Met à jour les TeamArrows pour un joueur spécifique :
     * affiche une flèche vers chaque coéquipier en vie.
     */
    public void updateTeamArrows(Player player) {
        if (!isEnabled()) return;

        Optional<ApolloPlayer> apolloOpt = Apollo.getPlayerManager().getApolloPlayer(player.getUniqueId());
        if (apolloOpt.isEmpty()) return;
        ApolloPlayer apolloPlayer = apolloOpt.get();

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || uhcPlayer.getTeam().isEmpty()) return;

        UHCTeam team = uhcPlayer.getTeam().get();
        TeamArrowModule module = Apollo.getModuleManager().getModule(TeamArrowModule.class);

        module.resetTeamArrows(apolloPlayer);

        Color teamColor = dyeToColor(team.dyeColor());
        for (UHCPlayer teammate : team.getPlayers()) {
            if (teammate.getUuid().equals(player.getUniqueId())) continue;
            if (!teammate.isPlaying()) continue;

            module.overrideTeamArrow(apolloPlayer, TeamArrow.builder()
                    .playerUuid(teammate.getUuid())
                    .teamColor(teamColor)
                    .build());
        }
    }

    /**
     * Retire la flèche du joueur mort chez tous ses coéquipiers sur Lunar Client.
     */
    public void onPlayerDeath(Player player) {
        if (!isEnabled()) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || uhcPlayer.getTeam().isEmpty()) return;

        UHCTeam team = uhcPlayer.getTeam().get();
        TeamArrowModule module = Apollo.getModuleManager().getModule(TeamArrowModule.class);

        for (UHCPlayer teammate : team.getPlayers()) {
            if (teammate.getUuid().equals(player.getUniqueId())) continue;
            Apollo.getPlayerManager().getApolloPlayer(teammate.getUuid()).ifPresent(apolloTeammate ->
                    module.resetTeamArrow(apolloTeammate, player.getUniqueId()));
        }
    }

    /**
     * Affiche un cooldown sur le HUD Lunar Client du joueur.
     *
     * @param player   le joueur ciblé
     * @param name     identifiant unique du cooldown
     * @param seconds  durée en secondes
     * @param material icône à afficher (nom de l'item vanilla)
     */
    public void displayCooldown(Player player, String name, int seconds, Material material) {
        if (!isEnabled()) return;
        if (seconds <= 0) return;

        Apollo.getPlayerManager().getApolloPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
            CooldownModule module = Apollo.getModuleManager().getModule(CooldownModule.class);
            module.displayCooldown(apolloPlayer, Cooldown.builder()
                    .name(name)
                    .duration(Duration.ofSeconds(seconds))
                    .icon(ItemStackIcon.builder()
                            .itemName(material.name())
                            .build())
                    .build());
        });
    }

    /**
     * Retire un cooldown du HUD Lunar Client du joueur.
     */
    public void removeCooldown(Player player, String name) {
        if (!isEnabled()) return;
        Apollo.getPlayerManager().getApolloPlayer(player.getUniqueId()).ifPresent(apolloPlayer ->
                Apollo.getModuleManager().getModule(CooldownModule.class).removeCooldown(apolloPlayer, name));
    }

    /**
     * Retire tous les cooldowns et TeamArrows Apollo d'un joueur
     * (appelé à la fin de la partie ou à la déconnexion).
     */
    public void clearPlayer(Player player) {
        if (!available) return;
        Apollo.getPlayerManager().getApolloPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
            Apollo.getModuleManager().getModule(TeamArrowModule.class).resetTeamArrows(apolloPlayer);
            Apollo.getModuleManager().getModule(CooldownModule.class).resetCooldowns(apolloPlayer);
        });
    }

    private Color dyeToColor(DyeColor dye) {
        switch (dye) {
            case RED:        return new Color(200, 0, 0);
            case BLUE:       return new Color(0, 0, 200);
            case GREEN:      return new Color(0, 150, 0);
            case YELLOW:     return new Color(230, 230, 0);
            case PURPLE:     return new Color(128, 0, 128);
            case PINK:       return new Color(255, 105, 180);
            case WHITE:      return Color.WHITE;
            case GRAY:       return Color.GRAY;
            case LIGHT_BLUE: return new Color(100, 200, 255);
            case ORANGE:     return new Color(255, 140, 0);
            case CYAN:       return new Color(0, 200, 200);
            case LIME:       return new Color(50, 220, 50);
            case MAGENTA:    return new Color(200, 0, 200);
            default:         return Color.WHITE;
        }
    }
}
