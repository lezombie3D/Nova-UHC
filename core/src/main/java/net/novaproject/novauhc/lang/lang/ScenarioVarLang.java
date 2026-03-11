package net.novaproject.novauhc.lang.lang;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;


public enum ScenarioVarLang implements Lang {


    
    ACIDRAIN_VAR_NEXT_RAIN_IN_BASE_DESC("Temps de base en secondes entre deux pluies", "Base time in seconds between two rains"),
    ACIDRAIN_VAR_NEXT_RAIN_IN_BASE_NAME("Temps entre les pluies", "Time between rains"),
    ACIDRAIN_VAR_RAIN_DAMAGE_DESC("Dégâts par seconde sous la pluie", "Damage per second under the rain"),
    ACIDRAIN_VAR_RAIN_DAMAGE_NAME("Dégâts", "Damage"),
    ACIDRAIN_VAR_RAIN_DURATION_BASE_DESC("Durée de base de la pluie en secondes", "Base rain duration in seconds"),
    ACIDRAIN_VAR_RAIN_DURATION_BASE_NAME("Durée de la pluie", "Rain duration"),

    
    AURAPEUR_VAR_RADIUS_DESC("Permet de définir le rayon d'action", "Defines the area of effect radius"),
    AURAPEUR_VAR_RADIUS_NAME("Rayon", "Radius"),
    AURAPEUR_VAR_RESIST_DEBUFF_PERCENT_DESC("Permet de définir le pourcentage de résistance perdu dans la zone.", "Defines the percentage of resistance lost in the zone."),
    AURAPEUR_VAR_RESIST_DEBUFF_PERCENT_NAME("Perte de Résistance", "Resistance Loss"),

    
    BATROULETTE_VAR_CHANCE_DESC("Chance (en pourcentage) d'être téléporté en hauteur au lieu de recevoir une pomme dorée.", "Chance (in percentage) of being teleported high up instead of receiving a golden apple."),
    BATROULETTE_VAR_CHANCE_NAME("Chance d'être obligé de faire un MLG", "Chance of being forced to MLG"),

    
    BESTPVE_VAR_TIMER_DESC("Définit le temps entre chaque gain de cœur pour les joueurs dans le classement PvE.", "Defines the timebetween each heart gain for players in the PvE ranking."),
    BESTPVE_VAR_TIMER_NAME("Temps entre les gains de cœur", "Time between heart gains"),
    TPMEETUP_VAR_TIMER_TP_NAME("Temps avant TP","Time before TP"),
    TPMEETUP_VAR_TIMER_TP_DESC("Definit le temps avant le Tp Global","Set the time before the global TP"),
    BLIZZARD_VAR_BLIND_EFFECT_LEVEL_DESC("Niveau de l'effet Blindness lors de froid", "Blindness effect level during cold"),
    BLIZZARD_VAR_BLIND_EFFECT_LEVEL_NAME("Niveau Blindness", "Blindness Level"),
    BLIZZARD_VAR_COLD_DAMAGE_INTERVAL_DESC("Intervalle de tick pour appliquer les effets de froid (en secondes)", "Tick interval to apply cold effects (in seconds)"),
    BLIZZARD_VAR_COLD_DAMAGE_INTERVAL_NAME("Intervalle de dégâts de froid", "Cold Damage Interval"),
    BLIZZARD_VAR_EXPOSURE_CHECK_HEIGHT_DESC("Hauteur au-dessus du joueur pour vérifier l'exposition", "Height above the player to check exposure"),
    BLIZZARD_VAR_EXPOSURE_CHECK_HEIGHT_NAME("Hauteur de vérification", "Exposure Check Height"),
    BLIZZARD_VAR_MAX_BLIZZARD_DURATION_DESC("Durée maximale d'une tempête (en secondes)", "Maximum storm duration (in seconds)"),
    BLIZZARD_VAR_MAX_BLIZZARD_DURATION_NAME("Durée max tempête", "Max Blizzard Duration"),
    BLIZZARD_VAR_MAX_TIME_BETWEEN_BLIZZARDS_DESC("Temps maximum entre deux tempêtes (en secondes)", "Maximum time between two storms (in seconds)"),
    BLIZZARD_VAR_MAX_TIME_BETWEEN_BLIZZARDS_NAME("Temps max entre tempêtes", "Max Time Between Blizzards"),
    BLIZZARD_VAR_MIN_BLIZZARD_DURATION_DESC("Durée minimale d'une tempête (en secondes)", "Minimum storm duration (in seconds)"),
    BLIZZARD_VAR_MIN_BLIZZARD_DURATION_NAME("Durée min tempête", "Min Blizzard Duration"),
    BLIZZARD_VAR_MIN_TIME_BETWEEN_BLIZZARDS_DESC("Temps minimum entre deux tempêtes (en secondes)", "Minimum time between two storms (in seconds)"),
    BLIZZARD_VAR_MIN_TIME_BETWEEN_BLIZZARDS_NAME("Temps min entre tempêtes", "Min Time Between Blizzards"),
    BLIZZARD_VAR_SLOW_EFFECT_LEVEL_DESC("Niveau de l'effet Slowness lors de froid modéré", "Slowness effect level during moderate cold"),
    BLIZZARD_VAR_SLOW_EFFECT_LEVEL_NAME("Niveau Slowness", "Slowness Level"),
    BLIZZARD_VAR_WARMTH_DECREASE_PER_TICK_DESC("Perte de chaleur par tick quand exposé au blizzard", "Warmth loss per tick when exposed to blizzard"),
    BLIZZARD_VAR_WARMTH_DECREASE_PER_TICK_NAME("Perte de chaleur/tick", "Warmth Decrease Per Tick"),
    BLIZZARD_VAR_WARMTH_MAX_DESC("Chaleur maximale d'un joueur", "Maximum warmth of a player"),
    BLIZZARD_VAR_WARMTH_MAX_NAME("Chaleur max", "Max Warmth"),
    BLIZZARD_VAR_WARMTH_NEAR_HEAT_SOURCE_BONUS_DESC("Gain de chaleur si proche d'une source de chaleur", "Warmth gain when near a heat source"),
    BLIZZARD_VAR_WARMTH_NEAR_HEAT_SOURCE_BONUS_NAME("Bonus chaleur (source)", "Heat Source Warmth Bonus"),
    BLIZZARD_VAR_WARMTH_NORMAL_GAIN_DESC("Gain de chaleur normal par tick", "Normal warmth gain per tick"),
    BLIZZARD_VAR_WARMTH_NORMAL_GAIN_NAME("Gain de chaleur/tick", "Normal Warmth Gain"),

    
    BLOCKRUSH_VAR_BASE_REWARD_AMOUNT_DESC("Quantité de lingots d'or par nouveau bloc", "Gold ingots per new block"),
    BLOCKRUSH_VAR_BASE_REWARD_AMOUNT_NAME("Récompense de base", "Base Reward Amount"),
    BLOCKRUSH_VAR_DIAMOND_BONUS_DESC("Diamants pour le premier à miner un diamant", "Diamonds for the first to mine a diamond"),
    BLOCKRUSH_VAR_DIAMOND_BONUS_NAME("Bonus diamant", "Diamond Bonus"),
    BLOCKRUSH_VAR_EMERALD_BONUS_DESC("Émeraudes pour le premier à miner une émeraude", "Emeralds for the first to mine an emerald"),
    BLOCKRUSH_VAR_EMERALD_BONUS_NAME("Bonus émeraude", "Emerald Bonus"),
    BLOCKRUSH_VAR_GOLD_BONUS_DESC("Lingots d'or pour le premier à miner de l'or", "Gold ingots for the first to mine gold"),
    BLOCKRUSH_VAR_GOLD_BONUS_NAME("Bonus or", "Gold Bonus"),
    BLOCKRUSH_VAR_MILESTONE10REWARD_DESC("Pommes d'or pour le palier 10", "Golden apples for milestone 10"),
    BLOCKRUSH_VAR_MILESTONE10REWARD_NAME("Récompense palier 10", "Milestone 10 Reward"),
    BLOCKRUSH_VAR_MILESTONE25REWARD_DESC("Livres enchantés pour le palier 25", "Enchanted books for milestone 25"),
    BLOCKRUSH_VAR_MILESTONE25REWARD_NAME("Récompense palier 25", "Milestone 25 Reward"),
    BLOCKRUSH_VAR_MILESTONE50REWARD_DESC("Étoiles du Nether pour le palier 50", "Nether stars for milestone 50"),
    BLOCKRUSH_VAR_MILESTONE50REWARD_NAME("Récompense palier 50", "Milestone 50 Reward"),

    
    BLOODCYCLE_VAR_BETWEEN_DESC("Temps en secondes entre chaque changement de type de minerai", "Time in seconds between each ore type change"),
    BLOODCYCLE_VAR_BETWEEN_NAME("Temps entre les changements", "Time Between Changes"),

    
    BLOODDIAMONDS_VAR_DAMAGE_AMOUNT_DESC("Quantité de cœurs perdus en minant un diamant.", "Hearts lost when mining a diamond."),
    BLOODDIAMONDS_VAR_DAMAGE_AMOUNT_NAME("Quantité de dégâts", "Damage Amount"),

    
    BLOODLUST_VAR_COUNTDOWN10SEC_DESC("Activer le message de countdown à 10 secondes", "Enable the 10 seconds countdown message"),
    BLOODLUST_VAR_COUNTDOWN10SEC_NAME("Countdown 10s", "10s Countdown"),
    BLOODLUST_VAR_COUNTDOWN5SEC_DESC("Activer le message de countdown à 5 secondes", "Enable the 5 seconds countdown message"),
    BLOODLUST_VAR_COUNTDOWN5SEC_NAME("Countdown 5s", "5s Countdown"),
    BLOODLUST_VAR_COUNTDOWN_END_DESC("Activer le message à la fin de l'effet", "Enable the message at the end of the effect"),
    BLOODLUST_VAR_COUNTDOWN_END_NAME("Message de fin", "End Message"),
    BLOODLUST_VAR_SPEED_DURATION_DESC("Durée de l'effet Speed en secondes", "Speed effect duration in seconds"),
    BLOODLUST_VAR_SPEED_DURATION_NAME("Durée Speed", "Speed Duration"),
    BLOODLUST_VAR_SPEED_LEVEL_DESC("Niveau de l'effet Speed", "Speed effect level"),
    BLOODLUST_VAR_SPEED_LEVEL_NAME("Niveau Speed", "Speed Level"),
    BLOODLUST_VAR_STRENGTH_DURATION_DESC("Durée de l'effet Strength en secondes", "Strength effect duration in seconds"),
    BLOODLUST_VAR_STRENGTH_DURATION_NAME("Durée Strength", "Strength Duration"),
    BLOODLUST_VAR_STRENGTH_LEVEL_DESC("Niveau de l'effet Strength", "Strength effect level"),
    BLOODLUST_VAR_STRENGTH_LEVEL_NAME("Niveau Strength", "Strength Level"),

    
    BUFFKILLER_VAR_EFFECT_LEVEL_DESC("Niveau des effets de potion appliqués", "Applied potion effects level"),
    BUFFKILLER_VAR_EFFECT_LEVEL_NAME("Niveau des effets", "Effect Level"),
    BUFFKILLER_VAR_FIRE_RESISTANCE_ACTIVE_DESC("Activer l'effet Fire Resistance pour le tueur", "Enable Fire Resistance effect for the killer"),
    BUFFKILLER_VAR_FIRE_RESISTANCE_ACTIVE_NAME("Fire Resistance actif", "Fire Resistance Active"),
    BUFFKILLER_VAR_MAX_DURATION_DESC("Durée maximale des effets en secondes", "Maximum effect duration in seconds"),
    BUFFKILLER_VAR_MAX_DURATION_NAME("Durée max", "Max Duration"),
    BUFFKILLER_VAR_MIN_DURATION_DESC("Durée minimale des effets en secondes", "Minimum effect duration in seconds"),
    BUFFKILLER_VAR_MIN_DURATION_NAME("Durée min", "Min Duration"),
    BUFFKILLER_VAR_RESISTANCE_ACTIVE_DESC("Activer l'effet Resistance pour le tueur", "Enable Resistance effect for the killer"),
    BUFFKILLER_VAR_RESISTANCE_ACTIVE_NAME("Resistance actif", "Resistance Active"),
    BUFFKILLER_VAR_SPEED_ACTIVE_DESC("Activer l'effet Speed pour le tueur", "Enable Speed effect for the killer"),
    BUFFKILLER_VAR_SPEED_ACTIVE_NAME("Speed actif", "Speed Active"),
    BUFFKILLER_VAR_STRENGTH_ACTIVE_DESC("Activer l'effet Strength pour le tueur", "Enable Strength effect for the killer"),
    BUFFKILLER_VAR_STRENGTH_ACTIVE_NAME("Strength actif", "Strength Active"),

    
    COMPENSATION_VAR_HEARTS_PER_DEATH_DESC("Nombre de cœurs que chaque membre d'équipe reçoit à la mort d'un joueur", "Hearts each team member receives on a player's death"),
    COMPENSATION_VAR_HEARTS_PER_DEATH_NAME("Cœurs par mort", "Hearts Per Death"),

    
    CRIPPLE_VAR_WEAKNESS_DURATION_DESC("Durée de l'effet Weakness en secondes", "Weakness effect duration in seconds"),
    CRIPPLE_VAR_WEAKNESS_DURATION_NAME("Durée Weakness", "Weakness Duration"),
    CRIPPLE_VAR_WEAKNESS_LEVEL_DESC("Niveau de l'effet Weakness", "Weakness effect level"),
    CRIPPLE_VAR_WEAKNESS_LEVEL_NAME("Niveau Weakness", "Weakness Level"),

    
    DEATHEMERAULD_VAR_DAMAGE_AMOUNT_DESC("Quantité de dégâts infligés au joueur aléatoire", "Damage dealt to the random player"),
    DEATHEMERAULD_VAR_DAMAGE_AMOUNT_NAME("Quantité de dégâts", "Damage Amount"),
    DEATHEMERAULD_VAR_TARGET_BLOCK_DESC("Type de bloc qui déclenche l'effet", "Block type that triggers the effect"),
    DEATHEMERAULD_VAR_TARGET_BLOCK_NAME("Bloc cible", "Target Block"),

    
    DEMOCRACY_VAR_MIN_PLAYERS_DESC("Nombre minimum de joueurs pour que le vote commence", "Minimum players required for the vote to start"),
    DEMOCRACY_VAR_MIN_PLAYERS_NAME("Joueurs minimum", "Min Players"),
    DEMOCRACY_VAR_VOTE_DURATION_DESC("Durée d'un vote actif en secondes", "Active vote duration in seconds"),
    DEMOCRACY_VAR_VOTE_DURATION_NAME("Durée du vote", "Vote Duration"),
    DEMOCRACY_VAR_VOTE_INTERVAL_DESC("Intervalle entre chaque vote en secondes", "Interval between each vote in seconds"),
    DEMOCRACY_VAR_VOTE_INTERVAL_NAME("Intervalle de vote", "Vote Interval"),
    DEMOCRACY_VAR_WARNING10SEC_VOTE_DESC("Activer avertissement 10 secondes pendant le vote", "Enable 10 seconds warning during vote"),
    DEMOCRACY_VAR_WARNING10SEC_VOTE_NAME("Alerte 10s (vote)", "10s Warning (vote)"),
    DEMOCRACY_VAR_WARNING1MIN_DESC("Activer avertissement 1 minute avant vote", "Enable 1 minute warning before vote"),
    DEMOCRACY_VAR_WARNING1MIN_NAME("Alerte 1min (avant)", "1min Warning (before)"),
    DEMOCRACY_VAR_WARNING1MIN_VOTE_DESC("Activer avertissement 1 minute pendant le vote", "Enable 1 minute warning during vote"),
    DEMOCRACY_VAR_WARNING1MIN_VOTE_NAME("Alerte 1min (vote)", "1min Warning (vote)"),
    DEMOCRACY_VAR_WARNING5MIN_DESC("Activer avertissement 5 minutes avant vote", "Enable 5 minutes warning before vote"),
    DEMOCRACY_VAR_WARNING5MIN_NAME("Alerte 5min (avant)", "5min Warning (before)"),

    
    DOUBLEORE_VAR_DOUBLE_DIAMOND_DESC("Double la quantité de diamants minés", "Doubles the amount of mined diamonds"),
    DOUBLEORE_VAR_DOUBLE_DIAMOND_NAME("Double Diamant", "Double Diamond"),
    DOUBLEORE_VAR_DOUBLE_EMERALD_DESC("Double la quantité d'émeraudes minées", "Doubles the amount of mined emeralds"),
    DOUBLEORE_VAR_DOUBLE_EMERALD_NAME("Double Émeraude", "Double Emerald"),
    DOUBLEORE_VAR_DOUBLE_GOLD_DESC("Double la quantité d'or miné", "Doubles the amount of mined gold"),
    DOUBLEORE_VAR_DOUBLE_GOLD_NAME("Double Or", "Double Gold"),
    DOUBLEORE_VAR_DOUBLE_IRON_DESC("Double la quantité de fer miné", "Doubles the amount of mined iron"),
    DOUBLEORE_VAR_DOUBLE_IRON_NAME("Double Fer", "Double Iron"),

    
    FALLOUT_VAR_BASE_DAMAGE_DESC("Dégâts de radiation appliqués par tick pour exposition légère", "Radiation damage applied per tick for light exposure"),
    FALLOUT_VAR_BASE_DAMAGE_NAME("Dégâts de base", "Base Radiation Damage"),
    FALLOUT_VAR_FALLOUT_START_TIME_DESC("Temps en secondes avant le début des radiations", "Time in seconds before radiation starts"),
    FALLOUT_VAR_FALLOUT_START_TIME_NAME("Délai de démarrage", "Fallout Start Time"),
    FALLOUT_VAR_MAX_DAMAGE_DESC("Dégâts max de radiation par tick", "Max radiation damage per tick"),
    FALLOUT_VAR_MAX_DAMAGE_NAME("Dégâts max sévères", "Severe Max Damage"),
    FALLOUT_VAR_MODERATE_THRESHOLD_DESC("Nombre de ticks d'exposition avant radiation modérée", "Exposure ticks before moderate radiation"),
    FALLOUT_VAR_MODERATE_THRESHOLD_NAME("Seuil radiation modérée", "Moderate Radiation Threshold"),
    FALLOUT_VAR_SAFE_YLEVEL_DESC("Altitude Y en dessous de laquelle les joueurs sont en sécurité", "Y altitude below which players are safe"),
    FALLOUT_VAR_SAFE_YLEVEL_NAME("Niveau Y sûr", "Safe Y Level"),
    FALLOUT_VAR_SEVERE_THRESHOLD_DESC("Nombre de ticks d'exposition avant radiation sévère", "Exposure ticks before severe radiation"),
    FALLOUT_VAR_SEVERE_THRESHOLD_NAME("Seuil radiation sévère", "Severe Radiation Threshold"),

    
    FATALIS_VAR_AURA_NAME("Aura de Peur", "Aura of Fear"),
    FATALIS_VAR_CRIT_CHANCE_DESC("§fChance de §6provoquer §fun §5§lCritique§f.", "§fChance to §6trigger §fa §5§lCritical§f hit."),
    FATALIS_VAR_CRIT_CHANCE_NAME("Chance Critique", "Critical Chance"),
    FATALIS_VAR_CRIT_DAMAGE_DESC("§fAugmentation des §6dégâts §fen cas de §5§lCritique§f.", "§fIncreased §6damage §fon §5§lCritical§f hit."),
    FATALIS_VAR_CRIT_DAMAGE_NAME("Dégâts Critiques", "Critical Damage"),
    FATALIS_VAR_DRAG_DESC("§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f", "§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f"),
    FATALIS_VAR_DRAG_NAME("Résistance Dragon", "Dragon Resistance"),
    FATALIS_VAR_FIRE_DESC("§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f", "§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f"),
    FATALIS_VAR_FIRE_NAME("Résistance Feu", "Fire Resistance"),
    FATALIS_VAR_FLAME_NAME("Flamme Noire", "Black Flame"),
    FATALIS_VAR_FORCE_DESC("§fPoints de §c§lForce §fpar défaut.", "§fDefault §c§lStrength §fpoints."),
    FATALIS_VAR_FORCE_NAME("Force", "Strength"),
    FATALIS_VAR_ICE_DESC("§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f", "§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f"),
    FATALIS_VAR_ICE_NAME("Résistance Glace", "Ice Resistance"),
    FATALIS_VAR_MAX_HP_DESC("§fPoints de §a§lVie §fmaximum par défaut.", "§fDefault maximum §a§lHealth §fpoints."),
    FATALIS_VAR_MAX_HP_NAME("Vie", "Health"),
    FATALIS_VAR_MER_NAME("Mer de Flamme", "Sea of Flames"),
    FATALIS_VAR_RES_DESC("§fPoints de §b§lRésistance §fpar défaut.", "§fDefault §b§lResistance §fpoints."),
    FATALIS_VAR_RES_NAME("Résistance", "Resistance"),
    FATALIS_VAR_THUNDER_DESC("§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f", "§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f"),
    FATALIS_VAR_THUNDER_NAME("Résistance Foudre", "Thunder Resistance"),
    FATALIS_VAR_WATER_DESC("§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f", "§c§lIncrease§f/§a§lDecrease §fof §6§lElemental Resistance§f"),
    FATALIS_VAR_WATER_NAME("Résistance Eau", "Water Resistance"),

    
    FINALHEAL_VAR_HEAL_FOOD_DESC("Si vrai, restaure aussi la nourriture", "If true, also restores food"),
    FINALHEAL_VAR_HEAL_FOOD_NAME("Restaurer nourriture", "Heal Food"),
    FINALHEAL_VAR_HEAL_TIME1_DESC("Temps (en secondes) du premier heal", "Time (in seconds) of the first heal"),
    FINALHEAL_VAR_HEAL_TIME1_NAME("Temps du 1er Heal", "First Heal Time"),
    FINALHEAL_VAR_HEAL_TIME2_DESC("Temps (en secondes) du deuxième heal", "Time (in seconds) of the second heal"),
    FINALHEAL_VAR_HEAL_TIME2_NAME("Temps du 2ème Heal", "Second Heal Time"),

    
    FLAMENOIR_VAR_DEGAT_DESC("Dégâts infligés", "Damage dealt"),
    FLAMENOIR_VAR_DEGAT_NAME("Dégâts", "Damage"),
    FLAMENOIR_VAR_MAX_DISTANCE_DESC("Distance parcourue", "Distance traveled"),
    FLAMENOIR_VAR_MAX_DISTANCE_NAME("Distance", "Distance"),

    
    GAPROULETTE_VAR_ENABLE_FIRE_RESISTANCE_DESC("Activer l'effet Fire Resistance pour les pommes", "Enable Fire Resistance effect for apples"),
    GAPROULETTE_VAR_ENABLE_FIRE_RESISTANCE_NAME("Effet Fire Resistance", "Fire Resistance Effect"),
    GAPROULETTE_VAR_ENABLE_HUNGER_DESC("Activer l'effet Hunger pour les pommes", "Enable Hunger effect for apples"),
    GAPROULETTE_VAR_ENABLE_HUNGER_NAME("Effet Hunger", "Hunger Effect"),
    GAPROULETTE_VAR_ENABLE_JUMP_DESC("Activer l'effet Jump pour les pommes", "Enable Jump effect for apples"),
    GAPROULETTE_VAR_ENABLE_JUMP_NAME("Effet Jump", "Jump Effect"),
    GAPROULETTE_VAR_ENABLE_POISON_DESC("Activer l'effet Poison pour les pommes", "Enable Poison effect for apples"),
    GAPROULETTE_VAR_ENABLE_POISON_NAME("Effet Poison", "Poison Effect"),
    GAPROULETTE_VAR_ENABLE_RESISTANCE_DESC("Activer l'effet Resistance pour les pommes", "Enable Resistance effect for apples"),
    GAPROULETTE_VAR_ENABLE_RESISTANCE_NAME("Effet Resistance", "Resistance Effect"),
    GAPROULETTE_VAR_ENABLE_SLOWNESS_DESC("Activer l'effet Slowness pour les pommes", "Enable Slowness effect for apples"),
    GAPROULETTE_VAR_ENABLE_SLOWNESS_NAME("Effet Slowness", "Slowness Effect"),
    GAPROULETTE_VAR_ENABLE_SPEED_DESC("Activer l'effet Vitesse pour les pommes", "Enable Speed effect for apples"),
    GAPROULETTE_VAR_ENABLE_SPEED_NAME("Effet Vitesse", "Speed Effect"),
    GAPROULETTE_VAR_ENABLE_STRENGTH_DESC("Activer l'effet Strength pour les pommes", "Enable Strength effect for apples"),
    GAPROULETTE_VAR_ENABLE_STRENGTH_NAME("Effet Strength", "Strength Effect"),
    GAPROULETTE_VAR_ENABLE_WEAKNESS_DESC("Activer l'effet Weakness pour les pommes", "Enable Weakness effect for apples"),
    GAPROULETTE_VAR_ENABLE_WEAKNESS_NAME("Effet Weakness", "Weakness Effect"),
    GAPROULETTE_VAR_ENABLE_WITHER_DESC("Activer l'effet Wither pour les pommes", "Enable Wither effect for apples"),
    GAPROULETTE_VAR_ENABLE_WITHER_NAME("Effet Wither", "Wither Effect"),
    GAPROULETTE_VAR_MAX_AMPLIFIER_DESC("Amplificateur maximal des effets appliqués", "Maximum amplifier of applied effects"),
    GAPROULETTE_VAR_MAX_AMPLIFIER_NAME("Amplificateur maximum", "Max Amplifier"),
    GAPROULETTE_VAR_MAX_DURATION_DESC("Durée maximale des effets appliqués", "Maximum duration of applied effects"),
    GAPROULETTE_VAR_MAX_DURATION_NAME("Durée maximum (secondes)", "Max Duration (seconds)"),
    GAPROULETTE_VAR_MIN_DURATION_DESC("Durée minimale des effets appliqués", "Minimum duration of applied effects"),
    GAPROULETTE_VAR_MIN_DURATION_NAME("Durée minimum (secondes)", "Min Duration (seconds)"),

    
    GENIE_VAR_ADVANCED_KILL_REQUIREMENT_DESC("Nombre de kills requis pour débloquer les souhaits avancés", "Kills required to unlock advanced wishes"),
    GENIE_VAR_ADVANCED_KILL_REQUIREMENT_NAME("Kills pour souhaits avancés", "Advanced Wishes Kill Req."),
    GENIE_VAR_BASIC_KILL_REQUIREMENT_DESC("Nombre de kills requis pour débloquer les souhaits basiques", "Kills required to unlock basic wishes"),
    GENIE_VAR_BASIC_KILL_REQUIREMENT_NAME("Kills pour souhaits basiques", "Basic Wishes Kill Req."),
    GENIE_VAR_LEGENDARY_KILL_REQUIREMENT_DESC("Nombre de kills requis pour débloquer les souhaits légendaires", "Kills required to unlock legendary wishes"),
    GENIE_VAR_LEGENDARY_KILL_REQUIREMENT_NAME("Kills pour souhaits légendaires", "Legendary Wishes Kill Req."),
    GENIE_VAR_MAX_WISHES_DESC("Nombre de souhaits disponibles par joueur", "Number of wishes available per player"),
    GENIE_VAR_MAX_WISHES_NAME("Nombre maximal de souhaits", "Max Wishes"),
    GENIE_VAR_MEDIUM_KILL_REQUIREMENT_DESC("Nombre de kills requis pour débloquer les souhaits moyens", "Kills required to unlock medium wishes"),
    GENIE_VAR_MEDIUM_KILL_REQUIREMENT_NAME("Kills pour souhaits moyens", "Medium Wishes Kill Req."),
    GENIE_VAR_SPEED_DURATION_DESC("Durée de l'effet Speed II en ticks", "Speed II effect duration in ticks"),
    GENIE_VAR_SPEED_DURATION_NAME("Durée Speed II (ticks)", "Speed II Duration (ticks)"),
    GENIE_VAR_STRENGTH_DURATION_DESC("Durée de l'effet Strength I en ticks", "Strength I effect duration in ticks"),
    GENIE_VAR_STRENGTH_DURATION_NAME("Durée Strength I (ticks)", "Strength I Duration (ticks)"),

    
    GLADIATOR_VAR_ARENA_HEIGHT_DESC("Hauteur des murs de l'arène", "Arena wall height"),
    GLADIATOR_VAR_ARENA_HEIGHT_NAME("Hauteur de l'arène", "Arena Height"),
    GLADIATOR_VAR_ARENA_RADIUS_DESC("Rayon de l'arène circulaire", "Circular arena radius"),
    GLADIATOR_VAR_ARENA_RADIUS_NAME("Rayon de l'arène", "Arena Radius"),
    GLADIATOR_VAR_ARENA_SPAWN_Y_DESC("Altitude à laquelle l'arène apparaît", "Altitude at which the arena spawns"),
    GLADIATOR_VAR_ARENA_SPAWN_Y_NAME("Hauteur de spawn de l'arène", "Arena Spawn Height"),
    GLADIATOR_VAR_MAX_FIGHT_DURATION_DESC("Durée maximale d'un combat d'arène avant fin automatique", "Maximum arena fight duration before auto-end"),
    GLADIATOR_VAR_MAX_FIGHT_DURATION_NAME("Durée max du combat (ticks)", "Max Fight Duration (ticks)"),

    
    GOLDENDROP_VAR_GOLDEN_APPLE_AMOUNT_DESC("Nombre de Golden Apple droppées à la mort d'un joueur", "Golden Apples dropped on player death"),
    GOLDENDROP_VAR_GOLDEN_APPLE_AMOUNT_NAME("Quantité de Golden Apple", "Golden Apple Amount"),

    
    GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_APPLE_DESC("Niveau de l'effet Absorption pour les pommes dorées normales", "Absorption effect level for normal golden apples"),
    GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_APPLE_NAME("Niveau Absorption Pomme", "Absorption Level Apple"),
    GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_GOLDEN_HEAD_DESC("Niveau de l'effet Absorption pour la Golden Head", "Absorption effect level for the Golden Head"),
    GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_GOLDEN_HEAD_NAME("Niveau Absorption Golden Head", "Absorption Level Golden Head"),
    GOLDENHEAD_VAR_ABSORPTION_DURATION_APPLE_DESC("Durée de l'effet Absorption pour les pommes dorées normales", "Absorption effect duration for normal golden apples"),
    GOLDENHEAD_VAR_ABSORPTION_DURATION_APPLE_NAME("Durée Absorption Pomme (s)", "Absorption Duration Apple (s)"),
    GOLDENHEAD_VAR_ABSORPTION_DURATION_GOLDEN_HEAD_DESC("Durée de l'effet Absorption pour la Golden Head", "Absorption effect duration for the Golden Head"),
    GOLDENHEAD_VAR_ABSORPTION_DURATION_GOLDEN_HEAD_NAME("Durée Absorption Golden Head (s)", "Absorption Duration Golden Head (s)"),
    GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_APPLE_DESC("Niveau de l'effet Régénération pour les pommes dorées normales", "Regeneration effect level for normal golden apples"),
    GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_APPLE_NAME("Niveau Régénération Pomme", "Regen Level Apple"),
    GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_GOLDEN_HEAD_DESC("Niveau de l'effet Régénération pour la Golden Head", "Regeneration effect level for the Golden Head"),
    GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_GOLDEN_HEAD_NAME("Niveau Régénération Golden Head", "Regen Level Golden Head"),
    GOLDENHEAD_VAR_REGENERATION_DURATION_APPLE_DESC("Durée de l'effet Régénération pour les pommes dorées normales", "Regeneration effect duration for normal golden apples"),
    GOLDENHEAD_VAR_REGENERATION_DURATION_APPLE_NAME("Durée Régénération Pomme (s)", "Regen Duration Apple (s)"),
    GOLDENHEAD_VAR_REGENERATION_DURATION_GOLDEN_HEAD_DESC("Durée de l'effet Régénération pour la Golden Head", "Regeneration effect duration for the Golden Head"),
    GOLDENHEAD_VAR_REGENERATION_DURATION_GOLDEN_HEAD_NAME("Durée Régénération Golden Head (s)", "Regen Duration Golden Head (s)"),

    
    INVENTORS_VAR_DIAMOND_TOOL_BONUS_DESC("Diamants pour le premier crafteur diamant", "Diamonds for the first diamond crafter"),
    INVENTORS_VAR_DIAMOND_TOOL_BONUS_NAME("Bonus outils diamant", "Diamond Tool Bonus"),
    INVENTORS_VAR_IRON_TOOL_BONUS_DESC("Lingots de fer pour le premier crafteur fer", "Iron ingots for the first iron crafter"),
    INVENTORS_VAR_IRON_TOOL_BONUS_NAME("Bonus outils fer", "Iron Tool Bonus"),
    INVENTORS_VAR_MILESTONE10REWARD_DESC("Objet donné au palier 10", "Item given at milestone 10"),
    INVENTORS_VAR_MILESTONE10REWARD_NAME("Récompense palier 10", "Milestone 10 Reward"),
    INVENTORS_VAR_MILESTONE5REWARD_DESC("Objet donné au palier 5", "Item given at milestone 5"),
    INVENTORS_VAR_MILESTONE5REWARD_NAME("Récompense palier 5", "Milestone 5 Reward"),
    INVENTORS_VAR_STONE_TOOL_BONUS_DESC("Pains pour le premier crafteur pierre", "Bread for the first stone crafter"),
    INVENTORS_VAR_STONE_TOOL_BONUS_NAME("Bonus outils pierre", "Stone Tool Bonus"),
    INVENTORS_VAR_WOOD_TOOL_BONUS_DESC("Pommes pour le premier crafteur bois", "Apples for the first wood crafter"),
    INVENTORS_VAR_WOOD_TOOL_BONUS_NAME("Bonus outils bois", "Wood Tool Bonus"),

    
    LONGSHOOT_VAR_DAMAGE_MULTIPLIER_DESC("Multiplicateur pour les tirs longue distance", "Multiplier for long-range shots"),
    LONGSHOOT_VAR_DAMAGE_MULTIPLIER_NAME("Multiplicateur de dégâts", "Damage Multiplier"),
    LONGSHOOT_VAR_MIN_DISTANCE_DESC("Distance minimale pour le bonus", "Minimum distance for the bonus"),
    LONGSHOOT_VAR_MIN_DISTANCE_NAME("Distance minimale", "Min Distance"),

    
    LOOTCRATE_VAR_ENABLE_DIAMONDS_DESC("Activer l'apparition de diamants", "Enable diamond spawning"),
    LOOTCRATE_VAR_ENABLE_DIAMONDS_NAME("Diamants", "Diamonds"),
    LOOTCRATE_VAR_ENABLE_ENCHANTED_ITEMS_DESC("Activer l'apparition d'items enchantés", "Enable enchanted item spawning"),
    LOOTCRATE_VAR_ENABLE_ENCHANTED_ITEMS_NAME("Items enchantés", "Enchanted Items"),
    LOOTCRATE_VAR_ENABLE_FOOD_DESC("Activer l'apparition de nourriture", "Enable food spawning"),
    LOOTCRATE_VAR_ENABLE_FOOD_NAME("Nourriture", "Food"),
    LOOTCRATE_VAR_ENABLE_GOLDEN_APPLES_DESC("Activer l'apparition de pommes d'or", "Enable golden apple spawning"),
    LOOTCRATE_VAR_ENABLE_GOLDEN_APPLES_NAME("Pommes d'or", "Golden Apples"),
    LOOTCRATE_VAR_ENABLE_POTIONS_DESC("Activer l'apparition de potions", "Enable potion spawning"),
    LOOTCRATE_VAR_ENABLE_POTIONS_NAME("Potions", "Potions"),
    LOOTCRATE_VAR_MAX_CRATES_DESC("Nombre maximum de coffres à spawn", "Maximum number of crates to spawn"),
    LOOTCRATE_VAR_MAX_CRATES_NAME("Coffres max", "Max Crates"),
    LOOTCRATE_VAR_MIN_CRATES_DESC("Nombre minimum de coffres à spawn", "Minimum number of crates to spawn"),
    LOOTCRATE_VAR_MIN_CRATES_NAME("Coffres min", "Min Crates"),
    LOOTCRATE_VAR_SPAWN_INTERVAL_DESC("Temps en secondes entre chaque apparition de coffres", "Time in seconds between each crate spawn"),
    LOOTCRATE_VAR_SPAWN_INTERVAL_NAME("Intervalle de spawn", "Spawn Interval"),

    
    LUCKYORE_VAR_LUCKY_CHANCE_DESC("Pourcentage de chance d'obtenir un objet légendaire en minant.", "Percentage chance of getting a legendary item while mining."),
    LUCKYORE_VAR_LUCKY_CHANCE_NAME("Chance de loot", "Lucky Chance"),

    
    MAGNET_VAR_MAGNET_RADIUS_DESC("Rayon en blocs dans lequel les minerais sont attirés.", "Radius in blocks within which ores are attracted."),
    MAGNET_VAR_MAGNET_RADIUS_NAME("Rayon du magnétisme", "Magnet Radius"),

    
    MERFLAMME_VAR_DAMAGE_DESC("Dégâts infligés", "Damage dealt"),
    MERFLAMME_VAR_DAMAGE_NAME("Dégâts", "Damage"),
    MERFLAMME_VAR_FIRE_DURATION_DESC("Durée du statut Feu (s)", "Fire status duration (s)"),
    MERFLAMME_VAR_FIRE_DURATION_NAME("Durée feu", "Fire Duration"),
    MERFLAMME_VAR_RADIUS_DESC("Rayon de la vague", "Wave radius"),
    MERFLAMME_VAR_RADIUS_NAME("Rayon", "Radius"),

    
    NINJA_VAR_INVISIBILITY_DURATION_DESC("Durée de l'invisibilité après un kill en ticks (20 ticks = 1 seconde).", "Invisibility duration after a kill in ticks (20 ticks = 1 second)."),
    NINJA_VAR_INVISIBILITY_DURATION_NAME("Durée d'invisibilité", "Invisibility Duration"),
    NINJA_VAR_INVISIBILITY_LEVEL_DESC("Niveau de l'effet d'invisibilité après un kill.", "Invisibility effect level after a kill."),
    NINJA_VAR_INVISIBILITY_LEVEL_NAME("Niveau d'invisibilité", "Invisibility Level"),

    
    NOCLEANUP_VAR_HEALTH_RESTORE_DESC("Nombre de cœurs restaurés au tueur lors d'une élimination.", "Hearts restored to the killer on an elimination."),
    NOCLEANUP_VAR_HEALTH_RESTORE_NAME("Cœurs restaurés", "Health Restore"),

    
    PARKOURMASTER_VAR_CHECKPOINT_DISTANCE_MAX_DESC("Distance maximale entre les checkpoints.", "Maximum distance between checkpoints."),
    PARKOURMASTER_VAR_CHECKPOINT_DISTANCE_MAX_NAME("Distance max checkpoint", "Checkpoint Distance Max"),
    PARKOURMASTER_VAR_CHECKPOINT_DISTANCE_MIN_DESC("Distance minimale entre les checkpoints.", "Minimum distance between checkpoints."),
    PARKOURMASTER_VAR_CHECKPOINT_DISTANCE_MIN_NAME("Distance min checkpoint", "Checkpoint Distance Min"),
    PARKOURMASTER_VAR_MAX_CHECKPOINTS_DESC("Nombre maximum de checkpoints par parcours.", "Maximum checkpoints per course."),
    PARKOURMASTER_VAR_MAX_CHECKPOINTS_NAME("Checkpoints max", "Max Checkpoints"),
    PARKOURMASTER_VAR_MIN_CHECKPOINTS_DESC("Nombre minimum de checkpoints par parcours.", "Minimum checkpoints per course."),
    PARKOURMASTER_VAR_MIN_CHECKPOINTS_NAME("Checkpoints min", "Min Checkpoints"),
    PARKOURMASTER_VAR_PARKOUR_TIMEOUT_DESC("Temps maximum (en secondes) pour compléter un parcours.", "Maximum time (in seconds) to complete a course."),
    PARKOURMASTER_VAR_PARKOUR_TIMEOUT_NAME("Temps limite", "Parkour Timeout"),
    PARKOURMASTER_VAR_REWARD_ARROW_DESC("Quantité de flèches offertes.", "Number of arrows rewarded."),
    PARKOURMASTER_VAR_REWARD_ARROW_NAME("Récompense flèches", "Reward Arrows"),
    PARKOURMASTER_VAR_REWARD_DIAMOND_DESC("Quantité de diamants offerts.", "Number of diamonds rewarded."),
    PARKOURMASTER_VAR_REWARD_DIAMOND_NAME("Récompense diamants", "Reward Diamonds"),
    PARKOURMASTER_VAR_REWARD_ENCHANTED_BOOK_DESC("Quantité de livres enchantés offerts.", "Number of enchanted books rewarded."),
    PARKOURMASTER_VAR_REWARD_ENCHANTED_BOOK_NAME("Récompense livres enchantés", "Reward Enchanted Books"),
    PARKOURMASTER_VAR_REWARD_ENDER_PEARL_DESC("Quantité de perles d'ender offertes.", "Number of ender pearls rewarded."),
    PARKOURMASTER_VAR_REWARD_ENDER_PEARL_NAME("Récompense perles d'ender", "Reward Ender Pearls"),
    PARKOURMASTER_VAR_REWARD_GOLDEN_APPLE_DESC("Quantité de pommes d'or offertes.", "Number of golden apples rewarded."),
    PARKOURMASTER_VAR_REWARD_GOLDEN_APPLE_NAME("Récompense pommes d'or", "Reward Golden Apples"),
    PARKOURMASTER_VAR_REWARD_IRON_INGOT_DESC("Quantité de lingots de fer offerts.", "Number of iron ingots rewarded."),
    PARKOURMASTER_VAR_REWARD_IRON_INGOT_NAME("Récompense lingots de fer", "Reward Iron Ingots"),
    PARKOURMASTER_VAR_SPAWN_INTERVAL_DESC("Intervalle (en secondes) entre l'apparition des parcours.", "Interval (in seconds) between course spawns."),
    PARKOURMASTER_VAR_SPAWN_INTERVAL_NAME("Intervalle de spawn", "Spawn Interval"),

    
    POTENTIALPERMANENT_VAR_KILL_REWARD_DESC("Quantité d'absorption convertie en vie permanente lorsqu'un joueur tue quelqu'un (en cœurs).", "Absorption converted to permanent health on kill (in hearts)."),
    POTENTIALPERMANENT_VAR_KILL_REWARD_NAME("Récompense de kill", "Kill Reward"),
    POTENTIALPERMANENT_VAR_MAX_ABSORPTION_HEALTH_DESC("Absorption maximale qu'un joueur peut avoir (en cœurs).", "Maximum absorption a player can have (in hearts)."),
    POTENTIALPERMANENT_VAR_MAX_ABSORPTION_HEALTH_NAME("Absorption max", "Max Absorption Health"),
    POTENTIALPERMANENT_VAR_MAX_PERMANENT_HEALTH_DESC("Vie permanente maximale qu'un joueur peut avoir (en cœurs).", "Maximum permanent health a player can have (in hearts)."),
    POTENTIALPERMANENT_VAR_MAX_PERMANENT_HEALTH_NAME("Vie permanente max", "Max Permanent Health"),
    POTENTIALPERMANENT_VAR_STARTING_ABSORPTION_HEALTH_DESC("Points d'absorption de départ pour chaque joueur (en cœurs).", "Starting absorption points for each player (in hearts)."),
    POTENTIALPERMANENT_VAR_STARTING_ABSORPTION_HEALTH_NAME("Absorption de départ", "Starting Absorption Health"),
    POTENTIALPERMANENT_VAR_STARTING_PERMANENT_HEALTH_DESC("Vie permanente de départ pour chaque joueur (en cœurs).", "Starting permanent health for each player (in hearts)."),
    POTENTIALPERMANENT_VAR_STARTING_PERMANENT_HEALTH_NAME("Vie permanente de départ", "Starting Permanent Health"),

    
    REWARDLONGSHOT_VAR_DAMAGE_MULTIPLIER_DESC("Multiplicateur de dégâts", "Damage multiplier"),
    REWARDLONGSHOT_VAR_DAMAGE_MULTIPLIER_NAME("Multiplicateur de dégâts", "Damage Multiplier"),
    REWARDLONGSHOT_VAR_HEAL_AMOUNT_DESC("Points de vie rendus au tireur (2 = 1 cœur)", "Health restored to the shooter (2 = 1 heart)"),
    REWARDLONGSHOT_VAR_HEAL_AMOUNT_NAME("Soin au tireur", "Heal Amount"),
    REWARDLONGSHOT_VAR_MIN_DISTANCE_DESC("Distance minimale pour un long shot", "Minimum distance for a long shot"),
    REWARDLONGSHOT_VAR_MIN_DISTANCE_NAME("Distance minimale", "Minimum Distance"),

    
    SAFEMINER_VAR_DISABLE_AT_PVP_DESC("Désactive SafeMiner automatiquement à l'activation du PvP.", "Automatically disables SafeMiner when PvP is enabled."),
    SAFEMINER_VAR_DISABLE_AT_PVP_NAME("Désactiver au PvP", "Disable At PvP"),
    SAFEMINER_VAR_MAX_HEIGHT_DESC("Hauteur maximale (Y) en dessous de laquelle les joueurs sont protégés.", "Maximum height (Y) below which players are protected."),
    SAFEMINER_VAR_MAX_HEIGHT_NAME("Hauteur max", "Max Height"),

    
    SIMONSAYS_VAR_CROUCH_DURATION_DESC("Durée (en secondes) pour S'ACCROUPIR", "Duration (in seconds) for CROUCHING"),
    SIMONSAYS_VAR_CROUCH_DURATION_NAME("Durée accroupissement", "Crouch Duration"),
    SIMONSAYS_VAR_JUMP_DURATION_DESC("Durée (en secondes) pour SAUTER", "Duration (in seconds) for JUMPING"),
    SIMONSAYS_VAR_JUMP_DURATION_NAME("Durée saut", "Jump Duration"),
    SIMONSAYS_VAR_MAX_DELAY_DESC("Temps maximum (en secondes) entre deux commandes", "Maximum time (in seconds) between two commands"),
    SIMONSAYS_VAR_MAX_DELAY_NAME("Délai max", "Max Delay"),
    SIMONSAYS_VAR_MIN_DELAY_DESC("Temps minimum (en secondes) entre deux commandes", "Minimum time (in seconds) between two commands"),
    SIMONSAYS_VAR_MIN_DELAY_NAME("Délai min", "Min Delay"),
    SIMONSAYS_VAR_MOVE_DURATION_DESC("Durée (en secondes) pour les déplacements", "Duration (in seconds) for movement"),
    SIMONSAYS_VAR_MOVE_DURATION_NAME("Durée déplacement", "Move Duration"),
    SIMONSAYS_VAR_PENALTY_DAMAGE_DESC("Activer la pénalité de dégâts", "Enable damage penalty"),
    SIMONSAYS_VAR_PENALTY_DAMAGE_NAME("Pénalité dégâts", "Penalty Damage"),
    SIMONSAYS_VAR_PENALTY_EFFECTS_DESC("Activer les pénalités d'effets", "Enable effect penalties"),
    SIMONSAYS_VAR_PENALTY_EFFECTS_NAME("Pénalité effets", "Penalty Effects"),
    SIMONSAYS_VAR_PENALTY_HUNGER_DESC("Activer la pénalité de faim", "Enable hunger penalty"),
    SIMONSAYS_VAR_PENALTY_HUNGER_NAME("Pénalité faim", "Penalty Hunger"),
    SIMONSAYS_VAR_STOP_DURATION_DESC("Durée (en secondes) pour ARRÊTER DE BOUGER", "Duration (in seconds) for STOP MOVING"),
    SIMONSAYS_VAR_STOP_DURATION_NAME("Durée immobilité", "Stop Duration"),

    
    TIMEBOMBE_VAR_EXPLOSION_DELAY_DESC("Temps en secondes avant l'explosion après la mort du joueur.", "Time in seconds before the explosion after player death."),
    TIMEBOMBE_VAR_EXPLOSION_DELAY_NAME("Délai d'explosion", "Explosion Delay"),
    TIMEBOMBE_VAR_EXPLOSION_POWER_DESC("Puissance de l'explosion créée par la TimeBombe.", "Explosion power created by the TimeBomb."),
    TIMEBOMBE_VAR_EXPLOSION_POWER_NAME("Puissance d'explosion", "Explosion Power"),
    TIMEBOMBE_VAR_GIVE_GOLDEN_APPLE_DESC("Donner une pomme d'or supplémentaire dans le coffre.", "Give an extra golden apple in the chest."),
    TIMEBOMBE_VAR_GIVE_GOLDEN_APPLE_NAME("Donner Golden Apple", "Give Golden Apple"),

    
    TRANSMUTATION_VAR_COAL_INPUT_DESC("Nombre de charbons nécessaires pour obtenir un lingot de fer.", "Coal required to get an iron ingot."),
    TRANSMUTATION_VAR_COAL_INPUT_NAME("Charbon requis", "Coal Input"),
    TRANSMUTATION_VAR_DIAMOND_OUTPUT_DESC("Nombre de diamants obtenus lors d'une transmutation or → diamant.", "Diamonds obtained from gold → diamond transmutation."),
    TRANSMUTATION_VAR_DIAMOND_OUTPUT_NAME("Diamants obtenus", "Diamond Output"),
    TRANSMUTATION_VAR_GOLD_INPUT_DESC("Nombre de lingots d'or nécessaires pour obtenir un diamant.", "Gold ingots required to get a diamond."),
    TRANSMUTATION_VAR_GOLD_INPUT_NAME("Or requis", "Gold Input"),
    TRANSMUTATION_VAR_GOLD_OUTPUT_DESC("Nombre de lingots d'or obtenus lors d'une transmutation fer → or.", "Gold ingots obtained from iron → gold transmutation."),
    TRANSMUTATION_VAR_GOLD_OUTPUT_NAME("Or obtenu", "Gold Output"),
    TRANSMUTATION_VAR_IRON_INPUT_DESC("Nombre de minerais de fer nécessaires pour obtenir un lingot d'or.", "Iron ore required to get a gold ingot."),
    TRANSMUTATION_VAR_IRON_INPUT_NAME("Fer requis", "Iron Input"),
    TRANSMUTATION_VAR_IRON_OUTPUT_DESC("Nombre de lingots de fer obtenus lors d'une transmutation charbon → fer.", "Iron ingots obtained from coal → iron transmutation."),
    TRANSMUTATION_VAR_IRON_OUTPUT_NAME("Fer obtenu", "Iron Output"),

    
    VAMPIRE_VAR_HEAL_AMOUNT_DESC("Définit la vie gagnée par kill", "Defines health gained per kill"),
    VAMPIRE_VAR_HEAL_AMOUNT_NAME("Vie par kill", "Health Per Kill"),
    VAMPIRE_VAR_INTERVAL_DESC("Définit l'intervalle entre les dégâts du soleil", "Defines the interval between sun damage ticks"),
    VAMPIRE_VAR_INTERVAL_NAME("Intervalle soleil", "Sun Damage Interval"),
    VAMPIRE_VAR_SUN_DAMAGE_DESC("Définit le nombre de dégâts du soleil", "Defines the amount of sun damage"),
    VAMPIRE_VAR_SUN_DAMAGE_NAME("Dégâts du soleil", "Sun Damage"),

    
    WEAKESTLINK_VAR_MULTIPLIER_DESC("Le multiplicateur de dégâts pour le maillon faible", "Damage multiplier for the weakest link"),
    WEAKESTLINK_VAR_MULTIPLIER_NAME("Multiplicateur", "Multiplier"),

    
    WEBCAGE_VAR_SIZE_DESC("Définit la taille de la cage de toile", "Defines the web cage size"),
    WEBCAGE_VAR_SIZE_NAME("Taille de la cage", "Cage Size"),

    
    XPSANSUE_VAR_DAMAGE_DESC("Dégâts infligés lorsque le joueur n'a plus de niveau d'expérience.", "Damage dealt when the player has no experience levels left."),
    XPSANSUE_VAR_DAMAGE_NAME("Dégâts", "Damage"),

    
    ABILITY_VAR_MAX_USE_NAME("Nombre d'utilisation", "Max Uses"),
    ABILITY_VAR_MAX_USE_DESC("Nombre d'utilisation de la capacité", "Number of uses of the abilities"),
    ABILITY_VAR_COOLDOWN_NAME("Cooldown", "Cooldown"),
    ABILITY_VAR_COOLDOWN_DESC("Définir le temps en seconde entre chaque utilisation", "Time in seconds between each use"),
    ABILITY_VAR_ACTIVE_NAME("Active", "Active"),
    ABILITY_VAR_ACTIVE_DESC("Active la capacité", "Enable the abilities"),
    
