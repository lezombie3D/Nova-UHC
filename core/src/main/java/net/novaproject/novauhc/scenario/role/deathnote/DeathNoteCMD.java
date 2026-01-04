package net.novaproject.novauhc.scenario.role.deathnote;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.deathnote.roles.DeathNoteRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande principale pour le Death Note UHC
 * Gère toutes les sous-commandes liées au Death Note
 *
 * @author NovaProject
 * @version 1.0
 */
public class DeathNoteCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        // Vérifier que le scénario Death Note est actif
        if (!ScenarioManager.get().isScenarioActive("Death Note UHC")) {
            player.sendMessage(Common.get().getInfoTag() + "Le scénario Death Note UHC doit être actif pour utiliser ces commandes !");
            return false;
        }

        DeathNote scenario = ScenarioManager.get().getScenario(DeathNote.class);
        if (scenario == null) {
            player.sendMessage(ChatColor.RED + "Erreur : Scénario Death Note non trouvé !");
            return false;
        }

        String subCommand = args[0].toLowerCase();

        // Déléguer la commande au scénario Death Note
        scenario.onDeathNoteCMD(player, subCommand, args);

        return true;
    }

    /**
     * Envoie le message d'aide contextuel selon le rôle du joueur
     */
    private void sendHelpMessage(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        DeathNote scenario = ScenarioManager.get().getScenario(DeathNote.class);

        if (scenario == null) {
            player.sendMessage(ChatColor.RED + "Le scénario Death Note UHC n'est pas actif !");
            return;
        }

        DeathNoteRole role = scenario.getRoleByUHCPlayer(uhcPlayer);

        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        player.sendMessage("§c§l[DEATH NOTE UHC - COMMANDES]");
        player.sendMessage("");

        if (role != null) {
            player.sendMessage("§fVotre rôle : " + role.getColor() + "§l" + role.getName());
            player.sendMessage("§fCamp : " + role.getRoleCamp().getColor() + role.getRoleCamp().getName());
            player.sendMessage("");

            // Commandes spécifiques au rôle
            sendRoleSpecificHelp(player, role);
        } else {
            player.sendMessage("§7Vous n'avez pas encore de rôle assigné.");
            player.sendMessage("");
        }

        // Commandes générales
        player.sendMessage("§e§lCommandes générales :");
        player.sendMessage("§8• §a/dnote info §7- Informations sur votre rôle");
        player.sendMessage("§8• §a/dnote status §7- Statut de la partie");
        player.sendMessage("§8• §a/dnote help §7- Afficher cette aide");

        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }

    /**
     * Envoie l'aide spécifique au rôle du joueur
     */
    private void sendRoleSpecificHelp(Player player, DeathNoteRole role) {
        String roleName = role.getName().toLowerCase();

        switch (roleName) {
            case "kira":
                player.sendMessage("§c§lCommandes Kira :");
                player.sendMessage("§8• §c/dnote use <nom> §7- Utiliser le Death Note");
                player.sendMessage("§8• §c/dnote claim §7- Récupérer vos pouvoirs");
                player.sendMessage("§8• §c/dnote reveal §7- Révéler votre identité");
                player.sendMessage("§8• §c/dnote abandon §7- Activer le mode abandon");
                player.sendMessage("§8• §c/k <message> §7- Chat privé Kira");
                break;

            case "enquêteur":
            case "enqueteur":
                player.sendMessage("§a§lCommandes Enquêteur :");
                player.sendMessage("§8• §a/dnote investigate <nom> §7- Enquêter sur un joueur");
                player.sendMessage("§8• §a/dnote timers §7- Voir les timers Death Note actifs");
                break;

            case "near":
                player.sendMessage("§b§lCommandes Near :");
                player.sendMessage("§8• §b/dnote analyze <nom> §7- Analyser un joueur");
                player.sendMessage("§8• §b/dnote detect <nom> §7- Détecter si c'est un Kira");
                break;

            case "mello":
                player.sendMessage("§6§lCommandes Mello :");
                player.sendMessage("§8• §6/dnote form §7- Changer de forme");
                player.sendMessage("§8• §6/dnote reveal <nom> §7- Révéler votre camp");
                if (role.canUseKiraChat()) {
                    player.sendMessage("§8• §6/k <message> §7- Chat Kira (si méchant)");
                }
                break;

            case "shinigami":
                player.sendMessage("§5§lCommandes Shinigami :");
                player.sendMessage("§8• §5/dnote pact <nom> §7- Faire un pacte avec un Kira");
                player.sendMessage("§8• §5/dnote track §7- Localiser votre Kira");
                break;

            default:
                player.sendMessage("§7Aucune commande spécifique pour votre rôle.");
                break;
        }
        player.sendMessage("");
    }
}
