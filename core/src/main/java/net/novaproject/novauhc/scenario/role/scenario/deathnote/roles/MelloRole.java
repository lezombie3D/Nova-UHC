package net.novaproject.novauhc.scenario.role.scenario.deathnote.roles;

import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Rôle Mello - Rôle variable qui peut changer de camp
 * Peut être gentil ou méchant selon sa "forme" actuelle
 *
 * @author NovaProject
 * @version 1.0
 */
public class MelloRole extends DeathNoteRole {

    private final int maxFormChanges = 1;
    private MelloForm currentForm;
    private boolean hasChangedForm = false;
    private int formChangesUsed = 0;
    public MelloRole() {

        this.currentForm = (Math.random() < 0.5) ? MelloForm.GENTIL : MelloForm.MECHANT;

    }

    @Override
    public String getName() {
        return "Mello";
    }

    @Override
    public String getDescription() {
        return "§6Vous êtes Mello ! Votre camp peut changer selon votre forme.\n" +
                "§fForme actuelle : " + currentForm.getCamp().getColor() + "§l" + currentForm.getName() + "\n" +
                "§f" + currentForm.getDescription() + "\n" +
                "§fCapacités spéciales :\n" +
                "§8• §eChangement de forme §7: Vous pouvez changer de camp (1 fois max)\n" +
                "§8• §bAdaptation §7: Vos capacités changent selon votre forme\n" +
                "§8• §dPolyvalence §7: Vous pouvez aider les deux camps\n" +
                "§fUtilisez votre changement de forme au bon moment !";
    }

    @Override
    public DeathNoteCamps getRoleCamp() {
        return DeathNoteCamps.VARIABLE; // Toujours variable dans l'interface
    }

