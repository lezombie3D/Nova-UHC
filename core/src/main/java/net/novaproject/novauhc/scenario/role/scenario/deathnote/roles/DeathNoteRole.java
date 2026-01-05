package net.novaproject.novauhc.scenario.role.scenario.deathnote.roles;

import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Classe abstraite de base pour tous les rôles du mode Death Note UHC
 * Définit les méthodes communes et les camps (gentil/méchant)
 *
 * @author NovaProject
 * @version 1.0
 */
public abstract class DeathNoteRole extends Role {


    /**
     * Retourne le camp du rôle
     *
     * @return Le camp du rôle (GENTIL, MECHANT, VARIABLE)
     */
    public abstract DeathNoteCamps getRoleCamp();

    @Override
    public String getColor() {
        return getRoleCamp().getColor();
    }

    /**
     * Méthode appelée quand le rôle est révélé à son possesseur
     *
     * @param uhcPlayer Le joueur qui reçoit le rôle
     */
    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

        Player player = uhcPlayer.getPlayer();

        // Message de bienvenue avec le rôle
        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        player.sendMessage("§c§l[Death Note UHC]");
        player.sendMessage("");
        player.sendMessage("§fVous êtes : " + getColor() + "§l" + getName());
        player.sendMessage("§fCamp : " + getRoleCamp().getColor() + getRoleCamp().getName());
        player.sendMessage("");
        player.sendMessage("§7" + getDescription());
        player.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        // Donner les items spéciaux du rôle
        giveRoleItems(uhcPlayer);
    }

    /**
     * Donne les items spéciaux du rôle au joueur
     *
     * @param uhcPlayer Le joueur qui reçoit les items
     */
    public void giveRoleItems(UHCPlayer uhcPlayer) {
        // À implémenter dans les classes filles si nécessaire
    }

    /**
     * Vérifie si ce rôle peut être ciblé par le Death Note
     *
     * @return true si le rôle peut être ciblé, false sinon
     */
    public boolean canBeTargetedByDeathNote() {
        return true; // Par défaut, tous les rôles peuvent être ciblés
    }

    /**
     * Vérifie si ce rôle peut utiliser le Death Note
     *
     * @return true si le rôle peut utiliser le Death Note, false sinon
     */
    public boolean canUseDeathNote() {
        return false; // Par défaut, seuls les Kira peuvent utiliser le Death Note
    }

    /**
     * Vérifie si ce rôle peut voir les timers Death Note
     *
     * @return true si le rôle peut voir les timers, false sinon
     */
    public boolean canSeeDeathNoteTimers() {
        return false; // Par défaut, seuls les Enquêteurs peuvent voir les timers
    }

    /**
     * Méthode appelée quand un joueur avec ce rôle meurt
     *
     * @param uhcPlayer Le joueur qui meurt
     * @param killer    Le tueur (peut être null)
     */
    public void onRoleDeath(UHCPlayer uhcPlayer, UHCPlayer killer) {
        // À implémenter dans les classes filles si nécessaire
    }

    /**
     * Méthode appelée à chaque début d'épisode
     *
     * @param uhcPlayer Le joueur avec ce rôle
     * @param episode   Le numéro de l'épisode
     */
    public void onEpisodeStart(UHCPlayer uhcPlayer, int episode) {
        // À implémenter dans les classes filles si nécessaire
    }

    /**
     * Vérifie si ce rôle fait partie de l'équipe Kira
     *
     * @return true si le rôle est dans l'équipe Kira, false sinon
     */
    public boolean isKiraTeam() {
        return getRoleCamp() == DeathNoteCamps.MECHANT;
    }

    /**
     * Vérifie si ce rôle peut communiquer avec les autres Kira
     *
     * @return true si le rôle peut utiliser le chat Kira, false sinon
     */
    public boolean canUseKiraChat() {
        return false; // Par défaut, seuls les Kira peuvent utiliser le chat
    }

    public enum DeathNoteCamps implements Camps {
        GENTIL("Gentil", "§a", Material.REDSTONE),
        MECHANT("Méchant", "§c", Material.EMERALD),
        VARIABLE("Variable", "§6", Material.RECORD_10);

        private final Camps delegate;

        DeathNoteCamps(String name, String color, Material icon) {
            delegate = new AbstractCamp(name, color, icon) {
            };
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getColor() {
            return delegate.getColor();
        }

        @Override
        public ItemStack getItem() {
            return delegate.getItem();
        }

        @Override
        public boolean isMainCamp() {
            return false;
        }

        @Override
        public Camps getParent() {
            return null;
        }
    }
}
