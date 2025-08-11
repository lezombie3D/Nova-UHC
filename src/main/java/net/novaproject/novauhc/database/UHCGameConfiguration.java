package net.novaproject.novauhc.database;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class UHCGameConfiguration {

    private String name;
    private final List<String> enabledScenarios;
    private final int teamSize;
    private final int borderSize;
    private final int pvpTime;
    private final int finalsize;
    private final int bordecactivation;
    private final int timereduc;
    private final List<Integer> limite;
    private final int slot;
    private final int diamant;
    private final int limiteD;
    private final int protection;
    private final Map<String, ItemStack[]> stuff;

    public UHCGameConfiguration(String name, List<String> scenarios, int teamSize, int borderSize, int pvpTime, int finalsize, int bordecactivation, int timereduc, List<Integer> limite, int slot, int diamant, int limiteD, int protection, Map<String, ItemStack[]> stuff) {
        this.name = name;
        this.enabledScenarios = scenarios;
        this.teamSize = teamSize;
        this.borderSize = borderSize;
        this.pvpTime = pvpTime;
        this.finalsize = finalsize;
        this.bordecactivation = bordecactivation;
        this.timereduc = timereduc;
        this.limite = limite;
        this.slot = slot;
        this.diamant = diamant;
        this.limiteD = limiteD;
        this.protection = protection;
        this.stuff = stuff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEnabledScenarios() {
        return enabledScenarios;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public int getPvpTime() {
        return pvpTime;
    }

    public int getFinalsize() {
        return finalsize;
    }

    public int getBordecactivation() {
        return bordecactivation;
    }

    public List<Integer> getLimite() {
        return limite;
    }

    public int getSlot() {
        return slot;
    }

    public int getDiamant() {
        return diamant;
    }

    public int getLimiteD() {
        return limiteD;
    }

    public int getProtection() {
        return protection;
    }

    public Map<String, ItemStack[]> getStuff() {
        return stuff;
    }

    public List<String> getScenarios() {
        return enabledScenarios;
    }

    public int getTimereduc() {
        return timereduc;
    }
}
