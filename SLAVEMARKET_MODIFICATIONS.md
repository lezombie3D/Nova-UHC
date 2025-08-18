# Modifications SlaveMarket - Configuration Dynamique des Équipes

## Résumé des changements

La classe `SlaveMarket` a été modifiée pour permettre la configuration dynamique du nombre d'équipes et de leurs
positions via un fichier `config.yml`.

## Fichiers modifiés/créés

### 1. `TeamPlace.java` (nouveau)

- Classe utilitaire pour représenter les positions d'une équipe (capitaine + esclave)
- Méthode `isValid()` pour vérifier que les deux positions sont définies

### 2. `SlaveMarket.java` (modifié)

- **Changement du type** : `List<Location> team_place` → `List<TeamPlace> team_place`
- **Nouvelle méthode** : `loadTeamPlacesFromConfig()` - charge les positions depuis la config
- **Nouvelle méthode** : `getSlaveLocationForOwner()` - trouve la position esclave pour un propriétaire
- **Méthode modifiée** : `setup()` - initialise les positions depuis la config
- **Méthode modifiée** : `toggleActive()` - crée le nombre d'équipes basé sur la config
- **Méthode modifiée** : `startEnchere()` - utilise les positions configurées pour les capitaines
- **Téléportation des esclaves** : utilise maintenant les positions configurées

### 3. `slave.yml` (nouveau fichier de configuration)

- Configuration des positions d'enchères (`enchere_place`, `wait_place`)
- Section `team_place` avec positions pour chaque équipe (captain + slave)

## Format de configuration

```yaml
team_place:
  0:
    captain: { world: "arena", x: 50, y: 100, z: 0, pitch: 0, yaw: 90 }
    slave: { world: "arena", x: 45, y: 100, z: 0, pitch: 0, yaw: 90 }
  1:
    captain: { world: "arena", x: 60, y: 100, z: 0, pitch: 0, yaw: 90 }
    slave: { world: "arena", x: 55, y: 100, z: 0, pitch: 0, yaw: 90 }
```

## Comportement

1. **Chargement** : Les positions sont chargées au démarrage du scénario
2. **Validation** : Seules les équipes avec positions captain ET slave valides sont utilisées
3. **Nombre d'équipes** : Déterminé automatiquement par le nombre de sections valides
4. **Fallback** : Si aucune config, utilise les positions hardcodées originales
5. **Logs** : Messages informatifs sur le nombre d'équipes chargées

## Avantages

- ✅ Configuration flexible du nombre d'équipes
- ✅ Positions personnalisables pour chaque équipe
- ✅ Validation automatique des configurations
- ✅ Compatibilité descendante (fallback vers hardcodé)
- ✅ Logs détaillés pour le debugging
- ✅ Respect de l'architecture existante (utilise ConfigUtils.getLocation)

## Utilisation

1. Modifier le fichier `slave.yml` avec les positions désirées
2. Activer le scénario SlaveMarket
3. Le nombre d'équipes sera automatiquement adapté à la configuration
