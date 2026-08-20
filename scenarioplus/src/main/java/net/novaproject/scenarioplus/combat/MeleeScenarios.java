package net.novaproject.scenarioplus.combat;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.nms.NmsAccessor;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MeleeScenarios {

    private MeleeScenarios() {
    }

    private static Player arrowShooter(Entity damager) {
        if (!(damager instanceof Arrow arrow)) return null;
        return arrow.getShooter() instanceof Player shooter ? shooter : null;
    }

    public static class TripleArrowsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.3xarrows.desc",
                "§7Chaque tir à l'arc part en §e%fleches% §7flèches ; seule la première est récupérable.");

        private final Random random = new Random();

        @Var(name = "Flèches par tir", desc = "Nombre total de flèches envoyées à chaque tir d'arc.", type = VariableType.INTEGER, min = 1, max = 10)
        private int flechesParTir = 3;

        @Var(name = "Dispersion", desc = "Écart de trajectoire des flèches supplémentaires.", type = VariableType.DOUBLE, min = 0, max = 2)
        private double dispersion = 0.12;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "3x Arrows"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fleches%", flechesParTir));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ARROW); }

        @Override
        public void onBow(Entity entity, Player player, EntityShootBowEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (!(event.getProjectile() instanceof Arrow origin)) return;

            Vector base = origin.getVelocity();
            for (int i = 1; i < flechesParTir; i++) {
                Vector direction = base.clone().add(new Vector(
                        (random.nextDouble() - 0.5D) * dispersion,
                        (random.nextDouble() - 0.5D) * dispersion,
                        (random.nextDouble() - 0.5D) * dispersion));
                Arrow extra = player.launchProjectile(Arrow.class, direction);
                extra.setCritical(origin.isCritical());
                extra.setKnockbackStrength(origin.getKnockbackStrength());
                extra.setFireTicks(origin.getFireTicks());
                denyPickup(extra);
            }
        }

        private void denyPickup(Arrow arrow) {
            try {
                Object handle = NmsAccessor.getHandle(arrow);
                NmsAccessor.setField(handle, handle.getClass(), 0, "fromPlayer");
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static class AnvilArrowsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.anvilarrows.desc",
                "§7Une flèche géante en enclumes surplombe chaque lieu de mort pendant §e%duree% §7secondes.");

        private static final DynamicLang MARKER = DynamicLang.of("scenario.anvilarrows.marker",
                "§8» §7Une flèche d'enclumes marque le ciel en §fX %x% §7/ §fZ %z%§7.");

        private static final int ANVIL_ID = 145;

        private final List<Block> placed = new ArrayList<>();

        private final List<BukkitTask> retraits = new ArrayList<>();

        @Var(name = "Hauteur", desc = "Nombre de blocs entre le lieu de mort et le bas de la flèche.", type = VariableType.INTEGER, min = 1, max = 200)
        private int hauteur = 25;

        @Var(name = "Longueur du fût", desc = "Nombre d'enclumes composant le corps de la flèche.", type = VariableType.INTEGER, min = 2, max = 40)
        private int longueurFut = 12;

        @Var(name = "Taille de la pointe", desc = "Longueur de chaque branche de la pointe de la flèche.", type = VariableType.INTEGER, min = 1, max = 10)
        private int taillePointe = 4;

        @Var(name = "Durée d'affichage", desc = "Temps avant que la flèche d'enclumes ne disparaisse.", type = VariableType.TIME, min = 5)
        private int dureeSec = 180;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Anvil Arrows"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ANVIL); }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;
            Location death = event.getEntity().getLocation();
            World world = death.getWorld();
            if (world == null) return;

            int baseX = death.getBlockX();
            int baseZ = death.getBlockZ();
            int baseY = Math.min(world.getMaxHeight() - longueurFut - 1, death.getBlockY() + hauteur);
            if (baseY < 1) return;

            List<Block> marker = new ArrayList<>();
            for (int i = 0; i < longueurFut; i++) {
                raise(world, baseX, baseY + i, baseZ, marker);
            }
            for (int i = 1; i <= taillePointe; i++) {
                raise(world, baseX + i, baseY + i, baseZ, marker);
                raise(world, baseX - i, baseY + i, baseZ, marker);
                raise(world, baseX, baseY + i, baseZ + i, marker);
                raise(world, baseX, baseY + i, baseZ - i, marker);
            }
            if (marker.isEmpty()) return;

            placed.addAll(marker);
            LangManager.get().sendAll(MARKER, Map.of("%x%", baseX, "%z%", baseZ));

            retraits.add(new BukkitRunnable() {
                @Override
                public void run() {
                    for (Block block : marker) clear(block);
                    placed.removeAll(marker);
                }
            }.runTaskLater(Main.get(), dureeSec * 20L));
        }

        @Override
        public void onStop() {
            for (BukkitTask retrait : retraits) retrait.cancel();
            retraits.clear();
            for (Block block : placed) clear(block);
            placed.clear();
        }

        private void raise(World world, int x, int y, int z, List<Block> marker) {
            if (y < 1 || y >= world.getMaxHeight()) return;
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != Material.AIR) return;
            block.setTypeIdAndData(ANVIL_ID, (byte) 0, false);
            marker.add(block);
        }

        private void clear(Block block) {
            if (block.getType() == Material.ANVIL) block.setTypeIdAndData(0, (byte) 0, false);
        }
    }

    public static class ArmorCompensationScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.armorcompensation.desc",
                "§7Un joueur seul porte §b%solo% §7pièce(s) d'armure en diamant, et chaque équipier en retire §b%retrait%§7.");

        private static final DynamicLang QUOTA = DynamicLang.of("scenario.armorcompensation.quota",
                "§7Ton équipe te donne droit à §b%pieces% §7pièce(s) d'armure en diamant.");

        @Var(name = "Pièces en solo", desc = "Pièces d'armure en diamant autorisées à un joueur sans équipier.", type = VariableType.INTEGER, min = 0, max = 4)
        private int piecesSolo = 4;

        @Var(name = "Retrait par équipier", desc = "Pièces d'armure en diamant retirées par équipier supplémentaire.", type = VariableType.INTEGER, min = 0, max = 4)
        private int retraitParEquipier = 1;

        @Var(name = "Minimum garanti", desc = "Pièces d'armure en diamant toujours autorisées, quelle que soit la taille de l'équipe.", type = VariableType.INTEGER, min = 0, max = 4)
        private int minimumGaranti = 0;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Armor Compensation"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%solo%", piecesSolo, "%retrait%", retraitParEquipier));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_CHESTPLATE); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null) return;

            int taille = uhcPlayer.getTeam().map(team -> team.getPlayers().size()).orElse(1);
            int autorise = Math.max(minimumGaranti, piecesSolo - Math.max(0, taille - 1) * retraitParEquipier);
            uhcPlayer.setDiamondArmor(autorise);
            LangManager.get().send(QUOTA, player, Map.of("%pieces%", autorise));
        }

        @Override
        public void onStop() {
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                uhcPlayer.setDiamondArmor(-1);
            }
        }
    }

    public static class ArrowLifeScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.arrowlife.desc",
                "§7Une flèche qui touche t'annonce la vie restante de ta cible.");

        private static final DynamicLang REPORT = DynamicLang.of("scenario.arrowlife.report",
                "§e%cible% §7est à §c%pourcent%% §7de vie.");

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "ArrowLife"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOW); }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (!(entity instanceof Player victim)) return;
            Player shooter = arrowShooter(damager);
            if (shooter == null || shooter.equals(victim)) return;

            double restante = Math.max(0.0D, victim.getHealth() - event.getFinalDamage());
            int pourcent = (int) Math.round(restante / victim.getMaxHealth() * 100.0D);
            DisplayService.actionBar(shooter, t(REPORT, shooter, Map.of(
                    "%cible%", victim.getName(),
                    "%pourcent%", pourcent)));
        }
    }

    public static class AssaultAndBatteryScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.assaultandbattery.desc",
                "§7Dans chaque équipe, un joueur ne blesse qu'à l'épée et l'autre qu'à l'arc.");

        private static final DynamicLang ROLE_MELEE = DynamicLang.of("scenario.assaultandbattery.role-melee",
                "§7Tu es l'§cépée §7de ton équipe : tes flèches ne font aucun dégât.");

        private static final DynamicLang ROLE_RANGED = DynamicLang.of("scenario.assaultandbattery.role-ranged",
                "§7Tu es l'§barc §7de ton équipe : tes coups au corps à corps ne font aucun dégât.");

        private static final DynamicLang DENY_MELEE = DynamicLang.of("scenario.assaultandbattery.deny-melee",
                "§cTu ne blesses qu'à distance.");

        private static final DynamicLang DENY_RANGED = DynamicLang.of("scenario.assaultandbattery.deny-ranged",
                "§cTu ne blesses qu'au corps à corps.");

        private final Set<UUID> archers = new HashSet<>();

        private final Map<String, Integer> compteurs = new HashMap<>();

        @Var(name = "Uniquement en PvP", desc = "Limite la restriction aux dégâts infligés à un autre joueur.", type = VariableType.BOOLEAN)
        private boolean pvpUniquement = true;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Assault and Battery"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_SWORD); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null) return;

            String cle = uhcPlayer.getTeam().map(UHCTeam::name).orElse(player.getUniqueId().toString());
            int rang = compteurs.merge(cle, 1, Integer::sum) - 1;
            if (rang % 2 == 1) {
                archers.add(player.getUniqueId());
                LangManager.get().send(ROLE_RANGED, player);
                return;
            }
            LangManager.get().send(ROLE_MELEE, player);
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (pvpUniquement && !(entity instanceof Player)) return;

            Player shooter = arrowShooter(damager);
            if (shooter != null) {
                if (archers.contains(shooter.getUniqueId())) return;
                event.setCancelled(true);
                DisplayService.actionBar(shooter, t(DENY_RANGED, shooter));
                return;
            }
            if (!(damager instanceof Player attacker)) return;
            if (!archers.contains(attacker.getUniqueId())) return;
            event.setCancelled(true);
            DisplayService.actionBar(attacker, t(DENY_MELEE, attacker));
        }

        @Override
        public void onStop() {
            archers.clear();
            compteurs.clear();
        }
    }

    public static class BalanceScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.balance.desc",
                "§7Au-delà de §b%diamants% diamants §7ou §6%or% minerais d'or §7extraits, ce minerai devient pénible à miner.");

        private static final DynamicLang CAP_DIAMOND = DynamicLang.of("scenario.balance.cap-diamond",
                "§7Ton quota de §bdiamants §7est atteint : les suivants se minent péniblement.");

        private static final DynamicLang CAP_GOLD = DynamicLang.of("scenario.balance.cap-gold",
                "§7Ton quota d'§6or §7est atteint : les suivants se minent péniblement.");

        private static final DynamicLang KILL_BONUS = DynamicLang.of("scenario.balance.kill-bonus",
                "§7Ce kill relève ton quota de §b%diamants% diamant(s) §7et §6%or% minerai(s) d'or§7.");

        private final Map<UUID, Integer> diamantsMines = new HashMap<>();

        private final Map<UUID, Integer> orMine = new HashMap<>();

        private final Map<UUID, Integer> kills = new HashMap<>();

        @Var(name = "Quota de diamants", desc = "Diamants minables avant que le minerai ne devienne pénible.", type = VariableType.INTEGER, min = 1)
        private int quotaDiamant = 8;

        @Var(name = "Quota d'or", desc = "Minerais d'or minables avant que le minerai ne devienne pénible.", type = VariableType.INTEGER, min = 1)
        private int quotaOr = 48;

        @Var(name = "Diamants par kill", desc = "Diamants ajoutés au quota à chaque kill (mode Balance+).", type = VariableType.INTEGER, min = 0)
        private int bonusDiamantParKill = 0;

        @Var(name = "Or par kill", desc = "Minerais d'or ajoutés au quota à chaque kill (mode Balance+).", type = VariableType.INTEGER, min = 0)
        private int bonusOrParKill = 0;

        @Var(name = "Durée de la fatigue", desc = "Durée de la Fatigue de minage infligée au-delà du quota.", type = VariableType.TIME, min = 1)
        private int fatigueSec = 15;

        @Var(name = "Niveau de fatigue", desc = "Niveau de la Fatigue de minage infligée au-delà du quota.", type = VariableType.INTEGER, min = 1, max = 5)
        private int fatigueNiveau = 2;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Balance"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%diamants%", quotaDiamant, "%or%", quotaOr));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_ORE); }

        @Override
        public void onBreak(Player player, Block block, BlockBreakEvent event) {
            if (!isActive()) return;
            boolean diamant = block.getType() == Material.DIAMOND_ORE;
            if (!diamant && block.getType() != Material.GOLD_ORE) return;

            UUID uuid = player.getUniqueId();
            int bonus = kills.getOrDefault(uuid, 0) * (diamant ? bonusDiamantParKill : bonusOrParKill);
            int quota = (diamant ? quotaDiamant : quotaOr) + bonus;
            int mines = (diamant ? diamantsMines : orMine).merge(uuid, 1, Integer::sum);
            if (mines <= quota) return;

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,
                    fatigueSec * 20, fatigueNiveau - 1), true);
            if (mines == quota + 1) LangManager.get().send(diamant ? CAP_DIAMOND : CAP_GOLD, player);
        }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive()) return;
            if (killer == null) return;
            if (bonusDiamantParKill <= 0 && bonusOrParKill <= 0) return;

            kills.merge(killer.getUniqueId(), 1, Integer::sum);
            Player player = killer.getPlayer();
            if (player == null) return;
            LangManager.get().send(KILL_BONUS, player, Map.of(
                    "%diamants%", bonusDiamantParKill,
                    "%or%", bonusOrParKill));
        }

        @Override
        public void onStop() {
            diamantsMines.clear();
            orMine.clear();
            kills.clear();
        }
    }

    public static class BedBombsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bedbombs.desc",
                "§7Un clic droit sur un lit le fait exploser, dans toutes les dimensions.");

        private static final BlockFace[] AUTOUR = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        @Var(name = "Puissance", desc = "Puissance de l'explosion du lit.", type = VariableType.DOUBLE, min = 0, max = 20)
        private double puissance = 4.0D;

        @Var(name = "Met le feu", desc = "L'explosion du lit enflamme les blocs alentour.", type = VariableType.BOOLEAN)
        private boolean metLeFeu = false;

        @Var(name = "Casse les blocs", desc = "L'explosion du lit détruit le terrain.", type = VariableType.BOOLEAN)
        private boolean casseLesBlocs = true;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Bed Bombs"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BED); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            Block block = event.getClickedBlock();
            if (block == null || block.getType() != Material.BED_BLOCK) return;

            event.setCancelled(true);
            Location center = block.getLocation().add(0.5D, 0.5D, 0.5D);
            World world = center.getWorld();
            if (world == null) return;

            block.setTypeIdAndData(0, (byte) 0, false);
            for (BlockFace face : AUTOUR) {
                Block moitie = block.getRelative(face);
                if (moitie.getType() == Material.BED_BLOCK) moitie.setTypeIdAndData(0, (byte) 0, false);
            }
            world.createExplosion(center.getX(), center.getY(), center.getZ(),
                    (float) puissance, metLeFeu, casseLesBlocs);
        }
    }

    public static class BerserkScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.berserk.desc",
                "§7Un coup fatal te plonge §c%duree%s §7en rage : régénération, wither, et plus aucun dégât infligé.");

        private static final DynamicLang ENTER_TITLE = DynamicLang.of("scenario.berserk.enter-title",
                "§4§lBERSERK");

        private static final DynamicLang ENTER_SUB = DynamicLang.of("scenario.berserk.enter-sub",
                "§7Tu survis à ta mort, tiens bon.");

        private static final DynamicLang EXIT = DynamicLang.of("scenario.berserk.exit",
                "§7Ta rage retombe, tu as survécu.");

        private static final DynamicLang DENY = DynamicLang.of("scenario.berserk.deny",
                "§cTa rage t'empêche de blesser qui que ce soit.");

        private static final int TITRE_TICKS = 60;

        private final Map<UUID, Long> enrages = new HashMap<>();

        private final Map<UUID, Integer> declenchements = new HashMap<>();

        @Var(name = "Vie conservée", desc = "Cœurs laissés au joueur au moment où la rage remplace sa mort.", type = VariableType.INTEGER, min = 1, max = 10)
        private int coeursConserves = 1;

        @Var(name = "Durée de la rage", desc = "Durée de l'état de rage.", type = VariableType.TIME, min = 1)
        private int dureeSec = 20;

        @Var(name = "Niveau de régénération", desc = "Niveau de Régénération accordé pendant la rage.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauRegen = 2;

        @Var(name = "Niveau de wither", desc = "Niveau de Wither subi pendant la rage.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauWither = 1;

        @Var(name = "Rages par joueur", desc = "Nombre de fois où la rage peut remplacer la mort d'un même joueur.", type = VariableType.INTEGER, min = 1)
        private int ragesMax = 1;

        @Var(name = "Rage désarmée", desc = "Le joueur enragé ne peut infliger aucun dégât.", type = VariableType.BOOLEAN)
        private boolean rageDesarmee = true;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Berserk"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SKULL_ITEM); }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            UUID uuid = player.getUniqueId();
            if (estEnrage(uuid)) return;
            if (event.getFinalDamage() < player.getHealth()) return;
            if (declenchements.getOrDefault(uuid, 0) >= ragesMax) return;

            declenchements.merge(uuid, 1, Integer::sum);
            event.setCancelled(true);
            player.setHealth(Math.min(player.getMaxHealth(), coeursConserves * 2.0D));

            int ticks = dureeSec * 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, ticks, niveauRegen - 1), true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ticks, niveauWither - 1), true);
            enrages.put(uuid, System.currentTimeMillis() + dureeSec * 1000L);
            DisplayService.title(player, t(ENTER_TITLE, player), t(ENTER_SUB, player), TITRE_TICKS);
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!rageDesarmee || event.isCancelled()) return;

            Player shooter = arrowShooter(damager);
            Player attaquant = shooter != null ? shooter : (damager instanceof Player p ? p : null);
            if (attaquant == null || !estEnrage(attaquant.getUniqueId())) return;

            event.setCancelled(true);
            DisplayService.actionBar(attaquant, t(DENY, attaquant));
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            Long fin = enrages.get(player.getUniqueId());
            if (fin == null || fin > System.currentTimeMillis()) return;
            enrages.remove(player.getUniqueId());
            LangManager.get().send(EXIT, player);
        }

        @Override
        public void onStop() {
            enrages.clear();
            declenchements.clear();
        }

        private boolean estEnrage(UUID uuid) {
            Long fin = enrages.get(uuid);
            return fin != null && fin > System.currentTimeMillis();
        }
    }

    public static class BowFightersScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.bowfighters.desc",
                "§7Tout le monde démarre avec un livre Infinité, §e%fleches% §7flèche(s) et §e%fils% §7fil(s) ; au corps à corps, rien de mieux que l'épée en bois ou la hache en pierre.");

        private static final DynamicLang DENY = DynamicLang.of("scenario.bowfighters.deny",
                "§cCette arme est trop lourde pour un archer.");

        private static final Set<Material> ARMES_INTERDITES = EnumSet.of(
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLD_SWORD,
                Material.DIAMOND_SWORD,
                Material.IRON_AXE,
                Material.GOLD_AXE,
                Material.DIAMOND_AXE
        );

        @Var(name = "Flèches de départ", desc = "Flèches données à chaque joueur au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int flechesDepart = 1;

        @Var(name = "Fils de départ", desc = "Fils donnés à chaque joueur au début de la partie.", type = VariableType.INTEGER, min = 0)
        private int filsDepart = 2;

        @Var(name = "Niveau d'Infinité", desc = "Niveau de l'enchantement Infinité stocké dans le livre de départ.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauInfinite = 1;

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "BowFighters"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%fleches%", flechesDepart, "%fils%", filsDepart));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOW); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            PlayerUtils.giveOrDrop(player, new ItemCreator(Material.ENCHANTED_BOOK)
                    .addStoredEnchantment(Enchantment.ARROW_INFINITE, niveauInfinite)
                    .getItemstack());
            if (flechesDepart > 0) PlayerUtils.giveOrDrop(player, new ItemStack(Material.ARROW, flechesDepart));
            if (filsDepart > 0) PlayerUtils.giveOrDrop(player, new ItemStack(Material.STRING, filsDepart));
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (event.isCancelled()) return;
            if (!(damager instanceof Player attaquant)) return;

            ItemStack main = attaquant.getItemInHand();
            if (main == null || !ARMES_INTERDITES.contains(main.getType())) return;

            event.setCancelled(true);
            DisplayService.actionBar(attaquant, t(DENY, attaquant));
        }
    }

    public static class ChameleonsScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.chameleons.desc",
                "§7La couleur dominante de ton skin te désigne un milieu : tu y deviens invisible.");

        private static final DynamicLang ASSIGNED = DynamicLang.of("scenario.chameleons.assigned",
                "§7Ton skin te fond dans §f%milieu%§7.");

        private static final int SKIN_TIMEOUT_MS = 4000;

        private static final int TOLERANCE_GRIS = 30;

        private static final int SEUIL_CLARTE = 140;

        private static final int MARGE_CHAUDE = 40;

        private enum Camouflage {

            NEIGE(DynamicLang.of("scenario.chameleons.milieu-neige", "§fla neige et la glace"),
                    "ICE", "SNOW", "FROZEN", "TAIGA"),
            SABLE(DynamicLang.of("scenario.chameleons.milieu-sable", "§ele sable et les plages"),
                    "DESERT", "BEACH", "SAVANNA"),
            FEUILLAGE(DynamicLang.of("scenario.chameleons.milieu-feuillage", "§ales forêts et les plaines"),
                    "FOREST", "PLAINS", "JUNGLE", "SWAMP", "BIRCH", "ROOFED"),
            EAU(DynamicLang.of("scenario.chameleons.milieu-eau", "§9les océans et les rivières"),
                    "OCEAN", "RIVER"),
            ROCHE(DynamicLang.of("scenario.chameleons.milieu-roche", "§8la roche et le nether"),
                    "HELL", "MESA", "HILLS", "MOUNTAIN", "STONE", "EXTREME");

            private final DynamicLang libelle;
            private final String[] motsCles;

            Camouflage(DynamicLang libelle, String... motsCles) {
                this.libelle = libelle;
                this.motsCles = motsCles;
            }

            private boolean correspond(String biome) {
                for (String motCle : motsCles) {
                    if (biome.contains(motCle)) return true;
                }
                return false;
            }
        }

        private final Map<UUID, Camouflage> camouflages = new ConcurrentHashMap<>();

        private final List<BukkitTask> analyses = new ArrayList<>();

        @Override
        public Family getFamily() { return Family.COMBAT; }

        @Override public String getName() { return "Chameleons"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.INK_SACK); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            UUID uuid = player.getUniqueId();
            String textures = texturesProperty(player);

            analyses.add(new BukkitRunnable() {
                @Override
                public void run() {
                    Camouflage camouflage = resoudre(textures, uuid);
                    camouflages.put(uuid, camouflage);
                    if (player.isOnline()) {
                        LangManager.get().send(ASSIGNED, player,
                                Map.of("%milieu%", t(camouflage.libelle, player)));
                    }
                }
            }.runTaskAsynchronously(Main.get()));
        }

        @Override
        public void onSec(Player player) {
            if (!isActive()) return;
            Camouflage camouflage = camouflages.get(player.getUniqueId());
            if (camouflage == null) return;
            if (!camouflage.correspond(player.getLocation().getBlock().getBiome().name())) return;

            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0)}, player);
        }

        @Override
        public void onStop() {
            for (BukkitTask analyse : analyses) analyse.cancel();
            analyses.clear();
            camouflages.clear();
        }

        private String texturesProperty(Player player) {
            try {
                GameProfile profile = ((CraftPlayer) player).getHandle().getProfile();
                for (Property property : profile.getProperties().get("textures")) {
                    return property.getValue();
                }
            } catch (Throwable ignored) {
            }
            return null;
        }

        private Camouflage resoudre(String textures, UUID uuid) {
            Color dominante = couleurDominante(textures);
            if (dominante == null) {
                Camouflage[] valeurs = Camouflage.values();
                return valeurs[Math.floorMod(uuid.hashCode(), valeurs.length)];
            }
            return classer(dominante);
        }

        private Color couleurDominante(String textures) {
            if (textures == null) return null;
            try {
                String json = new String(Base64.getDecoder().decode(textures), StandardCharsets.UTF_8);
                int debut = json.indexOf("http");
                if (debut < 0) return null;
                int fin = json.indexOf('"', debut);
                if (fin < 0) return null;

                URLConnection connection = URI.create(json.substring(debut, fin).replace("\\/", "/"))
                        .toURL().openConnection();
                connection.setConnectTimeout(SKIN_TIMEOUT_MS);
                connection.setReadTimeout(SKIN_TIMEOUT_MS);

                BufferedImage skin;
                try (InputStream stream = connection.getInputStream()) {
                    skin = ImageIO.read(stream);
                }
                if (skin == null) return null;

                long rouge = 0;
                long vert = 0;
                long bleu = 0;
                long pixels = 0;
                for (int x = 0; x < skin.getWidth(); x++) {
                    for (int y = 0; y < skin.getHeight(); y++) {
                        int argb = skin.getRGB(x, y);
                        if (((argb >> 24) & 0xFF) < 128) continue;
                        rouge += (argb >> 16) & 0xFF;
                        vert += (argb >> 8) & 0xFF;
                        bleu += argb & 0xFF;
                        pixels++;
                    }
                }
                if (pixels == 0) return null;
                return new Color((int) (rouge / pixels), (int) (vert / pixels), (int) (bleu / pixels));
            } catch (Exception ignored) {
                return null;
            }
        }

        private Camouflage classer(Color couleur) {
            int rouge = couleur.getRed();
            int vert = couleur.getGreen();
            int bleu = couleur.getBlue();
            int max = Math.max(rouge, Math.max(vert, bleu));
            int min = Math.min(rouge, Math.min(vert, bleu));

            if (max - min < TOLERANCE_GRIS) return max > SEUIL_CLARTE ? Camouflage.NEIGE : Camouflage.ROCHE;
            if (bleu == max) return Camouflage.EAU;
            if (vert == max) return Camouflage.FEUILLAGE;
            return vert > bleu + MARGE_CHAUDE ? Camouflage.SABLE : Camouflage.ROCHE;
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new TripleArrowsScenario(),
                new AnvilArrowsScenario(),
                new ArmorCompensationScenario(),
                new ArrowLifeScenario(),
                new AssaultAndBatteryScenario(),
                new BalanceScenario(),
                new BedBombsScenario(),
                new BerserkScenario(),
                new BowFightersScenario(),
                new ChameleonsScenario()
        );
    }
}