    BEATSANTA_TEAM_SIZE_NAME("Taille du team du Santa", "Santa Team Size"),
    BEATSANTA_TEAM_SIZE_DESC("Nombre de joueurs dans le team du Santa", "Number of players in the Santa team"),
    BEATSANTA_STRENGTH_NAME("Force du Santa", "Santa Strength"),
    BEATSANTA_STRENGTH_DESC("Permet d'activée/desactivé la force du Santa","Activate/Deactivate the Santa strength"),
    BEATSANTA_SPEED_NAME("Speed du Santa", "Santa Speed"),
    BEATSANTA_SPEED_DESC("Permet d'activé/desactivé la Speed du santa","Activate/Disable Santa Speed"),
    BEATSANTA_FIRE_RESISTANCE_NAME("Fire Resistance", "Fire Resistance"),
    BEATSANTA_FIRE_RESISTANCE_DESC("Permet d'activé/desactivé la Fire Resistance du Santa","Activate/Disable Santa Fire Resistance "),
    BEATSANTA_HASTE_NAME("Haste", "Haste"),
    BEATSANTA_HASTE_DESC("Permet d'activé/desactivé la Haste du Santa","Activate/Disable Santa Haste"),
    BEATSANTA_RESISTANCE_NAME("Resistance", "Resistance"),
    BEATSANTA_RESISTANCE_DESC("Permet d'avctivé/desactivé la resisatnce du Santa","Activate/Disable Santa Resistance"),
    
