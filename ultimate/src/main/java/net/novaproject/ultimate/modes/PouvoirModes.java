package net.novaproject.ultimate.modes;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PouvoirModes {

    private PouvoirModes() {
    }

    private static void defaultScatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;
        if (UHCManager.get().getTeam_size() != 1 && uhcPlayer.getTeam().isPresent()) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        player.teleport(location);
    }

    public static class SuperZeroesMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.superzeroes.desc",
                "§7Chaque joueur reçoit un effet permanent tiré au sort parmi §eHâte§7, §eVision nocturne§7, §eRésistance au feu§7, §eSaturation §7et §eAbsorption§7.");

        private static final DynamicLang ASSIGNED = DynamicLang.of("mode.superzeroes.assigned",
                "§b☆ §7Ton super-pouvoir : §e%pouvoir%§7.");

        private static final DynamicLang[] POWER_NAMES = {
                DynamicLang.of("mode.superzeroes.power.hate", "Hâte"),
                DynamicLang.of("mode.superzeroes.power.visionnocturne", "Vision nocturne"),
                DynamicLang.of("mode.superzeroes.power.resistancefeu", "Résistance au feu"),
                DynamicLang.of("mode.superzeroes.power.saturation", "Saturation"),
                DynamicLang.of("mode.superzeroes.power.absorption", "Absorption")
        };

        @Var(name = "Niveau de Hâte", desc = "Niveau de l'effet Hâte du super-zéro qui le tire.", type = VariableType.INTEGER, min = 1)
        private int hasteLevel = 1;

        @Var(name = "Niveau d'Absorption", desc = "Niveau de l'effet Absorption du super-zéro qui le tire.", type = VariableType.INTEGER, min = 1)
        private int absorptionLevel = 1;

        private final Map<UUID, Integer> powers = new HashMap<>();

        @Override
        public String getName() {
            return "Super Zeroes";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.POTION);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            defaultScatter(uhcPlayer, location, teamloc);
        }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            int power = ThreadLocalRandom.current().nextInt(POWER_NAMES.length);
            powers.put(player.getUniqueId(), power);
            LangManager.get().send(ASSIGNED, player, Map.of("%pouvoir%", t(POWER_NAMES[power], player)));
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            Integer power = powers.get(p.getUniqueId());
            if (power == null) return;

            PotionEffect effect = switch (power) {
                case 0 -> new PotionEffect(PotionEffectType.FAST_DIGGING, 80, Math.max(0, hasteLevel - 1), false, false);
                case 1 -> new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, false, false);
                case 2 -> new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false);
                case 3 -> new PotionEffect(PotionEffectType.SATURATION, 80, 0, false, false);
                default -> new PotionEffect(PotionEffectType.ABSORPTION, 80, Math.max(0, absorptionLevel - 1), false, false);
            };

            UHCUtils.applyInfiniteEffects(new PotionEffect[]{effect}, p);
        }

        @Override
        public void onStop() {
            super.onStop();
            powers.clear();
        }
    }

    public static class DragonRushMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.dragonrush.desc",
                "§7Un portail de l'End apparaît en §e0,0§7, amputé de §e%yeux% yeux de l'Ender§7. Le premier à tuer le dragon gagne la partie.");

        private static final DynamicLang PORTAL_READY = DynamicLang.of("mode.dragonrush.portal",
                "§5✦ §7Le portail de l'End est ouvert en §e0,0 §7: il manque §e%yeux% yeux de l'Ender§7.");

        private static final DynamicLang DRAGON_SLAIN = DynamicLang.of("mode.dragonrush.slain",
                "§5✦ §e%player% §7a terrassé le dragon et remporte la partie !");

        private static final int[][] FRAMES = {
                {-1, -2, 0}, {0, -2, 0}, {1, -2, 0},
                {-1, 2, 2}, {0, 2, 2}, {1, 2, 2},
                {-2, -1, 3}, {-2, 0, 3}, {-2, 1, 3},
                {2, -1, 1}, {2, 0, 1}, {2, 1, 1}
        };

        @Var(name = "Yeux manquants", desc = "Nombre d'yeux de l'Ender absents du portail de 0,0.", type = VariableType.INTEGER, min = 0, max = 12)
        private int missingEyes = 3;

        private boolean dragonSlain = false;

        @Override
        public String getName() {
            return "Dragon Rush";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%yeux%", missingEyes));
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.DRAGON_EGG);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            defaultScatter(uhcPlayer, location, teamloc);
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            if (!buildPortal()) return;
            LangManager.get().sendAll(PORTAL_READY, Map.of("%yeux%", missingEyes));
        }

        private boolean buildPortal() {
            World world = Common.get().getArena();
            if (world == null) return false;

            int y = world.getHighestBlockYAt(0, 0);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    world.getBlockAt(dx, y - 1, dz).setType(Material.STONE);
                    for (int dy = 0; dy <= 3; dy++) {
                        world.getBlockAt(dx, y + dy, dz).setType(Material.AIR);
                    }
                }
            }

            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < FRAMES.length; i++) slots.add(i);
            Collections.shuffle(slots);
            Set<Integer> empty = new HashSet<>(slots.subList(0, Math.min(Math.max(missingEyes, 0), FRAMES.length)));

            for (int i = 0; i < FRAMES.length; i++) {
                Block block = world.getBlockAt(FRAMES[i][0], y, FRAMES[i][1]);
                block.setType(Material.ENDER_PORTAL_FRAME);
                block.setData((byte) (FRAMES[i][2] + (empty.contains(i) ? 0 : 4)));
            }
            return true;
        }

        @Override
        public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
            if (!isActive()) return;
            if (dragonSlain || !(entity instanceof EnderDragon)) return;
            if (killer == null) return;

            UHCPlayer winner = UHCPlayerManager.get().getPlayer(killer);
            if (winner == null || !winner.isPlaying()) return;

            dragonSlain = true;
            LangManager.get().sendAll(DRAGON_SLAIN, Map.of("%player%", killer.getName()));

            UHCTeam winningTeam = winner.getTeam().orElse(null);
            for (UHCPlayer other : new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers())) {
                if (other.getUuid().equals(winner.getUuid())) continue;
                if (winningTeam != null && other.getTeam().filter(winningTeam::equals).isPresent()) continue;

                other.setPlaying(false);
                Player op = other.getPlayer();
                if (op != null && op.isOnline()) {
                    op.setGameMode(GameMode.SPECTATOR);
                    op.playSound(op.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.0f);
                }
            }

            UHCManager.get().checkVictory();
        }

        @Override
        public void onStop() {
            super.onStop();
            dragonSlain = false;
        }
    }

    public static class CorruptKingsMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.corruptkings.desc",
                "§7Chaque équipe possède un §6roi secret §7qui absorbe les cœurs de ses coéquipiers morts, jusqu'à §c%coeurs% ❤§7.");

        private static final DynamicLang YOU_ARE_KING = DynamicLang.of("mode.corruptkings.crowned",
                "§6♛ §7Tu es le §6roi corrompu §7de ton équipe : personne ne le sait, et la mort de tes coéquipiers te renforce.");

        private static final DynamicLang ABSORBED = DynamicLang.of("mode.corruptkings.absorbed",
                "§6♛ §7Tu absorbes les cœurs de §e%player% §7(§c+%coeurs% ❤§7).");

        @Var(name = "Vie maximale du roi", desc = "Points de vie maximum que le roi peut atteindre en absorbant ses coéquipiers (40 = 20 cœurs).", type = VariableType.INTEGER, min = 2)
        private int kingMaxHealth = 40;

        private final Set<UUID> kings = new HashSet<>();

        @Override
        public String getName() {
            return "Corrupt Kings";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", kingMaxHealth / 2));
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.GOLD_HELMET);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        private void forceTeamMode() {
            if (UHCManager.get().getTeam_size() >= 2) return;
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CoreLang.COMMON_TEAM_REDFINIED_AUTO);
        }

        @Override
        public void enable() {
            super.enable();
            forceTeamMode();
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            if (isActive()) {
                forceTeamMode();
            } else {
                kings.clear();
            }
        }

        @Override
        public void onTeamUpdate() {
            forceTeamMode();
        }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            defaultScatter(uhcPlayer, location, teamloc);
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            kings.clear();

            for (UHCTeam team : UHCTeamManager.get().getTeams()) {
                List<UHCPlayer> members = new ArrayList<>(team.getPlayers());
                members.removeIf(member -> member.getPlayer() == null || !member.isPlaying());
                if (members.isEmpty()) continue;

                Player king = members.get(ThreadLocalRandom.current().nextInt(members.size())).getPlayer();
                kings.add(king.getUniqueId());
                LangManager.get().send(YOU_ARE_KING, king);
            }
        }

        @Override
        public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive()) return;

            Player dead = uhcPlayer.getPlayer();
            if (dead == null) return;
            if (kings.remove(dead.getUniqueId())) return;

            UHCTeam team = uhcPlayer.getTeam().orElse(null);
            if (team == null) return;

            for (UHCPlayer member : team.getPlayers()) {
                Player kingPlayer = member.getPlayer();
                if (kingPlayer == null || !kingPlayer.isOnline() || !member.isPlaying()) continue;
                if (!kings.contains(kingPlayer.getUniqueId())) continue;

                double gained = Math.min(dead.getMaxHealth(), kingMaxHealth - kingPlayer.getMaxHealth());
                if (gained <= 0) return;

                double hearts = gained / 2.0;
                kingPlayer.setMaxHealth(kingPlayer.getMaxHealth() + gained);
                kingPlayer.setHealth(Math.min(kingPlayer.getMaxHealth(), kingPlayer.getHealth() + gained));
                kingPlayer.playSound(kingPlayer.getLocation(), Sound.LEVEL_UP, 1.0f, 0.7f);
                LangManager.get().send(ABSORBED, kingPlayer, Map.of(
                        "%player%", dead.getName(),
                        "%coeurs%", hearts == Math.floor(hearts) ? String.valueOf((int) hearts) : String.valueOf(hearts)
                ));
                return;
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            kings.clear();
        }
    }

    public static class ElementsMode extends Scenario {

        private static final int EAU = 0;
        private static final int TERRE = 1;
        private static final int FEU = 2;
        private static final int AIR = 3;

        private static final DynamicLang DESC = DynamicLang.of("mode.elements.desc",
                "§7Chaque joueur reçoit l'un des quatre éléments — §9Eau§7, §6Terre§7, §cFeu §7ou §bAir §7— et le pouvoir qui va avec.");

        private static final DynamicLang ASSIGNED = DynamicLang.of("mode.elements.assigned",
                "§b❖ §7Ton élément : §e%element% §7— %pouvoir%§7.");

        private static final DynamicLang[] ELEMENT_NAMES = {
                DynamicLang.of("mode.elements.name.eau", "Eau"),
                DynamicLang.of("mode.elements.name.terre", "Terre"),
                DynamicLang.of("mode.elements.name.feu", "Feu"),
                DynamicLang.of("mode.elements.name.air", "Air")
        };

        private static final DynamicLang[] ELEMENT_POWERS = {
                DynamicLang.of("mode.elements.power.eau", "§7respiration aquatique permanente et nage accélérée"),
                DynamicLang.of("mode.elements.power.terre", "§7résistance aux dégâts et minage accéléré"),
                DynamicLang.of("mode.elements.power.feu", "§7immunité au feu, et tes coups enflamment ta cible"),
                DynamicLang.of("mode.elements.power.air", "§7saut amélioré et aucun dégât de chute")
        };

        @Var(name = "Niveau de Rapidité (Eau)", desc = "Niveau de l'effet Rapidité accordé à l'élément Eau lorsqu'il est dans l'eau.", type = VariableType.INTEGER, min = 1)
        private int eauSpeedLevel = 2;

        @Var(name = "Niveau de Résistance (Terre)", desc = "Niveau de l'effet Résistance permanent de l'élément Terre.", type = VariableType.INTEGER, min = 1)
        private int terreResistanceLevel = 1;

        @Var(name = "Niveau de Hâte (Terre)", desc = "Niveau de l'effet Hâte permanent de l'élément Terre.", type = VariableType.INTEGER, min = 1)
        private int terreHasteLevel = 2;

        @Var(name = "Durée d'embrasement (Feu)", desc = "Durée pendant laquelle la cible frappée par l'élément Feu brûle.", type = VariableType.TIME, min = 1)
        private int feuIgniteSec = 4;

        @Var(name = "Niveau de Saut (Air)", desc = "Niveau de l'effet Saut permanent de l'élément Air.", type = VariableType.INTEGER, min = 1)
        private int airJumpLevel = 2;

        private final Map<UUID, Integer> elements = new HashMap<>();

        @Override
        public String getName() {
            return "Elements";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.BLAZE_POWDER);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            defaultScatter(uhcPlayer, location, teamloc);
        }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            int element = ThreadLocalRandom.current().nextInt(ELEMENT_NAMES.length);
            elements.put(player.getUniqueId(), element);
            LangManager.get().send(ASSIGNED, player, Map.of(
                    "%element%", t(ELEMENT_NAMES[element], player),
                    "%pouvoir%", t(ELEMENT_POWERS[element], player)
            ));
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            Integer element = elements.get(p.getUniqueId());
            if (element == null) return;

            PotionEffect[] effects = switch (element) {
                case EAU -> inWater(p)
                        ? new PotionEffect[]{
                        new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, false, false),
                        new PotionEffect(PotionEffectType.SPEED, 80, Math.max(0, eauSpeedLevel - 1), false, false)
                } : new PotionEffect[]{new PotionEffect(PotionEffectType.WATER_BREATHING, 80, 0, false, false)};
                case TERRE -> new PotionEffect[]{
                        new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, Math.max(0, terreResistanceLevel - 1), false, false),
                        new PotionEffect(PotionEffectType.FAST_DIGGING, 80, Math.max(0, terreHasteLevel - 1), false, false)
                };
                case FEU -> new PotionEffect[]{new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false)};
                default -> new PotionEffect[]{new PotionEffect(PotionEffectType.JUMP, 80, Math.max(0, airJumpLevel - 1), false, false)};
            };

            UHCUtils.applyInfiniteEffects(effects, p);
        }

        private boolean inWater(Player player) {
            Material type = player.getLocation().getBlock().getType();
            return type == Material.WATER || type == Material.STATIONARY_WATER;
        }

        @Override
        public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (!(dammager instanceof Player attacker)) return;
            Integer element = elements.get(attacker.getUniqueId());
            if (element == null || element != FEU) return;
            entity.setFireTicks(Math.max(entity.getFireTicks(), feuIgniteSec * 20));
        }

        @Override
        public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
            if (!(entity instanceof Player player)) return;
            Integer element = elements.get(player.getUniqueId());
            if (element == null || element != AIR) return;
            event.setCancelled(true);
        }

        @Override
        public void onStop() {
            super.onStop();
            elements.clear();
        }
    }

    public static class WeatherWizardsMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.weatherwizards.desc",
                "§7Chaque joueur reçoit une §ebaguette météo §7: §eclic gauche §7pour changer de sort, §eclic droit §7pour le lancer. Six sorts, chacun sa recharge.");

        private static final DynamicLang WAND_NAME = DynamicLang.of("mode.weatherwizards.wand",
                "§b✦ §fBaguette météo");

        private static final DynamicLang WAND_GIVEN = DynamicLang.of("mode.weatherwizards.given",
                "§b✦ §7Tu reçois la §ebaguette météo §7: clic gauche pour changer de sort, clic droit pour le lancer.");

        private static final DynamicLang SELECTED = DynamicLang.of("mode.weatherwizards.selected",
                "§b✦ §7Sort sélectionné : §e%sort%§7.");

        private static final DynamicLang CAST = DynamicLang.of("mode.weatherwizards.cast",
                "§b✦ §7Tu lances §e%sort%§7.");

        private static final DynamicLang COOLDOWN = DynamicLang.of("mode.weatherwizards.cooldown",
                "§c✦ §7Ce sort est encore en recharge pour §e%temps%§7.");

        private static final DynamicLang NO_TARGET = DynamicLang.of("mode.weatherwizards.notarget",
                "§c✦ §7Aucune cible à portée : le sort n'est pas lancé.");

        private static final DynamicLang[] SPELL_NAMES = {
                DynamicLang.of("mode.weatherwizards.spell.eclaircie", "Éclaircie"),
                DynamicLang.of("mode.weatherwizards.spell.pluie", "Pluie"),
                DynamicLang.of("mode.weatherwizards.spell.bourrasque", "Bourrasque"),
                DynamicLang.of("mode.weatherwizards.spell.orage", "Orage"),
                DynamicLang.of("mode.weatherwizards.spell.foudre", "Foudre"),
                DynamicLang.of("mode.weatherwizards.spell.grele", "Grêle")
        };

        @Var(name = "Recharge — Éclaircie", desc = "Temps de recharge du sort Éclaircie.", type = VariableType.TIME, min = 1)
        private int eclaircieCooldownSec = 120;

        @Var(name = "Recharge — Pluie", desc = "Temps de recharge du sort Pluie.", type = VariableType.TIME, min = 1)
        private int pluieCooldownSec = 300;

        @Var(name = "Recharge — Bourrasque", desc = "Temps de recharge du sort Bourrasque.", type = VariableType.TIME, min = 1)
        private int bourrasqueCooldownSec = 420;

        @Var(name = "Recharge — Orage", desc = "Temps de recharge du sort Orage.", type = VariableType.TIME, min = 1)
        private int orageCooldownSec = 600;

        @Var(name = "Recharge — Foudre", desc = "Temps de recharge du sort Foudre.", type = VariableType.TIME, min = 1)
        private int foudreCooldownSec = 900;

        @Var(name = "Recharge — Grêle", desc = "Temps de recharge du sort Grêle.", type = VariableType.TIME, min = 1)
        private int greleCooldownSec = 1200;

        @Var(name = "Durée de la météo", desc = "Durée de la météo imposée par les sorts Éclaircie, Pluie et Orage.", type = VariableType.TIME, min = 1)
        private int weatherDurationSec = 300;

        @Var(name = "Rayon de Bourrasque", desc = "Rayon en blocs dans lequel Bourrasque repousse les adversaires.", type = VariableType.DOUBLE, min = 1)
        private double bourrasqueRadius = 8.0;

        @Var(name = "Poussée de Bourrasque", desc = "Distance en blocs sur laquelle Bourrasque projette les adversaires.", type = VariableType.DOUBLE, min = 1)
        private double bourrasquePushBlocks = 6.0;

        @Var(name = "Élévation de Bourrasque", desc = "Impulsion verticale appliquée par Bourrasque.", type = VariableType.DOUBLE, min = 0)
        private double bourrasqueVerticalBoost = 0.45;

        @Var(name = "Rayon de Foudre", desc = "Rayon en blocs dans lequel Foudre cherche l'adversaire le plus proche.", type = VariableType.DOUBLE, min = 1)
        private double foudreRadius = 25.0;

        @Var(name = "Rayon de Grêle", desc = "Rayon en blocs dans lequel Grêle frappe les adversaires.", type = VariableType.DOUBLE, min = 1)
        private double greleRadius = 10.0;

        @Var(name = "Dégâts de Grêle", desc = "Dégâts infligés par Grêle à chaque adversaire touché (2 = 1 cœur).", type = VariableType.DOUBLE, min = 1)
        private double greleDamage = 6.0;

        @Var(name = "Durée de Lenteur (Grêle)", desc = "Durée de l'effet Lenteur infligé par Grêle.", type = VariableType.TIME, min = 1)
        private int greleSlownessSec = 10;

        @Var(name = "Niveau de Lenteur (Grêle)", desc = "Niveau de l'effet Lenteur infligé par Grêle.", type = VariableType.INTEGER, min = 1)
        private int greleSlownessLevel = 2;

        private final Map<UUID, Integer> spells = new HashMap<>();

        @Override
        public String getName() {
            return "Weather Wizards";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.BLAZE_ROD);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
            defaultScatter(uhcPlayer, location, teamloc);
        }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            spells.put(player.getUniqueId(), 0);
            player.getInventory().addItem(new ItemCreator(Material.BLAZE_ROD).setName(t(WAND_NAME, player)).getItemstack());
            LangManager.get().send(WAND_GIVEN, player);
            LangManager.get().send(SELECTED, player, Map.of("%sort%", t(SPELL_NAMES[0], player)));
        }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            if (!isActive()) return;
            Integer spell = spells.get(player.getUniqueId());
            if (spell == null) return;

            ItemStack item = player.getItemInHand();
            if (item == null || item.getType() != Material.BLAZE_ROD) return;
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

            Action action = event.getAction();
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                int next = (spell + 1) % SPELL_NAMES.length;
                spells.put(player.getUniqueId(), next);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.6f);
                LangManager.get().send(SELECTED, player, Map.of("%sort%", t(SPELL_NAMES[next], player)));
                return;
            }
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
            event.setCancelled(true);

            String key = "WeatherWizardsSpell" + spell;
            long remaining = CooldownService.get(player, key);
            if (remaining > 0) {
                LangManager.get().send(COOLDOWN, player, Map.of("%temps%", TextUtils.getFormattedTime((int) ((remaining + 999) / 1000))));
                return;
            }
            if (!castSpell(player, spell)) {
                LangManager.get().send(NO_TARGET, player);
                return;
            }
            CooldownService.put(player, key, cooldownSecOf(spell) * 1000L);
            LangManager.get().send(CAST, player, Map.of("%sort%", t(SPELL_NAMES[spell], player)));
        }

        private int cooldownSecOf(int spell) {
            return switch (spell) {
                case 0 -> eclaircieCooldownSec;
                case 1 -> pluieCooldownSec;
                case 2 -> bourrasqueCooldownSec;
                case 3 -> orageCooldownSec;
                case 4 -> foudreCooldownSec;
                default -> greleCooldownSec;
            };
        }

        private boolean castSpell(Player caster, int spell) {
            World arena = Common.get().getArena();
            World world = arena != null ? arena : caster.getWorld();
            int weatherTicks = weatherDurationSec * 20;

            switch (spell) {
                case 0 -> {
                    world.setThundering(false);
                    world.setStorm(false);
                    world.setWeatherDuration(weatherTicks);
                }
                case 1 -> {
                    world.setThundering(false);
                    world.setStorm(true);
                    world.setWeatherDuration(weatherTicks);
                }
                case 2 -> {
                    List<Player> targets = enemiesAround(caster, bourrasqueRadius);
                    if (targets.isEmpty()) return false;
                    for (Player target : targets) {
                        PlayerUtils.knockbackFrom(target, caster.getLocation(), bourrasquePushBlocks, bourrasqueVerticalBoost);
                    }
                }
                case 3 -> {
                    world.setStorm(true);
                    world.setThundering(true);
                    world.setWeatherDuration(weatherTicks);
                    world.setThunderDuration(weatherTicks);
                }
                case 4 -> {
                    Player target = nearestEnemy(caster, foudreRadius);
                    if (target == null) return false;
                    target.getWorld().strikeLightning(target.getLocation());
                }
                default -> {
                    List<Player> targets = enemiesAround(caster, greleRadius);
                    if (targets.isEmpty()) return false;
                    for (Player target : targets) {
                        target.damage(greleDamage);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, greleSlownessSec * 20, Math.max(0, greleSlownessLevel - 1), false, true), true);
                    }
                }
            }

            caster.playSound(caster.getLocation(), Sound.AMBIENCE_THUNDER, 1.0f, 1.0f);
            return true;
        }

        private List<Player> enemiesAround(Player caster, double radius) {
            List<Player> targets = new ArrayList<>();
            UHCPlayer casterUhc = UHCPlayerManager.get().getPlayer(caster);
            UHCTeam team = casterUhc == null ? null : casterUhc.getTeam().orElse(null);
            double radiusSquared = radius * radius;

            for (UHCPlayer other : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player target = other.getPlayer();
                if (target == null || !target.isOnline()) continue;
                if (target.getUniqueId().equals(caster.getUniqueId())) continue;
                if (team != null && other.getTeam().filter(team::equals).isPresent()) continue;
                if (!target.getWorld().equals(caster.getWorld())) continue;
                if (target.getLocation().distanceSquared(caster.getLocation()) > radiusSquared) continue;
                targets.add(target);
            }
            return targets;
        }

        private Player nearestEnemy(Player caster, double radius) {
            Player nearest = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Player target : enemiesAround(caster, radius)) {
                double distance = target.getLocation().distanceSquared(caster.getLocation());
                if (distance >= nearestDistance) continue;
                nearestDistance = distance;
                nearest = target;
            }
            return nearest;
        }

        @Override
        public void onStop() {
            super.onStop();
            spells.clear();
        }
    }

    public static List<Scenario> all() {
        return List.of(new SuperZeroesMode(), new DragonRushMode(), new CorruptKingsMode(),
                new ElementsMode(), new WeatherWizardsMode());
    }
}
