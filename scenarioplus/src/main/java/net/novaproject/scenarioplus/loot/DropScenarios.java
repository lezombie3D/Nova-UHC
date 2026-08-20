package net.novaproject.scenarioplus.loot;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.normal.MiningScenarios;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.nms.EquipmentHider;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class DropScenarios {

    private DropScenarios() {
    }

    public static class AchievementHuntersScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.achievementhunters.desc",
                "§7Chaque succès débloqué a §e%chance%%% §7de te récompenser, sinon il te punit.");

        private static final DynamicLang RECOMPENSE = DynamicLang.of("scenario.achievementhunters.recompense",
                "§aTon succès te rapporte §e%pommes% pomme(s) dorée(s)§a.");

        private static final DynamicLang MALUS = DynamicLang.of("scenario.achievementhunters.malus",
                "§cTon succès se retourne contre toi.");

        private final Random random = new Random();

        @Var(name = "Chance de récompense", desc = "Chance qu'un succès rapporte une récompense plutôt qu'une punition.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceRecompense = 50;

        @Var(name = "Pommes dorées", desc = "Nombre de pommes dorées données quand le succès est récompensé.", type = VariableType.INTEGER, min = 1)
        private int pommesDorees = 1;

        @Var(name = "Durée du malus", desc = "Durée de l'aveuglement et de la lenteur infligés quand le succès est puni.", type = VariableType.TIME, min = 1)
        private int dureeMalusSec = 20;

        @Var(name = "Niveau du malus", desc = "Niveau de la lenteur infligée quand le succès est puni.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauMalus = 2;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Achievement Hunters"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chanceRecompense));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onAchievement(PlayerAchievementAwardedEvent event) {
            if (!isRunning()) return;
            event.setCancelled(false);
            Player player = event.getPlayer();

            if (UHCUtils.Rng.chance(chanceRecompense)) {
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, pommesDorees));
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                LangManager.get().send(RECOMPENSE, player, Map.of("%pommes%", pommesDorees));
                return;
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, dureeMalusSec * 20, 0, false, true), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, dureeMalusSec * 20, niveauMalus - 1, false, true), true);
            LangManager.get().send(MALUS, player);
        }
    }

    public static class AchievementParanoiaScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.achievementparanoia.desc",
                "§7Chaque succès débloqué annonce à tous son auteur et ses coordonnées.");

        private static final DynamicLang ANNONCE = DynamicLang.of("scenario.achievementparanoia.annonce",
                "§e%joueur% §7a débloqué un succès en §e%x%§7, §e%y%§7, §e%z%§7.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Achievement Paranoia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.COMPASS); }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onAchievement(PlayerAchievementAwardedEvent event) {
            if (!isRunning()) return;
            event.setCancelled(false);
            Player player = event.getPlayer();
            Location loc = player.getLocation();
            LangManager.get().sendAll(ANNONCE, Map.of(
                    "%joueur%", player.getName(),
                    "%x%", loc.getBlockX(),
                    "%y%", loc.getBlockY(),
                    "%z%", loc.getBlockZ()));
        }
    }

    public static class AchievementsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.achievements.desc",
                "§7Chaque succès débloqué donne au hasard de la force, de la vitesse ou §c%coeurs% cœur(s)§7.");

        private static final DynamicLang FORCE = DynamicLang.of("scenario.achievements.force",
                "§cTon succès t'accorde de la force.");

        private static final DynamicLang VITESSE = DynamicLang.of("scenario.achievements.vitesse",
                "§bTon succès t'accorde de la vitesse.");

        private static final DynamicLang COEUR = DynamicLang.of("scenario.achievements.coeur",
                "§cTon succès t'accorde §e%coeurs% cœur(s) §csupplémentaire(s).");

        private final Random random = new Random();

        @Var(name = "Durée des bonus", desc = "Durée de la force ou de la vitesse accordée par un succès.", type = VariableType.TIME, min = 1)
        private int dureeBonusSec = 60;

        @Var(name = "Niveau de force", desc = "Niveau de la force accordée par un succès.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauForce = 1;

        @Var(name = "Niveau de vitesse", desc = "Niveau de la vitesse accordée par un succès.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauVitesse = 1;

        @Var(name = "Cœurs gagnés", desc = "Cœurs de vie maximum ajoutés quand le succès donne de la vie.", type = VariableType.INTEGER, min = 1)
        private int coeursGagnes = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Achievements"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeursGagnes));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onAchievement(PlayerAchievementAwardedEvent event) {
            if (!isRunning()) return;
            event.setCancelled(false);
            Player player = event.getPlayer();
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);

            switch (random.nextInt(3)) {
                case 0 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, dureeBonusSec * 20, niveauForce - 1, false, true), true);
                    LangManager.get().send(FORCE, player);
                }
                case 1 -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dureeBonusSec * 20, niveauVitesse - 1, false, true), true);
                    LangManager.get().send(VITESSE, player);
                }
                default -> {
                    player.setMaxHealth(player.getMaxHealth() + coeursGagnes * 2.0);
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + coeursGagnes * 2.0));
                    LangManager.get().send(COEUR, player, Map.of("%coeurs%", coeursGagnes));
                }
            }
        }
    }

    public static class AloneTogetherScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.alonetogether.desc",
                "§7Tes coéquipiers te sont invisibles, et tu leur es invisible.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Alone Together"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EYE_OF_ENDER); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            UHCPlayer self = UHCPlayerManager.get().getPlayer(player);
            if (self == null || self.getTeam().isEmpty()) return;

            for (UHCPlayer mate : self.getTeam().get().getPlayers()) {
                if (mate == null) continue;
                Player other = mate.getPlayer();
                if (other == null || other.equals(player)) continue;
                if (player.canSee(other)) player.hidePlayer(other);
            }
        }
    }

    public static class AnonymousScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.anonymous.desc",
                "§7Personne n'a de pseudo : les plaques au-dessus des têtes disparaissent et le chat est anonyme.");

        private static final DynamicLang FORMAT_CHAT = DynamicLang.of("scenario.anonymous.format-chat",
                "§7Inconnu §8» §f%2$s");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Anonymous"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.NAME_TAG); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                DisplayService.hideNameTagFor(other, player, true);
                DisplayService.hideNameTagFor(player, other, true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onChat(AsyncPlayerChatEvent event) {
            if (!isRunning()) return;
            event.setFormat(t(FORMAT_CHAT, event.getPlayer()));
        }
    }

    public static class AppleFlintSwitchScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.appleflintswitch.desc",
                "§7Les feuilles lâchent du silex (§e%silex%%%§7) et le gravier lâche des pommes (§e%pomme%%%§7).");

        private static final Set<Material> FEUILLES = EnumSet.of(Material.LEAVES, Material.LEAVES_2);

        private final Random random = new Random();

        @Var(name = "Chance de silex", desc = "Chance qu'un bloc de feuilles cassé lâche du silex.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceSilex = 5;

        @Var(name = "Chance de pomme", desc = "Chance qu'un bloc de gravier cassé lâche une pomme au lieu du gravier.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chancePomme = 10;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Apple & Flint Switch"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%silex%", chanceSilex, "%pomme%", chancePomme));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FLINT); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            Material type = block.getType();
            Location loc = block.getLocation().add(0.5D, 0.5D, 0.5D);

            if (FEUILLES.contains(type)) {
                if (!UHCUtils.Rng.chance(chanceSilex)) return;
                loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.FLINT, 1));
                return;
            }

            if (type != Material.GRAVEL) return;

            event.setCancelled(true);
            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, player.getItemInHand());
            boolean pomme = UHCUtils.Rng.chance(chancePomme);
            loc.getWorld().dropItemNaturally(loc, new ItemStack(pomme ? Material.APPLE : Material.GRAVEL, 1));
        }
    }

    public static class ArmorRevealScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.armorreveal.desc",
                "§7Ton armure reste invisible aux autres tant que tu n'as pas tué quelqu'un.");

        private static final DynamicLang REVELE = DynamicLang.of("scenario.armorreveal.revele",
                "§c%joueur% §7a fait sa première victime : son armure est désormais visible.");

        private final Set<UUID> reveles = new HashSet<>();

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Armor Reveal"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_CHESTPLATE); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            EquipmentHider.setArmorHidden(player, true);
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null) return;
            if (!reveles.add(killer.getUniqueId())) return;

            EquipmentHider.setArmorHidden(player, false);
            LangManager.get().sendAll(REVELE, Map.of("%joueur%", player.getName()));
        }

        @Override
        public void onStop() {
            reveles.clear();
            EquipmentHider.clearArmorHidden();
        }
    }

    public static class BackpacksScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.backpacks.desc",
                "§7§e/bp §7ouvre un sac à dos de §e%slots% §7emplacements, vidé au sol à ta mort.");

        private static final DynamicLang TITRE = DynamicLang.of("scenario.backpacks.titre",
                "§8Sac à dos");

        private final Map<UUID, Inventory> sacs = new HashMap<>();

        @Var(name = "Lignes du sac", desc = "Nombre de lignes de neuf emplacements dans le sac à dos.", type = VariableType.INTEGER, min = 1, max = 6)
        private int lignes = 3;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Backpacks"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%slots%", lignes * 9));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CHEST); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            CommandManager.get().register("bp", Command.ofPlayer(player ->
                    player.openInventory(sacs.computeIfAbsent(player.getUniqueId(),
                            uuid -> Bukkit.createInventory(null, lignes * 9, t(TITRE, player))))), "backpack");
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Inventory sac = sacs.remove(uhcPlayer.getUniqueId());
            if (sac == null) return;
            for (ItemStack content : sac.getContents()) {
                if (content == null || content.getType() == Material.AIR) continue;
                event.getDrops().add(content);
            }
            sac.clear();
        }

        @Override
        public void onStop() {
            sacs.clear();
        }
    }

    public static class BattleSirenScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.battlesiren.desc",
                "§7Chaque combat entre joueurs annonce à tous les coordonnées de l'assaillant.");

        private static final DynamicLang ALERTE = DynamicLang.of("scenario.battlesiren.alerte",
                "§4Combat §7en §e%x%§7, §e%y%§7, §e%z%§7 !");

        private final Map<UUID, Long> dernieresAlertes = new HashMap<>();

        @Var(name = "Délai entre deux alertes", desc = "Temps minimum entre deux annonces déclenchées par le même assaillant.", type = VariableType.TIME, min = 1)
        private int delaiEntreAlertesSec = 30;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Battle Siren"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_SWORD); }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player) || !(dammager instanceof Player attaquant)) return;

            long now = System.currentTimeMillis();
            Long derniere = dernieresAlertes.get(attaquant.getUniqueId());
            if (derniere != null && now - derniere < delaiEntreAlertesSec * 1000L) return;
            dernieresAlertes.put(attaquant.getUniqueId(), now);

            Location loc = attaquant.getLocation();
            LangManager.get().sendAll(ALERTE, Map.of(
                    "%x%", loc.getBlockX(),
                    "%y%", loc.getBlockY(),
                    "%z%", loc.getBlockZ()));
        }

        @Override
        public void onStop() {
            dernieresAlertes.clear();
        }
    }

    public static class BiomeParanoiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.biomeparanoia.desc",
                "§7Dans le TAB, la couleur du pseudo de chacun trahit le biome où il se trouve.");

        private static final ChatColor[] COULEURS = {
                ChatColor.DARK_GREEN,
                ChatColor.GREEN,
                ChatColor.AQUA,
                ChatColor.DARK_AQUA,
                ChatColor.BLUE,
                ChatColor.DARK_BLUE,
                ChatColor.LIGHT_PURPLE,
                ChatColor.DARK_PURPLE,
                ChatColor.RED,
                ChatColor.DARK_RED,
                ChatColor.GOLD,
                ChatColor.YELLOW,
                ChatColor.WHITE,
                ChatColor.GRAY,
                ChatColor.DARK_GRAY
        };

        private final Map<UUID, Biome> derniersBiomes = new HashMap<>();

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Biome Paranoia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WOOL); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            Biome biome = player.getLocation().getBlock().getBiome();
            if (biome == derniersBiomes.put(player.getUniqueId(), biome)) return;

            ChatColor couleur = COULEURS[biome.ordinal() % COULEURS.length];
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.equals(player)) continue;
                DisplayService.colorFor(viewer, player, couleur);
            }
        }

        @Override
        public void onStop() {
            derniersBiomes.clear();
        }
    }

    public static class BleedingSomeDecentItemsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bleedingsomedecentitems.desc",
                "§7Chaque mort lâche un butin garanti en plus de son inventaire.");

        @Var(name = "Butin sucré", desc = "Lâche le butin sucré (diamant, or, livre, fil, flèches) au lieu du butin d'outillage.", type = VariableType.BOOLEAN)
        private boolean butinSucre = false;

        @Var(name = "Pots de fleur", desc = "Pots de fleur lâchés par le butin d'outillage.", type = VariableType.INTEGER, min = 0)
        private int potsDeFleur = 1;

        @Var(name = "Pépites d'or", desc = "Pépites d'or lâchées par le butin d'outillage.", type = VariableType.INTEGER, min = 0)
        private int pepitesOr = 5;

        @Var(name = "Bâtons", desc = "Bâtons lâchés par le butin d'outillage.", type = VariableType.INTEGER, min = 0)
        private int batons = 3;

        @Var(name = "Efficacité de la pioche", desc = "Niveau d'Efficacité de la pioche en fer lâchée par le butin d'outillage.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauEfficacite = 2;

        @Var(name = "Diamants", desc = "Diamants lâchés par le butin sucré.", type = VariableType.INTEGER, min = 0)
        private int diamants = 1;

        @Var(name = "Lingots d'or", desc = "Lingots d'or lâchés par le butin sucré.", type = VariableType.INTEGER, min = 0)
        private int lingotsOr = 5;

        @Var(name = "Livres", desc = "Livres lâchés par le butin sucré.", type = VariableType.INTEGER, min = 0)
        private int livres = 1;

        @Var(name = "Fils", desc = "Fils lâchés par le butin sucré.", type = VariableType.INTEGER, min = 0)
        private int fils = 1;

        @Var(name = "Flèches", desc = "Flèches lâchées par le butin sucré.", type = VariableType.INTEGER, min = 0)
        private int fleches = 16;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Bleeding Some Decent Items"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FLOWER_POT_ITEM); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;

            if (butinSucre) {
                ajouter(event, Material.DIAMOND, diamants);
                ajouter(event, Material.GOLD_INGOT, lingotsOr);
                ajouter(event, Material.BOOK, livres);
                ajouter(event, Material.STRING, fils);
                ajouter(event, Material.ARROW, fleches);
                return;
            }

            ajouter(event, Material.FLOWER_POT_ITEM, potsDeFleur);
            ajouter(event, Material.GOLD_NUGGET, pepitesOr);
            ajouter(event, Material.STICK, batons);

            ItemStack pioche = new ItemStack(Material.IRON_PICKAXE, 1);
            pioche.addUnsafeEnchantment(Enchantment.DIG_SPEED, niveauEfficacite);
            event.getDrops().add(pioche);
        }

        private void ajouter(PlayerDeathEvent event, Material material, int amount) {
            if (amount <= 0) return;
            event.getDrops().add(new ItemStack(material, amount));
        }
    }

    public static class BlockBattleScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.blockbattle.desc",
                "§7Toutes les §e%intervalle% §7secondes, celui qui porte le plus de blocs différents gagne §b%diamants% diamant(s)§7.");

        private static final DynamicLang GAGNANT = DynamicLang.of("scenario.blockbattle.gagnant",
                "§e%joueur% §7porte §e%blocs% §7blocs différents et remporte §b%diamants% diamant(s)§7.");

        private static final DynamicLang EGALITE = DynamicLang.of("scenario.blockbattle.egalite",
                "§7Égalité au comptage des blocs : personne ne remporte de diamant.");

        @Var(name = "Intervalle du comptage", desc = "Temps entre deux comptages des blocs portés.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 600;

        @Var(name = "Diamants gagnés", desc = "Diamants remis au joueur qui porte le plus de blocs différents.", type = VariableType.INTEGER, min = 1)
        private int diamants = 4;

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Block Battle"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%intervalle%", intervalleSec, "%diamants%", diamants));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    compter();
                }
            };
            task.runTaskTimer(Main.get(), intervalleSec * 20L, intervalleSec * 20L);
        }

        @Override
        public void onStop() {
            if (task == null) return;
            task.cancel();
            task = null;
        }

        private void compter() {
            Player meilleur = null;
            int meilleurTotal = 0;
            boolean egalite = false;

            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player == null) continue;

                Set<Material> blocs = EnumSet.noneOf(Material.class);
                for (ItemStack content : player.getInventory().getContents()) {
                    if (content == null || !content.getType().isBlock()) continue;
                    blocs.add(content.getType());
                }
                if (blocs.size() < meilleurTotal || blocs.isEmpty()) continue;
                if (blocs.size() == meilleurTotal) {
                    egalite = true;
                    continue;
                }
                meilleur = player;
                meilleurTotal = blocs.size();
                egalite = false;
            }

            if (meilleur == null || egalite) {
                LangManager.get().sendAll(EGALITE);
                return;
            }

            meilleur.getInventory().addItem(new ItemStack(Material.DIAMOND, diamants));
            meilleur.playSound(meilleur.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            LangManager.get().sendAll(GAGNANT, Map.of(
                    "%joueur%", meilleur.getName(),
                    "%blocs%", meilleurTotal,
                    "%diamants%", diamants));
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new AchievementHuntersScenario(),
                new AchievementParanoiaScenario(),
                new AchievementsScenario(),
                new AloneTogetherScenario(),
                new AnonymousScenario(),
                new AppleFlintSwitchScenario(),
                new ArmorRevealScenario(),
                new BackpacksScenario(),
                new BattleSirenScenario(),
                new BiomeParanoiaScenario(),
                new BleedingSomeDecentItemsScenario(),
                new BlockBattleScenario()
        );
    }
}
