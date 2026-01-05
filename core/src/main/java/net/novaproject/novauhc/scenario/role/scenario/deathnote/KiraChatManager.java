package net.novaproject.novauhc.scenario.role.scenario.deathnote;

import net.novaproject.novauhc.scenario.role.scenario.deathnote.roles.DeathNoteRole;
import net.novaproject.novauhc.scenario.role.scenario.deathnote.roles.MelloRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire du chat privé entre les Kira
 * Permet aux Kira de communiquer secrètement via la commande /k
 *
 * @author NovaProject
 * @version 1.0
 */
public class KiraChatManager {

    private final DeathNote scenario;
    private final List<UHCPlayer> kiraTeamMembers = new ArrayList<>();

    public KiraChatManager(DeathNote scenario) {
        this.scenario = scenario;
    }

    /**
     * Initialise le chat Kira en identifiant tous les traîtres de toutes les équipes
     */
    public void initializeKiraChat() {
        kiraTeamMembers.clear();

        // Identifier tous les joueurs qui peuvent utiliser le chat Kira (traîtres de toutes les équipes)
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            DeathNoteRole role = scenario.getRoleByUHCPlayer(player);
            if (role != null && role.canUseKiraChat()) {
                kiraTeamMembers.add(player);
            }
        }

        // Annoncer aux traîtres qu'ils peuvent communiquer entre équipes
        if (!kiraTeamMembers.isEmpty()) {
            announceKiraTeamFormation();
        }
    }

    /**
     * Annonce la formation de l'équipe Kira
     */
    private void announceKiraTeamFormation() {
        for (UHCPlayer kira : kiraTeamMembers) {
            Player player = kira.getPlayer();
            player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            player.sendMessage("§c§l[ÉQUIPE KIRA]");
            player.sendMessage("");
            player.sendMessage("§fVous faites partie de l'équipe Kira !");
            player.sendMessage("§fMembres de votre équipe :");

            for (UHCPlayer teammate : kiraTeamMembers) {
                if (!teammate.equals(kira)) {
                    DeathNoteRole teammateRole = scenario.getRoleByUHCPlayer(teammate);
                    String roleName = teammateRole != null ? teammateRole.getName() : "Inconnu";
                    player.sendMessage("§8• §c" + teammate.getPlayer().getName() + " §7(" + roleName + ")");
                }
            }

            player.sendMessage("");
            player.sendMessage("§fUtilisez §a/k <message> §fpour communiquer avec votre équipe.");
            player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        }
    }

    /**
     * Gère les messages du chat Kira
     *
     * @param sender Le joueur qui envoie le message
     * @param args   Les arguments du message
     */
    public void handleKiraChat(UHCPlayer sender, String[] args) {
        // Vérifier que le joueur peut utiliser le chat Kira
        if (!canUseKiraChat(sender)) {
            sender.getPlayer().sendMessage("§c[Kira Chat] §fVous n'avez pas accès au chat Kira !");
            return;
        }

        // Vérifier qu'il y a un message
        if (args.length == 0) {
            sender.getPlayer().sendMessage("§c[Kira Chat] §fUsage : /k <message>");
            return;
        }

        // Construire le message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

        // Envoyer le message à tous les membres de l'équipe Kira
        sendKiraMessage(sender, message);
    }

    /**
     * Envoie un message à tous les membres de l'équipe Kira
     *
     * @param sender  Le joueur qui envoie le message
     * @param message Le message à envoyer
     */
    private void sendKiraMessage(UHCPlayer sender, String message) {
        // Format du message
        String senderRole = getRoleDisplayName(sender);
        String formattedMessage = "§8[§cKira§8] §c" + sender.getPlayer().getName() +
                " §7(" + senderRole + ")§8: §f" + message;

        // Envoyer à tous les membres de l'équipe Kira
        for (UHCPlayer kira : kiraTeamMembers) {
            if (kira.getPlayer() != null && kira.getPlayer().isOnline()) {
                kira.getPlayer().sendMessage(formattedMessage);
            }
        }

        // Log pour les administrateurs (optionnel)
        logKiraMessage(sender, message);
    }

    /**
     * Vérifie si un joueur peut utiliser le chat Kira
     */
    private boolean canUseKiraChat(UHCPlayer player) {
        DeathNoteRole role = scenario.getRoleByUHCPlayer(player);
        return role != null && role.canUseKiraChat();
    }

    /**
     * Retourne le nom d'affichage du rôle pour le chat
     */
    private String getRoleDisplayName(UHCPlayer player) {
        DeathNoteRole role = scenario.getRoleByUHCPlayer(player);
        if (role == null) return "Inconnu";

        if (role instanceof MelloRole mello) {
            return "Mello " + mello.getCurrentForm().getName();
        }

        return role.getName();
    }

    /**
     * Log les messages Kira pour les administrateurs
     */
    private void logKiraMessage(UHCPlayer sender, String message) {
        // TODO: Implémenter le logging si nécessaire
        // Par exemple, envoyer aux spectateurs ou sauvegarder dans un fichier
    }

    /**
     * Met à jour la liste des membres de l'équipe Kira
     * Appelé quand un joueur change de rôle (ex: Mello qui change de forme)
     */
    public void updateKiraTeamMembers() {
        List<UHCPlayer> newKiraMembers = new ArrayList<>();

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            DeathNoteRole role = scenario.getRoleByUHCPlayer(player);
            if (role != null && role.canUseKiraChat()) {
                newKiraMembers.add(player);
            }
        }

        // Vérifier s'il y a des changements
        boolean hasChanges = !kiraTeamMembers.equals(newKiraMembers);

        if (hasChanges) {
            // Annoncer les changements aux membres existants
            announceTeamChanges(kiraTeamMembers, newKiraMembers);

            // Mettre à jour la liste
            kiraTeamMembers.clear();
            kiraTeamMembers.addAll(newKiraMembers);
        }
    }

    /**
     * Annonce les changements dans l'équipe Kira
     */
    private void announceTeamChanges(List<UHCPlayer> oldMembers, List<UHCPlayer> newMembers) {
        // Trouver les nouveaux membres
        List<UHCPlayer> joinedMembers = new ArrayList<>(newMembers);
        joinedMembers.removeAll(oldMembers);

        // Trouver les membres qui ont quitté
        List<UHCPlayer> leftMembers = new ArrayList<>(oldMembers);
        leftMembers.removeAll(newMembers);

        // Annoncer aux membres actuels
        for (UHCPlayer kira : newMembers) {
            Player player = kira.getPlayer();

            for (UHCPlayer joined : joinedMembers) {
                if (!joined.equals(kira)) {
                    String roleName = getRoleDisplayName(joined);
                    player.sendMessage("§c[Kira Chat] §a" + joined.getPlayer().getName() +
                            " §7(" + roleName + ") §aa rejoint l'équipe Kira !");
                }
            }

            for (UHCPlayer left : leftMembers) {
                if (!left.equals(kira)) {
                    String roleName = getRoleDisplayName(left);
                    player.sendMessage("§c[Kira Chat] §c" + left.getPlayer().getName() +
                            " §7(" + roleName + ") §ca quitté l'équipe Kira !");
                }
            }
        }
    }

    /**
     * Envoie un message système au chat Kira
     *
     * @param message Le message système à envoyer
     */
    public void sendSystemMessage(String message) {
        String formattedMessage = "§8[§cKira System§8] §7" + message;

        for (UHCPlayer kira : kiraTeamMembers) {
            if (kira.getPlayer() != null && kira.getPlayer().isOnline()) {
                kira.getPlayer().sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Annonce la mort d'un membre de l'équipe Kira
     *
     * @param deadKira Le membre Kira qui est mort
     */
    public void announceKiraDeath(UHCPlayer deadKira) {
        String roleName = getRoleDisplayName(deadKira);
        sendSystemMessage("§c" + deadKira.getPlayer().getName() + " §7(" + roleName + ") §fest mort !");

        // Retirer de la liste
        kiraTeamMembers.remove(deadKira);

        // Vérifier s'il reste des Kira
        if (kiraTeamMembers.isEmpty()) {
            // Plus de Kira vivants - fin de partie possible
            sendSystemMessage("§cPlus aucun Kira en vie...");
        }
    }

    /**
     * Retourne la liste des membres de l'équipe Kira
     */
    public List<UHCPlayer> getKiraTeamMembers() {
        return new ArrayList<>(kiraTeamMembers);
    }

    /**
     * Retourne le nombre de Kira vivants
     */
    public int getAliveKiraCount() {
        int count = 0;
        for (UHCPlayer kira : kiraTeamMembers) {
            if (!scenario.getDeadPlayers().contains(kira)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Vérifie s'il y a encore des Kira vivants
     */
    public boolean hasAliveKira() {
        return getAliveKiraCount() > 0;
    }
}