    FK_VAR_ZONE_SIZE_NAME("Taille de la zone", "Zone Size"),
    FK_VAR_ZONE_SIZE_DESC("Rayon de la zone protégée de chaque équipe (en blocs).", "Protected zone radius for each team (in blocks)."),
    FK_VAR_EP_TIMER_NAME("Durée d'un épisode", "Episode Duration"),
    FK_VAR_EP_TIMER_DESC("Durée d'un épisode en secondes (1200 = 20 min).", "Episode duration in seconds (1200 = 20 min)."),
    FK_VAR_ASSAUT_EP_NAME("Épisode d'assaut", "Assault Episode"),
    FK_VAR_ASSAUT_EP_DESC("Numéro de l'épisode à partir duquel l'assaut est lancé.", "Episode number at which the assault begins."),
    FK_VAR_NETHER_EP_NAME("Épisode Nether", "Nether Episode"),
    FK_VAR_NETHER_EP_DESC("Numéro de l'épisode à partir duquel le Nether est accessible.", "Episode number at which the Nether becomes accessible."),
    FK_VAR_END_EP_NAME("Épisode End", "End Episode"),
    FK_VAR_END_EP_DESC("Numéro de l'épisode à partir duquel l'End est accessible.", "Episode number at which the End becomes accessible."),
    
