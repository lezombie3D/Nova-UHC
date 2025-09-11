package net.novaproject.novauhc.scenario.role.deathnote.roles;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Rôle Enquêteur - Détective du camp des gentils
 * Peut voir les timers Death Note et enquêter sur les joueurs
 * Son objectif est de démasquer les traîtres Kira
 *
 * @author NovaProject
 * @version 1.0
 */
public class EnqueteurRole extends DeathNoteRole {

    private final int maxInvestigations = 3; // Nombre maximum d'enquêtes par partie
    private int investigationsUsed = 0;

    public EnqueteurRole() {
        setCamp(CromagnonCamps.ZOMS);
    }

    @Override
    public String getName() {
        return "Enquêteur";
    }

    @Override
    public String getDescription() {
        return "§aVous êtes l'Enquêteur ! Votre objectif est de démasquer les traîtres Kira.\n" +
                "§fCapacités spéciales :\n" +
                "§8• §eVision des timers §7: Vous voyez les comptes à rebours Death Note (40s)\n" +
                "§8• §bEnquêtes §7: Vous pouvez enquêter sur les joueurs (" + maxInvestigations + " fois max)\n" +
                "§8• §dDétection §7: Les Kira en mode abandon ne vous trompent pas\n" +
                "§fVotre rôle est crucial pour la victoire des gentils !";
    }

    @Override
    public DeathNoteCamps getRoleCamp() {
        return DeathNoteCamps.GENTIL;
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.COMPASS)
                .setName("§a§lEnquêteur")
                .addLore("§7Rôle gentil détective")
                .addLore("§7Peut voir les timers Death Note");
    }

    @Override
    public void giveRoleItems(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        player.getInventory().addItem(new ItemCreator(Material.COMPASS)
                .setName("§b§lLoupe d'Enquête")
                .addLore("§7Utilisations restantes : §e" + (maxInvestigations - investigationsUsed))
                .addLore("")
                .addLore("§7Révèle si le joueur est innocent ou suspect")
                .getItemstack());

        // Message d'instructions
        player.sendMessage("§a[Enquêteur] §fVous avez reçu votre loupe d'enquête !");
        player.sendMessage("§a[Enquêteur] §fVous pouvez voir les timers Death Note de 40 secondes.");
        player.sendMessage("§a[Enquêteur] §fUtilisez votre loupe pour enquêter sur les joueurs suspects.");
    }

    @Override
    public boolean canSeeDeathNoteTimers() {
        return true;
    }

    /**
     * Effectue une enquête sur un joueur cible
     *
     * @param investigator Le joueur enquêteur
     * @param target       Le joueur ciblé par l'enquête
     * @return true si l'enquête a été effectuée, false sinon
     */
    public boolean investigate(UHCPlayer investigator, UHCPlayer target) {
        if (investigationsUsed >= maxInvestigations) {
            investigator.getPlayer().sendMessage("§c[Enquêteur] §fVous avez épuisé toutes vos enquêtes !");
            return false;
        }

        if (target.equals(investigator)) {
            investigator.getPlayer().sendMessage("§c[Enquêteur] §fVous ne pouvez pas enquêter sur vous-même !");
            return false;
        }

        investigationsUsed++;

        // Déterminer le résultat de l'enquête
        String result = determineInvestigationResult(target);

        // Envoyer le résultat à l'enquêteur
        Player investigatorPlayer = investigator.getPlayer();
        investigatorPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        investigatorPlayer.sendMessage("§b§l[ENQUÊTE]");
        investigatorPlayer.sendMessage("");
        investigatorPlayer.sendMessage("§fCible : §e" + target.getPlayer().getName());
        investigatorPlayer.sendMessage("§fRésultat : " + result);
        investigatorPlayer.sendMessage("");
        investigatorPlayer.sendMessage("§7Enquêtes restantes : §e" + (maxInvestigations - investigationsUsed));
        investigatorPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        // Mettre à jour la loupe
        updateInvestigationTool(investigatorPlayer);

        return true;
    }

    /**
     * Détermine le résultat de l'enquête sur un joueur
     */
    private String determineInvestigationResult(UHCPlayer target) {
        // TODO: Récupérer le rôle du joueur cible depuis le scénario Death Note
        // Pour l'instant, simulation basique

        // Si le joueur est Kira
        // - En mode abandon : apparaît innocent (mais pas pour l'Enquêteur !)
        // - Normal : apparaît suspect

        // Si le joueur est Shinigami ou autre méchant : apparaît suspect
        // Si le joueur est gentil : apparaît innocent

        return "§a§lINNOCENT";
    }

    /**
     * Met à jour l'outil d'enquête avec le nombre d'utilisations restantes
     */
    private void updateInvestigationTool(Player player) {
        // Chercher la loupe dans l'inventaire et mettre à jour ses lores
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) != null &&
                    player.getInventory().getItem(i).getType() == Material.COMPASS &&
                    player.getInventory().getItem(i).hasItemMeta() &&
                    player.getInventory().getItem(i).getItemMeta().getDisplayName().equals("§b§lLoupe d'Enquête")) {

                // Remplacer l'item avec les nouvelles informations
                player.getInventory().setItem(i, new ItemCreator(Material.COMPASS)
                        .setName("§b§lLoupe d'Enquête")
                        .addLore("§7Utilisations restantes : §e" + (maxInvestigations - investigationsUsed))
                        .addLore("")
                        .addLore("§7Révèle si le joueur est innocent ou suspect")
                        .getItemstack());
                break;
            }
        }
    }

    /**
     * Affiche un timer Death Note visible uniquement par l'Enquêteur
     *
     * @param investigator Le joueur enquêteur
     * @param target       Le joueur ciblé par le Death Note
     * @param secondsLeft  Secondes restantes avant la mort
     */
    public void showDeathNoteTimer(UHCPlayer investigator, UHCPlayer target, int secondsLeft) {
        Player player = investigator.getPlayer();

        String color;
        if (secondsLeft > 20) {
            color = "§e"; // Jaune
        } else if (secondsLeft > 10) {
            color = "§6"; // Orange
        } else {
            color = "§c"; // Rouge
        }

        player.sendMessage("§c[Death Note Timer] " + color + target.getPlayer().getName() +
                " §fmourra dans §c" + secondsLeft + "s");
    }

    @Override
    public void onEpisodeStart(UHCPlayer uhcPlayer, int episode) {
        Player player = uhcPlayer.getPlayer();
        player.sendMessage("§a[Enquêteur] §fNouvel épisode ! Restez vigilant face aux Kira.");

        // L'Enquêteur ne récupère pas d'enquêtes supplémentaires par épisode
        // Il garde ses enquêtes pour toute la partie
    }

    @Override
    public void onRoleDeath(UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Annoncer discrètement la mort de l'Enquêteur aux gentils
        // TODO: Implémenter la notification aux autres gentils
    }

    public int getInvestigationsUsed() {
        return investigationsUsed;
    }

    public int getInvestigationsRemaining() {
        return maxInvestigations - investigationsUsed;
    }
}
