package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.dragon.Fatalis;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.dragon.WhiteFatalis;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;

public class DragonFall extends ScenarioRole<DragonRole> {
    @Override
    public String getName() {
        return "Monster Hunter : DragonRole Fall";
    }

    @Override
    public String getDescription() {
        return "Ce sous-scenrio apartien a la serie Monster Hunter UHC";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(UHCUtils.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFlZjVhNDRkZjQ0MmI3OGY1YTY4OTA1Y2U0ZDJjMjRmZTkyYmE4ZjE4ZWYwMWUwMzE4YjA1Zjg1MjJkMGNkMCJ9fX0="));
    }

    @Override
    public Camps[] getCamps() {
        return Tiers.values();
    }

    @Override
    public void setup() {
        super.setup();
        addRole(WhiteFatalis.class);
        addRole(Fatalis.class);
    }

    @Override
    public void onKill(UHCPlayer killer, UHCPlayer victim) {
        if (getRoleByUHCPlayer(killer).getEvolution() != null) {

        }
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
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!(entity instanceof Player) || !(damager instanceof Player)) return;

        // Victime = entity, Attaquant = damager
        Player victim = (Player) entity;
        Player attacker = (Player) damager;

        UHCPlayer uhcVictim = UHCPlayerManager.get().getPlayer(victim);
        UHCPlayer uhcAttacker = UHCPlayerManager.get().getPlayer(attacker);

        if (uhcVictim == null || uhcAttacker == null) return;

        event.setDamage(0.0);

        DragonRole dragonVictim = getRoleByUHCPlayer(uhcVictim);
        DragonRole dragonAttacker = getRoleByUHCPlayer(uhcAttacker);

        if (dragonVictim == null || dragonAttacker == null) return;

        int currentHP = dragonVictim.getCurrentHP();
        if (currentHP <= 0) {
            event.setDamage(Integer.MAX_VALUE);
            return;
        }

        double attackerForce = dragonAttacker.getCurrentStrength();
        double defenderResistance = dragonVictim.getResistance();
        double critPercent = dragonAttacker.getCritDamage();
        double critChance = dragonAttacker.getCritChance();

        double rawAttack = Math.pow(attackerForce / 100.0, 1.3) * 45;
        double defenseReduction = defenderResistance / (defenderResistance + 1200.0);

        long baseDamage = Math.round(rawAttack * (1 - defenseReduction));
        double variation = 1 + ((Math.random() * 0.10) - 0.05);
        baseDamage = Math.round(baseDamage * variation);


        int weaknessCount = 0;
        int immunityCount = 0;
        for (Element element : dragonVictim.getWeakness()) {
            if (dragonAttacker.getElement().contains(element)) {
                weaknessCount++;
            }
        }
        for (Element element : dragonVictim.getImmunity()) {
            if (dragonAttacker.getElement().contains(element)) {
                immunityCount++;
            }
        }

        baseDamage = Math.round(baseDamage * (1 - 0.02 * weaknessCount));
        baseDamage = Math.round(baseDamage * (1 + 0.02 * immunityCount));
        String display = "§c-" + baseDamage;
        if ((int) (Math.random() * 100) < critChance) {
            baseDamage = Math.round(baseDamage * (1 + critPercent / 100.0));
            display = "§c-✦" + baseDamage + "✦";
        }
        dragonVictim.setCurrentHP(currentHP - (int) baseDamage);

        UHCUtils.spawnFloatingDamage(attacker, display);
        UHCUtils.setRealHealth(dragonVictim.getMaxHP(), currentHP, victim, dragonVictim.getAbsortion());
    }


    @Override
    public void onDamage(Player player, EntityDamageEvent event) {

    }

}