    GONEFISH_VAR_LUCK_LEVEL_NAME("Niveau Luck of the Sea", "Luck of the Sea Level"),
    GONEFISH_VAR_LUCK_LEVEL_DESC("Niveau de l'enchantement Luck of the Sea sur la canne.", "Luck of the Sea enchantment level on the rod."),
    GONEFISH_VAR_LURE_LEVEL_NAME("Niveau Lure", "Lure Level"),
    GONEFISH_VAR_LURE_LEVEL_DESC("Niveau de l'enchantement Lure sur la canne.", "Lure enchantment level on the rod."),
    GONEFISH_VAR_ANVIL_AMOUNT_NAME("Nombre d'enclumes", "Anvil Amount"),
    GONEFISH_VAR_ANVIL_AMOUNT_DESC("Nombre d'enclumes données au début de la partie.", "Number of anvils given at the start of the game."),

    
    KING_VAR_KING_MAX_HEALTH_NAME("Vie du Roi", "King Max Health"),
    KING_VAR_KING_MAX_HEALTH_DESC("Points de vie maximum du Roi (20 = 10 cœurs).", "King's maximum health points (20 = 10 hearts)."),

    KING_VAR_SPEED_ENABLED_NAME("Speed activé", "Speed Enabled"),
    KING_VAR_SPEED_ENABLED_DESC("Active l'effet Speed permanent sur le Roi.", "Enable permanent Speed effect on the King."),
    KING_VAR_SPEED_LEVEL_NAME("Niveau de Speed", "Speed Level"),
    KING_VAR_SPEED_LEVEL_DESC("Niveau de l'effet Speed du Roi (0 = désactivé).", "King's Speed effect level (0 = disabled)."),

