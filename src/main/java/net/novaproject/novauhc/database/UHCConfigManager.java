package net.novaproject.novauhc.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
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

    public void saveConfig(UUID uuid, UHCGameConfiguration config) {
        try {
            Document existingConfig = getConfigDocument(uuid, config.getName());

            // Sérialiser la Map de stuff (ItemStack[])
            Document stuffDoc = new Document();
            for (Map.Entry<String, ItemStack[]> entry : config.getStuff().entrySet()) {
                Binary serializedItems = serializeItemStacks(entry.getValue());
                if (serializedItems != null) {
                    stuffDoc.append(entry.getKey(), serializedItems);
                }
            }

            Document configDoc = new Document()
                    .append("uuid", uuid.toString())
                    .append("name", config.getName())
                    .append("enabledScenarios", config.getEnabledScenarios())
                    .append("teamSize", config.getTeamSize())
                    .append("borderSize", config.getBorderSize())
                    .append("pvpTime", config.getPvpTime())
                    .append("finalsize", config.getFinalsize())
                    .append("bordecactivation", config.getBordecactivation())
                    .append("timereduc", config.getTimereduc())
                    .append("limite", config.getLimite())
                    .append("slot", config.getSlot())
                    .append("diamant", config.getDiamant())
                    .append("limiteD", config.getLimiteD())
                    .append("protection", config.getProtection())
                    .append("stuff", stuffDoc);

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

            return new UHCGameConfiguration(
                    configDoc.getString("name"),
                    scenarios,
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
                    stuff
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
            uhcConfigs.find(Filters.eq("uuid", uuid.toString())).forEach(doc -> {
                configNames.add(doc.getString("name"));
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des noms de configuration", e);
        }

        return configNames;
    }
}