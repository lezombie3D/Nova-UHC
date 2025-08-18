package net.novaproject.novauhc.scenario.special.legend.core;

import net.novaproject.novauhc.scenario.special.legend.legends.*;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry centralisé pour toutes les légendes UHC.
 * Utilise le pattern Registry pour gérer l'enregistrement et la récupération des légendes.
 *
 * @author NovaProject
 * @version 2.0
 */
public class LegendRegistry {

    private static LegendRegistry instance;

    // Maps thread-safe pour stocker les légendes
    private final Map<Integer, LegendClass> legendsById = new ConcurrentHashMap<>();
    private final Map<String, LegendClass> legendsByName = new ConcurrentHashMap<>();
    private final List<LegendClass> orderedLegends = new ArrayList<>();

    private LegendRegistry() {
        // Constructeur privé pour le singleton
    }

    /**
     * Obtient l'instance unique du registry
     */
    public static LegendRegistry getInstance() {
        if (instance == null) {
            instance = new LegendRegistry();
        }
        return instance;
    }

    /**
     * Initialise le registry avec toutes les légendes par défaut
     */
    public void initialize() {
        Bukkit.getLogger().info("[Legend] Initialisation du registry des légendes...");

        // Enregistrer toutes les légendes par défaut
        registerDefaultLegends();

        Bukkit.getLogger().info("[Legend] " + legendsById.size() + " légendes enregistrées avec succès !");
    }

    /**
     * Enregistre toutes les légendes par défaut
     */
    private void registerDefaultLegends() {
        // Enregistrer les légendes existantes refactorisées
        register(new Marionnettiste());
        register(new Mage());
        register(new Archer());
        register(new Assassin());
        register(new Tank());
        register(new Nain());
        register(new Zeus());
        register(new Necromancien());
        register(new Succube());
        register(new Soldat());
        register(new Princesse());
        register(new Cavalier());
        register(new Ogre());
        register(new Dragon());
        register(new Medecin());
        register(new Prisonnier());
        register(new Corne());

        // Exemple de nouvelle légende
        register(new Paladin());
    }

    /**
     * Enregistre une nouvelle légende dans le registry
     *
     * @param legend La légende à enregistrer
     * @throws IllegalArgumentException Si l'ID ou le nom est déjà utilisé
     */
    public void register(LegendClass legend) {
        Objects.requireNonNull(legend, "La légende ne peut pas être null");

        int id = legend.getId();
        String name = legend.getName();

        // Vérifier les conflits
        if (legendsById.containsKey(id)) {
            throw new IllegalArgumentException("Une légende avec l'ID " + id + " existe déjà : " + legendsById.get(id).getName());
        }

        if (legendsByName.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Une légende avec le nom '" + name + "' existe déjà");
        }

        // Enregistrer la légende
        legendsById.put(id, legend);
        legendsByName.put(name.toLowerCase(), legend);
        orderedLegends.add(legend);

        Bukkit.getLogger().info("[Legend] Légende enregistrée : " + legend.getName() + " (ID: " + id + ")");
    }

    /**
     * Récupère une légende par son ID
     *
     * @param id L'ID de la légende
     * @return La légende ou null si non trouvée
     */
    public LegendClass getLegend(int id) {
        return legendsById.get(id);
    }

    /**
     * Récupère une légende par son nom (insensible à la casse)
     *
     * @param name Le nom de la légende
     * @return La légende ou null si non trouvée
     */
    public LegendClass getLegend(String name) {
        return legendsByName.get(name.toLowerCase());
    }

    /**
     * Vérifie si une légende existe avec cet ID
     *
     * @param id L'ID à vérifier
     * @return true si la légende existe
     */
    public boolean exists(int id) {
        return legendsById.containsKey(id);
    }

    /**
     * Récupère toutes les légendes enregistrées
     *
     * @return Une liste non-modifiable de toutes les légendes
     */
    public List<LegendClass> getAllLegends() {
        return Collections.unmodifiableList(orderedLegends);
    }

    /**
     * Récupère toutes les légendes triées par ID
     *
     * @return Une liste des légendes triées par ID
     */
    public List<LegendClass> getAllLegendsSorted() {
        return orderedLegends.stream()
                .sorted(Comparator.comparingInt(LegendClass::getId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Récupère le nombre total de légendes enregistrées
     *
     * @return Le nombre de légendes
     */
    public int getCount() {
        return legendsById.size();
    }

    /**
     * Nettoie le registry (pour les tests ou le rechargement)
     */
    public void clear() {
        legendsById.clear();
        legendsByName.clear();
        orderedLegends.clear();
        Bukkit.getLogger().info("[Legend] Registry nettoyé");
    }

    /**
     * Recharge le registry avec les légendes par défaut
     */
    public void reload() {
        clear();
        initialize();
    }
}
