package net.novaproject.novauhc.scenario.role.deathnote.roles;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Rôle Near - Détective spécialisé du camp des gentils
 * Possède des capacités de détection avancées contre les Kira
 *
 * @author NovaProject
 * @version 1.0
 */
public class NearRole extends DeathNoteRole {

    private final int maxAnalysis = 2; // Nombre maximum d'analyses par partie
    private int analysisUsed = 0;
    private boolean hasUsedSpecialPower = false;

    public NearRole() {
        setCamp(CromagnonCamps.ZOMS);
    }

    @Override
    public String getName() {
        return "Near";
    }

    @Override
    public String getDescription() {
        return "§bVous êtes Near ! Vous êtes un détective spécialisé contre les Kira.\n" +
                "§fCapacités spéciales :\n" +
                "§8• §eAnalyse avancée §7: Analyse approfondie d'un joueur (" + maxAnalysis + " fois max)\n" +
                "§8• §dDétection Kira §7: Révèle si un joueur est Kira (1 fois par partie)\n" +
                "§8• §aRésistance §7: 50% de chances d'éviter les effets Death Note\n" +
                "§fVotre expertise est cruciale pour démasquer les traîtres !";
    }

    @Override
    public DeathNoteCamps getRoleCamp() {
        return DeathNoteCamps.GENTIL;
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.PAPER)
                .setName("§b§lNear")
                .addLore("§7Rôle gentil spécialisé")
                .addLore("§7Détective anti-Kira");
    }

    @Override
    public void giveRoleItems(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        // Donner l'outil d'analyse
        ItemStack analysisItem = new ItemCreator(Material.PAPER)
                .setName("§b§lAnalyse Near")
                .addLore("§7Clic droit sur un joueur pour l'analyser")
                .addLore("§7Utilisations restantes : §e" + (maxAnalysis - analysisUsed))
                .addLore("")
                .addLore("§7Révèle des informations détaillées")
                .getItemstack();

        // Donner le détecteur Kira
        ItemStack detectorItem = new ItemCreator(Material.REDSTONE)
                .setName("§d§lDétecteur Kira")
                .addLore("§7Clic droit sur un joueur pour détecter")
                .addLore("§7Utilisations : §e1 §7(usage unique)")
                .addLore("")
                .addLore("§cRévèle si le joueur est Kira !")
                .getItemstack();

        player.getInventory().addItem(analysisItem);
        player.getInventory().addItem(detectorItem);

        // Message d'instructions
        player.sendMessage("§b[Near] §fVous avez reçu vos outils de détection !");
        player.sendMessage("§b[Near] §fUtilisez l'analyse pour obtenir des informations détaillées.");
        player.sendMessage("§b[Near] §fLe détecteur Kira est à usage unique, utilisez-le avec sagesse !");
        player.sendMessage("§b[Near] §fVous avez 50% de chances de résister aux Death Note.");
    }

    /**
     * Effectue une analyse avancée sur un joueur
     *
     * @param near   Le joueur Near
     * @param target Le joueur ciblé
     * @return true si l'analyse a été effectuée, false sinon
     */
    public boolean performAnalysis(UHCPlayer near, UHCPlayer target) {
        if (analysisUsed >= maxAnalysis) {
            near.getPlayer().sendMessage("§c[Near] §fVous avez épuisé toutes vos analyses !");
            return false;
        }

        if (target.equals(near)) {
            near.getPlayer().sendMessage("§c[Near] §fVous ne pouvez pas vous analyser vous-même !");
            return false;
        }

        analysisUsed++;

        // Effectuer l'analyse
        String analysisResult = performDetailedAnalysis(target);

        // Envoyer le résultat
        Player nearPlayer = near.getPlayer();
        nearPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        nearPlayer.sendMessage("§b§l[ANALYSE NEAR]");
        nearPlayer.sendMessage("");
        nearPlayer.sendMessage("§fCible : §e" + target.getPlayer().getName());
        nearPlayer.sendMessage(analysisResult);
        nearPlayer.sendMessage("");
        nearPlayer.sendMessage("§7Analyses restantes : §e" + (maxAnalysis - analysisUsed));
        nearPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        // Mettre à jour l'outil d'analyse
        updateAnalysisItem(nearPlayer);

        return true;
    }

    /**
     * Utilise le détecteur Kira spécial
     *
     * @param near   Le joueur Near
     * @param target Le joueur ciblé
     * @return true si la détection a été effectuée, false sinon
     */
    public boolean useKiraDetector(UHCPlayer near, UHCPlayer target) {
        if (hasUsedSpecialPower) {
            near.getPlayer().sendMessage("§c[Near] §fVous avez déjà utilisé votre détecteur Kira !");
            return false;
        }

        if (target.equals(near)) {
            near.getPlayer().sendMessage("§c[Near] §fVous ne pouvez pas vous détecter vous-même !");
            return false;
        }

        hasUsedSpecialPower = true;

        // Effectuer la détection Kira
        boolean isKira = detectKira(target);

        // Envoyer le résultat
        Player nearPlayer = near.getPlayer();
        nearPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        nearPlayer.sendMessage("§d§l[DÉTECTEUR KIRA]");
        nearPlayer.sendMessage("");
        nearPlayer.sendMessage("§fCible : §e" + target.getPlayer().getName());

        if (isKira) {
            nearPlayer.sendMessage("§fRésultat : §c§lKIRA DÉTECTÉ !");
            nearPlayer.sendMessage("§cCe joueur est un traître Kira !");
        } else {
            nearPlayer.sendMessage("§fRésultat : §a§lPAS KIRA");
            nearPlayer.sendMessage("§aCe joueur n'est pas un Kira.");
        }

        nearPlayer.sendMessage("");
        nearPlayer.sendMessage("§7Détecteur utilisé - Plus d'utilisations disponibles");
        nearPlayer.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

        // Retirer le détecteur de l'inventaire
        removeKiraDetector(nearPlayer);

        return true;
    }

    /**
     * Effectue une analyse détaillée d'un joueur
     */
    private String performDetailedAnalysis(UHCPlayer target) {
        // TODO: Récupérer le rôle réel du joueur depuis le scénario Death Note
        // L'analyse de Near donne plus d'informations que l'enquête de l'Enquêteur

        String result = "§fComportement : §e" + getRandomBehavior() + "\n" +
                "§fSuspicion : §e" + getRandomSuspicionLevel() + "\n" +
                "§fActivité récente : §e" + getRandomActivity();

        return result;
    }

    /**
     * Détecte si un joueur est Kira (100% précis)
     */
    private boolean detectKira(UHCPlayer target) {
        // TODO: Vérifier le rôle réel du joueur depuis le scénario Death Note
        // Cette méthode doit retourner true si le joueur est effectivement Kira
        return false; // Placeholder
    }

    /**
     * Vérifie si Near résiste à un effet Death Note (50% de chances)
     */
    public boolean resistsDeathNote() {
        return Math.random() < 0.5; // 50% de chances
    }

    /**
     * Met à jour l'outil d'analyse
     */
    private void updateAnalysisItem(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null &&
                    item.getType() == Material.PAPER &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().equals("§b§lAnalyse Near")) {

                player.getInventory().setItem(i, new ItemCreator(Material.PAPER)
                        .setName("§b§lAnalyse Near")
                        .addLore("§7Clic droit sur un joueur pour l'analyser")
                        .addLore("§7Utilisations restantes : §e" + (maxAnalysis - analysisUsed))
                        .addLore("")
                        .addLore("§7Révèle des informations détaillées")
                        .getItemstack());
                break;
            }
        }
    }

    /**
     * Retire le détecteur Kira de l'inventaire
     */
    private void removeKiraDetector(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null &&
                    item.getType() == Material.REDSTONE &&
                    item.hasItemMeta() &&
                    item.getItemMeta().getDisplayName().equals("§d§lDétecteur Kira")) {

                player.getInventory().setItem(i, null);
                break;
            }
        }
    }

    // Méthodes utilitaires pour l'analyse (placeholders)
    private String getRandomBehavior() {
        String[] behaviors = {"Normal", "Suspect", "Nerveux", "Confiant", "Discret"};
        return behaviors[(int) (Math.random() * behaviors.length)];
    }

    private String getRandomSuspicionLevel() {
        String[] levels = {"Faible", "Modérée", "Élevée", "Très élevée", "Critique"};
        return levels[(int) (Math.random() * levels.length)];
    }

    private String getRandomActivity() {
        String[] activities = {"Exploration", "Combat", "Minage", "Construction", "Observation"};
        return activities[(int) (Math.random() * activities.length)];
    }

    @Override
    public void onEpisodeStart(UHCPlayer uhcPlayer, int episode) {
        Player player = uhcPlayer.getPlayer();
        player.sendMessage("§b[Near] §fNouvel épisode ! Continuez votre traque des Kira.");
    }

    public int getAnalysisUsed() {
        return analysisUsed;
    }

    public boolean hasUsedKiraDetector() {
        return hasUsedSpecialPower;
    }
}
