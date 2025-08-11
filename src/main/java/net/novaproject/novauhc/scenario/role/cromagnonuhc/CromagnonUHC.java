package net.novaproject.novauhc.scenario.role.cromagnonuhc;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.RoleConfigManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth.GagGag;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth.Many;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth.Poilu;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth.Putride;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.solo.PereJurasique;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.solo.Triceratops;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms.*;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.LongCooldownManager;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CromagnonUHC extends ScenarioRole<CromagnonRole> {

    private final HashMap<UHCPlayer, Boolean> meurt = new HashMap<>();
    private boolean listsend;
    private final RoleConfigManager roleConfigManager = new RoleConfigManager(Main.get().getDataFolder());


    @Override
    public String getName() {
        return "Cromagnon";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOL);
    }

    @Override
    public void setup() {

        meurt.clear();
        addRole(Roue.class);
        addRole(Patriarche.class);
        addRole(Peintre.class);
        addRole(Feu.class);
        addRole(Feuille.class);
        addRole(Agouagou.class);
        addRole(Caillou.class);
        addRole(Triceratops.class);
        addRole(Triceratops.class);
        addRole(PereJurasique.class);
        addRole(GagGag.class);
        addRole(Many.class);
        addRole(Poilu.class);
        addRole(Putride.class);

    }


    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (uhcPlayer == null || uhcPlayer.getPlayer() == null) {
            return;
        }

        Player player = uhcPlayer.getPlayer();
        Location location = player.getLocation();
        World world = Common.get().getArena();

        if (world == null) {
            player.sendMessage(ChatColor.RED + "Erreur : le monde 'arena' n'est pas chargé !");
            return;
        }

        meurt.put(uhcPlayer, true);
        ItemStack[] inventoryContents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        event.getDrops().clear();
        player.teleport(Common.get().getLobbySpawn());
        player.sendMessage(ChatColor.RED + "Vous êtes mort, mais vous pouvez toujours être ressuscité.");

        List<UHCPlayer> putri = getPlayersByRoleName("Putride");
        if (getRoleByUHCPlayer(killer).getCamps() == "mamouth" && killer != null) {
            for (UHCPlayer p : putri) {
                if (p.getPlayer() != null) {
                    TextComponent base = new TextComponent("[§aPrivée§f] Un joueur est mort vous pouvez le ");

                    TextComponent msg = new TextComponent("§a§lL'infecter");

                    msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            "§aVous allez infecter ." + uhcPlayer.getPlayer().getName()

                    ).create()));

                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/c infect " + uhcPlayer.getPlayer().getName()));


                    TextComponent space = new TextComponent(" §9ou ne rien faire. Vous avez 6s pour faire un choix");

                    p.getPlayer().spigot().sendMessage(base, msg, space);

                }
            }
        }
        List<UHCPlayer> patriarches = getPlayersByRoleName("patriarche");
        if (patriarches != null && !patriarches.isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!meurt.get(uhcPlayer)) {
                        cancel();
                    }
                    for (UHCPlayer p : patriarches) {
                        if (p.getPlayer() != null) {
                            TextComponent base = new TextComponent("[§aPrivée§f] Un joueur est mort vous pouvez le ");

                            TextComponent msg = new TextComponent("§a§lRessusciter");

                            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                    "§aVous allez ressuciter ." + uhcPlayer.getPlayer().getName()

                            ).create()));

                            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/c revive " + uhcPlayer.getPlayer().getName()));


                            TextComponent space = new TextComponent(" §9ou ne rien faire. Vous avez 6s pour faire un choix");

                            p.getPlayer().spigot().sendMessage(base, msg, space);

                        }
                    }
                }
            }.runTaskLater(Main.get(), 120);

        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!meurt.containsKey(uhcPlayer)) {
                    cancel();
                    return;
                }

                if (meurt.get(uhcPlayer)) {
                    for (ItemStack item : inventoryContents) {
                        if (item != null && item.getType() != Material.AIR) {
                            if (!(item.getType() == Material.NETHER_STAR)) {
                                world.dropItemNaturally(location, item);
                            }
                        }
                    }

                    for (ItemStack item : armorContents) {
                        if (item != null && item.getType() != Material.AIR) {
                            world.dropItemNaturally(location, item);
                        }
                    }

                    String message = "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml" + "§8§m----------§r\n" +
                            ChatColor.AQUA + "Les Zoms ont perdu un de leurs membres : §6" + player.getDisplayName() + "\n" +
                            "Il était : " + getRoleByUHCPlayer(uhcPlayer).getName() + "\n" +
                            "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml" + "§8§m----------§r";

                    Bukkit.broadcastMessage(message);
                    if (player != null) {
                        player.setGameMode(GameMode.SPECTATOR);
                    }
                    meurt.remove(uhcPlayer);
                } else {
                    meurt.remove(uhcPlayer);
                    cancel();
                }
            }
        }.runTaskLater(Main.get(), 120 * 2);
    }


    public List<UHCPlayer> MamouthList() {
        List<UHCPlayer> list = new ArrayList<>();
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(uhcPlayer);
            String playerCamp = role.getCamps();
            if (playerCamp != null && playerCamp.equals("mamouth")) {
                list.add(uhcPlayer);
            }
        }

        return list;
    }


    @Override
    public CromagnonRole getRoleByUHCPlayer(UHCPlayer player) {
        return super.getRoleByUHCPlayer(player);
    }

    @Override
    public List<UHCPlayer> getPlayersByRoleName(String name) {
        return super.getPlayersByRoleName(name);
    }

    public boolean isWin() {
        Map<String, Integer> campCounts = new HashMap<>();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = getRoleByUHCPlayer(uhcPlayer);
            String playerCamp = role.getCamps();
            campCounts.put(playerCamp, campCounts.getOrDefault(playerCamp, 0) + 1);
        }
        if (campCounts.size() == 1) {
            String remainingCamp = campCounts.keySet().iterator().next();

            if (isDuoCamp(remainingCamp)) {
                return campCounts.get(remainingCamp) == 2;
            }
            return true;
        }

        for (String camp : campCounts.keySet()) {
            if (isSoloCamp(camp) && campCounts.get(camp) == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isSoloCamp(String camp) {
        return camp.startsWith("Solo");
    }

    private boolean isDuoCamp(String camp) {
        return camp.startsWith("Duo");
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        super.onPlayerInteract(player, event);
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        int timer = UHCManager.get().getTimer();
        int pvp = UHCManager.get().getTimerpvp();

        if (timer == pvp + 3000 && !listsend) {
            listsend = true;
            List<UHCPlayer> mamou = MamouthList();
            mamou.stream().forEach(uhcPlayer -> {
                StringBuilder list = new StringBuilder();
                for (UHCPlayer pl : MamouthList()) {
                    list.append(pl.getPlayer().getDisplayName());
                }
                if (getPlayersByRoleName("Many").contains(uhcPlayer)) {
                    Random random = new Random();
                    int chosen = 0;
                    StringBuilder str = new StringBuilder();

                    do {
                        UHCPlayer value = mamou.get(random.nextInt(mamou.size()));
                        str.append(value.getPlayer().getDisplayName()).append(" a le role : ").append(getRoleByUHCPlayer(value).getName());
                        chosen++;
                    } while (chosen < 2);
                    uhcPlayer.getPlayer().sendMessage(str.toString());
                }
                uhcPlayer.getPlayer().sendMessage(ChatColor.RED + "Vos cooequiper sont \n : " + list);
            });

        }
    }

    @Override
    public void onCroCMD(Player player, String subCommand, String[] args) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        switch (subCommand) {

            case "revive":
                if (getRoleByUHCPlayer(p).getName() == "patriarche") {
                    PatriCMD(player, args);
                } else {
                    player.sendMessage("pas patriarche");
                }

                break;
            case "compa":
                if (getRoleByUHCPlayer(p).getName() == "Peintre") {
                    PeintreCMD(player, args);
                } else {
                    player.sendMessage("pas Peintre");
                }

                break;

            case "snif":
                if (getRoleByUHCPlayer(p).getName() == "chasseur") {
                    chasseurCMD(player, args);
                } else {
                    player.sendMessage("pas chasseur");
                }

                break;
            case "role":
                player.sendMessage(getRoleByUHCPlayer(UHCPlayerManager.get().getPlayer(player)).getDescription());
                break;
            case "infect":
                if (getRoleByUHCPlayer(p).getName() == "Putride") {
                    InfectCMD(player, args);
                } else {
                    player.sendMessage("pas putride");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Essayez /c pour plus d'informations.");
        }

    }

    private void InfectCMD(Player player, String[] args) {
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Nom du joueur invalide");
            return;
        }
        UHCPlayer target = UHCPlayerManager.get().getPlayer(targetPlayer);

        Role role = getRoleByUHCPlayer(UHCPlayerManager.get().getPlayer(player));
        if (role == null || role.getPowerUse().isEmpty() || role.getPowerUse().get(0) == 0) {
            player.sendMessage(ChatColor.RED + "Vous n'avez plus de pouvoir ou vous n'avez pas de rôle.");
            return;
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Cible invalide.");
            return;
        }

        if (!meurt.containsKey(target) || !meurt.get(target)) {
            player.sendMessage(ChatColor.RED + "Le joueur ne peut pas être ressuscité.");
            return;
        }
        Role targetrole = getRoleByUHCPlayer(target);
        targetrole.getCamps().replace(targetrole.getCamps(), "mamouth");
        role.getPowerUse().set(0, role.getPowerUse().get(0) - 1);

        meurt.replace(target, false);

        World world = Common.get().getArena();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Le monde 'arena' n'est pas chargé.");
            return;
        }

        WorldBorder worldBorder = world.getWorldBorder();
        Random random = new Random();

        double radius = worldBorder.getSize() / 2;
        double x = worldBorder.getCenter().getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = worldBorder.getCenter().getZ() + (random.nextDouble() * 2 - 1) * radius;
        double y = world.getHighestBlockYAt((int) x, (int) z);
        Location location = new Location(world, x, y, z);

        TeamsTagsManager.setNameTag(target.getPlayer(), "", "", "");
        target.setPlaying(true);
        target.getPlayer().setGameMode(GameMode.SURVIVAL);
        target.getPlayer().teleport(location);
        PlayerInventory inventory = target.getPlayer().getInventory();
        for (ItemStack item : target.getDeathIteam()) {
            if (item != null && item.getType() != Material.AIR) {
                if (isHelmet(item) && inventory.getHelmet() == null) {
                    inventory.setHelmet(item);
                } else if (isChestplate(item) && inventory.getChestplate() == null) {
                    inventory.setChestplate(item);
                } else if (isLeggings(item) && inventory.getLeggings() == null) {
                    inventory.setLeggings(item);
                } else if (isBoots(item) && inventory.getBoots() == null) {
                    inventory.setBoots(item);
                } else {
                    inventory.addItem(item);
                }
            }
        }

        player.sendMessage("Le joueur a bien été ressuscité.");
        target.getPlayer().sendMessage(ChatColor.RED + "N'oubliez pas de refaire votre inventaire !");

    }

    private void chasseurCMD(Player player, String[] args) {


        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Nom du joueur invalide");
            return;
        }
        UHCPlayer target = UHCPlayerManager.get().getPlayer(targetPlayer);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Cible invalide.");
            return;
        }
        Role role = getRoleByUHCPlayer(UHCPlayerManager.get().getPlayer(player));
        Role targetRole = getRoleByUHCPlayer(target);

        if (Objects.equals(role.getCamps(), targetRole.getCamps())) {
            if (LongCooldownManager.get(player.getUniqueId(), "renif") == -1) {
                LongCooldownManager.put(player.getUniqueId(), "renif", 30 * 1000);

                if (Objects.equals(role.getCamps(), targetRole.getCamps())) {
                    player.sendMessage("Vous etes dans meme camps");
                } else {
                    player.sendMessage("pas dans le meme camps");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Veillez a attendre encore : " + LongCooldownManager.get(player.getUniqueId(), "renif") / 1000 + "s");
            }
        }

    }

    private void PeintreCMD(Player player, String[] args) {

        if (args.length < 3) {
            player.sendMessage("/c compa <j1> <j2>");
            return;
        }

        Player target1 = Bukkit.getPlayer(args[1]);
        Player target2 = Bukkit.getPlayer(args[2]);

        if (target1 == null || target2 == null) {
            player.sendMessage(ChatColor.RED + "Verifier l'un des noms des joueur.");
            return;
        }
        UHCPlayer uhcPlayer1 = UHCPlayerManager.get().getPlayer(target1);
        UHCPlayer uhcPlayer2 = UHCPlayerManager.get().getPlayer(target2);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        Role role1 = getRoleByUHCPlayer(uhcPlayer1);
        Role role2 = getRoleByUHCPlayer(uhcPlayer2);
        Role role = getRoleByUHCPlayer(uhcPlayer);
        if (role == null || role.getPowerUse().isEmpty() || role.getPowerUse().get(0) == 0) {
            player.sendMessage(ChatColor.RED + "Vous n'avez plus de pouvoir ou vous n'avez pas de rôle.");
            return;
        }

        if (LongCooldownManager.get(player.getUniqueId(), "peintre") == -1) {
            LongCooldownManager.put(player.getUniqueId(), "peintre", 30 * 1000);
            role.getPowerUse().set(0, role.getPowerUse().get(0) - 1);

            if (role1.getCamps().equals(role2.getCamps()) || role2.getCamps().equals(role1.getCamps())) {
                player.sendMessage("les 2 joueur ont le meme camps");
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60, 1, false, false));
            } else {
                player.sendMessage("pas dans le meme camps");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Veillez a attendre encore : " + LongCooldownManager.get(player.getUniqueId(), "peintre") / 1000 + "s");
        }

    }


    private void PatriCMD(Player player, String[] args) {

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Nom du joueur invalide");
            return;
        }
        UHCPlayer target = UHCPlayerManager.get().getPlayer(targetPlayer);

        Role role = getRoleByUHCPlayer(UHCPlayerManager.get().getPlayer(player));
        if (role == null || role.getPowerUse().isEmpty() || role.getPowerUse().get(0) == 0) {
            player.sendMessage(ChatColor.RED + "Vous n'avez plus de pouvoir ou vous n'avez pas de rôle.");
            return;
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Cible invalide.");
            return;
        }

        if (!meurt.containsKey(target) || !meurt.get(target)) {
            player.sendMessage(ChatColor.RED + "Le joueur ne peut pas être ressuscité.");
            return;
        }

        role.getPowerUse().set(0, role.getPowerUse().get(0) - 1);

        meurt.replace(target, false);

        World world = Common.get().getArena();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Le monde 'arena' n'est pas chargé.");
            return;
        }

        WorldBorder worldBorder = world.getWorldBorder();
        Random random = new Random();

        double radius = worldBorder.getSize() / 2;
        double x = worldBorder.getCenter().getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = worldBorder.getCenter().getZ() + (random.nextDouble() * 2 - 1) * radius;
        double y = world.getHighestBlockYAt((int) x, (int) z);
        Location location = new Location(world, x, y, z);

        TeamsTagsManager.setNameTag(target.getPlayer(), "", "", "");
        target.setPlaying(true);
        target.getPlayer().setGameMode(GameMode.SURVIVAL);
        target.getPlayer().teleport(location);
        PlayerInventory inventory = target.getPlayer().getInventory();
        for (ItemStack item : target.getDeathIteam()) {
            if (item != null && item.getType() != Material.AIR) {
                if (isHelmet(item) && inventory.getHelmet() == null) {
                    inventory.setHelmet(item);
                } else if (isChestplate(item) && inventory.getChestplate() == null) {
                    inventory.setChestplate(item);
                } else if (isLeggings(item) && inventory.getLeggings() == null) {
                    inventory.setLeggings(item);
                } else if (isBoots(item) && inventory.getBoots() == null) {
                    inventory.setBoots(item);
                } else {
                    inventory.addItem(item);
                }
            }
        }

        player.sendMessage("Le joueur a bien été ressuscité.");
        target.getPlayer().sendMessage(ChatColor.RED + "N'oubliez pas de refaire votre inventaire !");
    }


    private boolean isHelmet(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_HELMET || type == Material.GOLD_HELMET ||
                type == Material.CHAINMAIL_HELMET || type == Material.IRON_HELMET ||
                type == Material.DIAMOND_HELMET;
    }

    private boolean isChestplate(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_CHESTPLATE || type == Material.GOLD_CHESTPLATE ||
                type == Material.CHAINMAIL_CHESTPLATE || type == Material.IRON_CHESTPLATE ||
                type == Material.DIAMOND_CHESTPLATE;
    }

    private boolean isLeggings(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_LEGGINGS || type == Material.GOLD_LEGGINGS ||
                type == Material.CHAINMAIL_LEGGINGS || type == Material.IRON_LEGGINGS ||
                type == Material.DIAMOND_LEGGINGS;
    }

    private boolean isBoots(ItemStack item) {
        Material type = item.getType();
        return type == Material.LEATHER_BOOTS || type == Material.GOLD_BOOTS ||
                type == Material.CHAINMAIL_BOOTS || type == Material.IRON_BOOTS ||
                type == Material.DIAMOND_BOOTS;
    }
}
