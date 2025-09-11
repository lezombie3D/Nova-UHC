package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall;

import org.bukkit.entity.Player;

public enum Status {

    PARALYSIE {
        @Override
        public void apply(Player player) {
            // Immobilise le joueur (par ex: Freeze en mettant la vitesse à 0)
            player.sendMessage("§eVous êtes paralysé ! Consommez un §6Cristal de Terre §epour réduire la durée.");
            // TODO: Appliquer l'effet via PotionEffect ou un système custom
        }
    },
    POISON {
        @Override
        public void apply(Player player) {
            player.sendMessage("§cVous êtes empoisonné ! -200 HP/s. Utilisez une Fleur Pourpre pour guérir.");
            // TODO: infliger dégâts répétés via un scheduler
        }
    },
    DRAGON {
        @Override
        public void apply(Player player) {
            player.sendMessage("§4Le statut DragonRole affaiblit vos résistances élémentaires !");
            // TODO: gérer immunités transformées en faiblesses
        }
    },
    FEU {
        @Override
        public void apply(Player player) {
            player.sendMessage("§cVous brûlez ! -200 HP/s. Plongez dans l'eau pour éteindre les flammes.");
            player.setFireTicks(200);
        }
    },
    ASOMMEMENT {
        @Override
        public void apply(Player player) {
            player.sendMessage("§6Vous êtes assommé ! Immobilisation + Nausée.");
            // TODO: immobiliser très brièvement + ajouter PotionEffect.CONFUSION
        }
    },
    GLACE {
        @Override
        public void apply(Player player) {
            player.sendMessage("§bVous êtes gelé ! Effet Slowness appliqué.");
            // TODO: PotionEffect.SLOW pour X secondes
        }
    },
    SOMMEIL {
        @Override
        public void apply(Player player) {
            player.sendMessage("§9Vous sombrez dans le sommeil... Vous êtes totalement vulnérable.");
            // TODO: immobiliser + blindness pendant 10s
        }
    },
    FOUDRE {
        @Override
        public void apply(Player player) {
            player.sendMessage("§eVous êtes électrocuté ! Plus vulnérable à l’Assommement.");
            // TODO: marquer un flag custom qui augmente chance d'ASOMMEMENT
        }
    },
    SAIGNEMENT {
        @Override
        public void apply(Player player) {
            player.sendMessage("§4Vous saignez ! Restez immobile ou consommez une Viande d’Astera.");
            // TODO: scheduler dégâts progressifs si le joueur bouge
        }
    };


    public abstract void apply(Player player);


    public void tryApply(Player player) {
        apply(player);
    }
}
