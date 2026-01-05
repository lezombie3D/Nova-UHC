package net.novaproject.novauhc.scenario.role.scenario.deathnote;


import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.scenario.deathnote.roles.DeathNoteRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class KiraChatCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (!ScenarioManager.get().isScenarioActive("Death Note UHC")) {
            player.sendMessage(Common.get().getInfoTag() + "Le scénario Death Note UHC doit être actif pour utiliser le chat Kira !");
            return false;
        }

        DeathNote scenario = ScenarioManager.get().getScenario(DeathNote.class);
        if (scenario == null) {
            player.sendMessage(ChatColor.RED + "Erreur : Scénario Death Note non trouvé !");
            return false;
        }

        // Vérifier que le chat Kira est activé
        if (!scenario.getConfig().getBoolean("kira_chat.enabled", true)) {
            player.sendMessage("§c[Kira Chat] §fLe chat Kira est désactivé !");
            return false;
        }

        // Vérifier que le joueur peut utiliser le chat Kira
        DeathNoteRole role = scenario.getRoleByUHCPlayer(uhcPlayer);
        if (role == null || !role.canUseKiraChat()) {
            player.sendMessage("§c[Kira Chat] §fVous n'avez pas accès au chat Kira !");
            return false;
        }

        // Si aucun argument, afficher l'aide
        if (args.length == 0) {
            sendKiraChatHelp(player, scenario);
            return true;
        }

        // Déléguer au scénario Death Note avec la commande "k"
        scenario.onDeathNoteCMD(player, "k", args);

        return true;
    }

    /**
     * Affiche l'aide du chat Kira
     */
    private void sendKiraChatHelp(Player player, DeathNote scenario) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        player.sendMessage("§c§l[CHAT KIRA]");
        player.sendMessage("");
        player.sendMessage("§fUtilisation : §c/k <message>");
        player.sendMessage("§fPermet de communiquer avec les autres membres de l'équipe Kira.");
        player.sendMessage("");

        if (scenario.getKiraChatManager() != null) {
            var kiraMembers = scenario.getKiraChatManager().getKiraTeamMembers();

            if (!kiraMembers.isEmpty()) {
                player.sendMessage("§c§lMembres de l'équipe Kira :");
                for (UHCPlayer kira : kiraMembers) {
                    if (kira.getPlayer() != null && kira.getPlayer().isOnline()) {
                        DeathNoteRole kiraRole = scenario.getRoleByUHCPlayer(kira);
                        String roleName = kiraRole != null ? kiraRole.getName() : "Inconnu";
                        String status = kira.equals(uhcPlayer) ? " §7(vous)" : "";

                        player.sendMessage("§8• §c" + kira.getPlayer().getName() + " §7(" + roleName + ")" + status);
                    }
                }
                player.sendMessage("");
            }
        }

        player.sendMessage("§7Exemples :");
        player.sendMessage("§8• §c/k Salut les Kira !");
        player.sendMessage("§8• §c/k J'ai utilisé mon Death Note sur X");
        player.sendMessage("§8• §c/k Attention, l'Enquêteur me suspecte");
        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }
}
