package net.novaproject.novauhc.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.novaproject.novauhc.ui.config.Potions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ApiConfigManager.class.getName());
    private static final Gson GSON = new Gson();
    private static final char DOT_SUBSTITUTE = '．';
    private static final char DOLLAR_SUBSTITUTE = '＄';

    private final ApiManager api;

    public ApiConfigManager(ApiManager api) {
        this.api = api;
    }

    public CompletableFuture<Boolean> saveConfig(UUID uuid, UHCGameConfiguration config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject configJson = configToJson(config);
                api.saveConfig(uuid.toString(), config.name(), configJson).join();
                LOGGER.info("✅ Configuration '" + config.name() + "' sauvegardée via API");
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "❌ Sauvegarde de la configuration '" + config.name()
                        + "' refusée par l'API : " + ApiManager.rootMessage(e), e);
                return false;
            }
        });
    }

    public CompletableFuture<UHCGameConfiguration> getConfig(UUID uuid, String configName) {
        return api.getConfig(uuid.toString(), configName).thenApply(response -> {
            try {
                if (!response.has("success") || !response.get("success").getAsBoolean()) {
                    LOGGER.warning("⚠️ Config '" + configName + "' non trouvée dans l'API");
                    return null;
                }
                JsonObject data = response.getAsJsonObject("data");
                String name = data.get("name").getAsString();
                JsonObject configData = data.getAsJsonObject("config");
                UHCGameConfiguration config = jsonToConfig(name, configData);
                LOGGER.info("✅ Configuration '" + name + "' chargée depuis l'API");
                return config;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "❌ Erreur chargement config '" + configName + "': " + e.getMessage(), e);
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
                return null;
            }
        }).exceptionally(ex -> {
            LOGGER.log(Level.SEVERE, "❌ Erreur API getConfig: " + ex.getMessage(), ex);
            return null;
        });
    }

    public CompletableFuture<List<String>> getPlayerConfigNames(UUID uuid) {
        return api.listConfigs(uuid.toString()).thenApply(response -> {
            List<String> names = new ArrayList<>();
            try {
                JsonArray configs = response.getAsJsonObject("data").getAsJsonArray("configs");
                for (JsonElement elem : configs) {
                    names.add(elem.getAsJsonObject().get("name").getAsString());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur liste configs: " + e.getMessage(), e);
            }
            return names;
        });
    }

    public CompletableFuture<Boolean> deleteConfig(UUID uuid, String configName) {
        return api.deleteConfig(uuid.toString(), configName).thenApply(response -> {
            try {
                boolean success = response.get("success").getAsBoolean();
                if (success) {
                    LOGGER.info("✅ Configuration '" + configName + "' supprimée");
                } else {
                    LOGGER.warning("⚠️ Configuration '" + configName + "' non trouvée");
                }
                return success;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur suppression config: " + e.getMessage(), e);
                return false;
            }
        });
    }

    public JsonObject configToJson(UHCGameConfiguration config) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 2);
        json.add("settings", GSON.fromJson(config.settings().toJson(), JsonObject.class));
        json.addProperty("borderSize", config.borderSize());
        json.add("enabledScenarios", GSON.toJsonTree(config.enabledScenarios()));
        json.add("limites", GSON.toJsonTree(config.limite()));
        if (config.potionStates() != null) {
            json.add("potionStates", GSON.toJsonTree(config.potionStates()));
        }
        if (config.dropRates() != null && !config.dropRates().isEmpty()) {
            json.add("dropRates", GSON.toJsonTree(config.dropRates()));
        }
        if (config.scenarioConfigs() != null && !config.scenarioConfigs().isEmpty()) {
            json.add("scenarioConfigs", documentMapToJson(config.scenarioConfigs()));
        }
        if (config.stuff() != null && !config.stuff().isEmpty()) {
            json.add("stuff", itemStackMapToJson(config.stuff()));
        }
        if (config.death() != null && !config.death().isEmpty()) {
            json.add("death", itemStackMapToJson(config.death()));
        }
        return json;
    }

    private UHCGameConfiguration jsonToConfig(String name, JsonObject json) {
        try {
            LOGGER.info("=== Début conversion JSON → Config pour '" + name + "' ===");

            if (!json.has("settings")) {
                LOGGER.warning("⚠️ Config '" + name + "' sans bloc 'settings' — format non supporté (legacy supprimé), configuration ignorée");
                return null;
            }

            List<String> scenarios = new ArrayList<>();
            if (json.has("enabledScenarios")) {
                LOGGER.info("Chargement enabledScenarios...");
                JsonArray arr = json.getAsJsonArray("enabledScenarios");
                for (JsonElement e : arr) scenarios.add(e.getAsString());
                LOGGER.info("✓ " + scenarios.size() + " scénarios chargés");
            }
            List<Integer> limites = new ArrayList<>();
            if (json.has("limites")) {
                LOGGER.info("Chargement limites...");
                JsonArray arr = json.getAsJsonArray("limites");
                for (JsonElement e : arr) limites.add(e.getAsInt());
                LOGGER.info("✓ " + limites.size() + " limites chargées");
            }
            Map<String, Document> scenarioConfigs = new HashMap<>();
            if (json.has("scenarioConfigs")) {
                LOGGER.info("Chargement scenarioConfigs...");
                try {
                    scenarioConfigs = jsonToDocumentMap(json.getAsJsonObject("scenarioConfigs"));
                    LOGGER.info("✓ " + scenarioConfigs.size() + " configs scénario chargées");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "❌ Erreur chargement scenarioConfigs", e);
                }
            }
            Map<String, Boolean> potionStates = new HashMap<>();
            if (json.has("potionStates")) {
                LOGGER.info("Chargement potionStates...");
                try {
                    JsonObject potions = json.getAsJsonObject("potionStates");
                    for (Map.Entry<String, JsonElement> e : potions.entrySet()) {
                        potionStates.put(e.getKey(), e.getValue().getAsBoolean());
                    }
                    LOGGER.info("✓ " + potionStates.size() + " états de potion chargés");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "❌ Erreur chargement potionStates", e);
                }
            }
            Map<String, ItemStack[]> stuff = new HashMap<>();
            if (json.has("stuff")) {
                LOGGER.info("Chargement stuff...");
                try {
                    stuff = jsonToItemStackMap(json.getAsJsonObject("stuff"));
                    LOGGER.info("✓ " + stuff.size() + " stuff chargés");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "❌ Erreur chargement stuff", e);
                }
            }
            Map<String, ItemStack[]> death = new HashMap<>();
            if (json.has("death")) {
                LOGGER.info("Chargement death...");
                try {
                    death = jsonToItemStackMap(json.getAsJsonObject("death"));
                    LOGGER.info("✓ " + death.size() + " death chargés");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "❌ Erreur chargement death", e);
                }
            }
            Document settings = Document.parse(json.getAsJsonObject("settings").toString());
            Map<String, Integer> dropRates = new HashMap<>();
            if (json.has("dropRates")) {
                for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("dropRates").entrySet()) {
                    dropRates.put(e.getKey(), e.getValue().getAsInt());
                }
            }
            UHCGameConfiguration config = new UHCGameConfiguration(
                    name, scenarios, scenarioConfigs, settings,
                    safeGetInt(json, "borderSize", 1000),
                    limites, stuff, death, potionStates, dropRates
            );
            LOGGER.info("✅ Configuration '" + name + "' convertie avec succès");
            return config;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ ERREUR FATALE lors de la conversion de '" + name + "'", e);
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            return null;
        }
    }

    public JsonObject documentMapToJson(Map<String, Document> map) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Document> entry : map.entrySet()) {
            String docJson = entry.getValue().toJson();
            json.add(escapeMongoKey(entry.getKey()), mapKeys(new JsonParser().parse(docJson), true));
        }
        return json;
    }

    private Map<String, Document> jsonToDocumentMap(JsonObject json) {
        Map<String, Document> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                Document doc = Document.parse(mapKeys(entry.getValue(), false).toString());
                map.put(unescapeMongoKey(entry.getKey()), doc);
            }
        }
        return map;
    }

    private static JsonElement mapKeys(JsonElement element, boolean escape) {
        if (element.isJsonObject()) {
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                String key = escape ? escapeMongoKey(entry.getKey()) : unescapeMongoKey(entry.getKey());
                out.add(key, mapKeys(entry.getValue(), escape));
            }
            return out;
        }
        if (element.isJsonArray()) {
            JsonArray out = new JsonArray();
            for (JsonElement item : element.getAsJsonArray()) {
                out.add(mapKeys(item, escape));
            }
            return out;
        }
        return element;
    }

    private static String escapeMongoKey(String key) {
        return key.replace('.', DOT_SUBSTITUTE).replace('$', DOLLAR_SUBSTITUTE);
    }

    private static String unescapeMongoKey(String key) {
        return key.replace(DOT_SUBSTITUTE, '.').replace(DOLLAR_SUBSTITUTE, '$');
    }

    private JsonObject itemStackMapToJson(Map<String, ItemStack[]> map) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, ItemStack[]> entry : map.entrySet()) {
            String base64 = serializeItemStacksToBase64(entry.getValue());
            json.addProperty(entry.getKey(), base64);
        }
        return json;
    }

    private Map<String, ItemStack[]> jsonToItemStackMap(JsonObject json) {
        Map<String, ItemStack[]> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                String base64 = entry.getValue().getAsString();
                ItemStack[] items = deserializeItemStacksFromBase64(base64);
                if (items != null) {
                    map.put(entry.getKey(), items);
                }
            }
        }
        return map;
    }

    private String serializeItemStacksToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur sérialisation ItemStacks", e);
            return null;
        }
    }

    private ItemStack[] deserializeItemStacksFromBase64(String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return items;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur désérialisation ItemStacks", e);
            return null;
        }
    }

    private int safeGetInt(JsonObject json, String key, int defaultValue) {
        try {
            return json.has(key) ? json.get(key).getAsInt() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void applyPotionStatesToEnum(Map<String, Boolean> potionStates) {
        if (potionStates == null) return;
        try {
            for (Potions potion : Potions.values()) {
                Boolean enabled = potionStates.get(potion.name());
                if (enabled != null && potion.isEnabled() != enabled) {
                    potion.toggleEnabled();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur application états potions", e);
        }
    }

    public Map<String, Boolean> getCurrentPotionStates() {
        Map<String, Boolean> states = new HashMap<>();
        for (Potions potion : Potions.values()) {
            states.put(potion.name(), potion.isEnabled());
        }
        return states;
    }
}