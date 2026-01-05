package net.novaproject.novauhc.utils;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.border.Border;
import com.lunarclient.apollo.module.border.BorderModule;
import com.lunarclient.apollo.module.cooldown.Cooldown;
import com.lunarclient.apollo.module.cooldown.CooldownModule;
import com.lunarclient.apollo.module.notification.Notification;
import com.lunarclient.apollo.module.notification.NotificationModule;
import com.lunarclient.apollo.module.team.TeamMember;
import com.lunarclient.apollo.module.team.TeamModule;
import com.lunarclient.apollo.module.title.Title;
import com.lunarclient.apollo.module.title.TitleModule;
import com.lunarclient.apollo.module.title.TitleType;
import com.lunarclient.apollo.player.ApolloPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bibliothèque d'intégration Apollo pour NovaUHC
 *
 * Apollo est l'API officielle de Lunar Client permettant aux serveurs d'interagir
 * avec les fonctionnalités client et d'offrir une expérience améliorée aux joueurs.
 *
 * Cette classe fournit des méthodes utilitaires pour utiliser Apollo dans le contexte UHC:
 * - Team (gestion des équipes avec marqueurs visuels)
 * - Notifications (messages visuels)
 * - Titles (titres et sous-titres)
 * - Border (bordures personnalisées)
 * - Cooldowns (affichage de cooldowns personnalisés)
 *
 * @author NovaProject
 * @version 2.0
 * @see <a href="https://lunarclient.dev/apollo/introduction">Documentation Apollo</a>
 */
public class ApolloUtils {

    private static final Logger LOGGER = Logger.getLogger("NovaUHC-Apollo");
    private static boolean apolloAvailable = false;

    // Modules Apollo
    private static TeamModule teamModule;
    private static NotificationModule notificationModule;
    private static TitleModule titleModule;
    private static BorderModule borderModule;
    private static CooldownModule cooldownModule;

    // Gestion des équipes
    private static final Map<UUID, ApolloTeam> teamsByTeamId = new HashMap<>();
    private static final Map<UUID, ApolloTeam> teamsByPlayerUuid = new HashMap<>();

    /**
     * Initialise l'intégration Apollo
     * Doit être appelé dans le onEnable() du plugin
     *
     * @return true si Apollo est disponible et initialisé
     */
    public static boolean initialize() {
        try {
            Class.forName("com.lunarclient.apollo.Apollo");

            // Récupération des modules
            teamModule = Apollo.getModuleManager().getModule(TeamModule.class);
            notificationModule = Apollo.getModuleManager().getModule(NotificationModule.class);
            titleModule = Apollo.getModuleManager().getModule(TitleModule.class);
            borderModule = Apollo.getModuleManager().getModule(BorderModule.class);
            cooldownModule = Apollo.getModuleManager().getModule(CooldownModule.class);

            apolloAvailable = true;
            LOGGER.log(Level.INFO, "Apollo integration successfully initialized!");

            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Apollo API not found - Enhanced features disabled");
            apolloAvailable = false;
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing Apollo integration", e);
            apolloAvailable = false;
            return false;
        }
    }

    /**
     * Vérifie si Apollo est disponible
     *
     * @return true si Apollo est chargé et fonctionnel
     */
    public static boolean isAvailable() {
        return apolloAvailable;
    }

    /**
     * Récupère un ApolloPlayer à partir d'un Player Bukkit
     *
     * @param player Le joueur Bukkit
     * @return Optional contenant l'ApolloPlayer si disponible
     */
    private static Optional<ApolloPlayer> getApolloPlayer(Player player) {
        if (!apolloAvailable || player == null) {
            return Optional.empty();
        }
        return Apollo.getPlayerManager().getPlayer(player.getUniqueId());
    }

    // ==================== TEAMS ====================

    /**
     * Classe interne représentant une équipe Apollo
     */
    public static class ApolloTeam {
        private final UUID teamId;
        private final Map<UUID, TeamMember> members;

