package net.novaproject.novauhc.scenario.special.legend;


import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.LongCooldownManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class LegendsUtils {

    public static void setMarionnette(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Vous avez reçu la classe Marionnettiste !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Marionnettiste " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fVous contrôlez des marionnettes en tuant des ennemis vous-même.\n\n" +
                ChatColor.LIGHT_PURPLE + "Description :\n" +
                "§fPas d'effet bonus ou malus permanent.\n" +
                "§fChaque kill invoque une marionnette liée à vous.\n\n" +
                ChatColor.LIGHT_PURPLE + "Règles des marionnettes :\n" +
                "§f• Ne peuvent pas s’éloigner de plus de 30 blocs pendant plus de 10 secondes.\n" +
                "§f  Sinon, elles reçoivent Poison I jusqu’à revenir dans la zone.\n" +
                "§f• Elles reçoivent l’une des classes suivantes :\n" +
                "   §c- Marionnette Féroce§f : Force I (5%)\n" +
                "   §9- Marionnette Massive§f : Résistance I (10%)\n" +
                "   §a- Marionnette Timide§f : Vitesse I (par défaut)\n" +
                "§f• Ne ramassent pas et ne donnent pas d’équipement.\n" +
                "§f• Ne frappent ni vous ni vos alliés.\n\n" +
                ChatColor.LIGHT_PURPLE + "Bonus :\n" +
                "§fChaque kill vous donne +2 pommes d’or pour les marionnettes (max 8).\n\n" +
                ChatColor.LIGHT_PURPLE + "Conditions :\n" +
                "§f• À votre mort, toutes les marionnettes meurent.\n" +
                "§f• Elles peuvent gagner avec vous.\n" +
                "§f• Limite = taille de votre équipe (ex : TO2 → 2 max).\n" +
                "§f• Les marionnettes en trop ne seront pas ressuscitées.\n" +
                "§8§m----------------------------");


    }

    public static void setMage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Vous avez reçu la classe Mage !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Mage " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fVous commencez la partie avec 3 potions jetables de 30 secondes :\n" +
                "§f• Force I\n" +
                "§f• Résistance I\n" +
                "§f• Vitesse I\n\n" +
                ChatColor.LIGHT_PURPLE + "Régénération :\n" +
                "§fVous récupérez automatiquement vos potions toutes les 10 minutes,\n" +
                "§fmême si vous ne les avez pas utilisées ou stockées.\n\n" +
                ChatColor.LIGHT_PURPLE + "Support :\n" +
                "§fVous pouvez donner vos potions à vos alliés.\n" +
                "§8§m----------------------------");
        LongCooldownManager.put(player, "mage", 1000);

    }

    public static void setArcher(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Vous avez reçu la classe Archer !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " L'Archer " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fLors de la sélection de votre classe, vous avez le choix entre 2 arcs,\n" +
                "§ftous deux nommés §e« Arc de Lumière »§f :\n\n" +
                "§f• §bChoix 1§f : Arc Power III + Infinity I\n" +
                "§f• §bChoix 2§f : Arc Power IV\n\n" +
                "§fFaites le bon choix selon votre stratégie de jeu !\n" +
                "§8§m----------------------------");
    }

    public static void setAssasin(Player player) {
        Inventory inv = player.getInventory();
        player.sendMessage(ChatColor.DARK_PURPLE + "Vous avez reçu la classe Assassin !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " L'Assassin " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez :\n" +
                "• Lame Secrète en Fer avec Sharpness IV\n" +
                "• 2 cœurs supplémentaires\n" +
                "• 5% de Force\n\n" +
                ChatColor.LIGHT_PURPLE + "Fonctionnement :\n" +
                "§fPour conserver votre boost de Force, vous devez :\n" +
                "• Garder votre Lame Secrète dans votre inventaire\n" +
                "• Ne posséder qu'une seule épée dans votre inventaire (la Lame Secrète est irremplaçable).\n\n" +
                ChatColor.LIGHT_PURPLE + "Règles spéciales :\n" +
                "§fSi votre Lame Secrète est cassée ou perdue, vous perdrez votre boost de Force, même si vous avez une autre épée dans votre inventaire.\n" +
                "§8§m----------------------------");

        ItemCreator epp2 = new ItemCreator(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 3).setName(ChatColor.BOLD + "" + ChatColor.GOLD + "Lame Secréte");
        player.setHealth(player.getMaxHealth());
        inv.addItem(epp2.getItemstack());
    }

    public static void setTank(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Vous avez reçu la classe Tank !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Tank " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez :\n" +
                "• 3 cœurs supplémentaires\n" +
                "• Un pouvoir passif : Résistance I lorsque vous avez moins de 5 cœurs\n\n" +
                "§8§m----------------------------");

        player.setMaxHealth(20 + 6);
    }

    public static void setNain(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Vous avez reçu la classe Nain !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Nain " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir actif :\n\n" +
                "§f• Vous pouvez vous équiper d'une armure en diamant complète enchantée avec Protection II pendant **30 secondes**.\n" +
                "§f• Le cooldown de ce pouvoir est de **10 minutes**.\n\n" +
                "§8§m----------------------------");

        givePouvoir(player);
    }

    public static void setZeus(Player player) {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Vous avez reçu la classe Zeus !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Zeus " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir passif :\n\n" +
                "§f• À chaque coup, vous avez 10% de chance de faire apparaître un éclair infligeant 1 cœur et demi à l'adversaire.\n" +
                "§f• Vous avez 20% de chance de vous octroyer Speed I pendant 10 secondes. Ce buff est réinitialisé chaque fois qu'il se déclenche.\n\n" +
                "§8§m----------------------------");
        givePouvoir(player);
    }

    public static void setNecro(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Nécromancien !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Nécromancien " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir activable :\n\n" +
                "§f• Invoque §d2 squelettes §fà vos côtés.\n" +
                "§f• Invoque §d3 bébés zombies §favec §aSpeed I§f sur la cible.\n" +
                "§f• La cible est le joueur ennemi le plus proche dans un rayon de §e30 blocs§f.\n\n" +
                "§8§m----------------------------");
        givePouvoir(player);
    }

    public static void setSucube(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Succube !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " La Succube " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir actif et §c2 cœurs supplémentaires§f.\n\n" +
                "§f• Lorsque vous activez votre pouvoir, tous les alliés à §e5 blocs§f reçoivent §63 cœurs d’absorption §fpour §b1 minute§f.\n" +
                "§f• Le pouvoir a un temps de recharge de §e6 minutes§f.\n\n" +
                "§8§m----------------------------");
        givePouvoir(player);
        player.setMaxHealth(20 + 4);
    }

    public static void setSoldat(Player player) {
        Inventory inv = player.getInventory();
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Forgeron !\n" +
                "§8§m------" + ChatColor.BLUE + " Le Forgeron " + "§8§m------§r\n" +
                ChatColor.BLUE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous recevez plusieurs objets :\n\n" +
                "§f• §e3 livres §favec §aProtection II\n" +
                "§f• §bPlastron en diamant §favec §aProtection III\n" +
                "§f• §bÉpée en diamant §favec §aSharpness IV\n\n" +
                "§8§m----------------------------");

        ItemCreator p2 = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).setAmount(3);
        ItemCreator t2 = new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        ItemCreator epp = new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 4);
        inv.addItem(epp.getItemstack());
        inv.addItem(p2.getItemstack());
        inv.addItem(t2.getItemstack());
    }

    public static void setPrincesse(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Princesse !\n" +
                "§8§m------" + ChatColor.BLUE + " La Princesse " + "§8§m------§r\n" +
                ChatColor.BLUE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez plusieurs pouvoirs passifs :\n\n" +
                "§f• Vous bénéficiez de l'effet permanent §aNo Fall§f, vous évitant les dégâts de chute.\n" +
                "§f• Un joueur de votre équipe est désigné comme votre §bprince§f.\n" +
                "§f• Tant que ce prince est en vie, vous gagnez §c+3 cœurs permanents§f.\n\n" +
                "§8§m----------------------------");
        player.setMaxHealth(20 + 6);
    }

    public static void setCavalier(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Cavalier !\n" +
                "§8§m------" + ChatColor.YELLOW + " Le Cavalier" + "§8§m------§r\n" +
                ChatColor.YELLOW + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir actif :\n\n" +
                "§f• Vous pouvez invoquer un §bcheval royal§f.\n" +
                "§f• Ce cheval possède :\n" +
                "  §c20 cœurs§f et §aSpeed I§f.\n" +
                "§f• Votre pouvoir a un temps de recharge de §e5 minutes§f.\n\n" +
                "§8§m----------------------------");

        givePouvoir(player);
    }

    public static void setOgre(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Ogre !\n" +
                "§8§m------" + ChatColor.GREEN + " L'Ogre " + "§8§m------§r\n" +
                ChatColor.GREEN + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir passif :\n\n" +
                "§f• Vous obtenez un effet aléatoire parmi les suivants lorsque vous mangez une §6pomme en or§f :\n" +
                "  - §cForce\n" +
                "  - §aSpeed\n" +
                "  - §9Resistance\n" +
                "  - §dFire Resistance\n" +
                "§f• Ces effets durent §e5 secondes§f.\n\n" +
                "§f• Si vous avez moins de §65 pommes en or§f dans votre inventaire, vous subirez §7Faiblesse I§f et §7Lenteur I§f.\n\n" +
                "§8§m----------------------------");
    }

    public static void setDragon(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Dragons !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le DragonRole " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous bénéficiez d'un pouvoir passif ainsi que de §aFire Resistance II §fpermanent.\n\n" +
                "§f• Votre pouvoir passif fonctionne comme suit :\n" +
                "  - À chaque coup donné, vous avez 30% de chance de mettre le joueur adverse en feu pendant 5 secondes.\n\n" +
                "§8§m----------------------------");

    }

    public static void setMedecin(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Soigneur !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Soigneur " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez un pouvoir passif :\n\n" +
                "§f• Vous avez une zone autour de vous qui soigne vos alliés de §a0.5 cœur par seconde§f tant qu'ils ont moins de 5 cœurs.\n" +
                "§f• Cette zone est visible par un cercle de particules en forme de §acœurs§f, autour de vous.\n\n" +
                "§8§m----------------------------");
    }

    public static void setPrisonier(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Prisonnier ! ");
        player.sendMessage("§8§m---------" + ChatColor.GREEN + "Le Prisonnier§8§m----------§r\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez speed 1 permanant mais 8 coeur permanant.\n" +
                "§fDescription de la classe : " + ChatColor.GREEN + "Vous êtes un Prisonnier, courir vite est votre atout.\n" +
                "§8§m--------------------------");
        player.setMaxHealth(20 - 4);
    }

    public static void setCorne(Player player) {
        Inventory inv = player.getInventory();
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Barde !\n" +
                "§8§m------" + ChatColor.LIGHT_PURPLE + " Le Barde " + "§8§m------§r\n" +
                ChatColor.LIGHT_PURPLE + "Pouvoirs :\n" +
                "§fAu début de la partie, vous possédez §cWeakness I§f de façon permanente.\n\n" +
                "§fVous avez accès à §e4 mélodies§f que vous pouvez activer pour soutenir vos alliés (rayon de 30 blocs) :\n\n" +
                "§6• Mélodie : Feu\n" +
                "§f→ Donne Fire Resistance pendant 22s (recharge : 1min)\n\n" +
                "§6• Mélodie : Heal\n" +
                "§f→ Restaure la vie de tous les alliés au maximum (recharge : 5min)\n\n" +
                "§6• Mélodie : Métal\n" +
                "§f→ Donne Resistance II pendant 15s (recharge : 3min)\n\n" +
                "§6• Mélodie : Air\n" +
                "§f→ Donne Speed II pendant 18s (recharge : 3min)\n\n" +
                "§8§m----------------------------");
        ItemCreator pov25 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Feu");
        ItemCreator pov3 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Heal");
        ItemCreator pov4 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Metal");
        ItemCreator pov5 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Melodie : Air");
        ItemCreator golde = new ItemCreator(Material.GOLDEN_APPLE).setAmount(5);

        inv.addItem(pov25.getItemstack());
        inv.addItem(pov3.getItemstack());
        inv.addItem(pov4.getItemstack());
        inv.addItem(pov5.getItemstack());
        inv.addItem(golde.getItemstack());
    }

    private static void givePouvoir(Player player) {
        Inventory inv = player.getInventory();
        ItemCreator pov1 = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Pouvoir");
        inv.addItem(pov1.getItemstack());
    }

    public static void spawnHorse(Player player) {
        Location loc = player.getLocation();
        Horse horse = player.getWorld().spawn(loc, Horse.class);
        horse.setCustomName("§6Cheval Royal");
        horse.setCustomNameVisible(true);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setAdult();
        horse.setColor(Horse.Color.WHITE);
        horse.setJumpStrength(1.0);
        horse.setPassenger(player);
        horse.setMaxHealth(40);
        horse.setHealth(40);
        ItemStack saddle = new ItemStack(Material.SADDLE);
        horse.getInventory().setSaddle(saddle);
        player.sendMessage("§aUn cheval royal est apparu !");
    }

    public static void sendFeroce(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Marionette : Feroce ! ");
        player.sendMessage("§8§m---------" + ChatColor.GREEN + "Le Marionette : Feroce §8§m----------§r\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possédez Force 1.\n" +
                "§8§m--------------------------");
    }

    public static void sendMassif(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Marionette : Massif ! ");
        player.sendMessage("§8§m---------" + ChatColor.GREEN + "Le Marionette : Massif §8§m----------§r\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possédez Resistance 1.\n" +
                "§8§m--------------------------");
    }

    public static void sendTimide(Player player) {
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu la classe Marionette : Timide ! ");
        player.sendMessage("§8§m---------" + ChatColor.GREEN + "Le Marionette : Timide §8§m----------§r\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possédez Speed 1.\n" +
                "§8§m--------------------------");
    }

    public static String getTimerFormatted(int seco) {
        int seconds = Math.max(seco, 0);
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static void giveGoldenApplesToPuppets(UHCPlayer marionnettiste, Map<UHCPlayer, List<UHCPlayer>> marionettes) {
        List<UHCPlayer> puppets = marionettes.get(marionnettiste);
        if (puppets == null) return;

        for (UHCPlayer puppet : puppets) {
            Player p = puppet.getPlayer();
            if (p != null) {
                ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 2);
                p.getInventory().addItem(apples);
            }
        }
    }

    public static Player findNearestPlayer(Player player) {
        double closestDistance = 30.0;
        Player closestPlayer = null;
        for (Entity entity : player.getNearbyEntities(closestDistance, closestDistance, closestDistance)) {
            if (entity instanceof Player && entity != player) {
                double distance = player.getLocation().distance(entity.getLocation());
                if (distance < closestDistance) {
                    closestPlayer = (Player) entity;
                    closestDistance = distance;
                }
            }
        }
        return closestPlayer;
    }

    public static void summonMobs(Player summoner, Player target, List<Entity> summoned) {
        World world = target.getWorld();
        Location loc = target.getLocation().add(0, 2, 0);
        Location loc2 = summoner.getLocation().add(0, 2, 0);

        for (int i = 0; i < 5; i++) {
            if (i < 2) {
                Skeleton skeleton = world.spawn(loc2, Skeleton.class);
                skeleton.setTarget(target);
                skeleton.setMaxHealth(40);
                skeleton.setHealth(40);
                skeleton.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 2, 0));
                summoned.add(skeleton);
            } else {
                Zombie zombie = world.spawn(loc, Zombie.class);
                zombie.setTarget(target);
                zombie.setMaxHealth(40);
                zombie.setHealth(40);
                zombie.setBaby(true);
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 2, 0));
                summoned.add(zombie);
            }
        }
    }

    public static void spawnParticleCircle(Player player, Location center, Effect effect, double radius, int points) {
        World world = player.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = center.clone().add(x, 0, z);
            world.spigot().playEffect(loc, effect, 0, 0, 0, 0, 0, 1, 0, 64);
        }
    }

}
