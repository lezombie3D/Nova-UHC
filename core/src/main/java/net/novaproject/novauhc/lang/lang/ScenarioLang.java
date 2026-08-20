package net.novaproject.novauhc.lang.lang;

import java.util.HashMap;
import java.util.Collections;
import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum ScenarioLang implements Lang {

    ACIDRAIN_ACID_RAIN_START("§2§lAcidRain §8│ §7Pluie acide ! Abritez-vous sous des blocs !"),
    ACIDRAIN_WARNING_ONE_MINUTE("§c§lAcidRain §8│ §7Pluie acide dans 1 minute ! Trouvez un abri !"),
    ACIDRAIN_WARNING_TEN_SECONDS("§c§lAcidRain §8│ §7Pluie acide dans 10 secondes !"),
    ACIDRAIN_ENDING_SOON("§c§lAcidRain §8│ §7La pluie acide s'arrête dans 10 secondes !"),
    ACIDRAIN_RAIN_STOPPED("§c§lAcidRain §8│ §7La pluie acide s'est arrêtée. Vous pouvez sortir en sécurité !"),
    ACIDRAIN_BURNING("§c§lAcidRain §8│ §7Vous êtes brûlé par la pluie acide ! Trouvez un abri !"),

    BESTPVE_LIST_QUIT("Vous avez quitté la liste BestPvE. Vous la rejoindrez à nouveau dans %best_timer%"),
    BESTPVE_LIST_JOIN("Vous avez rejoint la liste BestPvE. Attention à ne plus prendre de dégâts !"),
    BESTPVE_GAIN_MESSAGE("Vous avez gagné §a%heart_gain% §fcoeur(s) !"),

    BLIZZARD_WARNING_ONE_MINUTE("§b§lBlizzard §8│ §7Tempête de neige dans 1 minute !"),
    BLIZZARD_WARNING_TEN_SECONDS("§b§lBlizzard §8│ §7Tempête de neige dans 10 secondes !"),
    BLIZZARD_ENDING_SOON("§b§lBlizzard §8│ §7La tempête se calme dans 10 secondes !"),
    BLIZZARD_STORM_START("§b§lBlizzard §8│ §7Une tempête de neige commence ! Trouvez de la chaleur !"),
    BLIZZARD_STORM_END("§b§lBlizzard §8│ §7La tempête de neige s'est calmée !"),
    BLIZZARD_FREEZING("§b§lBlizzard §8│ §7Vous gelez ! Trouvez de la chaleur !"),
    BLIZZARD_VERY_COLD("§b§lBlizzard §8│ §7Vous avez très froid !"),
    BLIZZARD_GETTING_COLD("§b§lBlizzard §8│ §7Vous commencez à avoir froid..."),

    BLOCKRUSH_NEW_BLOCK("§6§lBlockRush §8│ §7Nouveau bloc miné : §f%block% §f! +%amount% Lingot(s) d'Or"),

    BLOODCYCLE_TAKE_DAMAGE("Ahhh ! Force à toi mauvais minerais"),
    BLOODCYCLE_DIAMOND("Vous ne devez pas miner §b§lDiamant§f. Changement du cycle dans §6§l%blood_timer%§f secondes"),
    BLOODCYCLE_GOLD("Vous ne devez pas miner §6§lOr"),
    BLOODCYCLE_IRON("Vous ne devez pas miner §7§lFer"),
    BLOODCYCLE_COAL("Vous ne devez pas miner §8§lCharbon"),
    BLOODCYCLE_LAPIS("Vous ne devez pas miner §9§lLapis"),
    BLOODCYCLE_REDSTONE("Vous ne devez pas miner §c§lRedstone"),

    BLOODLUST_KILL_BOOST("§c§lBloodLust §8│ §7Vous ressentez la soif de sang ! Speed II et Strength I pendant 30 secondes !"),
    BLOODLUST_BLOODLUST_ACTIVATED("§c§lBloodLust §8│ §f%player% §fest en état de soif de sang !"),
    BLOODLUST_ENDING_TEN_SECONDS("§c§lBloodLust §8│ §7Soif de sang se termine dans 10 secondes !"),
    BLOODLUST_ENDING_FIVE_SECONDS("§c§lBloodLust §8│ §7Soif de sang se termine dans 5 secondes !"),
    BLOODLUST_BLOODLUST_ENDED("§c§lBloodLust §8│ §7Votre soif de sang s'est calmée."),

    DEMOCRACY_VOTE_DURATION("§9§lDemocracy §8│ §7Vous avez %duration% minutes pour voter !"),
    DEMOCRACY_VOTE_RESULTS("§9§lDemocracy §8│ §7RÉSULTATS DU VOTE :"),
    DEMOCRACY_VOTE_RESULT_LINE("§9§lDemocracy §8│ §f%player%: %votes% vote(s)"),
    DEMOCRACY_PLAYER_ELIMINATED("§9§lDemocracy §8│ §f%player% §fa été éliminé par vote démocratique !"),
    DEMOCRACY_NO_VOTES("§9§lDemocracy §8│ §7Aucun vote ! Personne n'est éliminé."),
    DEMOCRACY_TIE_RANDOM("§9§lDemocracy §8│ §7Égalité ! Sélection aléatoire..."),
    DEMOCRACY_NOT_ENOUGH_PLAYERS("§9§lDemocracy §8│ §7Pas assez de joueurs pour un vote !"),
    DEMOCRACY_VOTE_CAST("§9§lDemocracy §8│ §f%voter% a voté ! (%current%/%total% votes reçus)"),
    DEMOCRACY_AVAILABLE_PLAYERS("§9§lDemocracy §8│ §7Joueurs disponibles :"),
    DEMOCRACY_VOTE_IN_FIVE_MINUTES("§9§lDemocracy §8│ §7Vote démocratique dans 5 minutes !"),
    DEMOCRACY_VOTE_IN_ONE_MINUTE("§9§lDemocracy §8│ §7Vote démocratique dans 1 minute !"),
    DEMOCRACY_ONE_MINUTE_LEFT("§9§lDemocracy §8│ §7Plus qu'1 minute pour voter !"),
    DEMOCRACY_TEN_SECONDS_LEFT("§9§lDemocracy §8│ §7Plus que 10 secondes pour voter !"),
    DEMOCRACY_VOTE_STARTS("§9§lDemocracy §8│ §7LE VOTE DÉMOCRATIQUE COMMENCE !"),
    DEMOCRACY_USE_VOTE_COMMAND("§9§lDemocracy §8│ §7Utilisez /vote <joueur> pour voter !"),
    DEMOCRACY_PLAYER_ENTRY("§9§lDemocracy §8│ §f%player%"),
    DEMOCRACY_YOU_ELIMINATED("§9§lDemocracy §8│ §7Vous avez été éliminé par le vote du peuple !"),
    DEMOCRACY_NOT_ENOUGH_VOTES("§9§lDemocracy §8│ §7Aucun joueur n'a reçu assez de votes pour être éliminé !"),
    DEMOCRACY_NO_VOTE_ACTIVE("§9§lDemocracy §8│ §7Aucun vote en cours !"),
    DEMOCRACY_ALREADY_VOTED("§9§lDemocracy §8│ §7Vous avez déjà voté !"),
    DEMOCRACY_PLAYER_NOT_FOUND("§9§lDemocracy §8│ §7Joueur introuvable !"),
    DEMOCRACY_NOT_PARTICIPATING("§9§lDemocracy §8│ §7Ce joueur ne participe pas !"),
    DEMOCRACY_CANNOT_VOTE_SELF("§9§lDemocracy §8│ §7Vous ne pouvez pas voter pour vous-même !"),
    DEMOCRACY_VOTE_BROADCAST("§9§lDemocracy §8│ §f%voter% a voté ! (%current%/%total%)"),
    FALLOUT_RADIATION_START("§c§lFallout §8│ §7LES RADIATIONS COMMENCENT !"),
    FALLOUT_GO_UNDERGROUND("§c§lFallout §8│ §7Descendez sous Y=%level% pour éviter les radiations !"),
    FALLOUT_WARNING_FIVE_MINUTES("§c§lFallout §8│ §7Radiations dans 5 minutes ! Préparez vos abris !"),
    FALLOUT_WARNING_ONE_MINUTE("§c§lFallout §8│ §7Radiations dans 1 minute ! Descendez sous Y=%level% !"),
    FALLOUT_WARNING_TEN_SECONDS("§c§lFallout §8│ §7Radiations dans 10 secondes !"),
    FALLOUT_EXPOSED("§c§lFallout §8│ §7Vous êtes exposé aux radiations !"),

    GENIE_WISHES_RECEIVED("§6§lGenie §8│ §7Vous avez 3 souhaits ! Utilisez /wish pour voir vos options."),
    GENIE_WISH_GRANTED("§6§lGenie §8│ §7Souhait exaucé ! Il vous reste %remaining% souhait(s)."),
    GENIE_WISH_ANNOUNCED("§6§lGenie §8│ §f%player% a utilisé un souhait !"),
    GENIE_NO_WISHES_LEFT("§6§lGenie §8│ §7Vous n'avez plus de souhaits !"),
    GENIE_NOT_ENOUGH_KILLS("§6§lGenie §8│ §7Vous n'avez pas assez de kills pour ce souhait !"),
    GENIE_HEAL_GRANTED("§6§lGenie §8│ §7Vous avez été soigné !"),
    GENIE_FOOD_GRANTED("§6§lGenie §8│ §7Votre faim a été restaurée !"),
    GENIE_SPEED_GRANTED("§6§lGenie §8│ §7Vous avez reçu Speed II pendant %duration% minutes !"),
    GENIE_STRENGTH_GRANTED("§6§lGenie §8│ §7Vous avez reçu Strength I pendant %duration% minutes !"),
    GENIE_WISHES_IMPROVED("§6§lGenie §8│ §7Vos options de souhaits se sont améliorées avec ce kill !"),
    GENIE_RECEIVED_RESISTANCE("§6§lGenie §8│ §7Vous avez reçu Resistance I pendant 5 minutes !"),
    GENIE_RECEIVED_INVISIBILITY("§6§lGenie §8│ §7Vous êtes invisible pendant 1 minute !"),
    GENIE_RECEIVED_ARROWS("§6§lGenie §8│ §7Vous avez reçu 32 flèches !"),

    GLADIATOR_ARENA_CREATED("§4§lGladiator §8│ §7Arène créée ! Combat entre %player1% et %player2% !"),
    GLADIATOR_COMBAT_STARTED("§4§lGladiator §8│ §7Que le combat commence ! Bonne chance !"),
    GLADIATOR_ARENA_WINNER("§c§lGladiator §8│ §f%player% §fa remporté le combat d'arène !"),
    GLADIATOR_ARENA_ENDED("§c§lGladiator §8│ §7Le combat d'arène s'est terminé."),

    GOLDENHEAD_ITEM_NAME("§6Golden Head"),
    GOLDENHEAD_SKULL_NAME("§6● §7Tête de %player%"),

    INVENTORS_FIRST_CRAFT("§e§lInventors §8│ §f%player% §fest le premier à crafter §f%item% §f!"),
    LONGSHOOT_LONG_SHOT("%servertag%Long shot !"),
    LOOTCRATE_CRATES_WARNING_1MIN("§d§lLootCrate §8│ §7Prochaine caisse dans 1 minute !"),
    LOOTCRATE_CRATES_WARNING_10SEC("§d§lLootCrate §8│ §7Prochaine caisse dans 10 secondes !"),

    LUCKYORE_LUCKY_PERSONAL("§6§lLuckyOre §8│ §7VOUS AVEZ EU DE LA CHANCE !"),
    LUCKYORE_INVENTORY_FULL("§6§lLuckyOre §8│ §7Inventaire plein ! L'objet a été jeté au sol."),
    LUCKYORE_LUCKY_BROADCAST("§6§lLuckyOre §8│ §f%player% a trouvé un minerai chanceux !"),


    NINESLOT_START("§8§lNineSlot §8│ §7Votre inventaire est maintenant limité à la hotbar !"),
    NINESLOT_START_ALL("§8§lNineSlot §8│ §7Tous les inventaires sont maintenant limités à 9 slots !"),
    NINESLOT_ITEM_DROPPED("§8§lNineSlot §8│ §7Objet jeté : inventaire plein !"),
    NINESLOT_CANNOT_USE_INV("§8§lNineSlot §8│ §7Vous ne pouvez pas utiliser l'inventaire principal !"),
    NINESLOT_CANNOT_MOVE("§8§lNineSlot §8│ §7Vous ne pouvez pas déplacer d'objets vers l'inventaire principal !"),
    NINJA_KILL_INVISIBILITY("§8§lNinja §8│ §7Vous devenez invisible pendant 10 secondes !"),
    NOEND_BLOCKED("§cL'accès à l'End est désactivé !"),

    NONETHER_BLOCKED("§cL'accès au Nether est désactivé !"),

    OREROULETTE_ORE_CHANGED("§d§lOreRoulette §8│ §f%from% → %to% !"),
    OREROULETTE_JACKPOT_DIAMOND("§d§lOreRoulette §8│ §7§lCHANCE INCROYABLE ! Diamant obtenu !"),
    OREROULETTE_JACKPOT_EMERALD("§d§lOreRoulette §8│ §7§lTRÈS RARE ! Émeraude obtenue !"),

    ORESWAP_SWAP_ANNOUNCEMENT("§6§lOreSwap §8│ §7Les minerais ont été mélangés !"),
    ORESWAP_ORE_SWAPPED("§6§lOreSwap §8│ §f%original_ore% → %swapped_ore% !"),
    ORESWAP_NEW_MAPPING("§6§lOreSwap §8│ §7Nouveau mapping des minerais :"),
    ORESWAP_MAPPING_LINE("§6§lOreSwap §8│ §f%original% §7→ §f%swapped%"),
    ORESWAP_MAPPING_UNCHANGED("§6§lOreSwap §8│ §f%original% §7→ §f%swapped% §7(inchangé)"),

    PARKOURMASTER_CHECKPOINT_REACHED("§a§lParkourMaster §8│ §7Checkpoint %current%/%total% atteint !"),
    PARKOURMASTER_PARKOUR_COMPLETED("§a§lParkourMaster §8│ §7Parcours complété ! Récompense : %reward%"),
    PARKOURMASTER_PARKOUR_FAILED("§c§lParkourMaster §8│ §7Vous avez échoué au parcours ! Réessayez la prochaine fois."),
    PARKOURMASTER_PARKOUR_EXPIRED("§c§lParkourMaster §8│ §7Le parcours a expiré !"),
    PARKOURMASTER_PARKOUR_SPAWNED_PERSONAL("§a§lParkourMaster §8│ §7Un parcours est apparu près de vous ! Complétez-le pour une récompense !"),
    PARKOURMASTER_PARKOUR_SPAWNED_BROADCAST("§a§lParkourMaster §8│ §7Un parcours est apparu près de %player% !"),
    PARKOURMASTER_PARKOUR_COMPLETED_BROADCAST("§a§lParkourMaster §8│ §f%player% a complété un parcours !"),
    PARKOURMASTER_INVENTORY_FULL_DROP("§a§lParkourMaster §8│ §7Inventaire plein ! Récompense jetée au sol."),

    POTENTIALPERMANENT_STARTING_HEALTH("§e§lPotentialPermanent §8│ §7Vous commencez avec %permanent_hearts% coeurs permanents + %absorption_hearts% coeurs d'absorption !"),
    POTENTIALPERMANENT_CONVERSION_INFO("§e§lPotentialPermanent §8│ §7Mangez une pomme d'or à vie pleine pour convertir l'absorption en vie permanente !"),

    SIMONSAYS_INCOMING_ORDER("§6§lSimonSays §8│ §7Simon va bientôt donner un ordre..."),
    SIMONSAYS_FIVE_SECONDS("§6§lSimonSays §8│ §75 secondes restantes !"),
    SIMONSAYS_NEW_ORDER("§6§lSimonSays §8│ §7Simon dit : §f%order%"),
    SIMONSAYS_ORDER_DURATION("§6§lSimonSays §8│ §7Vous avez %seconds% secondes !"),
    SIMONSAYS_FAILED_COUNT("§6§lSimonSays §8│ §f%count% joueur(s) ont échoué !"),
    SIMONSAYS_ALL_OBEYED("§6§lSimonSays §8│ §7Tous les joueurs ont obéi à Simon !"),
    SIMONSAYS_DAMAGE_MSG("§6§lSimonSays §8│ §7Vous prenez 1 coeur de dégâts !"),
    SIMONSAYS_HUNGER_MSG("§6§lSimonSays §8│ §7Vous perdez de la faim !"),

    SWITCH_SWAP_ANNOUNCE("§6§lSwitch §8│ §7Les équipes ont été mélangées !"),
    SWITCH_SWAP_PLAYER_INFO("§6§lSwitch §8│ §7Tu as rejoint l'équipe §r%team%§r§7 !"),
    SWITCH_MERGE_ANNOUNCE("§6§lSwitch §8│ §7Des équipes solo ont été fusionnées !"),
    SWITCH_MERGE_PLAYER_INFO("§6§lSwitch §8│ §7Tu as fusionné dans l'équipe §r%team%§r§7 !"),

    TEAMSWAPPERV3_TRANSFER("§6§lTeamSwapper §8│ §f%victim% §7a été capturé par §f%killer% §7et rejoint §r%team%§r§7 !"),
    TEAMSWAPPERV3_LIVES_LEFT("§6§lTeamSwapper §8│ §7Il te reste §f%lives%§f/§f%max% §fvie(s)."),
    TEAMSWAPPERV3_LIVES_GAINED("§6§lTeamSwapper §8│ §7Tu as gagné une vie ! §7(%lives%§7)"),
    TEAMSWAPPERV3_CLASSE_MAITRE_ASSIGNED("§6§lTeamSwapper §8│ §7Tu es le §7§lMaître §fde ton équipe ! §7(Force I permanent)"),
    TEAMSWAPPERV3_CLASSE_ANCIEN_ASSIGNED("§6§lTeamSwapper §8│ §7Tu es désormais un §7§lAncien §f! §7(Résistance I + Traqueur)"),
    TEAMSWAPPERV3_CLASSE_NOUVEAU_ASSIGNED("§6§lTeamSwapper §8│ §7Tu es un §7§lNouveau §fdans cette équipe."),
    TEAMSWAPPERV3_CLASSE_NOUVEAU_DEMOTED("§6§lTeamSwapper §8│ §7Tu es redevenu un §7§lNouveau §7suite à des changements d'équipe."),
    TEAMSWAPPERV3_MAITRE_PROTECTED("§6§lTeamSwapper §8│ §7Ton essence de §7§lMaître §7t'a protégé§7 ! §7(60%)"),
    TEAMSWAPPERV3_MAITRE_PROTECTED_KILLER("§6§lTeamSwapper §8│ §7L'essence du §7§lMaître §7de §f%player% §7l'a protégé. §7(60%)"),
    TEAMSWAPPERV3_HP_LOSS("§6§lTeamSwapper §8│ §7Trop proche d'un Ancien ! §7Tu perds §70.5❤ §7permanent. §8(-%hearts%❤ total)"),
    TEAMSWAPPERV3_COUP_DETAT_BROADCAST("§c§lCOUP D'ÉTAT §8│ §f%player% §7s'est rebellé et fonde §r%team%§r§7 comme §7§lMaître§7 !"),
    TEAMSWAPPERV3_COUP_DETAT_PLAYER("§6§lTeamSwapper §8│ §7Tu as fondé ta propre équipe §r%team%§r§7 et tu en es le §7§lMaître§7 !"),
    TEAMSWAPPERV3_CARGAISON_SPAWN("§6§lCARGAISON §8│ §7Une cargaison est apparue §7→ §fX: %x% §7/ §fY: %y% §7/ §fZ: %z%"),
    TEAMSWAPPERV3_CARGAISON_REWARD_EFFECT("§6§lTeamSwapper §8│ §7Tu as obtenu §f%effect% §f! §7(5 min)"),
    TEAMSWAPPERV3_CARGAISON_REWARD_LIFE("§6§lTeamSwapper §8│ §7Tu as gagné §7+1 vie §f! §7(%lives%§7/§f%max%§7)"),
    TEAMSWAPPERV3_CARGAISON_REWARD_ARMOR("§6§lTeamSwapper §8│ §7Ton armure a été réparée à §750%§f !"),
    TEAMSWAPPERV3_TRACKER_NAME("§6§lTraqueur §7(Anciens / Maîtres)"),
    TEAMSWAPPERV3_CARGAISON_MENU_TITLE("§6Cargaison"),
    TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_NAME("§a§lEffet temporaire"),
    TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_LORE_MAITRE("§7→ §fVitesse I"),
    TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_LORE_ANCIEN("§7→ §fForce I §7ou §fVitesse I"),
    TEAMSWAPPERV3_CARGAISON_SLOT_EFFECT_LORE_NOUVEAU("§7→ §fAléatoire §8(Vitesse / Force / Résistance I)"),
    TEAMSWAPPERV3_CARGAISON_SLOT_LIFE_NAME("§c§l+1 Vie"),
    TEAMSWAPPERV3_CARGAISON_SLOT_LIFE_LORE("§7Gagne une vie supplémentaire."),
    TEAMSWAPPERV3_CARGAISON_SLOT_ARMOR_NAME("§b§lRéparation d'armure §7(50%)"),
    TEAMSWAPPERV3_CARGAISON_SLOT_ARMOR_LORE("§7Répare 50% de la durabilité de chaque pièce."),
    TEAMSWAPPERV3_EFFECT_SPEED("Vitesse I"),
    TEAMSWAPPERV3_EFFECT_STRENGTH("Force I"),
    TEAMSWAPPERV3_EFFECT_RESISTANCE("Résistance I"),
    TEAMSWAPPERV3_VAINQUEURS_HEADER("\n  §5§lVAINQUEURS"),
    TEAMSWAPPERV3_VAINQUEURS_LINE("  §8│ §7#%rank% %prefix%§f%player% §8— §7ancienneté §f%anc%"),

    VAMPIRE_KILL_HEAL("§c§lVampire §8│ §7Vous avez récupéré %heal_hearts% coeur(s) en tuant %victim% !"),
    VAMPIRE_SUN_DAMAGE("§c§lVampire §8│ §7Vous brûlez au soleil ! Équipez un casque ou trouvez de l'ombre !"),
    ;

    private final Map<String, String> translations;

    ScenarioLang(String fr) {
        Map<String, String> map = new HashMap<>();
        map.put("fr_FR", fr);

        this.translations = Collections.unmodifiableMap(map);
    }

    @Override
    public String getKey() { return "scenario." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}