        private ApolloTeam() {
            this.teamId = UUID.randomUUID();
            this.members = new HashMap<>();
        }

        public UUID getTeamId() {
            return teamId;
        }

        public Collection<TeamMember> getMembers() {
            return members.values();
        }

        /**
         * Ajoute un membre à l'équipe
         *
         * @param player Le joueur à ajouter
         * @param displayName Nom affiché au-dessus de la tête
         * @param markerColor Couleur du marqueur
         */
        public void addMember(Player player, Component displayName, Color markerColor) {
            if (!apolloAvailable) return;

            Location loc = player.getLocation();
            TeamMember member = TeamMember.builder()
                .playerUuid(player.getUniqueId())
                .displayName(displayName)
                .markerColor(markerColor)
                .location(com.lunarclient.apollo.common.location.ApolloLocation.builder()
                    .world(loc.getWorld().getName())
                    .x(loc.getX())
                    .y(loc.getY())
                    .z(loc.getZ())
                    .build())
                .build();

            members.put(player.getUniqueId(), member);
            teamsByPlayerUuid.put(player.getUniqueId(), this);
            refresh();
        }

        /**
         * Retire un membre de l'équipe
         *
         * @param playerUuid UUID du joueur à retirer
         */
        public void removeMember(UUID playerUuid) {
            members.remove(playerUuid);
            teamsByPlayerUuid.remove(playerUuid);

            // Si l'équipe est vide, la supprimer
            if (members.isEmpty()) {
                deleteTeam(this.teamId);
            } else {
                refresh();
            }
        }

        /**
         * Met à jour la position d'un membre
         *
         * @param player Le joueur dont la position doit être mise à jour
         */
        public void updateMemberLocation(Player player) {
            if (!apolloAvailable) return;

            TeamMember existingMember = members.get(player.getUniqueId());
            if (existingMember == null) return;

            Location loc = player.getLocation();
            TeamMember updatedMember = TeamMember.builder()
                .playerUuid(player.getUniqueId())
                .displayName(existingMember.getDisplayName())
                .markerColor(existingMember.getMarkerColor())
                .location(com.lunarclient.apollo.common.location.ApolloLocation.builder()
                    .world(loc.getWorld().getName())
                    .x(loc.getX())
                    .y(loc.getY())
                    .z(loc.getZ())
                    .build())
                .build();

            members.put(player.getUniqueId(), updatedMember);
        }

        /**
         * Rafraîchit l'affichage de l'équipe pour tous les membres
         */
        public void refresh() {
            if (!apolloAvailable) return;

            for (UUID memberUuid : members.keySet()) {
                Apollo.getPlayerManager().getPlayer(memberUuid).ifPresent(apolloPlayer -> {
                    teamModule.updateTeamMembers(apolloPlayer, new ArrayList<>(members.values()));
                });
            }
        }
    }

    /**
     * Crée une nouvelle équipe Apollo
     *
     * @return La nouvelle équipe créée
     */
    public static ApolloTeam createTeam() {
        if (!apolloAvailable) return null;

        ApolloTeam team = new ApolloTeam();
        teamsByTeamId.put(team.getTeamId(), team);
        return team;
    }

    /**
     * Récupère une équipe par son ID
     *
     * @param teamId ID de l'équipe
     * @return Optional contenant l'équipe si elle existe
     */
    public static Optional<ApolloTeam> getTeamById(UUID teamId) {
        return Optional.ofNullable(teamsByTeamId.get(teamId));
    }

    /**
     * Récupère l'équipe d'un joueur
     *
     * @param playerUuid UUID du joueur
     * @return Optional contenant l'équipe du joueur si elle existe
     */
    public static Optional<ApolloTeam> getTeamByPlayer(UUID playerUuid) {
        return Optional.ofNullable(teamsByPlayerUuid.get(playerUuid));
    }

