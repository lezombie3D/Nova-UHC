package net.novaproject.scenarioplus.loot;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcGiveHotbarEvent;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.nms.DisguiseService;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class InventoryScenarios {

    private InventoryScenarios() {
    }

    public static class HeadHuntersScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.headhunters.desc",
                "§7La tête d'un mort apparaît en surface à §e%distance% blocs §7de son cadavre.");

        private static final DynamicLang HEAD_SPAWNED = DynamicLang.of("scenario.headhunters.head-spawned",
                "§6La tête de §e%joueur% §6est apparue en §e%x%§6, §e%z%§6.");

        private final Random random = new Random();

        @Var(name = "Distance de la tête", desc = "Distance entre le lieu de la mort et l'apparition de la tête.", type = VariableType.INTEGER, min = 1)
        private int distance = 75;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Head Hunters"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%distance%", distance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Player victim = event.getEntity();
            World world = victim.getWorld();

            double angle = random.nextDouble() * Math.PI * 2.0;
            int x = victim.getLocation().getBlockX() + (int) (Math.cos(angle) * distance);
            int z = victim.getLocation().getBlockZ() + (int) (Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z);

            Block block = world.getBlockAt(x, y, z);
            block.setTypeIdAndData(Material.SKULL.getId(), (byte) 1, false);
            BlockState state = block.getState();
            if (state instanceof Skull skull) {
                skull.setSkullType(SkullType.PLAYER);
                skull.setOwner(victim.getName());
                skull.setRotation(BlockFace.NORTH);
                skull.update(true);
            }

            LangManager.get().sendAll(HEAD_SPAWNED, Map.of(
                    "%joueur%", victim.getName(),
                    "%x%", x,
                    "%z%", z));
        }
    }

    public static class IdentityRevealScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.identityreveal.desc",
                "§7Tout le monde porte l'apparence d'un autre joueur : le premier kill révèle la véritable identité.");

        private static final DynamicLang REVEALED = DynamicLang.of("scenario.identityreveal.revealed",
                "§e%joueur% §7vient de tuer : sa véritable identité est révélée.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Identity Reveal"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.NAME_TAG); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            List<Player> players = new ArrayList<>();
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player != null) players.add(player);
            }
            if (players.size() < 2) return;

            Collections.shuffle(players);
            for (int i = 0; i < players.size(); i++) {
                DisguiseService.get().disguise(players.get(i), players.get((i + 1) % players.size()));
            }
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null || !DisguiseService.get().isDisguised(player.getUniqueId())) return;

            DisguiseService.get().undisguise(player);
            LangManager.get().sendAll(REVEALED, Map.of("%joueur%", player.getName()));
        }

        @Override
        public void onStop() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                DisguiseService.get().undisguise(player);
            }
        }
    }

    public static class IdentityTheftScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.identitytheft.desc",
                "§7Toucher un joueur à l'arc te vole son apparence.");

        private static final DynamicLang STOLEN = DynamicLang.of("scenario.identitytheft.stolen",
                "§7Tu as volé l'apparence de §e%joueur%§7.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Identity Theft"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(entity instanceof Player victim)) return;
            if (!(dammager instanceof Arrow arrow)) return;
            if (!(arrow.getShooter() instanceof Player shooter)) return;
            if (shooter.equals(victim)) return;

            DisguiseService.get().disguise(shooter, victim);
            LangManager.get().send(STOLEN, shooter, Map.of("%joueur%", victim.getName()));
        }

        @Override
        public void onStop() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                DisguiseService.get().undisguise(player);
            }
        }
    }

    public static class InvestmentsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.investments.desc",
                "§7Clic droit sur un joueur avec des diamants ou de l'or pour miser dessus : chacun de ses kills te rapporte §e×%facteur%§7, sa mort te fait tout perdre.");

        private static final DynamicLang INVESTED = DynamicLang.of("scenario.investments.invested",
                "§7Tu as misé §e%montant% §7sur §e%joueur%§7.");

        private static final DynamicLang BACKED = DynamicLang.of("scenario.investments.backed",
                "§e%joueur% §7a misé §e%montant% §7sur toi.");

        private static final DynamicLang PAYOUT = DynamicLang.of("scenario.investments.payout",
                "§7Ta mise sur §e%joueur% §7te rapporte §e%montant%§7.");

        private static final DynamicLang LOST = DynamicLang.of("scenario.investments.lost",
                "§c%joueur% §7est mort : ta mise de §e%montant% §7est perdue.");

        private record Stake(UUID investor, Material material, int amount) {
        }

        private final Map<UUID, List<Stake>> stakes = new HashMap<>();

        @Var(name = "Multiplicateur de gain", desc = "Facteur appliqué à la mise à chaque kill du joueur soutenu.", type = VariableType.INTEGER, min = 1)
        private int multiplicateur = 2;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Investments"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%facteur%", multiplicateur));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND); }

        @Override
        public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
            if (!isActive()) return;
            if (!(event.getRightClicked() instanceof Player target)) return;

            ItemStack hand = player.getItemInHand();
            if (hand == null) return;
            if (hand.getType() != Material.DIAMOND && hand.getType() != Material.GOLD_INGOT) return;

            event.setCancelled(true);
            int amount = hand.getAmount();
            stakes.computeIfAbsent(target.getUniqueId(), uuid -> new ArrayList<>())
                    .add(new Stake(player.getUniqueId(), hand.getType(), amount));
            player.setItemInHand(new ItemStack(Material.AIR));

            LangManager.get().send(INVESTED, player, Map.of("%montant%", amount, "%joueur%", target.getName()));
            LangManager.get().send(BACKED, target, Map.of("%montant%", amount, "%joueur%", player.getName()));
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player tueur = killer.getPlayer();
            if (tueur == null) return;
            List<Stake> list = stakes.get(killer.getUniqueId());
            if (list == null) return;

            for (Stake stake : list) {
                Player investor = Bukkit.getPlayer(stake.investor());
                if (investor == null || !investor.isOnline()) continue;

                int gain = stake.amount() * multiplicateur;
                for (ItemStack left : investor.getInventory().addItem(new ItemStack(stake.material(), gain)).values()) {
                    investor.getWorld().dropItemNaturally(investor.getLocation(), left);
                }
                LangManager.get().send(PAYOUT, investor, Map.of(
                        "%joueur%", tueur.getName(),
                        "%montant%", gain));
            }
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            List<Stake> list = stakes.remove(uhcPlayer.getUniqueId());
            if (list == null) return;

            for (Stake stake : list) {
                Player investor = Bukkit.getPlayer(stake.investor());
                if (investor == null || !investor.isOnline()) continue;
                LangManager.get().send(LOST, investor, Map.of(
                        "%joueur%", event.getEntity().getName(),
                        "%montant%", stake.amount()));
            }
        }

        @Override
        public void onStop() {
            stakes.clear();
        }
    }

    public static class KillRevealScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.killreveal.desc",
                "§7Les couleurs d'équipe restent cachées dans le tab jusqu'au premier kill de chacun.");

        private static final DynamicLang REVEALED = DynamicLang.of("scenario.killreveal.revealed",
                "§e%joueur% §7a tué : sa couleur d'équipe est désormais visible.");

        private final Set<UUID> revealed = new HashSet<>();

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Kill Reveal"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BANNER); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                if (!revealed.contains(player.getUniqueId())) {
                    DisplayService.colorFor(other, player, ChatColor.GRAY);
                }
                if (!revealed.contains(other.getUniqueId())) {
                    DisplayService.colorFor(player, other, ChatColor.GRAY);
                }
            }
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null || !revealed.add(player.getUniqueId())) return;

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                DisplayService.resetColorFor(viewer, player);
            }
            LangManager.get().sendAll(REVEALED, Map.of("%joueur%", player.getName()));
        }

        @Override
        public void onStop() {
            revealed.clear();
        }
    }

    public static class KillswitchScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.killswitch.desc",
                "§7Tuer un joueur transfère tout son inventaire dans le tien.");

        private static final DynamicLang TAKEN = DynamicLang.of("scenario.killswitch.taken",
                "§7Tu récupères l'inventaire complet de §e%joueur%§7.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Killswitch"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.CHEST); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            if (killer == null) return;
            Player player = killer.getPlayer();
            if (player == null || event.getDrops().isEmpty()) return;

            for (ItemStack drop : event.getDrops()) {
                if (drop == null || drop.getType() == Material.AIR) continue;
                for (ItemStack left : player.getInventory().addItem(drop).values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
            event.getDrops().clear();
            LangManager.get().send(TAKEN, player, Map.of("%joueur%", event.getEntity().getName()));
        }
    }

    public static class LeggingsParanoiaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.leggingsparanoia.desc",
                "§7La couleur d'un joueur dans le tab trahit le type de jambières qu'il porte.");

        private final Map<UUID, ChatColor> couleurs = new HashMap<>();

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Leggings Paranoia"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_LEGGINGS); }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            ItemStack leggings = player.getInventory().getLeggings();
            ChatColor couleur = leggings == null ? ChatColor.DARK_GRAY : switch (leggings.getType()) {
                case LEATHER_LEGGINGS -> ChatColor.YELLOW;
                case CHAINMAIL_LEGGINGS -> ChatColor.GRAY;
                case IRON_LEGGINGS -> ChatColor.WHITE;
                case GOLD_LEGGINGS -> ChatColor.GOLD;
                case DIAMOND_LEGGINGS -> ChatColor.AQUA;
                default -> ChatColor.DARK_GRAY;
            };

            if (couleur == couleurs.get(player.getUniqueId())) return;
            couleurs.put(player.getUniqueId(), couleur);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                DisplayService.colorFor(viewer, player, couleur);
            }
        }

        @Override
        public void onStop() {
            couleurs.clear();
        }
    }

    public static class LuckyChestsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.luckychests.desc",
                "§7§e%coffres% §7enderchests sont cachées dans les grottes sous §eY=%altitude%§7 : §e%chance%% §7de butin, sinon un mauvais sort.");

        private static final DynamicLang GOOD_LOOT = DynamicLang.of("scenario.luckychests.good-loot",
                "§aCoffre chanceux : le butin est pour toi.");

        private static final DynamicLang BAD_LOOT = DynamicLang.of("scenario.luckychests.bad-loot",
                "§cCoffre maudit : tu paies ta curiosité.");

        private final Set<Location> coffres = new HashSet<>();

        private final Random random = new Random();

        @Var(name = "Nombre de coffres", desc = "Nombre d'enderchests dispersées dans les grottes au début de la partie.", type = VariableType.INTEGER, min = 1)
        private int nombreDeCoffres = 40;

        @Var(name = "Altitude maximum", desc = "Altitude sous laquelle les coffres sont cachés.", type = VariableType.INTEGER, min = 5, max = 255)
        private int altitudeMax = 45;

        @Var(name = "Chance de bon butin", desc = "Chance qu'un coffre contienne du butin plutôt qu'un mauvais sort.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chanceBonButin = 50;

        @Var(name = "Diamants du butin", desc = "Diamants lâchés par un coffre chanceux.", type = VariableType.INTEGER, min = 0)
        private int diamants = 2;

        @Var(name = "Or du butin", desc = "Lingots d'or lâchés par un coffre chanceux.", type = VariableType.INTEGER, min = 0)
        private int or = 4;

        @Var(name = "Durée du mauvais sort", desc = "Durée des effets négatifs infligés par un coffre maudit.", type = VariableType.TIME, min = 1)
        private int dureeMalusSec = 20;

        @Var(name = "Niveau du mauvais sort", desc = "Niveau des effets négatifs infligés par un coffre maudit.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauMalus = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Lucky Chests"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of(
                    "%coffres%", nombreDeCoffres,
                    "%altitude%", altitudeMax,
                    "%chance%", chanceBonButin));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_CHEST); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            World world = Common.get().getArena();
            if (world == null) return;

            WorldBorder border = world.getWorldBorder();
            Location centre = border.getCenter();
            int demi = (int) Math.floor(border.getSize() / 2.0);
            int essais = nombreDeCoffres * 20;

            while (coffres.size() < nombreDeCoffres && essais-- > 0) {
                int x = centre.getBlockX() - demi + random.nextInt(demi * 2 + 1);
                int z = centre.getBlockZ() - demi + random.nextInt(demi * 2 + 1);
                for (int y = altitudeMax; y > 5; y--) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) continue;
                    if (world.getBlockAt(x, y + 1, z).getType() != Material.AIR) continue;
                    if (!world.getBlockAt(x, y - 1, z).getType().isSolid()) continue;

                    block.setTypeIdAndData(Material.ENDER_CHEST.getId(), (byte) 0, false);
                    coffres.add(block.getLocation());
                    break;
                }
            }
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Block block = event.getClickedBlock();
            if (block == null || block.getType() != Material.ENDER_CHEST) return;
            if (!coffres.remove(block.getLocation())) return;

            event.setCancelled(true);
            Location loc = block.getLocation().add(0.5D, 0.5D, 0.5D);
            block.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false);

            if (UHCUtils.Rng.chance(chanceBonButin)) {
                if (diamants > 0) loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND, diamants));
                if (or > 0) loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT, or));
                LangManager.get().send(GOOD_LOOT, player);
                return;
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, dureeMalusSec * 20, niveauMalus - 1, false, true), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, dureeMalusSec * 20, niveauMalus - 1, false, true), true);
            LangManager.get().send(BAD_LOOT, player);
        }

        @Override
        public void onStop() {
            coffres.clear();
        }
    }

    public static class LuckyLeavesScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.luckyleaves.desc",
                "§7Casser des feuilles a §e%chance%% §7de chances de lâcher une pomme dorée.");

        private final Random random = new Random();

        @Var(name = "Chance de pomme dorée", desc = "Chance en pourcentage qu'une feuille cassée lâche une pomme dorée.", type = VariableType.DOUBLE, min = 0.0, max = 100.0)
        private double chance = 0.5;

        @Var(name = "Pommes lâchées", desc = "Nombre de pommes dorées lâchées quand le tirage est gagnant.", type = VariableType.INTEGER, min = 1)
        private int quantite = 1;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Lucky Leaves"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%chance%", chance));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LEAVES); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            if (block.getType() != Material.LEAVES && block.getType() != Material.LEAVES_2) return;
            if (random.nextDouble() * 100.0 >= chance) return;

            Location loc = block.getLocation().add(0.5D, 0.5D, 0.5D);
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLDEN_APPLE, quantite));
        }
    }

    public static class ManaScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.mana.desc",
                "§7Les niveaux sont ta mana : §e+%gain% §7par kill, §c-1 §7tous les §c%degats% §7dégâts encaissés, dépensables avec §e/mana§7.");

        private static final DynamicLang GAINED = DynamicLang.of("scenario.mana.gained",
                "§b+%gain% mana §7pour ce kill.");

        private static final DynamicLang USAGE = DynamicLang.of("scenario.mana.usage",
                "§7Mana : §b%niveaux%§7. §e/mana haste §7(%haste%) §8· §e/mana or §7(%or%) §8· §e/mana fleches §7(%fleches%)");

        private static final DynamicLang NOT_ENOUGH = DynamicLang.of("scenario.mana.not-enough",
                "§cIl te faut §b%cout% mana §cpour cet achat.");

        private static final DynamicLang BOUGHT = DynamicLang.of("scenario.mana.bought",
                "§7Achat effectué pour §b%cout% mana§7.");

        private final Map<UUID, Double> degatsAccumules = new HashMap<>();

        @Var(name = "Mana par kill", desc = "Niveaux gagnés à chaque kill.", type = VariableType.INTEGER, min = 0)
        private int gainParKill = 4;

        @Var(name = "Dégâts par mana perdue", desc = "Dégâts encaissés qui coûtent un niveau.", type = VariableType.DOUBLE, min = 0.5)
        private double degatsParManaPerdue = 4.0;

        @Var(name = "Coût de la hâte", desc = "Mana dépensée pour le bonus de hâte.", type = VariableType.INTEGER, min = 0)
        private int coutHaste = 10;

        @Var(name = "Durée de la hâte", desc = "Durée du bonus de hâte acheté.", type = VariableType.TIME, min = 1)
        private int dureeHasteSec = 60;

        @Var(name = "Niveau de la hâte", desc = "Niveau du bonus de hâte acheté.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauHaste = 2;

        @Var(name = "Coût de l'or", desc = "Mana dépensée pour un lot de lingots d'or.", type = VariableType.INTEGER, min = 0)
        private int coutOr = 5;

        @Var(name = "Lingots d'or achetés", desc = "Lingots d'or reçus par achat.", type = VariableType.INTEGER, min = 1)
        private int quantiteOr = 3;

        @Var(name = "Coût des flèches", desc = "Mana dépensée pour un lot de flèches.", type = VariableType.INTEGER, min = 0)
        private int coutFleches = 3;

        @Var(name = "Flèches achetées", desc = "Flèches reçues par achat.", type = VariableType.INTEGER, min = 1)
        private int quantiteFleches = 16;

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Mana"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%gain%", gainParKill, "%degats%", degatsParManaPerdue));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EXP_BOTTLE); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            CommandManager.get().register("mana", new Command.PlayerCommand() {
                @Override
                protected void run(Player player, CommandArguments args) {
                    acheter(player, args);
                }

                @Override
                public List<String> tabComplete(CommandArguments args) {
                    return getStrings(args, "haste", "or", "fleches");
                }
            });
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null || gainParKill <= 0) return;
            Player player = killer.getPlayer();
            if (player == null) return;

            player.giveExpLevels(gainParKill);
            LangManager.get().send(GAINED, player, Map.of("%gain%", gainParKill));
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;

            double cumul = degatsAccumules.getOrDefault(player.getUniqueId(), 0.0) + event.getFinalDamage();
            int perdus = (int) (cumul / degatsParManaPerdue);
            degatsAccumules.put(player.getUniqueId(), cumul - perdus * degatsParManaPerdue);
            if (perdus <= 0) return;

            player.setLevel(Math.max(0, player.getLevel() - perdus));
        }

        @Override
        public void onStop() {
            degatsAccumules.clear();
        }

        private void acheter(Player player, CommandArguments args) {
            if (!isActive()) return;
            switch (args.get(0, "").toLowerCase(Locale.ROOT)) {
                case "haste" -> {
                    if (!payer(player, coutHaste)) return;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, dureeHasteSec * 20, niveauHaste - 1, false, true), true);
                }
                case "or" -> {
                    if (!payer(player, coutOr)) return;
                    player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, quantiteOr));
                }
                case "fleches" -> {
                    if (!payer(player, coutFleches)) return;
                    player.getInventory().addItem(new ItemStack(Material.ARROW, quantiteFleches));
                }
                default -> LangManager.get().send(USAGE, player, Map.of(
                        "%niveaux%", player.getLevel(),
                        "%haste%", coutHaste,
                        "%or%", coutOr,
                        "%fleches%", coutFleches));
            }
        }

        private boolean payer(Player player, int cout) {
            if (player.getLevel() < cout) {
                LangManager.get().send(NOT_ENOUGH, player, Map.of("%cout%", cout));
                return false;
            }
            player.setLevel(player.getLevel() - cout);
            LangManager.get().send(BOUGHT, player, Map.of("%cout%", cout));
            return true;
        }
    }

    public static class MysteryScenariosScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.mysteryscenarios.desc",
                "§7Les joueurs ne peuvent pas consulter la liste des scénarios actifs.");

        private static final DynamicLang HIDDEN = DynamicLang.of("scenario.mysteryscenarios.hidden",
                "§7Les scénarios de cette partie resteront §esecrets§7.");

        @Override
        public Family getFamily() { return Family.LOOT; }

        @Override public String getName() { return "Mystery Scenarios"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @EventHandler
        public void onGiveHotbar(UhcGiveHotbarEvent event) {
            if (!isActive()) return;
            if (RankManager.isStaff(event.getPlayer())) return;

            ItemStack livre = Common.get().getActiveScenario().getItemstack();
            event.getItems().removeIf(item -> item != null && livre.isSimilar(item.buildItem()));
            LangManager.get().send(HIDDEN, event.getPlayer());
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new HeadHuntersScenario(),
                new IdentityRevealScenario(),
                new IdentityTheftScenario(),
                new InvestmentsScenario(),
                new KillRevealScenario(),
                new KillswitchScenario(),
                new LeggingsParanoiaScenario(),
                new LuckyChestsScenario(),
                new LuckyLeavesScenario(),
                new ManaScenario(),
                new MysteryScenariosScenario()
        );
    }
}
