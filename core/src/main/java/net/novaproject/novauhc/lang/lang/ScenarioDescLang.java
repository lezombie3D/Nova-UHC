package net.novaproject.novauhc.lang.lang;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;


public enum ScenarioDescLang implements Lang {

    
    ACID_RAIN(
        "Pluie acide qui fait des dégâts. Protégez-vous sous des blocs !",
        "Acid rain that deals damage. Take shelter under blocks!"
    ),
    ARROW_SWITCH(
        "Les joueurs touchés par une flèche échangent leur position avec le tireur.",
        "Players hit by an arrow swap positions with the shooter."
    ),
    AUTO_REVIVE(
        "Les joueurs ressuscitent automatiquement une fois par partie.",
        "Players automatically revive once per game."
    ),

    
    BAT_ROULETTE(
        "Tuer une chauve-souris donne un effet aléatoire (positif ou négatif).",
        "Killing a bat gives a random effect (positive or negative)."
    ),
    BEST_PVE(
        "Récompense les joueurs qui excellent dans le PvE avec des bonus.",
        "Rewards players who excel in PvE with bonuses."
    ),
    BETA_ZOMBIE(
        "Fait en sorte que les zombies drop des plumes au lieu de chair pourrie.",
        "Makes zombies drop feathers instead of rotten flesh."
    ),
    BLACK_ARROW(
        "Les minerais de charbon donnent des flèches au lieu de charbon.",
        "Coal ores drop arrows instead of coal."
    ),
    BEATSANTA("%santa_size% Pere Noël contre %lutin_size% Lutin !",
            "%santa_size% Santa Claus against %lutin_size% Drawf"),
    BLIZZARD(
        "Tempêtes de neige qui ralentissent et aveuglent. Restez près du feu !",
        "Snowstorms that slow and blind. Stay near fire!"
    ),
    BLOCK_RUSH(
        "Miner un type de bloc pour la première fois donne %reward% lingot(s) d'or.",
        "Mining a block type for the first time gives %reward% gold ingot(s)."
    ),
    BLOOD_CYCLE(
        "Un type de minerai change périodiquement et inflige des dégâts si miné.",
        "An ore type changes periodically and deals damage if mined."
    ),
    BLOOD_DIAMONDS(
        "Les joueurs perdent 1 cœur en minant un diamant.",
        "Players lose 1 heart when mining a diamond."
    ),
    BLOOD_LUST(
        "Chaque kill donne Speed %speed_level% et Strength %strength_level% pendant %duration% secondes.",
        "Each kill gives Speed %speed_level% and Strength %strength_level% for %duration% seconds."
    ),
    BUFF_KILLER(
        "Tuer un joueur donne des effets de potion bénéfiques au tueur.",
        "Killing a player gives beneficial potion effects to the killer."
    ),

    
    CAT_EYE(
        "Donne une vision nocturne permanente à tous les joueurs.",
        "Gives permanent night vision to all players."
    ),
    CHAT_PVP(
        "À l'arrivée du PVP, le chat est désactivé.",
        "When PvP starts, chat is disabled."
    ),
    COMPENSATION(
        "À la mort d'un joueur, tous les membres de son équipe gagnent %hearts% cœurs.",
        "On a player's death, all team members gain %hearts% hearts."
    ),
    CRIPPLE(
        "Rend tous les joueurs faibles pendant %duration% secondes.",
        "Makes all players weak for %duration% seconds."
    ),
    CUTCLEAN(
        "Les minerais et les drops d'animaux sont automatiquement fondus, aucun four nécessaire.",
        "Ores and animal drops are automatically smelted, no furnace needed."
    ),

    
    DEATH_EMERAULD(
        "Miner une %block% inflige %damage% dégâts à un joueur aléatoire.",
        "Mining a %block% deals %damage% damage to a random player."
    ),
    DEMOCRACY(
        "Votez pour éliminer un joueur toutes les %minutes% minutes !",
        "Vote to eliminate a player every %minutes% minutes!"
    ),
    DOUBLE_ORE(
        "Double la quantité de minerais obtenus lors du minage, configurable pour chaque minerai.",
        "Doubles the amount of ores obtained when mining, configurable for each ore."
    ),

    
    FALLOUT(
        "Après un certain temps, rester au-dessus de Y=%level% inflige des dégâts de radiation aux joueurs.",
        "After some time, staying above Y=%level% deals radiation damage to players."
    ),
    FAST_FURNACE(
        "Les fours cuisent et fondent 3 fois plus rapidement.",
        "Furnaces cook and smelt 3 times faster."
    ),
    FAST_MINER(
        "Augmente la vitesse de minage de tous les joueurs.",
        "Increases the mining speed of all players."
    ),
    FINAL_HEAL(
        "Heal tous les joueurs à des moments précis avant le PVP.",
        "Heals all players at specific times before PvP."
    ),
    FIRE_LESS(
        "Désactive les dégâts de feu et de lave.",
        "Disables fire and lava damage."
    ),
    FK(
            "Mode de jeu par équipes avec des bases à défendre et des objectifs de capture.",
            "Team-based game mode with bases to defend and capture objectives."
    ),
    
