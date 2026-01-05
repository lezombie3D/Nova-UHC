package net.novaproject.novauhc.scenario.role.scenario.deathnote.roles;


import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Rôle Shinigami - Allié des Kira, immunisé au Death Note
 * Peut faire des pactes avec les Kira pour obtenir un tracker
 *
 * @author NovaProject
 * @version 1.0
 */
public class ShinigamiRole extends DeathNoteRole {

    private final Map<UHCPlayer, Integer> pactedKira = new HashMap<>(); // Kira avec qui il a fait un pacte -> utilisations tracker restantes
    private final int maxTrackerUses = 3;


    @Override
    public String getName() {
        return "Shinigami";
    }

    @Override
    public String getDescription() {
        return "§5Vous êtes un Shinigami ! Vous êtes l'allié des Kira.\n" +
                "§fCapacités spéciales :\n" +
                "§8• §cImmunité Death Note §7: Les Death Note n'ont aucun effet sur vous\n" +
                "§8• §ePacte Kira §7: Vous pouvez faire un pacte avec un Kira\n" +
                "§8• §bTracker §7: Le pacte vous donne un tracker de position (3 utilisations)\n" +
                "§fVotre objectif est d'aider les Kira à gagner !";
    }

    @Override
    public DeathNoteCamps getRoleCamp() {
        return DeathNoteCamps.MECHANT;
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM)
                .setName("§5§lShinigami")
                .addLore("§7Rôle allié des Kira")
                .addLore("§7Immunisé au Death Note")
                .setDurability((short) 1); // Tête de wither
    }

    @Override
    public void giveRoleItems(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        // Donner l'item de pacte
        ItemStack pactItem = new ItemCreator(Material.NETHER_STAR)
                .setName("§5§lPacte Shinigami")
                .addLore("§7Clic droit sur un Kira pour faire un pacte")
                .addLore("§7Le Kira perdra 3 cœurs permanents")
                .addLore("§7Vous obtiendrez un tracker de sa position")
                .addLore("")
                .addLore("§cAttention : Un seul pacte possible !")
                .getItemstack();

        player.getInventory().addItem(pactItem);

        // Message d'instructions
        player.sendMessage("§5[Shinigami] §fVous êtes immunisé aux Death Note !");
        player.sendMessage("§5[Shinigami] §fUtilisez votre Nether Star pour faire un pacte avec un Kira.");
        player.sendMessage("§5[Shinigami] §fLe pacte vous donnera un tracker de position du Kira.");
    }

    @Override
    public boolean canBeTargetedByDeathNote() {
        return false; // Immunisé au Death Note
    }

    /**
     * Fait un pacte avec un Kira
     *
     * @param shinigami Le joueur Shinigami
     * @param kira      Le joueur Kira
     * @return true si le pacte a été effectué, false sinon
     */
    public boolean makePactWithKira(UHCPlayer shinigami, UHCPlayer kira) {
        if (!pactedKira.isEmpty()) {
            shinigami.getPlayer().sendMessage("§c[Shinigami] §fVous avez déjà fait un pacte !");
            return false;
        }

        // TODO: Vérifier que le joueur cible est bien un Kira

        // Effectuer le pacte
        pactedKira.put(kira, maxTrackerUses);

        // Donner le tracker au Shinigami
        giveTrackerItem(shinigami);

        // Notifier les deux joueurs
        shinigami.getPlayer().sendMessage("§5[Shinigami] §fPacte effectué avec §c" + kira.getPlayer().getName() + " §f!");
        shinigami.getPlayer().sendMessage("§5[Shinigami] §fVous avez reçu un tracker de position !");

        kira.getPlayer().sendMessage("§c[Kira] §fUn Shinigami a fait un pacte avec vous !");
        kira.getPlayer().sendMessage("§c[Kira] §fVous perdez 3 cœurs permanents mais gagnez le pouvoir de vision !");

        // TODO: Retirer 3 cœurs permanents au Kira
        // TODO: Donner le pouvoir de vision au Kira

        return true;
    }

    /**
     * Donne l'item tracker au Shinigami
     */
    private void giveTrackerItem(UHCPlayer shinigami) {
        Player player = shinigami.getPlayer();

        ItemStack tracker = new ItemCreator(Material.COMPASS)
                .setName("§e§lTracker Kira")
                .addLore("§7Clic droit pour localiser votre Kira")
                .addLore("§7Utilisations restantes : §e" + maxTrackerUses)
                .addLore("")
                .addLore("§7Indique la position exacte du Kira")
                .getItemstack();

        player.getInventory().addItem(tracker);
    }

    /**
     * Utilise le tracker pour localiser le Kira avec qui il a fait un pacte
     *
     * @param shinigami Le joueur Shinigami
     * @return true si le tracker a été utilisé, false sinon
     */
    public boolean useTracker(UHCPlayer shinigami) {
        if (pactedKira.isEmpty()) {
            shinigami.getPlayer().sendMessage("§c[Shinigami] §fVous n'avez fait de pacte avec aucun Kira !");
            return false;
        }

        UHCPlayer kira = pactedKira.keySet().iterator().next();
        int usesLeft = pactedKira.get(kira);

        if (usesLeft <= 0) {
            shinigami.getPlayer().sendMessage("§c[Shinigami] §fVotre tracker n'a plus d'utilisations !");
            return false;
        }

        // Utiliser une charge du tracker
        pactedKira.put(kira, usesLeft - 1);

        // Obtenir la position du Kira
        Player kiraPlayer = kira.getPlayer();
        if (kiraPlayer == null || !kiraPlayer.isOnline()) {
            shinigami.getPlayer().sendMessage("§c[Shinigami] §fVotre Kira n'est pas en ligne !");
            return false;
        }

        // Envoyer la position
        Player shinigamiPlayer = shinigami.getPlayer();
        shinigamiPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        shinigamiPlayer.sendMessage("§e§l[TRACKER KIRA]");
        shinigamiPlayer.sendMessage("");
        shinigamiPlayer.sendMessage("§fKira : §c" + kiraPlayer.getName());
        shinigamiPlayer.sendMessage("§fMonde : §e" + kiraPlayer.getWorld().getName());
        shinigamiPlayer.sendMessage("§fPosition : §e" +
                (int) kiraPlayer.getLocation().getX() + ", " +
                (int) kiraPlayer.getLocation().getY() + ", " +
                (int) kiraPlayer.getLocation().getZ());
        shinigamiPlayer.sendMessage("§fDistance : §e" +
                (int) shinigamiPlayer.getLocation().distance(kiraPlayer.getLocation()) + " blocs");
        shinigamiPlayer.sendMessage("");
        shinigamiPlayer.sendMessage("§7Utilisations restantes : §e" + (usesLeft - 1));
        shinigamiPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        // Mettre à jour l'item tracker
        updateTrackerItem(shinigamiPlayer, usesLeft - 1);

        return true;
    }

    /**
     * Met à jour l'item tracker avec le nombre d'utilisations restantes
     */
    private void updateTrackerItem(Player player, int usesLeft) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null &&
                    item.getType() == Material.COMPASS &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().equals("§e§lTracker Kira")) {

                // Remplacer l'item avec les nouvelles informations
                player.getInventory().setItem(i, new ItemCreator(Material.COMPASS)
                        .setName("§e§lTracker Kira")
                        .addLore("§7Clic droit pour localiser votre Kira")
                        .addLore("§7Utilisations restantes : §e" + usesLeft)
                        .addLore("")
                        .addLore("§7Indique la position exacte du Kira")
                        .getItemstack());
                break;
            }
        }
    }

    @Override
    public void onEpisodeStart(UHCPlayer uhcPlayer, int episode) {
        Player player = uhcPlayer.getPlayer();
        player.sendMessage("§5[Shinigami] §fNouvel épisode ! Continuez à aider les Kira.");

        // Le Shinigami ne récupère pas d'utilisations supplémentaires du tracker
    }

    @Override
    public void onRoleDeath(UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Notifier le Kira avec qui il avait fait un pacte
        if (!pactedKira.isEmpty()) {
            UHCPlayer kira = pactedKira.keySet().iterator().next();
            if (kira.getPlayer() != null && kira.getPlayer().isOnline()) {
                kira.getPlayer().sendMessage("§c[Kira] §fVotre Shinigami allié est mort !");
            }
        }
    }

    public boolean hasPact() {
        return !pactedKira.isEmpty();
    }

    public UHCPlayer getPactedKira() {
        return pactedKira.isEmpty() ? null : pactedKira.keySet().iterator().next();
    }

    public int getTrackerUsesLeft() {
        return pactedKira.isEmpty() ? 0 : pactedKira.values().iterator().next();
    }
}