    KING_VAR_STRENGTH_ENABLED_NAME("Force activée", "Strength Enabled"),
    KING_VAR_STRENGTH_ENABLED_DESC("Active l'effet Force permanent sur le Roi.", "Enable permanent Strength effect on the King."),
    KING_VAR_STRENGTH_LEVEL_NAME("Niveau de Force", "Strength Level"),
    KING_VAR_STRENGTH_LEVEL_DESC("Niveau de l'effet Force du Roi (0 = désactivé).", "King's Strength effect level (0 = disabled)."),

    KING_VAR_RESISTANCE_ENABLED_NAME("Résistance activée", "Resistance Enabled"),
    KING_VAR_RESISTANCE_ENABLED_DESC("Active l'effet Résistance permanent sur le Roi.", "Enable permanent Resistance effect on the King."),
    KING_VAR_RESISTANCE_LEVEL_NAME("Niveau de Résistance", "Resistance Level"),
    KING_VAR_RESISTANCE_LEVEL_DESC("Niveau de l'effet Résistance du Roi (0 = désactivé).", "King's Resistance effect level (0 = disabled)."),

    KING_VAR_POISON_ENABLED_NAME("Poison activé", "Poison Enabled"),
    KING_VAR_POISON_ENABLED_DESC("Active le poison sur l'équipe à la mort du Roi.", "Enable poison on the team when the King dies."),
    KING_VAR_POISON_DURATION_NAME("Durée du Poison", "Poison Duration"),
    KING_VAR_POISON_DURATION_DESC("Durée du poison en secondes (0 = désactivé).", "Poison duration in seconds (0 = disabled)."),
    KING_VAR_POISON_LEVEL_NAME("Niveau du Poison", "Poison Level"),
    KING_VAR_POISON_LEVEL_DESC("Niveau du poison (0 = désactivé).", "Poison level (0 = disabled)."),


