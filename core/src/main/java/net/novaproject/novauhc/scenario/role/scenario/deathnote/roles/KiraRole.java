package net.novaproject.novauhc.scenario.role.scenario.deathnote.roles;

import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.scenario.deathnote.DeathNote;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Rôle Kira - Le traître principal du mode Death Note UHC
 * Possède le Death Note avec 3 pouvoirs : dégâts, vision, abandon
 * Peut communiquer avec les autres Kira et faire des pactes avec Shinigami
 *
 * @author NovaProject
 * @version 1.0
 */
public class KiraRole extends DeathNoteRole {

    private boolean hasUsedDeathNoteThisEpisode = false;
    private boolean isInAbandonMode = false;
    private long abandonModeEndTime = 0;
    private boolean hasShinigamiPact = false;
    private boolean hasRevealed = false;

    @Override
    public String getName() {
        return "Kira";
    }

    @Override
    public String getDescription() {
        return "§cVous êtes Kira ! Votre objectif est de trahir votre équipe et de rejoindre les autres Kira.\n" +
                "§fVous possédez un Death Note avec 3 pouvoirs :\n" +
                "§8• §cPouvoir de dégâts §7: Affaiblit un coéquipier (1 fois par épisode)\n" +
                "§8• §ePouvoir de vision §7: Voir la vie des coéquipiers (avec pacte Shinigami)\n" +
                "§8• §9Pouvoir d'abandon §7: Paraître innocent pendant 20 minutes\n" +
                "§fCommandes : §a/k <message> §7(chat Kira), §a/dnote claim §7(récupérer pouvoirs)";
    }

    @Override
    public DeathNoteCamps getRoleCamp() {
        return DeathNoteCamps.MECHANT;
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOOK)
                .setName("§c§lKira")
                .addLore("§7Rôle traître principal")
                .addLore("§7Possède le Death Note");
    }

    @Override
    public void giveRoleItems(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        // Donner le Death Note
        ItemStack deathNote = new ItemCreator(Material.WRITTEN_BOOK)
                .setName("§c§lDeath Note")
                .addLore("§7Clic droit pour utiliser")
                .addLore("§7/dnote claim pour récupérer les pouvoirs")
                .addLore("")
                .addLore("§cPouvoirs disponibles :")
                .addLore("§8• §cDégâts §7(1 fois par épisode)")
                .addLore("§8• §eVision §7(avec pacte Shinigami)")
                .addLore("§8• §9Abandon §7(20 minutes d'innocence)")
                .getItemstack();

        player.getInventory().addItem(deathNote);

        // Message d'instructions
        player.sendMessage("§c[Death Note] §fVous avez reçu votre Death Note !");
        player.sendMessage("§c[Death Note] §fUtilisez §a/dnote claim §fpour récupérer vos pouvoirs.");
        player.sendMessage("§c[Death Note] §fUtilisez §a/k <message> §fpour parler aux autres Kira.");
    }

    @Override
    public boolean canUseDeathNote() {
        return true;
    }

    @Override
    public boolean canUseKiraChat() {
        return true;
    }

    @Override
    public void onIteract(Player player1, PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        ItemStack item = event.getItem();
        if (item.getType() == Material.WRITTEN_BOOK &&
                item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals("§c§lDeath Note")) {

            event.setCancelled(true);

            // Ouvrir le menu Death Note
            openDeathNoteMenu(player1);
        }
    }

    /**
     * Ouvre le menu d'utilisation du Death Note
     */
    private void openDeathNoteMenu(Player player) {
        DeathNote scenario = ScenarioManager.get().getScenario(DeathNote.class);
        if (scenario != null) {
            // TODO: Ouvrir le menu Death Note
            player.sendMessage("§c[Death Note] §fMenu Death Note - À implémenter");
        }
    }

    @Override
    public void onEpisodeStart(UHCPlayer uhcPlayer, int episode) {
        // Réinitialiser l'utilisation du Death Note pour ce nouvel épisode
        hasUsedDeathNoteThisEpisode = false;

        Player player = uhcPlayer.getPlayer();
        player.sendMessage("§c[Death Note] §fNouvel épisode ! Vous pouvez à nouveau utiliser votre Death Note.");

        // Régénération de vie si le joueur a été affecté par un Death Note
        // TODO: Implémenter la régénération de 1 cœur
    }

    @Override
    public void onRoleDeath(UHCPlayer uhcPlayer, UHCPlayer killer) {
        Player player = uhcPlayer.getPlayer();

        // Annoncer la mort de Kira aux autres Kira
        DeathNote scenario = ScenarioManager.get().getScenario(DeathNote.class);
        if (scenario != null) {
            // TODO: Notifier les autres Kira de la mort
            player.sendMessage("§c[Death Note] §fUn Kira est mort...");
        }
    }

    /**
     * Utilise le pouvoir de dégâts du Death Note
     */
    public boolean useDamagePower(UHCPlayer target, int episode) {
        if (hasUsedDeathNoteThisEpisode) {
            return false; // Déjà utilisé ce tour
        }

        if (isInAbandonMode) {
            return false; // Ne peut pas utiliser en mode abandon
        }

        // Calculer les dégâts selon l'épisode
        int damage;
        switch (episode) {
            case 2:
                damage = 5; // 5 cœurs
                break;
            case 3:
                damage = 4; // 4 cœurs
                break;
            default:
                damage = 3; // 3 cœurs (épisode 4+)
                break;
        }

        // TODO: Appliquer les dégâts avec un délai de 40 secondes
        hasUsedDeathNoteThisEpisode = true;
        return true;
    }

    /**
     * Active le pouvoir d'abandon (paraître innocent)
     */
    public boolean useAbandonPower() {
        if (isInAbandonMode) {
            return false; // Déjà en mode abandon
        }

        isInAbandonMode = true;
        abandonModeEndTime = System.currentTimeMillis() + (20 * 60 * 1000); // 20 minutes

        return true;
    }

    /**
     * Vérifie si Kira est actuellement en mode abandon (innocent)
     */
    public boolean isInAbandonMode() {
        if (isInAbandonMode && System.currentTimeMillis() > abandonModeEndTime) {
            isInAbandonMode = false; // Le mode abandon a expiré
        }
        return isInAbandonMode;
    }

    /**
     * Fait un pacte avec un Shinigami
     */
    public boolean makeShinigamiPact() {
        if (hasShinigamiPact) {
            return false; // Déjà un pacte
        }

        hasShinigamiPact = true;
        // TODO: Retirer 3 cœurs permanents à Kira
        // TODO: Donner le pouvoir de vision

        return true;
    }

    /**
     * Révèle l'identité de Kira publiquement
     */
    public boolean revealIdentity() {
        if (hasRevealed) {
            return false; // Déjà révélé
        }

        hasRevealed = true;
        // TODO: Donner une pomme en or
        // TODO: Permettre d'utiliser le Death Note sur tous les joueurs

        return true;
    }

    public boolean hasUsedDeathNoteThisEpisode() {
        return hasUsedDeathNoteThisEpisode;
    }

    public boolean hasShinigamiPact() {
        return hasShinigamiPact;
    }

    public boolean hasRevealed() {
        return hasRevealed;
    }
}