    GAP_ROULETTE(
        "Les pommes dorées donnent des effets aléatoires configurables au lieu de leurs effets normaux.",
        "Golden apples give configurable random effects instead of their normal effects."
    ),
    GENIE(
        "3 souhaits par partie ! Les options dépendent du nombre de kills.",
        "3 wishes per game! Options depend on the number of kills."
    ),
    GLADIATOR(
        "Les combats 1v1 se déroulent dans une arène fermée qui apparaît automatiquement.",
        "1v1 fights take place in a closed arena that appears automatically."
    ),
    GOLDEN_DROP(
        "Les joueurs morts drop automatiquement une pomme dorée.",
        "Dead players automatically drop a golden apple."
    ),
    GOLDEN_HEAD(
        "Les têtes de joueurs morts peuvent être craftées en pommes dorées spéciales.",
        "Dead players' heads can be crafted into special golden apples."
    ),
    GONE_FISH(
            "Scénario centré sur la pêche avec des récompenses spéciales.",
            "Fishing-focused scenario with special rewards."
    ),
    
    HASTEY_ALPHA(
        "Version extrême de HasteyBoy - Efficacité VII et Solidité V sur tous les outils.",
        "Extreme version of HasteyBoy - Efficiency VII and Unbreaking V on all tools."
    ),
    HASTEY_BABIE(
        "Version réduite de HasteyBoy - seulement Efficacité I sur les outils.",
        "Reduced version of HasteyBoy - only Efficiency I on tools."
    ),
    HASTEY_BOY(
        "Tous les outils craftés reçoivent automatiquement Efficacité III et Solidité I.",
        "All crafted tools automatically receive Efficiency III and Unbreaking I."
    ),
    HEATH_CHARITY(
        "Partage automatiquement la vie entre les membres d'une équipe.",
        "Automatically shares health between team members."
    ),

    
    INFINITE_ENCHANTER(
        "Les tables d'enchantement ne consomment pas de lapis-lazuli.",
        "Enchanting tables don't consume lapis lazuli."
    ),
    INVENTORS(
        "Le premier à crafter un objet est annoncé et reçoit un bonus !",
        "The first to craft an item is announced and receives a bonus!"
    ),

    

    KING(
            "Un joueur devient roi et doit être protégé par son équipe.",
            "A player becomes the King and must be protected by their team."
    ),

    LEGEND(
            "Scénario à 18 classes de légendes aux capacités uniques. Choisissez votre classe ou elle sera assignée aléatoirement.",
            "Scenario with 18 unique legend classes. Choose your class or it will be randomly assigned."
    ),
    LONG_SHOOT(
        "Les tirs à l'arc de plus de %distance% blocs infligent x%multiplier% de dégâts.",
        "Bow shots over %distance% blocks deal x%multiplier% damage."
    ),
    LOOT_CRATE(
        "Coffres de loot distribués toutes les %minutes% minutes avec des objets configurables !",
        "Loot crates distributed every %minutes% minutes with configurable items!"
    ),
    LUCKY_ORE(
        "%chance%% de chance d'obtenir un objet légendaire en minant des minerais.",
        "%chance%% chance to get a legendary item while mining ores."
    ),

    
    MAGNET(
        "Les minerais dans un rayon de %radius% blocs viennent automatiquement à vous.",
        "Ores within a %radius% block radius automatically come to you."
    ),
    MEETUP(
        "Donne à tous les joueurs le même équipement.",
        "Gives all players the same equipment."
    ),

    MYSTERY_TEAM(
            "§7Des équipes mystérieuses sont formées, découvrez vos coéquipiers !",
            "§7Mysterious teams are formed, discover your teammates!"
    ),

    NINE_SLOT(
        "Seule la hotbar peut être utilisée. L'inventaire principal est inaccessible !",
        "Only the hotbar can be used. The main inventory is inaccessible!"
    ),
    NINJA(
        "Devenez invisible pendant %seconds% secondes après chaque kill !",
        "Become invisible for %seconds% seconds after each kill!"
    ),
    NO_CLEAN_UP(
        "Tuer un joueur restaure %hearts% cœurs de vie au tueur.",
        "Killing a player restores %hearts% hearts to the killer."
    ),
    NO_END(
        "Désactive complètement l'accès à l'End.",
        "Completely disables access to the End."
    ),
    NO_FALL(
        "Permet de ne pas prendre de dégât de chute.",
        "Prevents fall damage."
    ),
    NO_FOOD(
        "Désactive la faim - les joueurs ne perdent jamais de nourriture.",
        "Disables hunger - players never lose food."
    ),
    NO_HORSE(
        "Interdit de monter sur les chevaux.",
        "Prevents riding horses."
    ),
    NO_NAME_TAG(
        "Masque les noms des joueurs au-dessus de leur tête.",
        "Hides player names above their heads."
    ),
    NO_NETHER(
        "Désactive complètement l'accès au Nether.",
        "Completely disables access to the Nether."
    ),
    NO_WOODEN_TOOL(
        "Remplace automatiquement les outils en bois par des outils en fer.",
        "Automatically replaces wooden tools with iron tools."
    ),

