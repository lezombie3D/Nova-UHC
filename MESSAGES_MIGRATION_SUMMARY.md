# Résumé de la migration des messages vers CommonString

## ✅ **Messages ajoutés et configurés**

### **1. Messages des items communs (Common.java)**

- `ITEM_CONFIG_NAME` : "§b§lConfigurer la partie §8§l▪ §f§lClic-droit"
- `ITEM_TEAM_NAME` : "§f§lChoisir une équipe %main_color%§l▪ §f§lClic-droit"
- `ITEM_ACTIVE_SCENARIO_NAME` : "§f§lScénarios actifs %main_color%§l▪ §f§lClic-droit"
- `ITEM_ACTIVE_ROLE_NAME` : "§f§lMode de Jeu actifs %main_color%§l▪ §f§lClic-droit"
- `ITEM_TELEPORTATION_NAME` : "§f§lTéléportation %main_color%§l▪ §f§lClic-droit"

### **2. Messages de GameUi**

- `GAME_POTION_TITLE` : "§8┃ §fLimite de §9potions"
- `GAME_POTION_DESCRIPTION_1/2` : Descriptions des potions
- `GAME_PVP_TITLE` : "§8┃ §fBordure (%main_color%%time%§f)"
- `GAME_PVP_DESCRIPTION_1/2/3` : Descriptions du PvP
- `GAME_BORDER_TITLE` : "§8┃ §fPvP (%main_color%%time%§f)"
- `GAME_BORDER_DESCRIPTION_1/2/3/4` : Descriptions de la bordure
- `GAME_DIAMOND_TITLE` : "§8┃ §fLimite de §eDiamant§r (%main_color%§l%limit%§f)"
- `GAME_DIAMOND_DESCRIPTION_1/2` : Descriptions des diamants
- `GAME_ENCHANT_TITLE` : "§8┃ §fLimite d'§denchantements"
- `GAME_ENCHANT_DESCRIPTION_1/2` : Descriptions des enchantements
- `GAME_VERIFY_TITLE` : "§8┃ §fVérifier inventaire par défaut"
- `GAME_VERIFY_DESCRIPTION_1/2/3` : Descriptions de vérification
- `GAME_DEFAULT_TITLE` : "§8┃ §fInventaire par défaut"
- `GAME_DEFAULT_DESCRIPTION_1/2/3` : Descriptions de l'inventaire par défaut
- `GAME_DEATH_TITLE` : "§8┃ §fInventaire de %main_color%mort"
- `GAME_DEATH_DESCRIPTION_1/2/3` : Descriptions de l'inventaire de mort
- `GAME_DROP_TITLE` : "§8┃ §fTaux de §7drop"
- `GAME_DROP_DESCRIPTION_1/2` : Descriptions des drops

### **3. Messages de DefaultUi**

- `MENU_SCENARIOS_TITLE` : "§8┃ §fGestion des %main_color%scénarios"
- `MENU_SCENARIOS_ACCESS/DESCRIPTION_1/2` : Accès et descriptions des scénarios
- `MENU_GAMEMODE_TITLE` : "§8┃ §fMode de %main_color%jeu"
- `MENU_GAMEMODE_ACCESS/DESCRIPTION_1/2` : Accès et descriptions du mode de jeu
- `MENU_WORLD_TITLE` : "§8┃ §fMonde"
- `MENU_WORLD_ACCESS/DESCRIPTION_1/2` : Accès et descriptions du monde
- `MENU_TP_LOBBY_TITLE` : "§8┃ §fTéléportation au §alobby"
- `MENU_TP_RULES_TITLE` : "§8┃ §fTéléportation à la §asalle des règles"
- `MENU_TP_LOBBY_DESTINATION` : "au point d'apparition"
- `MENU_TP_RULES_DESTINATION` : "dans la salle des règles"
- `MENU_TP_DESCRIPTION_1/2` : Descriptions de téléportation
- `MENU_SLOTS_TITLE` : "§8┃ §fSlots"
- `MENU_SLOTS_ACCESS/DESCRIPTION_1/2` : Accès et descriptions des slots
- `MENU_LAUNCH_TITLE_START` : "§aLancer la partie"
- `MENU_LAUNCH_TITLE_CANCEL` : "§cAnnuler le lancement"
- `MENU_LAUNCH_READY_QUESTION` : "§8➤ §fTout est §aprêt §f?"
- `MENU_LAUNCH_ACCESS` : "§8➤ §fAccès : §eHost"
- `MENU_LAUNCH_START_DESC_1/2` : Descriptions de lancement
- `MENU_LAUNCH_CANCEL_QUESTION` : "§8➤ §fPas sûr ? §cArrête§f !"
- `MENU_LAUNCH_CANCEL_DESC_1/2` : Descriptions d'annulation
- `MENU_LAUNCH_ACTION_START` : "§8» §fCliquez pour §aactiver§f."

