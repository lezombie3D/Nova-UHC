package net.novaproject.scenarioplus.vie;

import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.game.PendingDeathManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class AbsorptionScenarios {

    private static final Random RANDOM = new Random();

    private static final int SAFE_LOCATION_TRIES = 30;
    private static final double BORDER_MARGIN = 16.0;

    private AbsorptionScenarios() {
    }

    private static boolean isGoldenApple(ItemStack item) {
        return item != null && item.getType() == Material.GOLDEN_APPLE;
    }

    private static Location safeLocation(World world) {
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double radius = Math.max(BORDER_MARGIN, border.getSize() / 2.0 - BORDER_MARGIN);
        for (int i = 0; i < SAFE_LOCATION_TRIES; i++) {
            int x = (int) (center.getX() + (RANDOM.nextDouble() * 2 - 1) * radius);
            int z = (int) (center.getZ() + (RANDOM.nextDouble() * 2 - 1) * radius);
            Block ground = world.getHighestBlockAt(x, z);
            Material type = ground.getRelative(0, -1, 0).getType();
            if (type == Material.WATER || type == Material.STATIONARY_WATER) continue;
            if (type == Material.LAVA || type == Material.STATIONARY_LAVA) continue;
            if (type == Material.AIR) continue;
            return ground.getLocation().add(0.5, 0.0, 0.5);
        }
        return world.getSpawnLocation();
    }

    private static void scheduleRevive(UHCPlayer victim, UHCPlayer killer, PlayerDeathEvent event,
                                       long waitTicks, int priority, Consumer<Player> onRevive) {
        PendingDeathManager manager = PendingDeathManager.get();
        manager.beginPendingDeath(victim, killer, event, Math.max(1L, waitTicks));
        manager.registerResurrection(victim, priority, () -> {
            manager.cancelPendingDeath(victim);
            Player player = victim.getPlayer();
            if (player != null && player.isOnline()) onRevive.accept(player);
        });
    }

    public static class DoubleHealthScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.doublehealth.desc",
                "§7Tout le monde démarre avec §c%coeurs% ❤§7.");

        @Var(name = "Cœurs de départ", desc = "Nombre de cœurs maximum accordés à chaque joueur au démarrage.", type = VariableType.INTEGER, min = 1, max = 100)
        private int coeurs = 20;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Double Health"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%coeurs%", coeurs));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.setMaxHealth(coeurs * 2.0);
            player.setHealth(player.getMaxHealth());
        }
    }

    public static class GameHealthScenario extends Scenario {

        private static final String OBJECTIVE_NAME = "gamehealth";

        private static final DynamicLang DESC = DynamicLang.of("scenario.gamehealth.desc",
                "§7Le TAB affiche la §csomme des points de vie §7de tous les joueurs encore vivants.");

        private static final DynamicLang TAB_TITLE = DynamicLang.of("scenario.gamehealth.tab-title", "§c❤");

        @Var(name = "Intervalle de rafraîchissement", desc = "Secondes entre deux mises à jour du total affiché en TAB.", type = VariableType.TIME, min = 1)
        private int intervalleSec = 1;

        private BukkitRunnable task;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Game Health"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            if (task != null) task.cancel();
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    refresh();
                }
            };
            task.runTaskTimer(Main.get(), 0L, intervalleSec * 20L);
        }

        private void refresh() {
            Objective objective = objective(true);
            if (objective == null) return;

            int total = 0;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                if (player != null) total += (int) Math.ceil(player.getHealth());
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                objective.getScore(online.getName()).setScore(total);
            }
        }

        private Objective objective(boolean create) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) return null;
            Scoreboard board = manager.getMainScoreboard();
            Objective objective = board.getObjective(OBJECTIVE_NAME);
            if (objective == null && create) {
                objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
                objective.setDisplayName(t(TAB_TITLE));
                objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }
            return objective;
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            Objective objective = objective(false);
            if (objective != null) objective.unregister();
        }
    }

    public static class GapZapScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.gapzap.desc",
                "§7Subir des dégâts pendant les §e%duree% §7secondes de régénération d'une pomme d'or l'annule.");

        private static final DynamicLang ZAPPED = DynamicLang.of("scenario.gapzap.zapped",
                "§c✖ §7Ta régénération de pomme d'or a été coupée par les dégâts.");

        private final Map<UUID, Long> gappleUntil = new HashMap<>();

        @Var(name = "Fenêtre de régénération", desc = "Secondes après une pomme d'or pendant lesquelles la régénération est annulable.", type = VariableType.TIME, min = 1)
        private int fenetreSec = 5;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Gap Zap"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", fenetreSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE); }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive() || !isGoldenApple(item)) return;
            gappleUntil.put(player.getUniqueId(), System.currentTimeMillis() + fenetreSec * 1000L);
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive() || event.isCancelled() || event.getFinalDamage() <= 0) return;
            Long until = gappleUntil.get(player.getUniqueId());
            if (until == null || System.currentTimeMillis() > until) return;

            gappleUntil.remove(player.getUniqueId());
            if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) return;
            player.removePotionEffect(PotionEffectType.REGENERATION);
            LangManager.get().send(ZAPPED, player);
        }

        @Override
        public void onStop() {
            gappleUntil.clear();
        }
    }

    public static class HealthDonorScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.healthdonor.desc",
                "§7§e/donatehp <joueur> [demi-cœurs] §7donne de la vie pour §a%cout% niveau(x) §7par demi-cœur.");

        private static final DynamicLang USAGE = DynamicLang.of("scenario.healthdonor.usage",
                "§7Utilisation : §e/donatehp <joueur> [demi-cœurs]§7.");

        private static final DynamicLang NO_TARGET = DynamicLang.of("scenario.healthdonor.no-target",
                "§c✖ §7Ce joueur est introuvable.");

        private static final DynamicLang NOT_ENOUGH_XP = DynamicLang.of("scenario.healthdonor.not-enough-xp",
                "§c✖ §7Il te faut §a%cout% niveau(x) §7pour ce don.");

        private static final DynamicLang NOTHING_TO_GIVE = DynamicLang.of("scenario.healthdonor.nothing",
                "§c✖ §7Tu ne peux rien donner : ta vie est trop basse ou la cible est pleine.");

        private static final DynamicLang GIVEN = DynamicLang.of("scenario.healthdonor.given",
                "§a✔ §7Tu as donné §c%coeurs% ❤ §7à §f%joueur%§7.");

        private static final DynamicLang RECEIVED = DynamicLang.of("scenario.healthdonor.received",
                "§a✔ §f%joueur% §7t'a donné §c%coeurs% ❤§7.");

        @Var(name = "Coût par demi-cœur", desc = "Niveaux d'expérience dépensés par demi-cœur donné.", type = VariableType.INTEGER, min = 0)
        private int coutParDemiCoeur = 1;

        @Var(name = "Don par défaut", desc = "Demi-cœurs donnés quand la commande est lancée sans quantité.", type = VariableType.INTEGER, min = 1)
        private int donParDefaut = 2;

        @Var(name = "Vie minimum du donneur", desc = "Demi-cœurs que le donneur doit conserver après son don.", type = VariableType.INTEGER, min = 1)
        private int vieMinimumDonneur = 2;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Health Donor"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%cout%", coutParDemiCoeur));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EXP_BOTTLE); }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            CommandManager.get().register("donatehp", new DonateHpCMD(this), "dhp");
        }

        private void donate(Player donor, Player target, int demiCoeurs) {
            double transfert = Math.min(demiCoeurs, target.getMaxHealth() - target.getHealth());
            transfert = Math.min(transfert, donor.getHealth() - vieMinimumDonneur);
            if (transfert <= 0) {
                LangManager.get().send(NOTHING_TO_GIVE, donor);
                return;
            }

            int cout = (int) Math.ceil(transfert) * coutParDemiCoeur;
            if (donor.getLevel() < cout) {
                LangManager.get().send(NOT_ENOUGH_XP, donor, Map.of("%cout%", cout));
                return;
            }

            donor.setLevel(donor.getLevel() - cout);
            donor.setHealth(donor.getHealth() - transfert);
            target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + transfert));

            Map<String, Object> donorPlaceholders = Map.of("%coeurs%", transfert / 2.0, "%joueur%", target.getName());
            Map<String, Object> targetPlaceholders = Map.of("%coeurs%", transfert / 2.0, "%joueur%", donor.getName());
            LangManager.get().send(GIVEN, donor, donorPlaceholders);
            LangManager.get().send(RECEIVED, target, targetPlaceholders);
        }

        public static class DonateHpCMD extends Command.PlayerCommand {

            private final HealthDonorScenario scenario;

            public DonateHpCMD(HealthDonorScenario scenario) {
                this.scenario = scenario;
            }

            @Override
            protected void run(Player player, CommandArguments args) {
                if (!scenario.isActive()) return;
                if (args.size() < 1) {
                    LangManager.get().send(USAGE, player);
                    return;
                }
                Player target = Bukkit.getPlayerExact(args.getArgument(0));
                if (target == null || !target.isOnline() || target == player) {
                    LangManager.get().send(NO_TARGET, player);
                    return;
                }
                scenario.donate(player, target, Math.max(1, args.getInt(1, scenario.donParDefaut)));
            }

            @Override
            public List<String> tabComplete(CommandArguments args) {
                return playersFirstArg(args);
            }
        }
    }

    public static class LifestealScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.lifesteal.desc",
                "§7Chaque kill ajoute §c%gain% ❤ §7définitivement à ta vie maximale, jusqu'à §c%plafond% ❤§7.");

        private static final DynamicLang STOLEN = DynamicLang.of("scenario.lifesteal.stolen",
                "§4✦ §7Vie maximale volée : tu montes à §c%coeurs% ❤§7.");

        @Var(name = "Gain par kill", desc = "Demi-cœurs de vie maximale gagnés à chaque kill.", type = VariableType.INTEGER, min = 1)
        private int gainDemiCoeurs = 1;

        @Var(name = "Plafond de vie", desc = "Cœurs maximum atteignables par le vol de vie.", type = VariableType.INTEGER, min = 1, max = 100)
        private int plafondCoeurs = 30;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Lifesteal"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%gain%", gainDemiCoeurs / 2.0, "%plafond%", plafondCoeurs));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_SWORD); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive() || killer == null) return;
            Player player = killer.getPlayer();
            if (player == null || !player.isOnline()) return;

            double plafond = plafondCoeurs * 2.0;
            if (player.getMaxHealth() >= plafond) return;

            double nouvelle = Math.min(plafond, player.getMaxHealth() + gainDemiCoeurs);
            player.setMaxHealth(nouvelle);
            LangManager.get().send(STOLEN, player, Map.of("%coeurs%", nouvelle / 2.0));
        }
    }

    public static class NerfedAbsorptionScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.nerfedabsorption.desc",
                "§7Les §e%pommes% §7première(s) pomme(s) d'or ne donnent que l'absorption, pas la régénération.");

        private static final DynamicLang NERFED = DynamicLang.of("scenario.nerfedabsorption.nerfed",
                "§6✦ §7Cette pomme d'or ne t'accorde que l'absorption.");

        private final Map<UUID, Integer> eaten = new HashMap<>();

        @Var(name = "Pommes sans régénération", desc = "Nombre de premières pommes d'or privées de régénération.", type = VariableType.INTEGER, min = 1)
        private int pommesSansRegen = 1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Nerfed Absorption"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%pommes%", pommesSansRegen));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SPECKLED_MELON); }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive() || !isGoldenApple(item)) return;
            int count = eaten.merge(player.getUniqueId(), 1, Integer::sum);
            if (count > pommesSansRegen) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    LangManager.get().send(NERFED, player);
                }
            }.runTaskLater(Main.get(), 1L);
        }

        @Override
        public void onStop() {
            eaten.clear();
        }
    }

    public static class PotentialHeartsScenario extends Scenario implements Listener {

        private static final DynamicLang DESC = DynamicLang.of("scenario.potentialhearts.desc",
                "§7Tu démarres à §c%depart% ❤ §7et chaque soin gaspillé débloque de la vie maximale, jusqu'à §c%maximum% ❤§7.");

        private static final DynamicLang UNLOCKED = DynamicLang.of("scenario.potentialhearts.unlocked",
                "§c✦ §7Cœur potentiel débloqué : §c%coeurs% ❤ §7de vie maximale.");

        @Var(name = "Cœurs de départ", desc = "Vie maximale au démarrage, avant tout déblocage.", type = VariableType.INTEGER, min = 1, max = 100)
        private int coeursDepart = 10;

        @Var(name = "Cœurs maximum", desc = "Vie maximale atteignable en débloquant des cœurs potentiels.", type = VariableType.INTEGER, min = 1, max = 100)
        private int coeursMaximum = 20;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Potential Hearts"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%depart%", coeursDepart, "%maximum%", coeursMaximum));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MELON); }

        @Override
        public void onStart(Player player) {
            if (!isActive()) return;
            player.setMaxHealth(coeursDepart * 2.0);
            player.setHealth(player.getMaxHealth());
        }

        @EventHandler
        public void onRegain(EntityRegainHealthEvent event) {
            if (!isRunning()) return;
            if (!(event.getEntity() instanceof Player player)) return;

            double plafond = coeursMaximum * 2.0;
            if (player.getMaxHealth() >= plafond) return;
            if (player.getHealth() < player.getMaxHealth()) return;

            double gain = Math.min(event.getAmount(), plafond - player.getMaxHealth());
            if (gain <= 0) return;

            event.setCancelled(true);
            player.setMaxHealth(player.getMaxHealth() + gain);
            player.setHealth(player.getMaxHealth());
            LangManager.get().send(UNLOCKED, player, Map.of("%coeurs%", player.getMaxHealth() / 2.0));
        }
    }

    public static class PotionHealingScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.potionhealing.desc",
                "§7Les pommes d'or deviennent un soin instantané §e%pomme% §7et les têtes dorées un soin instantané §e%tete%§7.");

        private static final DynamicLang HEALED = DynamicLang.of("scenario.potionhealing.healed",
                "§d✦ §7Soin instantané au lieu de la régénération.");

        @Var(name = "Niveau pomme d'or", desc = "Niveau du soin instantané accordé par une pomme d'or.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauPomme = 1;

        @Var(name = "Niveau tête dorée", desc = "Niveau du soin instantané accordé par une tête dorée.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveauTete = 2;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Potion Healing"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%pomme%", niveauPomme, "%tete%", niveauTete));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.POTION); }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive() || !isGoldenApple(item)) return;
            int niveau = isGoldenHead(item) ? niveauTete : niveauPomme;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, niveau - 1), true);
                    LangManager.get().send(HEALED, player);
                }
            }.runTaskLater(Main.get(), 1L);
        }

        private boolean isGoldenHead(ItemStack item) {
            return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().equals(t(ScenarioLang.GOLDENHEAD_ITEM_NAME));
        }
    }

    public static class RedemptionScenario extends Scenario {

        private static final int REVIVE_PRIORITY = 5000;

        private static final DynamicLang DESC = DynamicLang.of("scenario.redemption.desc",
                "§7Chacun dispose de §e%vies% vies §7: la mort te réexpédie ailleurs avec ton inventaire, la dernière t'élimine.");

        private static final DynamicLang REVIVED = DynamicLang.of("scenario.redemption.revived",
                "§b✦ §f%joueur% §7revient au combat — il lui reste §e%vies% vie(s)§7.");

        private static final DynamicLang SELF = DynamicLang.of("scenario.redemption.self",
                "§b✦ §7Tu es réexpédié sur la carte avec ton inventaire.");

        private final Map<UUID, Integer> viesRestantes = new HashMap<>();

        @Var(name = "Nombre de vies", desc = "Vies dont dispose chaque joueur avant d'être réellement éliminé.", type = VariableType.INTEGER, min = 2)
        private int vies = 2;

        @Var(name = "Délai de renvoi", desc = "Secondes d'attente avant le renvoi sur la carte.", type = VariableType.TIME, min = 1)
        private int delaiSec = 5;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Redemption"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%vies%", vies));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BEACON); }

        @Override
        public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive() || uhcPlayer == null) return;
            Player player = uhcPlayer.getPlayer();
            if (player == null || !player.isOnline()) return;

            UUID uuid = uhcPlayer.getUniqueId();
            int restantes = viesRestantes.getOrDefault(uuid, vies) - 1;
            viesRestantes.put(uuid, restantes);
            if (restantes <= 0) return;

            World world = player.getWorld();
            scheduleRevive(uhcPlayer, killer, event, delaiSec * 20L, REVIVE_PRIORITY, revived -> {
                revived.teleport(safeLocation(world));
                LangManager.get().send(SELF, revived);
                LangManager.get().sendAll(REVIVED, Map.of("%joueur%", revived.getName(), "%vies%", restantes));
            });
        }

        @Override
        public void onStop() {
            viesRestantes.clear();
        }
    }

    public static class RestorationScenario extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("scenario.restoration.desc",
                "§7Un kill consomme automatiquement tes pommes d'or jusqu'à revenir à §c%marge% ❤ §7près de ta vie maximale.");

        private static final DynamicLang RESTORED = DynamicLang.of("scenario.restoration.restored",
                "§a✔ §7%pommes% pomme(s) d'or consommée(s) automatiquement.");

        @Var(name = "Soin par pomme", desc = "Demi-cœurs rendus par chaque pomme d'or consommée automatiquement.", type = VariableType.INTEGER, min = 1)
        private int soinParPomme = 4;

        @Var(name = "Marge de vie", desc = "Demi-cœurs manquants tolérés avant d'arrêter de consommer.", type = VariableType.INTEGER, min = 0)
        private int margeDemiCoeurs = 2;

        @Var(name = "Pommes maximum", desc = "Pommes d'or consommées au maximum par kill.", type = VariableType.INTEGER, min = 1)
        private int pommesMaximum = 10;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Restoration"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%marge%", margeDemiCoeurs / 2.0));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_CARROT); }

        @Override
        public void onKill(UHCPlayer killer, UHCPlayer victim) {
            if (!isActive() || killer == null) return;
            Player player = killer.getPlayer();
            if (player == null || !player.isOnline()) return;

            double cible = player.getMaxHealth() - margeDemiCoeurs;
            int consommees = 0;
            while (consommees < pommesMaximum && player.getHealth() < cible) {
                ItemStack apple = firstGoldenApple(player);
                if (apple == null) break;
                ItemCreator.consumeOne(player, apple);
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + soinParPomme));
                consommees++;
            }
            if (consommees == 0) return;

            player.updateInventory();
            LangManager.get().send(RESTORED, player, Map.of("%pommes%", consommees));
        }

        private ItemStack firstGoldenApple(Player player) {
            for (ItemStack content : player.getInventory().getContents()) {
                if (isGoldenApple(content)) return content;
            }
            return null;
        }
    }

    public static class RottenPotionsScenario extends Scenario {

        private static final PotionEffectType[] POSITIFS = {
                PotionEffectType.SPEED,
                PotionEffectType.INCREASE_DAMAGE,
                PotionEffectType.REGENERATION,
                PotionEffectType.DAMAGE_RESISTANCE,
                PotionEffectType.FIRE_RESISTANCE,
                PotionEffectType.JUMP
        };

        private static final PotionEffectType[] NEGATIFS = {
                PotionEffectType.SLOW,
                PotionEffectType.WEAKNESS,
                PotionEffectType.POISON,
                PotionEffectType.BLINDNESS,
                PotionEffectType.CONFUSION,
                PotionEffectType.SLOW_DIGGING
        };

        private static final DynamicLang DESC = DynamicLang.of("scenario.rottenpotions.desc",
                "§7Manger de la chair putréfiée applique §e%duree%s §7d'un effet aléatoire, bénéfique dans §a%chance%% §7des cas.");

        private static final DynamicLang GOOD = DynamicLang.of("scenario.rottenpotions.good",
                "§a✔ §7La chair putréfiée t'a été bénéfique.");

        private static final DynamicLang BAD = DynamicLang.of("scenario.rottenpotions.bad",
                "§c✖ §7La chair putréfiée te retourne l'estomac.");

        @Var(name = "Durée de l'effet", desc = "Secondes pendant lesquelles l'effet tiré s'applique.", type = VariableType.TIME, min = 1)
        private int dureeSec = 10;

        @Var(name = "Chance d'effet positif", desc = "Chance que l'effet tiré soit bénéfique plutôt que néfaste.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int chancePositif = 50;

        @Var(name = "Niveau de l'effet", desc = "Niveau de l'effet appliqué par la chair putréfiée.", type = VariableType.INTEGER, min = 1, max = 5)
        private int niveau = 1;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Rotten Potions"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%duree%", dureeSec, "%chance%", chancePositif));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ROTTEN_FLESH); }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive() || item == null || item.getType() != Material.ROTTEN_FLESH) return;

            boolean positif = UHCUtils.Rng.chance(chancePositif);
            PotionEffectType[] pool = positif ? POSITIFS : NEGATIFS;
            PotionEffectType type = pool[RANDOM.nextInt(pool.length)];

            player.addPotionEffect(new PotionEffect(type, dureeSec * 20, niveau - 1), true);
            LangManager.get().send(positif ? GOOD : BAD, player);
        }
    }

    public static class SecondChanceScenario extends Scenario {

        private static final int REVIVE_PRIORITY = 4000;

        private static final DynamicLang DESC = DynamicLang.of("scenario.secondchance.desc",
                "§7Chaque joueur revient une fois après sa mort, plus rien après §e%limite% §7secondes de partie.");

        private static final DynamicLang REVIVED = DynamicLang.of("scenario.secondchance.revived",
                "§b✦ §f%joueur% §7a utilisé sa seconde chance.");

        private static final DynamicLang SELF = DynamicLang.of("scenario.secondchance.self",
                "§b✦ §7Ta seconde chance est consommée : la prochaine mort est définitive.");

        private final Set<UUID> used = new HashSet<>();

        @Var(name = "Limite de la seconde chance", desc = "Secondes de partie après lesquelles plus aucune seconde chance n'est accordée.", type = VariableType.TIME, min = 1)
        private int limiteSec = 3600;

        @Var(name = "Délai de retour", desc = "Secondes d'attente avant le retour en jeu.", type = VariableType.TIME, min = 1)
        private int delaiSec = 5;

        @Override
        public Family getFamily() { return Family.VIE; }

        @Override public String getName() { return "Second Chance"; }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%limite%", limiteSec));
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FEATHER); }

        @Override
        public void onAfterDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
            if (!isActive() || uhcPlayer == null) return;
            if (UHCManager.get().getTimer() >= limiteSec) return;

            Player player = uhcPlayer.getPlayer();
            if (player == null || !player.isOnline()) return;
            if (!used.add(uhcPlayer.getUniqueId())) return;

            scheduleRevive(uhcPlayer, killer, event, delaiSec * 20L, REVIVE_PRIORITY, revived -> {
                LangManager.get().send(SELF, revived);
                LangManager.get().sendAll(REVIVED, Map.of("%joueur%", revived.getName()));
            });
        }

        @Override
        public void onStop() {
            used.clear();
        }
    }

    public static List<Scenario> all() {
        return List.of(
                new DoubleHealthScenario(),
                new GameHealthScenario(),
                new GapZapScenario(),
                new HealthDonorScenario(),
                new LifestealScenario(),
                new NerfedAbsorptionScenario(),
                new PotentialHeartsScenario(),
                new PotionHealingScenario(),
                new RedemptionScenario(),
                new RestorationScenario(),
                new RottenPotionsScenario(),
                new SecondChanceScenario()
        );
    }
}