    /**
     * Supprime une équipe
     *
     * @param teamId ID de l'équipe à supprimer
     */
    public static void deleteTeam(UUID teamId) {
        if (!apolloAvailable) return;

        ApolloTeam team = teamsByTeamId.remove(teamId);
        if (team != null) {
            // Nettoyer les références des joueurs
            for (UUID playerUuid : team.members.keySet()) {
                teamsByPlayerUuid.remove(playerUuid);

                // Retirer l'équipe du client
                Apollo.getPlayerManager().getPlayer(playerUuid).ifPresent(apolloPlayer -> {
                    teamModule.resetTeamMembers(apolloPlayer);
                });
            }
        }
    }

    /**
     * Supprime toutes les équipes
     */
    public static void deleteAllTeams() {
        if (!apolloAvailable) return;

        new ArrayList<>(teamsByTeamId.keySet()).forEach(ApolloUtils::deleteTeam);
    }

    /**
     * Met à jour les positions de tous les membres de toutes les équipes
     * À appeler périodiquement (ex: toutes les secondes)
     */
    public static void updateAllTeamLocations() {
        if (!apolloAvailable) return;

        for (ApolloTeam team : teamsByTeamId.values()) {
            boolean updated = false;

            for (UUID memberUuid : team.members.keySet()) {
                Player player = org.bukkit.Bukkit.getPlayer(memberUuid);
                if (player != null && player.isOnline()) {
                    team.updateMemberLocation(player);
                    updated = true;
                }
            }

            if (updated) {
                team.refresh();
            }
        }
    }

    // ==================== NOTIFICATIONS ====================

    /**
     * Affiche une notification à un joueur
     * Parfait pour les annonces importantes: début de partie, PvP activé, meetup, etc.
     *
     * @param player Le joueur
     * @param title Titre de la notification (max 15 caractères)
     * @param description Description (peut contenir plusieurs lignes)
     * @param iconPath Chemin de l'icône (optionnel, ex: "icons/golden_apple.png")
     * @param duration Durée d'affichage
     */
    public static void sendNotification(Player player, String title, String description,
                                       String iconPath, Duration duration) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                Notification.NotificationBuilder builder = Notification.builder()
                    .titleComponent(Component.text(title))
                    .descriptionComponent(Component.text(description))
                    .displayTime(duration);

                if (iconPath != null && !iconPath.isEmpty()) {
                    builder.resourceLocation(iconPath);
                }