    NETHERIBUS(
            "Tous les joueurs hors du Nether après %time% secondes subiront %damage% points de dégâts par seconde.",
            "All players outside the Nether after %time% seconds will take %damage% damage per second."
    ),

    ORE_ROULETTE(
        "Chaque minerai miné donne un minerai aléatoire.",
        "Each mined ore gives a random ore."
    ),
    ORE_SWAP(
        "Les minerais changent aléatoirement toutes les 15 minutes.",
        "Ores randomly change every 15 minutes."
    ),

    
    PARKOUR_MASTER(
        "Des parcours apparaissent aléatoirement. Les compléter donne des récompenses !",
        "Courses appear randomly. Completing them gives rewards!"
    ),
    POTENTIAL_PERMANENT(
        "Commencez avec 10 cœurs + 10 d'absorption qui peuvent devenir permanents.",
        "Start with 10 hearts + 10 absorption that can become permanent."
    ),

    
    REWARD_LONG_SHOT(
        "Les tirs longue distance infligent plus de dégâts et soignent le tireur.",
        "Long-range shots deal more damage and heal the shooter."
    ),
    RODLESS(
        "Empêche l'utilisation et la fabrication de cannes à pêche.",
        "Prevents the use and crafting of fishing rods."
    ),

    RANDOMCRAFT(
            "§7Les recettes de craft donnent des objets aléatoires.",
            "§7Crafting recipes give random items."
    ),
    RANDOMDROP(
            "§7Tous les drops de mobs et blocs sont aléatoires.",
            "§7All mob and block drops are randomized."
    ),
    SAFE_MINER(
        "Protège les joueurs des dégâts de lave et d'étouffement en minant.",
        "Protects players from lava and suffocation damage while mining."
    ),
    SIMON_SAYS(
        "Suivez les commandes de Simon ou subissez des pénalités !",
        "Follow Simon's commands or suffer penalties!"
    ),
    STARTER_TOOLS(
        "Donne des outils de base à tous les joueurs au début de la partie.",
        "Gives basic tools to all players at the start of the game."
    ),

    SUPER_HEROS(
            "§eDeviens un SuperHéros avec des pouvoirs aléatoires !",
            "§eBecome a SuperHero with random powers!"
    ),
    TEAM_ARROW(
        "Les flèches ne peuvent pas blesser les membres de sa propre équipe.",
        "Arrows cannot hurt members of your own team."
    ),
    TEAM_INV(
        "Les membres d'une équipe partagent leurs inventaires.",
        "Team members share their inventories."
    ),
    TPMEETUP("Teleporte tout les joueur au centre de la carte apres un temps donné",
            "All players teleport to the center of the map after a given time"),
    TIMBER(
        "Casse les arbres tel un bûcheron - un seul bloc cassé fait tomber tout l'arbre.",
        "Chop trees like a lumberjack - breaking one block fells the whole tree."
    ),
    TIME_BOMBE(
        "Les joueurs morts explosent après un délai, créant un cratère.",
        "Dead players explode after a delay, creating a crater."
    ),
    TRANSMUTATION(
        "Transformez des ressources via l'alchimie (craft uniquement).",
        "Transform resources through alchemy (crafting only)."
    ),

    
    VAMPIRE(
        "Récupérez 1 cœur en tuant un joueur, mais brûlez au soleil sans armure.",
        "Recover 1 heart by killing a player, but burn in the sun without armor."
    ),

    
    WEAKEST_LINK(
        "Le joueur avec le moins de kills prend 2x plus de dégâts.",
        "The player with the fewest kills takes 2x more damage."
    ),
    WEB_CAGE(
        "À la mort, le joueur est entouré d'une cage de toiles d'araignée.",
        "On death, the player is surrounded by a web cage."
    ),
    WOOL_TO_STRING(
        "Convertit automatiquement la laine en ficelle.",
        "Automatically converts wool into string."
    ),

    
    XP_SANSUE(
        "Modifie le système d'expérience pour le rendre plus équilibré.",
        "Modifies the experience system to make it more balanced."
    ),

    ;

    private final Map<String, String> translations;

    ScenarioDescLang(String fr, String en) {
        this.translations = Map.of("fr_FR", fr, "en_US", en);
    }

    @Override
    public String getKey() { return "scenario.desc." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}
