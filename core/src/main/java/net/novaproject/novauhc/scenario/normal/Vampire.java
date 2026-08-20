package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.world.ArenaMirrorManager;import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class Vampire extends Scenario {

    @Override
    public Family getFamily() { return Family.VIE; }

    private BukkitRunnable sunDamageTask;
    @Var(lang = ScenarioVarLang.class, nameKey = "VAMPIRE_VAR_HEAL_AMOUNT_NAME", descKey = "VAMPIRE_VAR_HEAL_AMOUNT_DESC", type = VariableType.DOUBLE)
    private double healAmount = 2.0;
    @Var(lang = ScenarioVarLang.class, nameKey = "VAMPIRE_VAR_INTERVAL_NAME", descKey = "VAMPIRE_VAR_INTERVAL_DESC", type = VariableType.INTEGER)
    private int interval = 40;
    @Var(lang = ScenarioVarLang.class, nameKey = "VAMPIRE_VAR_SUN_DAMAGE_NAME", descKey = "VAMPIRE_VAR_SUN_DAMAGE_DESC", type = VariableType.DOUBLE)
    private double sunDamage = 2.0;
    @Override
    public String getName() {
        return "Vampire";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.VAMPIRE, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.REDSTONE);
    }

    @Override
    public void onGameStart() {
        ArenaMirrorManager.get().propagateTime(12000);
        startSunDamageTask();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            Player killerPlayer = killer.getPlayer();

            double currentHealth = killerPlayer.getHealth();
            double maxHealth = killerPlayer.getMaxHealth();
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);

            killerPlayer.setHealth(newHealth);

            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%victim%", uhcPlayer.getPlayer().getName());
            placeholders.put("%heal_hearts%", String.valueOf(healAmount / 2));
            LangManager.get().send(ScenarioLang.VAMPIRE_KILL_HEAL, killerPlayer, placeholders);

        }
    }

    private void startSunDamageTask() {
        if (sunDamageTask != null) {
            sunDamageTask.cancel();
        }

        sunDamageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player player = uhcPlayer.getPlayer();
                    World world = player.getWorld();

                    long time = world.getTime();
                    boolean isDay = time >= 0 && time < 12000;

                    if (isDay) {
                        if (world.getHighestBlockYAt(player.getLocation()) <= player.getLocation().getBlockY()) {
                            if (player.getInventory().getHelmet() == null ||
                                    player.getInventory().getHelmet().getType() == Material.AIR) {

                                player.damage(sunDamage);
                                Map<String, Object> sunPlaceholders = new HashMap<>();
                                sunPlaceholders.put("%sun_damage%", String.valueOf(sunDamage));
                                LangManager.get().send(ScenarioLang.VAMPIRE_SUN_DAMAGE, uhcPlayer.getPlayer(), sunPlaceholders);

                                player.setFireTicks(40);
                            }
                        }
                    }
                }
            }
        };

        sunDamageTask.runTaskTimer(Main.get(), 0, interval);
    }

    @Override
    public void onStop() {
        if (sunDamageTask != null) {
            sunDamageTask.cancel();
            sunDamageTask = null;
        }
    }
}
