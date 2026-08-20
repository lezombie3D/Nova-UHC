package net.novaproject.ultimate.superheros;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;

public class SuperHeros extends Scenario {

    private final HashMap<Player, Integer> superHeros = new HashMap<>();

    @Var(name = "Speed", desc = "Enable speed potion.", type = VariableType.BOOLEAN)
    private boolean enableSpeed = true;

    @Var(name = "Strength", desc = "Enable strength potion.", type = VariableType.BOOLEAN)
    private boolean enableDamage = true;

    @Var(name = "Resistance", desc = "Enable resistance potion.", type = VariableType.BOOLEAN)
    private boolean enableResistance = true;

    @Var(name = "Jump", desc = "Enable jump potion.", type = VariableType.BOOLEAN)
    private boolean enableJump = true;

    @Var(name = "Extra health", desc = "Adds extra max health for the special hero.", type = VariableType.BOOLEAN)
    private boolean enableExtraHealth = true;

    @Var(name = "Speed amplifier", desc = "Defines the power of the speed potion.", type = VariableType.INTEGER)
    private int speedAmplifier = 1;

    @Var(name = "Strength amplifier", desc = "Defines the power of the strength potion.", type = VariableType.INTEGER)
    private int damageAmplifier = 0;

    @Var(name = "Resistance amplifier", desc = "Defines the power of the resistance potion.", type = VariableType.INTEGER)
    private int resistanceAmplifier = 1;

    @Var(name = "Jump amplifier", desc = "Defines the power of the jump potion.", type = VariableType.INTEGER)
    private int jumpAmplifier = 3;

    @Var(name = "Fire resistance amplifier", desc = "Defines the power of the fire resistance potion.", type = VariableType.INTEGER)
    private int fireResistanceAmplifier = 1;

    @Var(name = "Extra Absorption", desc = "Extra absorption amount on golden apple.", type = VariableType.INTEGER)
    private int absorptionExtraHealth = 1;

    @Var(name = "Extra Regeneration", desc = "Extra regeneration amount on golden apple.", type = VariableType.INTEGER)
    private int regenerationExtraHealth = 1;

    @Var(name = "Superheros Var Regeneration Duration", type = VariableType.INTEGER)
    private int regenerationDurationExtraHeath = 1;

    @Var(name = "Absorption duration", desc = "Duration of absorption in seconds.", type = VariableType.INTEGER)
    private int absorptionDurationExtraHealth = 1;

    @Override
    public String getName() {
        return "SuperHeros";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.SUPER_HEROS, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }
    }

    @Override
    public void onStart(Player player) {
        superHeros.clear();
        Random ran = new Random();
        int aleatoire = ran.nextInt(5);
        superHeros.put(player, aleatoire);

        if (aleatoire == 4 && enableExtraHealth) {
            player.setMaxHealth(40);
            player.setHealth(player.getMaxHealth());
        }
    }

    @Override
    public void onSec(Player player) {
        if (!superHeros.containsKey(player)) return;

        int type = superHeros.get(player);

        int speedLvl = Math.max(0, speedAmplifier);
        int damageLvl = Math.max(0, damageAmplifier);
        int resistanceLvl = Math.max(0, resistanceAmplifier);
        int jumpLvl = Math.max(0, jumpAmplifier);
        int fireResistLvl = Math.max(0, fireResistanceAmplifier);

        PotionEffect[] effects = switch (type) {
            case 0 -> enableSpeed && speedLvl > 0
                    ? new PotionEffect[]{new PotionEffect(PotionEffectType.SPEED, 80, speedLvl, false, false)}
                    : new PotionEffect[]{};
            case 1 -> enableDamage && damageLvl > 0
                    ? new PotionEffect[]{new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, damageLvl, false, false)}
                    : new PotionEffect[]{};
            case 2 -> enableResistance && resistanceLvl > 0
                    ? new PotionEffect[]{new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, resistanceLvl, false, false)}
                    : new PotionEffect[]{};
            case 3 -> enableJump && jumpLvl > 0
                    ? new PotionEffect[]{
                    new PotionEffect(PotionEffectType.JUMP, 80, jumpLvl, false, false),
                    new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, fireResistLvl, false, false)
            } : new PotionEffect[]{};
            default -> new PotionEffect[]{};
        };

        if (effects.length > 0) {
            UHCUtils.applyInfiniteEffects(effects, player);
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!(entity instanceof Player player)) return;
        if (!superHeros.containsKey(player)) return;

        int type = superHeros.get(player);
        if (type == 3 && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if(item.getType() != Material.GOLDEN_APPLE ) return;
        if(!(superHeros.get(player) == 4)) return;
        if(ScenarioManager.get().getScenario(GoldenHead.class).isActive()){
            for (PotionEffect e : player.getActivePotionEffects()) {
                if (e.getType().equals(PotionEffectType.REGENERATION) || e.getType().equals(PotionEffectType.ABSORPTION))
                    player.removePotionEffect(e.getType());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*absorptionDurationExtraHealth*2, absorptionExtraHealth*2, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20* regenerationDurationExtraHeath*2, regenerationExtraHealth*2, false, true));
            return;
        }
        for (PotionEffect e : player.getActivePotionEffects()) {
            if (e.getType().equals(PotionEffectType.REGENERATION) || e.getType().equals(PotionEffectType.ABSORPTION))
                player.removePotionEffect(e.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*absorptionDurationExtraHealth, absorptionExtraHealth, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20* regenerationDurationExtraHeath, regenerationExtraHealth, false, true));
    }
}