    /**
     * Retourne le camp actuel selon la forme
     */
    public DeathNoteCamps getCurrentCamp() {
        return currentForm.getCamp();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CAULDRON_ITEM)
                .setName("§6§lMello")
                .addLore("§7Rôle variable")
                .addLore("§7Peut changer de camp");
    }

    @Override
    public void giveRoleItems(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        // Donner l'item de changement de forme
        if (formChangesUsed < maxFormChanges) {
            ItemStack formChanger = new ItemCreator(Material.NETHER_STAR)
                    .setName("§6§lChangement de Forme")
                    .addLore("§7Clic droit pour changer de camp")
                    .addLore("§7Forme actuelle : " + currentForm.getCamp().getColor() + currentForm.getName())
                    .addLore("§7Changements restants : §e" + (maxFormChanges - formChangesUsed))
                    .addLore("")
                    .addLore("§cAttention : Usage unique !")
                    .getItemstack();

            player.getInventory().addItem(formChanger);
        }

        // Donner les items selon la forme actuelle
        giveFormSpecificItems(uhcPlayer);

        // Message d'instructions
        player.sendMessage("§6[Mello] §fVotre forme actuelle : " + currentForm.getCamp().getColor() + "§l" + currentForm.getName());
        player.sendMessage("§6[Mello] §f" + currentForm.getDescription());

        if (formChangesUsed < maxFormChanges) {
            player.sendMessage("§6[Mello] §fVous pouvez changer de forme §e1 fois §fpar partie !");
        }
    }

    /**
     * Donne les items spécifiques à la forme actuelle
     */
    private void giveFormSpecificItems(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        if (currentForm == MelloForm.GENTIL) {
            // Items pour la forme gentille
            ItemStack gentilItem = new ItemCreator(Material.EMERALD)
                    .setName("§a§lCapacité Gentille")
                    .addLore("§7Vous pouvez aider les autres gentils")
                    .addLore("§7Clic droit pour révéler votre camp")
                    .getItemstack();

            player.getInventory().addItem(gentilItem);

        } else {
            // Items pour la forme méchante
            ItemStack mechantItem = new ItemCreator(Material.REDSTONE)
                    .setName("§c§lCapacité Méchante")
                    .addLore("§7Vous pouvez aider les Kira")
                    .addLore("§7Clic droit pour des informations sur les Kira")
                    .getItemstack();

            player.getInventory().addItem(mechantItem);
        }
    }

    /**
     * Change la forme de Mello
     *
     * @param mello Le joueur Mello
     * @return true si le changement a été effectué, false sinon
     */
    public boolean changeForm(UHCPlayer mello) {
        if (formChangesUsed >= maxFormChanges) {
            mello.getPlayer().sendMessage("§c[Mello] §fVous avez déjà utilisé votre changement de forme !");
            return false;
        }

        // Changer de forme
        MelloForm oldForm = currentForm;
        currentForm = (currentForm == MelloForm.GENTIL) ? MelloForm.MECHANT : MelloForm.GENTIL;
        formChangesUsed++;
        hasChangedForm = true;

        Player player = mello.getPlayer();

        // Annoncer le changement
        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        player.sendMessage("§6§l[CHANGEMENT DE FORME]");
        player.sendMessage("");
        player.sendMessage("§fAncienne forme : " + oldForm.getCamp().getColor() + "§l" + oldForm.getName());
        player.sendMessage("§fNouvelle forme : " + currentForm.getCamp().getColor() + "§l" + currentForm.getName());
        player.sendMessage("");
        player.sendMessage("§f" + currentForm.getDescription());
        player.sendMessage("");
        player.sendMessage("§7Changements restants : §e" + (maxFormChanges - formChangesUsed));
        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        // Retirer l'ancien item de changement de forme
        removeFormChangerItem(player);

        // Nettoyer les anciens items spécifiques à la forme
        clearFormSpecificItems(player);

        // Donner les nouveaux items
        giveFormSpecificItems(mello);

        return true;
    }

    /**
     * Retire l'item de changement de forme
     */
    private void removeFormChangerItem(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null &&
                    item.getType() == Material.NETHER_STAR &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().equals("§6§lChangement de Forme")) {

                player.getInventory().setItem(i, null);
                break;
            }
        }
    }

    /**
     * Nettoie les items spécifiques à l'ancienne forme
     */
    private void clearFormSpecificItems(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName.equals("§a§lCapacité Gentille") || displayName.equals("§c§lCapacité Méchante")) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }

    @Override
    public boolean isKiraTeam() {
        return currentForm.getCamp() == DeathNoteCamps.MECHANT;
    }

    @Override
    public boolean canUseKiraChat() {
        return currentForm.getCamp() == DeathNoteCamps.MECHANT; // Peut utiliser le chat Kira si méchant
    }

    @Override
    public void onEpisodeStart(UHCPlayer uhcPlayer, int episode) {
        Player player = uhcPlayer.getPlayer();
        player.sendMessage("§6[Mello] §fNouvel épisode ! Forme actuelle : " +
                currentForm.getCamp().getColor() + "§l" + currentForm.getName());
    }

    public MelloForm getCurrentForm() {
        return currentForm;
    }

    public boolean hasChangedForm() {
        return hasChangedForm;
    }

    public int getFormChangesRemaining() {
        return maxFormChanges - formChangesUsed;
    }

    /**
     * Les différentes formes de Mello
     */
    public enum MelloForm implements Camps {
        GENTIL("Gentil", DeathNoteCamps.GENTIL, "§aMello travaille avec les gentils"),
        MECHANT("Méchant", DeathNoteCamps.MECHANT, "§cMello travaille avec les Kira");

        private final String name;
        private final DeathNoteCamps camp;
        private final String description;

        MelloForm(String name, DeathNoteCamps camp, String description) {
            this.name = name;
            this.camp = camp;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getColor() {
            return "";
        }

        @Override
        public ItemStack getItem() {
            return null;
        }

        @Override
        public boolean isMainCamp() {
            return false;
        }

        @Override
        public Camps getParent() {
            return null;
        }

        public DeathNoteCamps getCamp() {
            return camp;
        }

        public String getDescription() {
            return description;
        }
    }
}
