package net.novaproject.novauhc.scenario.special.taupegun;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class TaupeGun extends Scenario {

    private static TaupeGun instance;
    private final HashMap<UHCPlayer, UHCTeam> taupeTeam = new HashMap<>();
    private final HashMap<UHCTeam, UHCPlayer> taupePlayer = new HashMap<>();
    private final HashMap<UHCPlayer, UHCTeam> oldTeam = new HashMap<>();
    private final HashMap<UHCPlayer, Integer> kit = new HashMap<>();
    private final List<UHCTeam> TeamsTaupe = new ArrayList<>();
    private final List<UHCPlayer> calimed = new ArrayList<>();
    private final Set<UHCPlayer> alreadyChosen = new HashSet<>();
    private int mole = 1;
    private int molesize = 3;
    private boolean taupeAssigned = false;

    public static TaupeGun getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "TaupeGun";
    }

    @Override
    public String getDescription() {
        return "Certains joueurs deviennent des taupes secrètes avec des kits spéciaux.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SADDLE);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void setup() {
        instance = this;
        taupeTeam.clear();
        taupePlayer.clear();
        oldTeam.clear();
        TeamsTaupe.clear();
        kit.clear();
    }

    public int getMole() {
        return mole;
    }

    public void setMole(int mole) {
        this.mole = mole;
    }

    public int getMolesize() {
        return molesize;
    }

    public void setMolesize(int molesize) {
        this.molesize = molesize;
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "La taille des équipes a été automatiquement définie à 2 pour le mode FallenKingdom.");
        }

    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int pvp = UHCManager.get().getTimerpvp();

        if (timer == pvp && !taupeAssigned) {
            if (taupeAssigned) return;

            taupeAssigned = true;

            ShuffleMultiTaupe(mole, molesize);

        }
    }

    @Override
    public void onTaupeCMD(Player player, String subCommand, String[] args) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (!getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {
            player.sendMessage("vous ete pas une taupe ");
            return;
        }


        switch (subCommand) {

            case "tc":
                taupeCoordManager(uhcPlayer);
                break;
            case "ti":
                taupeTiManager(uhcPlayer);
                break;
            case "kit":
                TaupeKitManager(uhcPlayer);
                break;

            case "reveal":
                TaupeRevealManager(uhcPlayer);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Essayez /p pour plus d'informations.");
        }
    }

    private void TaupeRevealManager(UHCPlayer uhcPlayer) {
        if (uhcPlayer.getTeam().isPresent()) {
            if (getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {

                UHCTeam team = uhcPlayer.getTeam().get();
                TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), team.getName(), "[§c" + team.getName() + "§r] ", "");
                Bukkit.broadcastMessage(Common.get().getInfoTag() + "La Taupe " + uhcPlayer.getPlayer().getName() + " s'est révélés");
                uhcPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
            } else {
                uhcPlayer.getPlayer().sendMessage(Common.get().getInfoTag() + "Vous ne pouvais pas vous reveal car vous etes pas Taupe ! ");
            }
        }
    }


    private void taupeTiManager(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        UHCTeam team = getOldTeamforPlayer(uhcPlayer);
        if (isScenarioActive("TeamInventory")) {
            if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying() && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
                player.openInventory(TeamInv.inventory.get(team));
            } else {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas d'équipe, vous êtes mort ou la partie n'a pas commencé !");
            }
        } else {
            player.sendMessage(Common.get().getInfoTag() + "TeamInventory est désactivée durant cette partie.");
        }
    }

    private void taupeCoordManager(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        UHCTeam team = getOldTeamforPlayer(uhcPlayer);
        if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying() && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();
            String message = ChatColor.GREEN + "Coord : x: " + x + " y: " + y + " z: " + z;

            team.getPlayers().forEach(teamPlayer ->
                    teamPlayer.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "❖ Team ❖ " + ChatColor.DARK_GRAY + player.getName() + " » " + message));
            player.sendMessage(ChatColor.DARK_PURPLE + "❖ Team ❖ " + ChatColor.DARK_GRAY + player.getName() + " » " + message);
        } else {

            player.sendMessage(ChatColor.RED + "Vous n'avez pas d'équipe ou la partie n'a pas commencé !");
        }

    }

    private void TaupeKitManager(UHCPlayer uhcPlayer) {
        int kit = getKit().get(uhcPlayer);
        Inventory inventory = uhcPlayer.getPlayer().getInventory();
        if (calimed.contains(uhcPlayer)) {
            uhcPlayer.getPlayer().sendMessage("[§cTaupeGun§r] Sois pas gourmant mon cochon :)");
            return;
        }
        calimed.add(uhcPlayer);
        switch (kit) {
            case 0:
                ItemCreator puch = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
                ItemCreator power3 = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_DAMAGE, 3);
                ItemCreator arrow = new ItemCreator(Material.ARROW).setAmount(64);
                ItemCreator string = new ItemCreator(Material.STRING).setAmount(3);

                inventory.addItem(power3.getItemstack());
                inventory.addItem(puch.getItemstack());
                inventory.addItem(arrow.getItemstack());
                inventory.addItem(string.getItemstack());

                break;
            case 1:
                ItemCreator pearl = new ItemCreator(Material.ENDER_PEARL).setAmount(4);
                ItemCreator fether = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.PROTECTION_FALL, 4);
                inventory.addItem(fether.getItemstack());
                inventory.addItem(pearl.getItemstack());
                break;
            case 2:
                ItemCreator speed = new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.BLUE + "Speed 1");
                ItemCreator fire = new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.GOLD + "FireResistance 1");
                ItemCreator potion = new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30 * 20, 0, false, false), true).setName(ChatColor.GREEN + "Poison 1");


                inventory.addItem(speed.getItemstack());
                inventory.addItem(fire.getItemstack());
                inventory.addItem(potion.getItemstack());
                break;
            case 3:
                ItemCreator pro = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                ItemCreator sharp = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.DAMAGE_ALL, 3);
                ItemCreator power = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_DAMAGE, 3);

                inventory.addItem(pro.getItemstack());
                inventory.addItem(sharp.getItemstack());
                inventory.addItem(power.getItemstack());

                break;
            case 4:
                ItemCreator obsi = new ItemCreator(Material.OBSIDIAN).setAmount(14);
                ItemCreator diam = new ItemCreator(Material.DIAMOND).setAmount(10);
                ItemCreator gold = new ItemCreator(Material.GOLD_INGOT).setAmount(32);
                ItemCreator iron = new ItemCreator(Material.IRON_INGOT).setAmount(64);

                inventory.addItem(obsi.getItemstack());
                inventory.addItem(diam.getItemstack());
                inventory.addItem(gold.getItemstack());
                inventory.addItem(iron.getItemstack());

                break;
            case 5:
                ItemCreator fireas = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.FIRE_ASPECT, 3);
                ItemCreator flam = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_FIRE, 1);

                inventory.addItem(fireas.getItemstack());
                inventory.addItem(flam.getItemstack());

                break;
            case 6:
                ItemCreator invi = new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60 * 20, 1, false, false), false).setName(ChatColor.GRAY + "Invisibilité 2");
                ItemCreator force = new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.RED + "Force 1");

                inventory.addItem(invi.getItemstack());
                inventory.addItem(force.getItemstack());

                break;
            default:

                break;
        }
        uhcPlayer.getPlayer().sendMessage("[§cTaupeGun§r] Vous avez bien recu votre Kit");
    }

    @Override
    public void onTaupeTcCMD(Player player, int x, int y, int z, String coordsMessage) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {

            String taupeMessage = ChatColor.RED + "❖ Taupe ❖ " +
                    ChatColor.DARK_GRAY + player.getName() + " » " + coordsMessage;

            uhcPlayer.getTeam().get().getPlayers().forEach(teamPlayer ->
                    teamPlayer.getPlayer().sendMessage(taupeMessage));

        } else {

            String teamMessage = ChatColor.DARK_PURPLE + "❖ Team ❖ " +
                    ChatColor.DARK_GRAY + player.getName() + " » " + coordsMessage;

            uhcPlayer.getTeam().get().getPlayers().forEach(teamPlayer ->
                    teamPlayer.getPlayer().sendMessage(teamMessage));


            for (Map.Entry<UHCPlayer, UHCTeam> entry : getOldTeam().entrySet()) {
                if (entry.getValue().equals(uhcPlayer.getTeam().get())) {
                    UHCPlayer taupe = entry.getKey();
                    taupe.getPlayer().sendMessage(teamMessage);
                    break;
                }
            }

        }
    }


    @Override
    public CustomInventory getMenu(Player player) {
        return new TaupeGunUI(player);
    }

    private void ShuffleMultiTaupe(int mole, int size) {

        List<UHCTeam> picked = new ArrayList<>();
        Random random = new Random();
        createTeamTaupe(size);


        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
            if (player.getTeam().isPresent()) {
                UHCTeam team = player.getTeam().get();

                if (picked.contains(team)) {
                    return;
                }
                picked.add(team);

                List<UHCPlayer> availablePlayers = team.getPlayers().stream()
                        .filter(ps -> !alreadyChosen.contains(ps))
                        .collect(Collectors.toList());

                if (availablePlayers.isEmpty()) {
                    return;
                }

                List<UHCTeam> availableTeams = TeamsTaupe.stream()
                        .filter(t -> t.getPlayers().size() < t.getTeamSize())
                        .collect(Collectors.toList());

                if (availableTeams.isEmpty()) {
                    return;
                }

                int numMolesToAssign = Math.min(mole, availablePlayers.size());

                for (int i = 0; i < numMolesToAssign; i++) {
                    UHCPlayer chosenPlayer = availablePlayers.get(random.nextInt(availablePlayers.size()));
                    alreadyChosen.add(chosenPlayer);

                    UHCTeam chosenTeam = availableTeams.get(random.nextInt(availableTeams.size()));

                    saveOldTeam(chosenPlayer, team);

                    chosenPlayer.setTeam(Optional.of(chosenTeam));

                    int kitnumber = random.nextInt(7);
                    kit.put(chosenPlayer, kitnumber);
                    TeamsTagsManager.setNameTag(chosenPlayer.getPlayer(), team.getName(), team.getPrefix(), "");
                    new Titles().sendTitle(chosenPlayer.getPlayer(), ChatColor.RED + "Vous êtes la Taupe", "", 10);
                    sendKitDescription(kitnumber, chosenPlayer);

                    addTaupe(chosenPlayer, chosenTeam);
                }

                if (isScenarioActive("TeamInventory")) {
                    for (UHCTeam t : TeamsTaupe) {
                        TeamInv.inventory.put(t, Bukkit.createInventory(null, InventoryType.CHEST));
                    }
                }
            }
        });

    }


    private void sendKitDescription(int kitnumber, UHCPlayer chosenPlayer) {

        switch (kitnumber) {

            case 0:
                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Punchonator ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous possedez un livre Punch 1, un livre Power 3, 64 Flèche et 3 fil\n" +
                        "§8§m--------------------------");
                break;
            case 1:
                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Aérien ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous possedez 4 enderPearl et un livre FeatherFalling 4\n" +
                        "§8§m--------------------------");
                break;
            case 2:

                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Alchimiste ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous possedez une potion de Speed 1 de 8min, une potion de FireResistance 1 de 8min et une potion de Poison 1 de 30s\n" +
                        "§8§m--------------------------");
                break;
            case 3:

                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Assasin ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous possedez des Livre Protection 3, Power 3 et Sharpness 3\n" +
                        "§8§m--------------------------");
                break;

            case 4:
                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Mineur ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous possédez 14 Obsidian, 10 Diams , 32 Gold ingot et 64 Iron Ingot\n" +
                        "§8§m--------------------------");
                break;

            case 5:
                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Pyromane ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous possédez un livre FireAspect 1 et Flame 1\n" +
                        "§8§m--------------------------");
                break;
            case 6:

                chosenPlayer.getPlayer().sendMessage("§8§m---------" + ChatColor.RED + "❖ Taupe Ninja ❖§8§m----------§r\n" +
                        "§fDescription du Kit : " + ChatColor.DARK_PURPLE + "Vous posséedez une PotionUi d'invisibilité 2 de 8 min et une potion de Force 1 de 3min \n" +
                        "§8§m--------------------------");
                break;

            default:

                break;
        }

    }

    @Override
    public boolean hasCustomTeamTchat() {
        return true;
    }

    private void createTeamTaupe(int size) {

        Pattern[] patterns = new Pattern[]{};
        int totalPlayers = UHCTeamManager.get().getTeams().size();
        int numberTeamTaupe = (int) Math.ceil((double) totalPlayers / size);
        for (int i = 0; i < numberTeamTaupe; i++) {
            UHCTeam taupe = new UHCTeam(DyeColor.CYAN, i + "", "Taupe " + i, patterns, size, true);
            UHCTeamManager.get().addTeams(taupe);
            TeamsTaupe.add(taupe);
        }

    }

    @Override
    public void onChatSpeek(Player player, String message, AsyncPlayerChatEvent event) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.DARK_GRAY + "❯ "
                        + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de parler !");
            return;
        }

        if (!uhcPlayer.getTeam().isPresent()) return;

        if (message.startsWith("!")) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.GREEN + "✦ Global ✦ "
                        + ChatColor.DARK_GRAY + player.getName() + " » "
                        + ChatColor.WHITE + message.substring(1));
            }
            return;
        }

        if (message.startsWith("?") && !TeamsTaupe.contains(uhcPlayer.getTeam().get())) {
            player.sendMessage("[§cTaupeGun§r]" + ChatColor.RED + "Vous n'êtes pas une taupe, vous ne pouvez pas parler en chat taupe !");
            return;
        }

        if (taupeTeam.containsKey(uhcPlayer)) {
            if (message.startsWith("?")) {
                UHCTeam taupeTeamGroup = uhcPlayer.getTeam().get();
                for (UHCPlayer taupePlayer : taupeTeamGroup.getPlayers()) {
                    taupePlayer.getPlayer().sendMessage(ChatColor.RED + "❖ Taupe ❖ "
                            + ChatColor.DARK_GRAY + player.getName() + " » "
                            + ChatColor.WHITE + message.substring(1));
                }
            } else {
                UHCTeam originalTeam = getOldTeamforPlayer(uhcPlayer);
                if (originalTeam != null) {
                    String teamMessage = ChatColor.DARK_PURPLE + "❖ Team ❖ "
                            + ChatColor.DARK_GRAY + player.getName() + " » "
                            + ChatColor.WHITE + message;

                    for (UHCPlayer teamPlayer : originalTeam.getPlayers()) {
                        teamPlayer.getPlayer().sendMessage(teamMessage);
                    }
                    uhcPlayer.getPlayer().sendMessage(teamMessage);
                }
            }
            return;
        }

        UHCTeam team = uhcPlayer.getTeam().get();
        String teamMessage = ChatColor.DARK_PURPLE + "❖ Team ❖ "
                + ChatColor.DARK_GRAY + player.getName() + " » "
                + ChatColor.WHITE + message;

        for (UHCPlayer teamPlayer : team.getPlayers()) {
            teamPlayer.getPlayer().sendMessage(teamMessage);
        }

        for (Map.Entry<UHCPlayer, UHCTeam> entry : oldTeam.entrySet()) {
            if (entry.getValue().equals(team)) {
                UHCPlayer taupe = entry.getKey();
                taupe.getPlayer().sendMessage(teamMessage);
                break;
            }
        }

    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
    }

    public UHCTeam getOldTeamforPlayer(UHCPlayer player) {
        return oldTeam.getOrDefault(player, null);
    }


    private boolean isScenarioActive(String scenarioName) {
        return ScenarioManager.get().getScenarioByName(scenarioName).map(Scenario::isActive).orElse(false);
    }


    public void addTaupe(UHCPlayer player, UHCTeam team) {
        taupePlayer.put(team, player);
        taupeTeam.put(player, team);
        TeamsTaupe.add(team);
    }

    public void saveOldTeam(UHCPlayer player, UHCTeam team) {
        oldTeam.put(player, team);
    }

    public HashMap<UHCPlayer, Integer> getKit() {
        return kit;
    }

    public List<UHCTeam> getTeamsTaupe() {
        return TeamsTaupe;
    }


    public HashMap<UHCTeam, UHCPlayer> getTaupePlayer() {
        return taupePlayer;
    }

    public HashMap<UHCPlayer, UHCTeam> getOldTeam() {
        return oldTeam;
    }
}