                notificationModule.displayNotification(apolloPlayer, builder.build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error sending notification to " + player.getName(), e);
            }
        });
    }

    /**
     * Affiche une notification colorée à un joueur
     *
     * @param player Le joueur
     * @param title Titre (composant Adventure avec couleurs)
     * @param description Description (composant Adventure avec couleurs)
     * @param iconPath Chemin de l'icône
     * @param duration Durée d'affichage
     */
    public static void sendNotification(Player player, Component title, Component description,
                                       String iconPath, Duration duration) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                Notification.NotificationBuilder builder = Notification.builder()
                    .titleComponent(title)
                    .descriptionComponent(description)
                    .displayTime(duration);

                if (iconPath != null && !iconPath.isEmpty()) {
                    builder.resourceLocation(iconPath);
                }

                notificationModule.displayNotification(apolloPlayer, builder.build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error sending notification to " + player.getName(), e);
            }
        });
    }

    /**
     * Envoie une notification de début de partie
     *
     * @param player Le joueur
     */
    public static void sendGameStartNotification(Player player) {
        sendNotification(player,
            Component.text("UHC Started!", NamedTextColor.GREEN),
            Component.text("Good luck!"),
            null,
            Duration.ofSeconds(5));
    }

    // ==================== TITLES ====================

    /**
     * Affiche un titre à un joueur
     *
     * @param player Le joueur
     * @param message Message du titre
     * @param color Couleur du texte
     * @param bold Si true, texte en gras
     * @param scale Échelle du titre (1.0 = normal)
     * @param displayTime Durée d'affichage
     * @param fadeIn Durée du fade in
     * @param fadeOut Durée du fade out
     */
    public static void displayTitle(Player player, String message, NamedTextColor color,
                                   boolean bold, float scale,
                                   Duration displayTime, Duration fadeIn, Duration fadeOut) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                Component messageComponent;
                if (bold) {
                    messageComponent = Component.text(message, color, TextDecoration.BOLD);
                } else {
                    messageComponent = Component.text(message, color);
                }

                titleModule.displayTitle(apolloPlayer, Title.builder()
                    .type(TitleType.TITLE)
                    .message(messageComponent)
                    .scale(scale)
                    .displayTime(displayTime)
                    .fadeInTime(fadeIn)
                    .fadeOutTime(fadeOut)
                    .build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error displaying title to " + player.getName(), e);
            }
        });
    }

    /**
     * Affiche un sous-titre à un joueur
     *
     * @param player Le joueur
     * @param message Message du sous-titre
     * @param color Couleur du texte
     * @param displayTime Durée d'affichage
     */
    public static void displaySubtitle(Player player, String message, NamedTextColor color,
                                      Duration displayTime) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                titleModule.displayTitle(apolloPlayer, Title.builder()
                    .type(TitleType.SUBTITLE)
                    .message(Component.text(message, color))
                    .scale(1.0f)
                    .displayTime(displayTime)
                    .fadeInTime(Duration.ofMillis(250))
                    .fadeOutTime(Duration.ofMillis(250))
                    .build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error displaying subtitle to " + player.getName(), e);
            }
        });
    }

    /**
     * Affiche un titre qui s'agrandit (effet d'animation)
     *
     * @param player Le joueur
     * @param message Message du titre
     * @param color Couleur
     * @param fromScale Échelle de départ
     * @param toScale Échelle d'arrivée
     * @param interpolationRate Vitesse d'interpolation (0.01 = lent, 0.1 = rapide)
     * @param displayTime Durée d'affichage
     */
    public static void displayInterpolatedTitle(Player player, String message, NamedTextColor color,
                                               float fromScale, float toScale, float interpolationRate,
                                               Duration displayTime) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                titleModule.displayTitle(apolloPlayer, Title.builder()
                    .type(TitleType.TITLE)
                    .message(Component.text(message, color, TextDecoration.BOLD))
                    .scale(fromScale)
                    .interpolationScale(toScale)
                    .interpolationRate(interpolationRate)
                    .displayTime(displayTime)
                    .fadeInTime(Duration.ofMillis(250))
                    .fadeOutTime(Duration.ofMillis(300))
                    .build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error displaying interpolated title to " + player.getName(), e);
            }
        });
    }

    /**
     * Efface les titres d'un joueur
     *
     * @param player Le joueur
     */
    public static void resetTitles(Player player) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                titleModule.resetTitles(apolloPlayer);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error resetting titles for " + player.getName(), e);
            }
        });
    }

    // ==================== BORDERS ====================

    /**
     * Affiche une bordure personnalisée pour un joueur
     * Permet de créer des zones restreintes (spawn PvP tag, arène gladiator, etc.)
     *
     * @param player Le joueur
     * @param id Identifiant unique de la bordure
     * @param world Nom du monde
     * @param minX Coordonnée X minimale
     * @param minZ Coordonnée Z minimale
     * @param maxX Coordonnée X maximale
     * @param maxZ Coordonnée Z maximale
     * @param color Couleur de la bordure
     * @param cancelEntry Si true, empêche l'entrée
     * @param cancelExit Si true, empêche la sortie
     * @param durationTicks Durée en ticks (pour animation)
     */
    public static void displayBorder(Player player, String id, String world,
                                    int minX, int minZ, int maxX, int maxZ,
                                    Color color, boolean cancelEntry, boolean cancelExit,
                                    int durationTicks) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                borderModule.displayBorder(apolloPlayer, Border.builder()
                    .id(id)
                    .world(world)
                    .cancelEntry(cancelEntry)
                    .cancelExit(cancelExit)
                    .canShrinkOrExpand(false)
                    .color(color)
                    .bounds(com.lunarclient.apollo.common.cuboid.Cuboid2D.builder()
                        .minX(minX)
                        .minZ(minZ)
                        .maxX(maxX)
                        .maxZ(maxZ)
                        .build())
                    .durationTicks(durationTicks)
                    .build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error displaying border to " + player.getName(), e);
            }
        });
    }

    /**
     * Supprime une bordure pour un joueur
     *
     * @param player Le joueur
     * @param id Identifiant de la bordure
     */
    public static void removeBorder(Player player, String id) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                borderModule.removeBorder(apolloPlayer, id);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error removing border for " + player.getName(), e);
            }
        });
    }

    /**
     * Supprime toutes les bordures d'un joueur
     *
     * @param player Le joueur
     */
    public static void resetBorders(Player player) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                borderModule.resetBorders(apolloPlayer);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error resetting borders for " + player.getName(), e);
            }
        });
    }

    // ==================== COOLDOWNS ====================

    /**
     * Affiche un cooldown personnalisé pour un joueur
     * Utile pour afficher les cooldowns des abilities, scenarios, etc.
     *
     * @param player Le joueur
     * @param name Nom du cooldown (affiché au joueur)
     * @param durationMs Durée en millisecondes
     * @param iconPath Chemin de l'icône (optionnel, ex: "textures/items/diamond_sword.png")
     */
    public static void displayCooldown(Player player, String name, long durationMs, String iconPath) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                Cooldown.CooldownBuilder builder = Cooldown.builder()
                    .name(name)
                    .duration(Duration.ofMillis(durationMs));

                if (iconPath != null && !iconPath.isEmpty()) {
                    builder.icon(com.lunarclient.apollo.common.icon.ItemStackIcon.builder()
                        .itemName(iconPath)
                        .build());
                }

                cooldownModule.displayCooldown(apolloPlayer, builder.build());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error displaying cooldown to " + player.getName(), e);
            }
        });
    }

    /**
     * Supprime un cooldown pour un joueur
     *
     * @param player Le joueur
     * @param name Nom du cooldown
     */
    public static void removeCooldown(Player player, String name) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                cooldownModule.removeCooldown(apolloPlayer, name);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error removing cooldown for " + player.getName(), e);
            }
        });
    }

    /**
     * Supprime tous les cooldowns d'un joueur
     *
     * @param player Le joueur
     */
    public static void resetCooldowns(Player player) {
        if (!apolloAvailable) return;

        getApolloPlayer(player).ifPresent(apolloPlayer -> {
            try {
                cooldownModule.resetCooldowns(apolloPlayer);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error resetting cooldowns for " + player.getName(), e);
            }
        });
    }

    // ==================== UTILITY COLORS ====================

    /**
     * Couleurs prédéfinies pour faciliter l'utilisation
     */
    public static class Colors {
        public static final Color UHC_RED = new Color(220, 20, 60);
        public static final Color UHC_BLUE = new Color(30, 144, 255);
        public static final Color UHC_GREEN = new Color(50, 205, 50);
        public static final Color UHC_YELLOW = new Color(255, 215, 0);
        public static final Color UHC_ORANGE = new Color(255, 140, 0);
        public static final Color UHC_PURPLE = new Color(138, 43, 226);
        public static final Color UHC_PINK = new Color(255, 105, 180);
        public static final Color UHC_WHITE = new Color(255, 255, 255);
        public static final Color UHC_BLACK = new Color(0, 0, 0);
        public static final Color UHC_GOLD = new Color(255, 215, 0);
        public static final Color UHC_CYAN = new Color(0, 255, 255);
        public static final Color MEETUP_COLOR = new Color(255, 69, 0);
        public static final Color SPAWN_COLOR = new Color(124, 252, 0);
        public static final Color BORDER_WARNING = new Color(255, 0, 0);
    }
}
