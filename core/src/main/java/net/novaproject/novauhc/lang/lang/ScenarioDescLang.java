package net.novaproject.novauhc.lang.lang;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum ScenarioDescLang implements Lang {

    ACID_RAIN("Pluie acide qui fait des dégâts. Protégez-vous sous des blocs !"),
    ARROW_SWITCH("Les joueurs touchés par une flèche échangent leur position avec le tireur."),

    BAT_ROULETTE("Tuer une chauve-souris donne une pomme dorée, ou tue le joueur sur le coup."),
    FK("Mode de jeu par équipes avec des bases à défendre et des objectifs de capture."),
    BEST_PVE("Gagne un cœur toutes les X minutes sans subir de dégâts. Un dégât te sort de la liste, un kill t'y remet."),
    BETA_ZOMBIE("Les zombies droppent 0 à 2 plumes au lieu de leur butin habituel."),
    BLACK_ARROW("Les minerais de charbon donnent des flèches au lieu de charbon."),
    QUIVER("Les flèches sont limitées par joueur."),
    BEATSANTA("%santa_size% Pere Noël contre %lutin_size% Lutin !"),
    BLIZZARD("Tempêtes de neige qui ralentissent et aveuglent. Restez près du feu !"),
    BLOCK_RUSH("Miner un type de bloc pour la première fois donne %reward% lingot(s) d'or."),
    BLOOD_CYCLE("Un type de minerai change périodiquement et inflige des dégâts si miné."),
    BLOOD_DIAMONDS("Les joueurs perdent 1 coeur en minant un diamant."),
    BLOOD_LUST("Chaque kill donne Speed %speed_level% et Strength %strength_level% pendant %duration% secondes."),
    BUFF_KILLER("Tuer un joueur donne des effets de potion bénéfiques au tueur."),

    CAT_EYE("Donne une vision nocturne permanente à tous les joueurs."),
    CHAT_PVP("À l'arrivée du PVP, le chat est désactivé."),
    COMPENSATION("À la mort d'un joueur, tous les membres de son équipe gagnent %hearts% coeurs."),
    CRIPPLE("Rend tous les joueurs faibles pendant %duration% secondes."),
    CUTCLEAN("Les minerais et les drops d'animaux sont automatiquement fondus, aucun four nécessaire."),
    COBBLE_UNIFIED("Granite, Andésite et Diorite (et leurs versions polies) drop de la cobblestone."),
    LAVALESS("Interdit de remplir un seau de lave ou de poser un seau de lave."),

    DEATH_EMERAULD("Miner une %block% inflige %damage% dégâts à un joueur aléatoire."),
    DEMOCRACY("Votez pour éliminer un joueur toutes les %minutes% minutes !"),
    DOUBLE_ORE("Double la quantité de minerais obtenus lors du minage, configurable pour chaque minerai."),
    OBSI_BUFF("Casser de l'obsidienne avec une pioche donne Haste III et drop 2 obsidiennes (configurable)."),

    FALLOUT("Après un certain temps, rester au-dessus de Y=%level% inflige des dégâts de radiation aux joueurs."),
    FAST_FURNACE("Les fours cuisent un item par seconde."),
    FAST_MINER("Jusqu'au PvP : Rapidité II, Hâte I (Hâte II sur l'obsidienne) et Saturation."),
    FINAL_HEAL("Heal tous les joueurs à des moments précis avant le PVP."),
    FIRE_LESS("Désactive les dégâts de feu et de lave."),
    GAP_ROULETTE("Les pommes dorées donnent des effets aléatoires configurables au lieu de leurs effets normaux."),
    GENIE("3 souhaits par partie ! Les options dépendent du nombre de kills."),
    GLADIATOR("Les combats 1v1 se déroulent dans une arène fermée qui apparaît automatiquement."),
    GOLDEN_DROP("Les joueurs morts drop automatiquement une pomme dorée."),
    GOLDEN_HEAD("Les têtes de joueurs morts peuvent être craftées en pommes dorées spéciales."),
    GONE_FISH("Scénario centré sur la pêche avec des récompenses spéciales."),

    HASTEY_ALPHA("Version extrême de HasteyBoy - Efficacité V et Solidité III sur tous les outils."),
    HASTEY_BABIE("Version réduite de HasteyBoy - seulement Efficacité I sur les outils."),
    HASTEY_BOY("Tous les outils craftés reçoivent automatiquement Efficacité III et Solidité I."),
    HEATH_CHARITY("Toutes les X minutes, le joueur le plus bas en vie est soigné à fond."),

    INFINITE_ENCHANTER("Les tables d'enchantement ne consomment pas de lapis-lazuli."),
    INVENTORS("Le premier à crafter un objet est annoncé à tout le monde."),

    KING("Un joueur devient roi et doit être protégé par son équipe."),

    LEGEND("Scénario à 18 classes de légendes aux capacités uniques. Choisissez votre classe ou elle sera assignée aléatoirement."),
    LONG_SHOOT("Les tirs à l'arc de plus de %distance% blocs rapportent du minerai, d'autant plus que le tir est long."),
    LOOT_CRATE("Chaque joueur reçoit une caisse toutes les %minutes% minutes, en tier 1 ou tier 2."),
    LUCKY_ORE("%chance%% de chance d'obtenir un objet légendaire en minant des minerais."),

    MAGNET("Les minerais que tu casses vont directement dans ton inventaire."),
    MEETUP("Donne à tous les joueurs le même équipement."),

    MYSTERY_TEAM("§7Des équipes mystérieuses sont formées, découvrez vos coéquipiers !"),

    NINE_SLOT("Seule la hotbar peut être utilisée. L'inventaire principal est inaccessible !"),
    NINJA("Devenez invisible pendant %seconds% secondes après chaque kill !"),
    NO_CLEAN_UP("Tuer un joueur rend le tueur invulnérable au PvP pendant %seconds% secondes, sauf s'il attaque."),
    NO_END("Désactive complètement l'accès à l'End."),
    NO_FALL("Permet de ne pas prendre de dégât de chute."),
    NO_FOOD("Désactive la faim - les joueurs ne perdent jamais de nourriture."),
    NO_HORSE("Interdit de monter sur les chevaux."),
    NO_NAME_TAG("Masque les noms des joueurs au-dessus de leur tête."),
    NO_NETHER("Désactive complètement l'accès au Nether."),
    NO_NOTCH_GAP("Désactive le craft de la pomme dorée enchantée."),
    NO_WOODEN_TOOL("Remplace automatiquement les outils en bois par des outils en fer."),

    NETHERIBUS("Tous les joueurs hors du Nether après %time% secondes subiront %damage% points de dégâts par seconde."),

    ORE_ROULETTE("Chaque minerai miné donne un minerai aléatoire."),
    ORE_SWAP("Les minerais changent aléatoirement toutes les 15 minutes."),

    PARKOUR_MASTER("Des parcours apparaissent aléatoirement. Les compléter donne des récompenses !"),
    POTENTIAL_PERMANENT("10 coeurs + 10 d'absorption. Les dégâts subis réduisent définitivement ta vie max ; manger une gap à vie pleine convertit de l'absorption en permanent."),

    RODLESS("Empêche l'utilisation et la fabrication de cannes à pêche."),

    RANDOMCRAFT("§7Les recettes de craft donnent des objets aléatoires."),
    RANDOMDROP("§7Tous les drops de mobs et blocs sont aléatoires."),
    SAFE_MINER("Sous une certaine hauteur, réduit les dégâts des mobs et annule les dégâts de feu et de lave."),
    SIMON_SAYS("Suivez les commandes de Simon ou subissez des pénalités !"),
    STARTER_TOOLS("Donne des outils de base à tous les joueurs au début de la partie."),

    SUPER_HEROS("§eDeviens un SuperHéros avec des pouvoirs aléatoires !"),
    TEAM_ARROW("Les flèches ne peuvent pas blesser les membres de sa propre équipe."),
    TEAM_INV("Les membres d'une équipe partagent leurs inventaires."),
    TPMEETUP("Teleporte tout les joueur au centre de la carte apres un temps donné"),
    TIMBER("Casse les arbres tel un bûcheron - un seul bloc cassé fait tomber tout l'arbre. S'arrête au PvP."),
    TIME_BOMBE("Les joueurs morts explosent après un délai, créant un cratère."),
    TRANSMUTATION("Transformez des ressources via l'alchimie (craft uniquement)."),

    VAMPIRE("Récupérez 1 coeur en tuant un joueur, mais brûlez au soleil sans armure."),

    WEAKEST_LINK("Toutes les X minutes, le ou les joueurs les plus bas en vie sont éliminés. Personne si tout le monde est à égalité."),
    WEB_CAGE("À la mort, le joueur est entouré d'une cage de toiles d'araignée."),
    WOOL_TO_STRING("Une laine se craft en une ficelle."),

    XP_SANSUE("Toutes les X minutes, chaque joueur perd un niveau d'expérience, ou de la vie s'il n'en a plus."),

    SWITCH("Toutes les X minutes, un joueur aléatoire de chaque équipe est envoyé dans une autre équipe (avec sa position)."),

    TEAM_SWAPPER_CLASSIC("Tuer un joueur ennemi le fait rejoindre votre équipe s'il a encore des vies. Pas de classes."),

    TEAM_SWAPPER_V3("TeamSwapper complet : classes (Maître/Ancien/Nouveau), Coup d'État, Cargaisons et système de vies."),

    ;

    private final Map<String, String> translations;

    ScenarioDescLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override
    public String getKey() { return "scenario.desc." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}