    LEGEND_VAR_MIN_TEAM_SIZE_NAME("Taille min. d'équipe", "Min Team Size"),
    LEGEND_VAR_MIN_TEAM_SIZE_DESC("Taille minimum des équipes pour ce scénario.", "Minimum team size for this scenario."),
    ARCHER_BOW_NAME("Arc amélioré", "Enhanced Bow"),

    ASSASSIN_EXTRA_HEARTS_NAME("Cœurs bonus Assassin", "Assassin Extra Hearts"),
    ASSASSIN_EXTRA_HEARTS_DESC("Cœurs supplémentaires (demi-cœurs).", "Extra hearts (half-hearts)."),
    ASSASSIN_FORCE_LEVEL_NAME("Niveau Force Assassin", "Assassin Force Level"),
    ASSASSIN_FORCE_LEVEL_DESC("Niveau de Force passive.", "Passive Strength level."),


    TANK_EXTRA_HEARTS_NAME("Cœurs bonus Tank", "Tank Extra Hearts"),
    TANK_EXTRA_HEARTS_DESC("Cœurs supplémentaires (demi-cœurs).", "Extra hearts (half-hearts)."),
    TANK_RESIST_THRESHOLD_NAME("Seuil vie Tank", "Tank HP Threshold"),
    TANK_RESIST_THRESHOLD_DESC("Vie sous laquelle Résistance s'active.", "HP below which Resistance activates."),
    TANK_RESIST_LEVEL_NAME("Niveau Résistance Tank", "Tank Resistance Level"),
    TANK_RESIST_LEVEL_DESC("Niveau de Résistance.", "Resistance level."),

    NAIN_ARMOR_DURATION_NAME("Durée armure Nain (s)", "Nain Armor Duration (s)"),
    NAIN_ARMOR_DURATION_DESC("Durée de l'armure temporaire.", "Temporary armor duration."),
    NAIN_ARMOR_PROT_LEVEL_NAME("Niveau Protection Nain", "Nain Protection Level"),
    NAIN_ARMOR_PROT_LEVEL_DESC("Niveau Protection de l'armure.", "Armor Protection level."),

    ARCHER_BOW_POWER_NAME("Power arc Archer", "Archer Bow Power"),
    ARCHER_BOW_POWER_DESC("Niveau Power de l'arc.", "Bow Power level."),
    ARCHER_BOW_BONUS_NAME("Bonus dégâts arc", "Bow Bonus Damage"),
    ARCHER_BOW_BONUS_DESC("Dégâts bonus sur flèche.", "Bonus arrow damage."),
    ARCHER_SLOW_CHANCE_NAME("Chance Slowness arc", "Arrow Slow Chance"),
    ARCHER_SLOW_CHANCE_DESC("Chance d'appliquer Slowness.", "Chance to apply Slowness."),
    ARCHER_SLOW_DURATION_NAME("Durée Slowness arc (s)", "Arrow Slow Duration (s)"),
    ARCHER_SLOW_DURATION_DESC("Durée du Slowness.", "Slowness duration."),

