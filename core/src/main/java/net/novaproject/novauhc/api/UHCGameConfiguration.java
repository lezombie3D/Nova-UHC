package net.novaproject.novauhc.api;

import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UHCGameConfiguration(String name, List<String> enabledScenarios, Map<String, Document> scenarioConfigs,
                                   Document settings, int borderSize, List<Integer> limite,
                                   Map<String, ItemStack[]> stuff, Map<String, ItemStack[]> death,
                                   Map<String, Boolean> potionStates, Map<String, Integer> dropRates) {

    public UHCGameConfiguration {
        scenarioConfigs = scenarioConfigs != null ? scenarioConfigs : new HashMap<>();
        settings = settings != null ? settings : new Document();
        stuff = stuff != null ? stuff : new HashMap<>();
        death = death != null ? death : new HashMap<>();
        potionStates = potionStates != null ? potionStates : new HashMap<>();
        dropRates = dropRates != null ? dropRates : new HashMap<>();
    }
}

