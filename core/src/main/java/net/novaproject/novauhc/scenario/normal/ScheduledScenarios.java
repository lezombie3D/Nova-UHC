package net.novaproject.novauhc.scenario.normal;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class ScheduledScenarios {

    public static class TpMeetup extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        private static final Random RANDOM = new Random();

        @Var(lang = ScenarioVarLang.class, nameKey = "TPMEETUP_VAR_TIMER_TP_NAME", descKey = "TPMEETUP_VAR_TIMER_TP_DESC", type = VariableType.TIME)
        private int timerTP = 3600;

        @Var(lang = ScenarioVarLang.class, nameKey = "TPMEETUP_VAR_RADIUS_NAME", descKey = "TPMEETUP_VAR_RADIUS_DESC", type = VariableType.INTEGER)
        private int radius = 250;

        private boolean tpDone = false;

        @Override public String getName() { return "TP Meetup"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.TPMEETUP, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_PEARL); }

        @Override
        public void onGameStart() {
            tpDone = false;
        }

        @Override
        public void onSec(Player p) {
            if (!isActive() || tpDone) return;
            if (UHCManager.get().getTimer() < timerTP) return;

            tpDone = true;
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                int x = RANDOM.nextInt(radius * 2 + 1) - radius;
                int z = RANDOM.nextInt(radius * 2 + 1) - radius;
                int y = Common.get().getArena().getHighestBlockYAt(x, z);
                Location loc = new Location(Common.get().getArena(), x + 0.5, y + 1, z + 0.5);
                uhcPlayer.getPlayer().teleport(loc);
                uhcPlayer.getPlayer().setNoDamageTicks(100);
            }
            Bukkit.broadcastMessage(LangManager.get().get(BROADCAST));
        }

        private static final DynamicLang BROADCAST = DynamicLang.of("scenario.tpmeetup.broadcast",
                "§a● §7Tous les joueurs ont été téléportés vers le centre !");
    }

    public static class FinalHeal extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Getter @Setter
        @Var(lang = ScenarioVarLang.class, nameKey = "FINALHEAL_VAR_HEAL_TIME1_NAME", descKey = "FINALHEAL_VAR_HEAL_TIME1_DESC", type = VariableType.TIME)
        private int healTime1 = 600;

        @Getter @Setter
        @Var(lang = ScenarioVarLang.class, nameKey = "FINALHEAL_VAR_HEAL_TIME2_NAME", descKey = "FINALHEAL_VAR_HEAL_TIME2_DESC", type = VariableType.TIME)
        private int healTime2 = 1200;

        @Getter @Setter
        @Var(lang = ScenarioVarLang.class, nameKey = "FINALHEAL_VAR_HEAL_FOOD_NAME", descKey = "FINALHEAL_VAR_HEAL_FOOD_DESC", type = VariableType.BOOLEAN)
        private boolean healFood = true;

        private boolean healed1 = false;
        private boolean healed2 = false;

        @Override public String getName() { return "FinalHeal"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.FINAL_HEAL, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.POTION).setDurability((short) 8229).addItemFlags(ItemFlag.HIDE_POTION_EFFECTS); }

        @Override
        public void onGameStart() {
            healed1 = false;
            healed2 = false;
        }

        @Override
        public void onSec(Player p) {
            if (!isActive()) return;
            int timer = UHCManager.get().getTimer();

            if (!healed1 && timer >= healTime1) {
                healed1 = true;
                healAll();
            } else if (!healed2 && timer >= healTime2) {
                healed2 = true;
                healAll();
            }
        }

        private void healAll() {
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                Player player = uhcPlayer.getPlayer();
                player.setHealth(player.getMaxHealth());
                if (healFood) {
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                }
            }
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_FINAL_HEAL_BROADCAST));
        }
    }

    public static class XpSansue extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Var(lang = ScenarioVarLang.class, nameKey = "XPSANSUE_VAR_DAMAGE_NAME", descKey = "XPSANSUE_VAR_DAMAGE_DESC", type = VariableType.INTEGER)
        private int damage = 1;

        @Var(name = "Intervalle", desc = "Secondes entre deux ponctions d'expérience.", type = VariableType.TIME, min = 1)
        private int intervalSeconds = 600;

        @Var(name = "Dégâts sans niveau", desc = "Dégâts subis quand le joueur n'a plus de niveau à perdre.", type = VariableType.DOUBLE, min = 0)
        private double noLevelDamage = 2.0;

        private BukkitRunnable task;

        @Override public String getName() { return "XpSansue"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.XP_SANSUE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.EXP_BOTTLE); }

        @Override
        public void onGameStart() {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) { cancel(); return; }
                    for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        if (p.getPlayer().getLevel() > 0) {
                            p.getPlayer().setLevel(Math.max(0, p.getPlayer().getLevel() - damage));
                        } else {
                            p.getPlayer().damage(noLevelDamage);
                        }
                    }
                }
            };
            long period = Math.max(1L, intervalSeconds) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public static class HeathCharity extends Scenario {

        @Override
        public Family getFamily() { return Family.TIMERS; }

        @Var(name = "Intervalle", desc = "Secondes entre deux soins du joueur le plus faible.", type = VariableType.TIME, min = 1)
        private int intervalSeconds = 600;

        private BukkitRunnable task;

        @Override public String getName() { return "Heath Charity"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.HEATH_CHARITY, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.RED_ROSE); }

        @Override
        public void onGameStart() {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) {
                        cancel();
                        return;
                    }

                    Player lowestHealthPlayer = null;
                    double lowestHealth = Double.MAX_VALUE;

                    for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                        double health = uhcPlayer.getPlayer().getHealth();
                        if (health < lowestHealth) {
                            lowestHealth = health;
                            lowestHealthPlayer = uhcPlayer.getPlayer();
                        }
                    }
                    if (lowestHealthPlayer != null) {
                        lowestHealthPlayer.setHealth(lowestHealthPlayer.getMaxHealth());
                    }
                }
            };
            long period = Math.max(1L, intervalSeconds) * 20L;
            task.runTaskTimer(Main.get(), period, period);
        }

        @Override
        public void onStop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public static class FastFurnace extends Scenario {

        @Override
        public Family getFamily() { return Family.CRAFT; }

        private final Set<Location> activeFurnaces = new HashSet<>();
        private final Set<BukkitRunnable> tasks = new HashSet<>();

        @Var(name = "Ticks de cuisson bonus", desc = "Progression de cuisson ajoutée par tick (vanilla = 1).", type = VariableType.INTEGER, min = 0)
        private int cookBoost = 9;

        @Override public String getName() { return "FastFurnace"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.FAST_FURNACE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FURNACE); }

        @Override
        public void onFurnaceBurn(FurnaceBurnEvent event) {
            if (!isActive()) return;
            Location loc = event.getBlock().getLocation();
            if (activeFurnaces.contains(loc)) return;
            activeFurnaces.add(loc);
            startUpdate(loc);
        }

        private void startUpdate(final Location loc) {
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    if (!(loc.getBlock().getState() instanceof Furnace furnace)
                            || !isActive()
                            || (furnace.getCookTime() <= 0 && furnace.getBurnTime() <= 0)) {
                        activeFurnaces.remove(loc);
                        tasks.remove(this);
                        cancel();
                        return;
                    }
                    furnace.setCookTime((short) (furnace.getCookTime() + cookBoost));
                    furnace.update();
                }
            };
            tasks.add(task);
            task.runTaskTimer(Main.get(), 1L, 1L);
        }

        @Override
        public void onStop() {
            for (BukkitRunnable task : tasks) task.cancel();
            tasks.clear();
            activeFurnaces.clear();
        }
    }
}