    ZEUS_LIGHTNING_CHANCE_NAME("Chance éclair Zeus", "Zeus Lightning Chance"),
    ZEUS_LIGHTNING_CHANCE_DESC("Chance d'éclair au hit.", "Lightning chance on hit."),
    ZEUS_SPEED_CHANCE_NAME("Chance Speed Zeus", "Zeus Speed Chance"),
    ZEUS_SPEED_CHANCE_DESC("Chance de Speed I 10s.", "Speed I 10s chance."),
    ZEUS_EFFECT_DURATION_NAME("Durée effets Zeus (s)", "Zeus Effect Duration (s)"),
    ZEUS_EFFECT_DURATION_DESC("Durée des effets aléatoires.", "Random effects duration."),

    NECRO_SKELETON_COUNT_NAME("Nb squelettes", "Skeleton Count"),
    NECRO_SKELETON_COUNT_DESC("Nombre de squelettes invoqués.", "Summoned skeletons count."),
    NECRO_ZOMBIE_COUNT_NAME("Nb zombies", "Zombie Count"),
    NECRO_ZOMBIE_COUNT_DESC("Nombre de baby zombies.", "Baby zombies count."),
    NECRO_SEARCH_RADIUS_NAME("Rayon recherche ennemi", "Enemy Search Radius"),
    NECRO_SEARCH_RADIUS_DESC("Rayon de recherche.", "Search radius."),

    SUCCUBE_LIFESTEAL_NAME("Vol de vie Succube", "Succube Lifesteal"),
    SUCCUBE_LIFESTEAL_DESC("PV volés au hit melee.", "HP stolen on melee hit."),
    SUCCUBE_ABSORB_RADIUS_NAME("Rayon Absorption", "Absorption Radius"),
    SUCCUBE_ABSORB_RADIUS_DESC("Rayon du buff Absorption.", "Absorption buff radius."),
    SUCCUBE_ABSORB_LEVEL_NAME("Niveau Absorption", "Absorption Level"),
    SUCCUBE_ABSORB_LEVEL_DESC("Niveau d'Absorption.", "Absorption level."),
    SUCCUBE_ABSORB_DURATION_NAME("Durée Absorption (s)", "Absorption Duration (s)"),
    SUCCUBE_ABSORB_DURATION_DESC("Durée de l'Absorption.", "Absorption duration."),

    SOLDAT_BONUS_DAMAGE_NAME("Bonus dégâts Soldat", "Soldier Bonus Damage"),
    SOLDAT_BONUS_DAMAGE_DESC("Dégâts bonus avec épée (melee).", "Bonus sword damage (melee)."),

    PRINCESSE_EXTRA_HEARTS_NAME("Cœurs bonus Princesse", "Princess Extra Hearts"),
    PRINCESSE_EXTRA_HEARTS_DESC("Cœurs supplémentaires.", "Extra hearts."),

    CAVALIER_HORSE_HP_NAME("PV cheval Cavalier", "Cavalier Horse HP"),
    CAVALIER_HORSE_HP_DESC("Points de vie du cheval.", "Horse HP."),

    DRAGON_EXTRA_HEARTS_NAME("Cœurs bonus Dragon", "Dragon Extra Hearts"),
    DRAGON_EXTRA_HEARTS_DESC("Cœurs supplémentaires.", "Extra hearts."),
    DRAGON_FIRE_CHANCE_NAME("Chance feu Dragon", "Dragon Fire Chance"),
    DRAGON_FIRE_CHANCE_DESC("Chance d'enflammer au melee.", "Ignite chance on melee."),
    DRAGON_FIRE_DURATION_NAME("Durée feu Dragon (s)", "Dragon Fire Duration (s)"),
    DRAGON_FIRE_DURATION_DESC("Durée du feu.", "Fire duration."),
    DRAGON_FIREBALL_YIELD_NAME("Puissance boule de feu", "Fireball Yield"),
    DRAGON_FIREBALL_YIELD_DESC("Puissance explosion.", "Explosion power."),

    MEDECIN_HEAL_RADIUS_NAME("Rayon soin Médecin", "Medic Heal Radius"),
    MEDECIN_HEAL_RADIUS_DESC("Rayon zone de soin.", "Healing zone radius."),
    MEDECIN_HEAL_LEVEL_NAME("Niveau Régénération", "Regeneration Level"),
    MEDECIN_HEAL_LEVEL_DESC("Niveau Régénération alliés.", "Ally Regeneration level."),
    // ── Prisonnier ──
    PRISONNIER_SPEED_LEVEL_NAME("Niveau Speed Prisonnier", "Prisoner Speed Level"),
    PRISONNIER_SPEED_LEVEL_DESC("Niveau Speed permanente.", "Permanent Speed level."),
    PRISONNIER_CHAIN_RADIUS_NAME("Rayon chaînes", "Chain Radius"),
    PRISONNIER_CHAIN_RADIUS_DESC("Rayon recherche chaînes.", "Chain search radius."),
    PRISONNIER_CHAIN_DURATION_NAME("Durée chaînes (s)", "Chain Duration (s)"),
    PRISONNIER_CHAIN_DURATION_DESC("Durée des chaînes.", "Chain duration."),
    // ── Ogre ──
    OGRE_EXTRA_HEARTS_NAME("Cœurs bonus Ogre", "Ogre Extra Hearts"),
    OGRE_EXTRA_HEARTS_DESC("Cœurs supplémentaires.", "Extra hearts."),
    OGRE_START_GAPPLES_NAME("Gapples début Ogre", "Ogre Starting Gapples"),
    OGRE_START_GAPPLES_DESC("Pommes dorées de départ.", "Starting golden apples."),
    OGRE_MIN_APPLES_NAME("Seuil min gapples", "Min Gapples Threshold"),
    OGRE_MIN_APPLES_DESC("En dessous = malus.", "Below = debuff."),
    OGRE_EFFECT_DURATION_NAME("Durée effet gapple (s)", "Gapple Effect Duration (s)"),
    OGRE_EFFECT_DURATION_DESC("Durée effet aléatoire.", "Random effect duration."),
    // ── Corne ──
    CORNE_FEU_DURATION_NAME("Durée Mélodie Feu (s)", "Fire Melody Duration (s)"),
    CORNE_FEU_DURATION_DESC("Durée Fire Resistance.", "Fire Resistance duration."),
    CORNE_METAL_DURATION_NAME("Durée Mélodie Métal (s)", "Metal Melody Duration (s)"),
    CORNE_METAL_DURATION_DESC("Durée Résistance.", "Resistance duration."),
    CORNE_AIR_DURATION_NAME("Durée Mélodie Air (s)", "Air Melody Duration (s)"),
    CORNE_AIR_DURATION_DESC("Durée Speed.", "Speed duration."),
    // ── Paladin ──
    PALADIN_EXTRA_HEARTS_NAME("Cœurs bonus Paladin", "Paladin Extra Hearts"),
    PALADIN_EXTRA_HEARTS_DESC("Cœurs supplémentaires.", "Extra hearts."),
    PALADIN_BLESSING_DURATION_NAME("Durée Bénédiction (s)", "Blessing Duration (s)"),
    PALADIN_BLESSING_DURATION_DESC("Durée Bénédiction.", "Blessing duration."),
    PALADIN_LOW_HP_THRESHOLD_NAME("Seuil vie Paladin", "Paladin HP Threshold"),
    PALADIN_LOW_HP_THRESHOLD_DESC("Vie sous laquelle Résistance s'active.", "HP below which Resistance activates."),
    PALADIN_ALLY_RADIUS_NAME("Rayon alliés Paladin", "Paladin Ally Radius"),
    PALADIN_ALLY_RADIUS_DESC("Rayon pour Force I.", "Radius for Strength I."),
    // ── Marionnettiste ──
    MARIONNETTISTE_PUPPET_RANGE_NAME("Portée marionnettes", "Puppet Range"),
    MARIONNETTISTE_PUPPET_RANGE_DESC("Distance max avant Poison.", "Max distance before Poison."),
    // ── Mage ──
    MAGE_REGEN_INTERVAL_NAME("Intervalle potions Mage (s)", "Mage Potion Interval (s)"),
    MAGE_REGEN_INTERVAL_DESC("Intervalle entre régénérations.", "Interval between regenerations."),
    CAVALIER_HORSE_NAME("Cheval", "Horse"),
    ASSASSIN_FORCE_NAME("Force", "Force"),
    CORNE_WEAKNESS_NAME("Faiblesse", "Weakness"),
    CORNE_MELODIE_FEU_NAME("Mélodie du Feu", "Fire Melody"),
    CORNE_MELODIE_HEAL_NAME("Mélodie des Soins", "Heal Melody"),
    CORNE_MELODIE_METAL_NAME("Mélodie du Métal", "Metal Melody"),
    CORNE_MELODIE_AIR_NAME("Mélodie de l'Air", "Air Melody"),
    DRAGON_FIRE_NAME("Feu Passif", "Fire Passive"),
    DRAGON_FIREBALL_NAME("Boule de Feu", "Fireball"),
    MAGE_POTION_NAME("Potion", "Potion"),
    MARIONNETTISTE_PUPPET_NAME("Marionnette", "Puppet"),
    MEDECIN_HEAL_NAME("Soin", "Heal"),
    NAIN_ARMOR_NAME("Armure", "Armor"),
    NECROMANCIEN_SUMMON_NAME("Invocation", "Summon"),
    OGRE_PASSIVE_NAME("Force Brute", "Brute Force"),
    PALADIN_LOWHEALTH_NAME("Bas Niveau de Vie", "Low Health"),
    PALADIN_BLESSING_NAME("Bénédiction", "Blessing"),
    PRINCESSE_NOFALL_NAME("Grâce Royale", "Royal Grace"),
    PRISONNIER_SPEED_NAME("Vitesse", "Speed"),
    PRISONNIER_CHAIN_NAME("Chaîne", "Chain"),
    SOLDAT_EQUIPMENT_NAME("Équipement", "Equipment"),
    SUCCUBE_ABSORPTION_NAME("Absorption", "Absorption"),
    TANK_RESISTANCE_NAME("Résistance", "Resistance"),
    ZEUS_LIGHTNING_NAME("Éclair", "Lightning"),
    ZEUS_EFFECTS_NAME("Effets", "Effects"),

    MYSTERY_TEAM_GIVE_BANNER_NAME(
            "§eDonner la bannière au start",
            "§eGive banner on start"
    ),

    MYSTERY_TEAM_GIVE_BANNER_DESC(
            "§7Donne automatiquement la bannière d'équipe au début.",
            "§7Automatically give the team banner at start."
    ),

    MYSTERY_TEAM_RESET_TEAM_NAME(
            "§eReset team au scatter",
            "§eReset team on scatter"
    ),

    MYSTERY_TEAM_RESET_TEAM_DESC(
            "§7Supprime l'équipe du joueur avant l'assignation aléatoire.",
            "§7Remove player team before random assignment."
    ),

