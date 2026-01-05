package net.novaproject.novauhc.database;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.Potions;
import org.bson.Document;
import org.bson.types.Binary;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UHCConfigManager {

    private static final Logger LOGGER = Logger.getLogger(UHCConfigManager.class.getName());
    private static final ReplaceOptions UPSERT_OPTION = new ReplaceOptions().upsert(true);

    private final MongoCollection<Document> uhcConfigs;

    public UHCConfigManager(MongoCollection<Document> uhcConfigs) {
        this.uhcConfigs = uhcConfigs;
    }


    private Binary serializeItemStacks(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return new Binary(outputStream.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sérialisation des ItemStacks", e);
            return null;
        }
    }

    private ItemStack[] deserializeItemStacks(Binary binary) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(binary.getData());
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la désérialisation des ItemStacks", e);
            return null;
        }
    }

    private Map<String, Boolean> serializePotionStates() {
        Map<String, Boolean> potionStates = new HashMap<>();
        try {
            for (Potions potion : Potions.values()) {
                potionStates.put(potion.name(), potion.isEnabled());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sérialisation des états de potions", e);
        }
        return potionStates;
    }

    private Map<String, Boolean> deserializePotionStates(Map<String, Boolean> potionStates) {
        try {
            // Create a safe validated copy
            Map<String, Boolean> safePotionStates = createSafePotionStatesCopy(potionStates);

            // Apply the states to the enum instances
            for (Potions potion : Potions.values()) {
                Boolean enabled = safePotionStates.get(potion.name());
                if (enabled != null && potion.isEnabled() != enabled) {
                    potion.toggleEnabled();
                }
            }

            return safePotionStates;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la désérialisation des états de potions", e);
            // Return default states on error and reset enum states
            Map<String, Boolean> defaultStates = new HashMap<>();
            try {
                resetPotionStatesToDefault();
                for (Potions potion : Potions.values()) {
                    defaultStates.put(potion.name(), true);
                }
            } catch (Exception resetError) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la réinitialisation des états de potions par défaut", resetError);
            }
            return defaultStates;
        }
    }


    public void applyPotionStatesToEnum(Map<String, Boolean> potionStates) {
        if (potionStates == null) {
            return;
        }

        try {
            for (Potions potion : Potions.values()) {
                Boolean enabled = potionStates.get(potion.name());
                if (enabled != null && potion.isEnabled() != enabled) {
                    potion.toggleEnabled();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'application des états de potions aux enums", e);
        }
    }

    public Map<String, Boolean> getCurrentPotionStates() {
        return serializePotionStates();
    }

    public void resetPotionStatesToDefault() {
        try {
            for (Potions potion : Potions.values()) {
                if (!potion.isEnabled()) {
                    potion.toggleEnabled();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la réinitialisation des états de potions", e);
        }
    }

    private Map<String, Boolean> validatePotionStates(Map<String, Boolean> potionStates) {
        Map<String, Boolean> validatedStates = new HashMap<>();

        if (potionStates == null) {
            return validatedStates;
        }

        try {
            Set<String> validPotionNames = new HashSet<>();
            for (Potions potion : Potions.values()) {
                validPotionNames.add(potion.name());
            }

            for (Map.Entry<String, Boolean> entry : potionStates.entrySet()) {
                String potionName = entry.getKey();
                Boolean enabled = entry.getValue();

                if (potionName != null && enabled != null && validPotionNames.contains(potionName)) {
                    validatedStates.put(potionName, enabled);
                } else {
                    LOGGER.log(Level.WARNING, "État de potion invalide ignoré: " + potionName + " = " + enabled);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la validation des états de potions", e);
        }

        return validatedStates;
    }


    private Map<String, Boolean> createSafePotionStatesCopy(Map<String, Boolean> potionStates) {
        Map<String, Boolean> safeCopy = new HashMap<>();

        try {
            Map<String, Boolean> validated = validatePotionStates(potionStates);
            safeCopy.putAll(validated);

            // Ensure all potion types are represented
            for (Potions potion : Potions.values()) {
                if (!safeCopy.containsKey(potion.name())) {
                    safeCopy.put(potion.name(), true); // Default to enabled
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création d'une copie sûre des états de potions", e);
            // Return default states on error
            for (Potions potion : Potions.values()) {
                safeCopy.put(potion.name(), true);
            }
        }

        return safeCopy;
    }

    public void saveConfig(UUID uuid, UHCGameConfiguration config) {
        try {
            Document existingConfig = getConfigDocument(uuid, config.getName());

            Document stuffDoc = new Document();
            for (Map.Entry<String, ItemStack[]> entry : config.getStuff().entrySet()) {
                Binary serializedItems = serializeItemStacks(entry.getValue());
                if (serializedItems != null) {
                    stuffDoc.append(entry.getKey(), serializedItems);
                }
            }

            Document deathDoc = new Document();
            for (Map.Entry<String, ItemStack[]> entry : config.getDeath().entrySet()) {
                Binary serializedItems = serializeItemStacks(entry.getValue());
                if (serializedItems != null) {
                    deathDoc.append(entry.getKey(), serializedItems);
                }
            }

            Map<String, Boolean> potionStates = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : config.getPotionStates().entrySet()) {
                if (entry.getValue() != null && !entry.getValue()) {
                    potionStates.put(entry.getKey(), false);
                }
            }

            Document configDoc = new Document()
                    .append("uuid", uuid.toString())
                    .append("name", config.getName())
                    .append("enabledScenarios", config.getEnabledScenarios());

            if (!config.getScenarioConfigs().isEmpty()) {
                configDoc.append("scenarioConfigs", config.getScenarioConfigs());
            }

            configDoc.append("teamSize", config.getTeamSize())
                    .append("borderSize", config.getBorderSize())
                    .append("pvpTime", config.getPvpTime())
                    .append("finalsize", config.getFinalsize())
                    .append("bordecactivation", config.getBordecactivation())
                    .append("timereduc", config.getTimereduc())
                    .append("limite", config.getLimite())
                    .append("slot", config.getSlot())
                    .append("diamant", config.getDiamant())
                    .append("limiteD", config.getLimiteD())
                    .append("protection", config.getProtection());

            if (!stuffDoc.isEmpty()) {
                configDoc.append("stuff", stuffDoc);
            }
            if (!deathDoc.isEmpty()) {
                configDoc.append("death", deathDoc);
            }
            if (!potionStates.isEmpty()) {
                configDoc.append("potionStates", potionStates);
            }

            if (existingConfig != null) {
                uhcConfigs.replaceOne(
                        Filters.and(
                                Filters.eq("uuid", uuid.toString()),
                                Filters.eq("name", config.getName())
                        ),
                        configDoc,
                        UPSERT_OPTION
                );
            } else {
                uhcConfigs.insertOne(configDoc);
            }

            LOGGER.info("Configuration UHC '" + config.getName() + "' sauvegardée pour " + uuid);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de la configuration UHC", e);
        }
    }

    private List<Scenario> deserializeScenarios(List<String> scenarioNames) {
        List<Scenario> scenarios = new ArrayList<>();
        for (String name : scenarioNames) {
            ScenarioManager.get().getScenarioByName(name).ifPresent(scenarios::add);
        }
        return scenarios;
    }


    public UHCGameConfiguration getConfig(UUID uuid, String configName) {
        try {
            Document configDoc = getConfigDocument(uuid, configName);

            if (configDoc == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            List<String> scenarios = (List<String>) configDoc.get("enabledScenarios");
            @SuppressWarnings("unchecked")
            List<Integer> limites = (List<Integer>) configDoc.get("limite");

            Map<String, ItemStack[]> stuff = new HashMap<>();
            Document stuffDoc = (Document) configDoc.get("stuff");
            if (stuffDoc != null) {
                for (String key : stuffDoc.keySet()) {
                    Binary binary = (Binary) stuffDoc.get(key);
                    ItemStack[] items = deserializeItemStacks(binary);
                    if (items != null) {
                        stuff.put(key, items);
                    }
                }
            }
            Map<String, ItemStack[]> death = new HashMap<>();
            Document deathDoc = (Document) configDoc.get("death");
            if (deathDoc != null) {
                for (String key : deathDoc.keySet()) {
                    Binary binary = (Binary) deathDoc.get(key);
                    ItemStack[] items = deserializeItemStacks(binary);
                    if (items != null) {
                        death.put(key, items);
                    }
                }
            }


            // Désérialiser les états des potions
            Map<String, Boolean> potionStates = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Boolean> storedPotionStates = (Map<String, Boolean>) configDoc.get("potionStates");
            potionStates = deserializePotionStates(storedPotionStates);

            // Désérialiser les configurations des scénarios
            @SuppressWarnings("unchecked")
            Map<String, Document> scenarioConfigs = (Map<String, Document>) configDoc.get("scenarioConfigs");
            if (scenarioConfigs == null) {
                scenarioConfigs = new HashMap<>();
            }

            return new UHCGameConfiguration(
                    configDoc.getString("name"),
                    scenarios,
                    scenarioConfigs,
                    configDoc.getInteger("teamSize"),
                    configDoc.getInteger("borderSize"),
                    configDoc.getInteger("pvpTime"),
                    configDoc.getInteger("finalsize"),
                    configDoc.getInteger("bordecactivation"),
                    configDoc.getInteger("timereduc"),
                    limites,
                    configDoc.getInteger("slot"),
                    configDoc.getInteger("diamant"),
                    configDoc.getInteger("limiteD"),
                    configDoc.getInteger("protection"),
                    stuff,
                    death,
                    potionStates
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la configuration UHC", e);
            return null;
        }
    }

    private Document getConfigDocument(UUID uuid, String configName) {
        try {
            return uhcConfigs.find(
                    Filters.and(
                            Filters.eq("uuid", uuid.toString()),
                            Filters.eq("name", configName)
                    )
            ).first();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du document de configuration", e);
            return null;
        }
    }

    public boolean deleteConfig(UUID uuid, String configName) {
        try {
            return uhcConfigs.deleteOne(
                    Filters.and(
                            Filters.eq("uuid", uuid.toString()),
                            Filters.eq("name", configName)
                    )
            ).getDeletedCount() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la configuration UHC", e);
            return false;
        }
    }

    public List<String> getPlayerConfigNames(UUID uuid) {
        List<String> configNames = new ArrayList<>();

        try {
            uhcConfigs.find(Filters.eq("uuid", uuid.toString())).forEach((Block<? super Document>) doc -> {
                configNames.add(doc.getString("name"));
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des noms de configuration", e);
        }

        return configNames;
    }
}