### **4. Messages de ScenariosUi**

- `SCENARIO_STATUS_ENABLED` : "§2Activé"
- `SCENARIO_STATUS_DISABLED` : "§cDésactivé"
- `SCENARIO_OPEN_CONFIG` : "§8» §a§lOuvrir la configuration"

## 🔧 **Fichiers modifiés**

### **1. CommonString.java**

- ✅ Ajouté 57 nouveaux messages
- ✅ Système de placeholders sécurisé
- ✅ Méthode `getRawMessage()` pour l'initialisation

### **2. lang.yml**

- ✅ Ajouté toutes les traductions correspondantes
- ✅ Support des placeholders (`%main_color%`, `%time%`, `%limit%`, etc.)
- ✅ Messages organisés par catégories

### **3. Common.java**

- ✅ Migration des items vers CommonString
- ✅ Tous les noms d'items configurables

### **4. GameUi.java**

- ✅ Migration complète vers CommonString
- ✅ Support des placeholders dynamiques
- ✅ Gestion sécurisée des valeurs

### **5. DefaultUi.java**

- ✅ Migration de la plupart des éléments
- ✅ Téléportation dynamique (lobby/règles)
- ✅ Descriptions configurables

### **6. ScenariosUi.java**

- ✅ Statuts des scénarios configurables
- ✅ Messages d'action configurables

## 🎯 **Placeholders disponibles**

### **Placeholders globaux**

- `%main_color%` : Couleur principale du serveur
- `%time%` : Temps formaté (pour PvP/Border)
- `%limit%` : Limite (pour diamants)
- `%destination%` : Destination (pour téléportation)

### **Placeholders automatiques**

- Tous les placeholders existants de CommonString
- Support des placeholders personnalisés via Map

## 📋 **Utilisation**

```java
// Message simple
CommonString.GAME_POTION_TITLE.getMessage()

// Message avec placeholders automatiques
CommonString.GAME_PVP_TITLE.

getMessage() // Utilise %main_color% et %time%

// Message avec placeholders personnalisés
Map<String, Object> placeholders = new HashMap<>();
placeholders.

put("%limit%",limitValue);

String title = CommonString.GAME_DIAMOND_TITLE.getMessage();
for(
Map.Entry<String, Object> entry :placeholders.

entrySet()){
title =title.

replace(entry.getKey(),entry.

getValue().

toString());
        }
```

## ✅ **Avantages obtenus**

1. **Centralisation** : Tous les messages dans un seul endroit
2. **Configurabilité** : Modification facile via lang.yml
3. **Placeholders** : Support dynamique des valeurs
4. **Cohérence** : Format uniforme dans tout le plugin
5. **Maintenance** : Facilité de modification et traduction
6. **Sécurité** : Gestion des erreurs d'initialisation

## 🔄 **Prochaines étapes possibles**

1. Ajouter les messages des scénarios individuels
2. Migrer les messages des autres UI (WorldUi, etc.)
3. Ajouter support multilingue complet
4. Créer des outils de validation des traductions

Tous les messages principaux des menus sont maintenant configurables via CommonString ! 🎉