    RANDOMCRAFT_GIVE_STARTER_NAME(
            "§eDonner le kit de départ",
            "§eGive starter kit"
    ),

    RANDOMCRAFT_GIVE_STARTER_DESC(
            "§7Donne établi, four et outils en début de partie.",
            "§7Give crafting table, furnace and tools at start."
    ),

    RANDOMCRAFT_ALLOW_RARE_NAME(
            "§eAutoriser objets rares",
            "§eAllow rare items"
    ),

    RANDOMCRAFT_ALLOW_RARE_DESC(
            "§7Permet les items rares comme enchanted book.",
            "§7Allow rare items like enchanted book."
    ),

    RANDOMCRAFT_MAX_INGOT_NAME(
            "§eQuantité max lingots",
            "§eMax ingot amount"
    ),

    RANDOMCRAFT_MAX_INGOT_DESC(
            "§7Nombre donné si lingot généré.",
            "§7Amount given if ingot generated."
    ),

    RANDOMCRAFT_GAPPLE_AMOUNT_NAME(
            "§eQuantité golden apple",
            "§eGolden apple amount"
    ),

    RANDOMCRAFT_GAPPLE_AMOUNT_DESC(
            "§7Nombre de golden apple si générée.",
            "§7Amount of golden apples if generated."
    ),

    RANDOMCRAFT_MAX_ENCHANT_NAME(
            "§eNiveau max enchant",
            "§eMax enchant level"
    ),

    RANDOMCRAFT_MAX_ENCHANT_DESC(
            "§7Niveau maximum pour les livres enchantés.",
            "§7Maximum level for enchanted books."
    ),

    RANDOMCRAFT_ALLOW_DUPLICATE_NAME(
            "§eAutoriser doublons",
            "§eAllow duplicates"
    ),

    RANDOMCRAFT_ALLOW_DUPLICATE_DESC(
            "§7Permet qu'un même item soit généré plusieurs fois.",
            "§7Allow the same item to be generated multiple times."
    ),

    RANDOMDROP_VAR_GIVE_STARTER_NAME(
            "§eDonner le kit de départ",
            "§eGive starter kit"
    ),

    RANDOMDROP_VAR_GIVE_STARTER_DESC(
            "§7Donne établi et four au début.",
            "§7Give crafting table and furnace at start."
    ),

    RANDOMDROP_VAR_ALLOW_RARE_NAME(
            "§eAutoriser objets rares",
            "§eAllow rare items"
    ),

    RANDOMDROP_VAR_ALLOW_RARE_DESC(
            "§7Permet les objets rares dans les drops.",
            "§7Allow rare items in drops."
    ),

    RANDOMDROP_VAR_MAX_INGOT_NAME(
            "§eQuantité max lingots",
            "§eMax ingot amount"
    ),

    RANDOMDROP_VAR_MAX_INGOT_DESC(
            "§7Nombre donné si lingot généré.",
            "§7Amount given if ingot generated."
    ),

    RANDOMDROP_VAR_GAPPLE_AMOUNT_NAME(
            "§eQuantité golden apple",
            "§eGolden apple amount"
    ),

    RANDOMDROP_VAR_GAPPLE_AMOUNT_DESC(
            "§7Nombre de golden apple si générée.",
            "§7Amount of golden apples if generated."
    ),

    RANDOMDROP_VAR_MAX_ENCHANT_NAME(
            "§eNiveau max enchant",
            "§eMax enchant level"
    ),

    RANDOMDROP_VAR_MAX_ENCHANT_DESC(
            "§7Niveau maximum pour les livres enchantés.",
            "§7Maximum level for enchanted books."
    ),

    RANDOMDROP_VAR_ALLOW_DUPLICATE_NAME(
            "§eAutoriser doublons",
            "§eAllow duplicates"
    ),

    RANDOMDROP_VAR_ALLOW_DUPLICATE_DESC(
            "§7Permet qu'un même item soit généré plusieurs fois.",
            "§7Allow the same item to be generated multiple times."
    ),

    SKYHIGH_VAR_FIRST_LEVEL_NAME("§ePremière couche","§eFirst layer"),
    SKYHIGH_VAR_FIRST_LEVEL_DESC("§7Altitude en dessous de laquelle le joueur prend des dégâts (première couche)","§7Altitude under which player takes damage (first layer)"),
    SKYHIGH_VAR_SECOND_LEVEL_NAME("§eDeuxième couche","§eSecond layer"),
    SKYHIGH_VAR_SECOND_LEVEL_DESC("§7Altitude en dessous de laquelle le joueur prend des dégâts (deuxième couche)","§7Altitude under which player takes damage (second layer)"),
    SKYHIGH_VAR_THIRD_LEVEL_NAME("§eTroisième couche","§eThird layer"),
    SKYHIGH_VAR_THIRD_LEVEL_DESC("§7Altitude en dessous de laquelle le joueur prend des dégâts (troisième couche)","§7Altitude under which player takes damage (third layer)"),
    SKYHIGH_VAR_FIRST_DAMAGE_NAME("§eDégâts première couche","§eFirst layer damage"),
    SKYHIGH_VAR_FIRST_DAMAGE_DESC("§7Dégâts infligés en dessous de la première couche","§7Damage dealt below first layer"),
    SKYHIGH_VAR_SECOND_DAMAGE_NAME("§eDégâts deuxième couche","§eSecond layer damage"),
    SKYHIGH_VAR_SECOND_DAMAGE_DESC("§7Dégâts infligés en dessous de la deuxième couche","§7Damage dealt below second layer"),
    SKYHIGH_VAR_THIRD_DAMAGE_NAME("§eDégâts troisième couche","§eThird layer damage"),
    SKYHIGH_VAR_THIRD_DAMAGE_DESC("§7Dégâts infligés en dessous de la troisième couche","§7Damage dealt below third layer"),

    SUPERHEROS_VAR_SPEED_NAME(
            "Vitesse",
            "Speed"
    ),
    SUPERHEROS_VAR_SPEED_DESC(
            "Active la potion de vitesse.",
            "Enable speed potion."
    ),
    SUPERHEROS_VAR_DAMAGE_NAME(
            "Force",
            "Strength"
    ),
    SUPERHEROS_VAR_DAMAGE_DESC(
            "Active la potion de force.",
            "Enable strength potion."
    ),
    SUPERHEROS_VAR_RESISTANCE_NAME(
            "Résistance",
            "Resistance"
    ),
    SUPERHEROS_VAR_RESISTANCE_DESC(
            "Active la potion de résistance.",
            "Enable resistance potion."
    ),
    SUPERHEROS_VAR_JUMP_NAME(
            "Saut",
            "Jump"
    ),
    SUPERHEROS_VAR_JUMP_DESC(
            "Active la potion de saut.",
            "Enable jump potion."
    ),
    SUPERHEROS_VAR_EXTRA_HEALTH_NAME(
            "Santé supplémentaire",
            "Extra health"
    ),
    SUPERHEROS_VAR_EXTRA_HEALTH_DESC(
            "Ajoute de la santé maximale pour le héros spécial.",
            "Adds extra max health for the special hero."
    ),
    SUPERHEROS_VAR_SPEED_AMPLIFIER_NAME(
            "Amplificateur vitesse",
            "Speed amplifier"
    ),
    SUPERHEROS_VAR_SPEED_AMPLIFIER_DESC(
            "Définit la puissance de la potion de vitesse.",
            "Defines the power of the speed potion."
    ),
    SUPERHEROS_VAR_DAMAGE_AMPLIFIER_NAME(
            "Amplificateur force",
            "Strength amplifier"
    ),
    SUPERHEROS_VAR_DAMAGE_AMPLIFIER_DESC(
            "Définit la puissance de la potion de force.",
            "Defines the power of the strength potion."
    ),
    SUPERHEROS_VAR_RESISTANCE_AMPLIFIER_NAME(
            "Amplificateur résistance",
            "Resistance amplifier"
    ),
    SUPERHEROS_VAR_RESISTANCE_AMPLIFIER_DESC(
            "Définit la puissance de la potion de résistance.",
            "Defines the power of the resistance potion."
    ),
    SUPERHEROS_VAR_JUMP_AMPLIFIER_NAME(
            "Amplificateur saut",
            "Jump amplifier"
    ),
    SUPERHEROS_VAR_JUMP_AMPLIFIER_DESC(
            "Définit la puissance de la potion de saut.",
            "Defines the power of the jump potion."
    ),
    SUPERHEROS_VAR_FIRE_RESISTANCE_AMPLIFIER_NAME(
            "Amplificateur résistance feu",
            "Fire resistance amplifier"
    ),
    SUPERHEROS_VAR_FIRE_RESISTANCE_AMPLIFIER_DESC(
            "Définit la puissance de la potion de résistance au feu.",
            "Defines the power of the fire resistance potion."
    ),

    SUPERHEROS_VAR_ABSORPTION_HEALTH_NAME("Absorption extra", "Extra Absorption"),
    SUPERHEROS_VAR_ABSORPTION_HEALTH_DESC("Quantité d’absorption supplémentaire sur golden apple.", "Extra absorption amount on golden apple."),
    SUPERHEROS_VAR_REGENERATION_HEALTH_NAME("Régénération extra", "Extra Regeneration"),
    SUPERHEROS_VAR_REGENERATION_HEALTH_DESC("Quantité de régénération supplémentaire sur golden apple.", "Extra regeneration amount on golden apple."),
    SUPERHEROS_VAR_REGENERATION_DURATION_DESC("Durée de la régénération en secondes.", "Duration of regeneration in seconds."),
    SUPERHEROS_VAR_ABSORPTION_DURATION_NAME("Durée absorption", "Absorption duration"),
    SUPERHEROS_VAR_ABSORPTION_DURATION_DESC("Durée de l’absorption en secondes.", "Duration of absorption in seconds."),

    NETHERIBUS_VAR_DAMAGE_NAME("Dégâts hors Nether", "Damage outside Nether"),
    NETHERIBUS_VAR_DAMAGE_DESC("Dégâts infligés aux joueurs hors du Nether après le timer.", "Damage dealt to players outside Nether after timer."),
    NETHERIBUS_VAR_TIMER_NAME("Timer de début", "Start timer"),
    NETHERIBUS_VAR_TIMER_DESC("Tempsavant que les joueurs hors du Nether commencent à recevoir des dégâts.", "Time before players outside Nether start taking damage.");
    ;



    private final Map<String, String> translations;

    ScenarioVarLang(String fr, String en) {
        this.translations = Map.of("fr_FR", fr, "en_US", en);
    }

    @Override
    public String getKey() { return "scenariovar." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}