package net.novaproject.novauhc.database;

import lombok.Getter;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Getter
public class UHCGameConfiguration {

    private String name;
    private final List<String> enabledScenarios;
    private final Map<String, Document> scenarioConfigs;
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
    private final Map<String, ItemStack[]> death;
    private final Map<String, Boolean> potionStates;

    public UHCGameConfiguration(String name, List<String> scenarios, Map<String, Document> scenarioConfigs, int teamSize, int borderSize, int pvpTime, int finalsize, int bordecactivation, int timereduc, List<Integer> limite, int slot, int diamant, int limiteD, int protection, Map<String, ItemStack[]> stuff,Map<String, ItemStack[]> death, Map<String, Boolean> potionStates) {
        this.name = name;
        this.enabledScenarios = scenarios;
        this.scenarioConfigs = scenarioConfigs != null ? scenarioConfigs : new HashMap<>();
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
        this.death = death;
        this.potionStates = potionStates != null ? potionStates : new HashMap<>();
    }


}
