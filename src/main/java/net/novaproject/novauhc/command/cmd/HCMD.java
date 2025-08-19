package net.novaproject.novauhc.command.cmd;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.ui.player.LimiteStuffbyPlayerUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.Titles;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

import static net.novaproject.novauhc.utils.UHCUtils.*;

public class HCMD implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }

        Player player = (Player) commandSender;

        if (!player.hasPermission("novauhc.host") && !player.hasPermission("novauhc.cohost")) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();


        switch (subCommand) {
            case "scenario":
                new ScenariosUi(player).open();
                break;
            case "reset":
                UHCManager.get().reset();
                break;
            case "config":
                handleConfigCommand(player);
                break;

            case "bypass":
                toggleBypassMode(player);
                break;

            case "say":
                if (args.length >= 2) {
                    broadcastMessage(args, player);
                } else {
                    CommonString.HOST_SAY_USAGE.send(player);
                }
                break;
            case "title":
                titlesMessage(args, player);
                break;
            case "cohost":
                if (args.length >= 3) {
                    manageCohost(player, args[1].toLowerCase(), args[2]);
                } else {
                    CommonString.HOST_COHOST_USAGE.send(player);
                }
                break;

            case "revive":
                if (args.length >= 2) {
                    manageRevive(player, args);
                }
                break;

            case "heal":
                finalHealManager();
                break;
            case "limite":
                if (args.length >= 2) {
                    limiteManager(player, args);
                }
                break;
            case "forcepvp":
                forcepvp();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + "Le PvP vient d'etre forcé.");
                break;
            case "forcemtp":
                forceMTP();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + "Le meetup vient d'etre forcé.");
                break;
            case "whitelist":
                if (args.length >= 2) {
                    manageWhitelist(player, args);
                } else {
                    player.sendMessage("/h whitelist add/remove <target>");
                    player.sendMessage("/h whitelist list/clear/on/off");
                }
                break;
            case "stuff":
                startSTuff(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Essayez /h pour plus d'informations.");
        }


        return true;
    }

    private void limiteManager(Player player, String[] args) {
        UHCPlayer target = UHCPlayerManager.get().getPlayer(Bukkit.getPlayer(args[1]));
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Nom du joueur invalide");
            return;
        }
        new LimiteStuffbyPlayerUi(player, target.getPlayer()).open();
    }

    private void startSTuff(Player player, String[] args) {
        String arg = args[1];
        if (args.length == 2) {

            switch (arg) {
                case "clear":
                    UHCManager.get().start.clear();
                    player.sendMessage(ChatColor.RED + "Le Stuff de depart est clear.");
                    break;
                case "list":
                    String sb = "";
                    getInventoryContentsAsString(UHCManager.get().start);
                    player.sendMessage(ChatColor.RED + "Le contenu est : \n" +
                            sb);
                    break;
                case "modif":
                    if (UHCManager.get().isLobby()) {
                        TextComponent base = new TextComponent("Modification de l'inventaire de depart : ");
                        TextComponent msg = new TextComponent("§a§lSauvegarder");
                        TextComponent msg2 = new TextComponent(" §fou §c§lAnnuler");

                        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                "§aSauvegarder l'inventaire actuel"

                        ).create()));
                        msg2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                "§aAnnuler la modification de l'inventaire").create()));

                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/h stuff save"));
                        msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/h stuff cancel"));


                        player.spigot().sendMessage(base, msg, msg2);
                        player.setOp(true);
                        player.getInventory().clear();
                        player.setGameMode(GameMode.CREATIVE);
                        restorePlayerInventory(player, UHCManager.get().start);

                    }
                    break;
                case "save":

                    if (UHCManager.get().isLobby()) {
                        UHCManager.get().start = savePlayerInventory(player);
                        player.setGameMode(GameMode.ADVENTURE);
                        player.setOp(false);
                        clearPlayerInventory(player);
                        ItemCreator menuconf = new ItemCreator(Material.REDSTONE_COMPARATOR)
                                .setName(ChatColor.YELLOW + "Configurer");
                        ItemCreator item = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Salle des règles");
                        player.getInventory().setItem(8, item.getItemstack());
                        player.getInventory().setItem(4, menuconf.getItemstack());
                        ItemCreator team = new ItemCreator(Material.BANNER).setName(ChatColor.DARK_PURPLE + "Team");
                        player.getInventory().setItem(0, team.getItemstack());
                        player.sendMessage(ChatColor.GOLD + "L'inventaire de depart a bien été sauvegarder ! ");
                        if (player == PlayerConnectionEvent.getHost()) {
                            player.setOp(true);
                        }
                    }
                    break;
                case "cancel":
                    if (UHCManager.get().isLobby()) {
                        player.setGameMode(GameMode.ADVENTURE);
                        player.setOp(false);
                        clearPlayerInventory(player);
                        ItemCreator menuconf = new ItemCreator(Material.REDSTONE_COMPARATOR)
                                .setName(ChatColor.YELLOW + "Configurer");
                        ItemCreator item = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GOLD + "Salle des règles");
                        player.getInventory().setItem(8, item.getItemstack());
                        player.getInventory().setItem(4, menuconf.getItemstack());
                        ItemCreator team = new ItemCreator(Material.BANNER).setName(ChatColor.DARK_PURPLE + "Team");
                        player.getInventory().setItem(0, team.getItemstack());
                        player.sendMessage(ChatColor.GOLD + "La sauvegarde de l'inventaire de depart a bien été annulé ! ");
                        if (player == PlayerConnectionEvent.getHost()) {
                            player.setOp(true);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

    }

    private void forceMTP() {

        UHCManager.get().setTimerborder(UHCManager.get().getTimer() + 1);

    }

    private void forcepvp() {

        UHCManager.get().setTimerpvp(UHCManager.get().getTimer() + 1);

    }

    private void finalHealManager() {

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            player.getPlayer().setHealth(player.getPlayer().getMaxHealth());
        }

        Bukkit.broadcastMessage(CommonString.HEAL_BROADCAST.getMessage());

    }


    private void sendHelpMessage(Player player) {
        CommonString.HOST_HELP_MESSAGE.send(player);
    }

    private void handleConfigCommand(Player player) {
        UHCManager uhcManager = UHCManager.get();
        if (uhcManager.getGameState() != UHCManager.GameState.INGAME &&
                uhcManager.getGameState() != UHCManager.GameState.SCATTERING) {
            ItemCreator menuConf = new ItemCreator(Material.REDSTONE_COMPARATOR)
                    .setName(ChatColor.YELLOW + "Configurer");
            player.getInventory().setItem(4, menuConf.getItemstack());
            new DefaultUi(player).open();
        } else {
            CommonString.CONFIG_CANNOT_INGAME.send(player);
        }
    }

    private void toggleBypassMode(Player player) {
        UHCPlayer host = UHCPlayerManager.get().getPlayer(player);
        host.setBypassed(!host.isBypassed());
        boolean bypassed = host.isBypassed();

        if (bypassed) {
            CommonString.HOST_BYPASS_ENABLED.send(player);
            player.setGameMode(GameMode.CREATIVE);
        } else {
            CommonString.HOST_BYPASS_DISABLED.send(player);
            GameMode newGameMode = UHCManager.get().isGame() ? GameMode.SURVIVAL : GameMode.ADVENTURE;
            player.setGameMode(newGameMode);
        }
    }

    private void broadcastMessage(String[] args, Player player) {
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.RED + message);
    }

    private void titlesMessage(String[] args, Player player) {
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        for (Player p : Bukkit.getOnlinePlayers()) {
            new Titles().sendTitle(p, message, "", 10);
        }
    }

    private void manageCohost(Player player, String action, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Le joueur " + targetName + " est introuvable.");
            return;
        }

        switch (action) {
            case "add":
                if (target == player) {
                    return;
                }
                target.addAttachment(Main.get(), "novauhc.cohost", true);
                player.sendMessage(ChatColor.GREEN + "Le joueur " + target.getName() + " a été ajouté comme cohost.");
                TeamsTagsManager.setNameTag(target, "cohost", "§f[§5CoHost§f] ", "");
                break;

            case "remove":
                if (target == player) {
                    return;
                }
                target.addAttachment(Main.get(), "novauhc.cohost", false);
                player.sendMessage(ChatColor.GREEN + "Le joueur " + target.getName() + " a été retiré comme cohost.");
                TeamsTagsManager.setNameTag(target, "", "", "");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Usage incorrect : /h cohost <add|remove> <joueur>");
                break;
        }
    }

    private void manageWhitelist(Player player, String[] args) {
        String arg = args[1];
        if (args.length == 2) {
            switch (arg) {
                case "clear":
                    Bukkit.getWhitelistedPlayers().forEach(offlinePlayer -> {
                        offlinePlayer.setWhitelisted(false);
                    });
                    player.sendMessage(ChatColor.RED + "La whitelist est clear.");
                    break;
                case "list":
                    StringBuilder sb = new StringBuilder();
                    for (OfflinePlayer offlinePlayer : Bukkit.getWhitelistedPlayers()) {
                        sb.append(offlinePlayer.getName()).append(" ");
                    }
                    player.sendMessage(ChatColor.RED + "Les joueurs whitelist: \n" +
                            sb);
                    break;
                case "on":
                    if (Bukkit.hasWhitelist()) {
                        player.sendMessage(ChatColor.GREEN + "La WhiteListe est déjà activée.");
                    } else {
                        Bukkit.setWhitelist(true);
                        player.sendMessage(ChatColor.GOLD + "La WhiteListe est désormais activée ! ");
                    }
                    break;
                case "off":
                    if (Bukkit.hasWhitelist()) {
                        player.sendMessage(ChatColor.GOLD + "La WhiteListe est désormais inactive ! ");
                        Bukkit.setWhitelist(false);
                    } else {
                        player.sendMessage(ChatColor.RED + "La WhiteListe est déjà inactive.");
                    }
                    break;
                default:
                    break;
            }
        } else if (args.length == 3) {

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);

            if (offlinePlayer == null) {
                player.sendMessage(ChatColor.RED + "Le joueur est inexistant");
            }

            switch (arg) {
                case "add":
                    if (offlinePlayer.isWhitelisted()) {
                        player.sendMessage(ChatColor.RED + "Le joueur est déjà present");
                        return;
                    }

                    offlinePlayer.setWhitelisted(true);
                    player.sendMessage(ChatColor.RED + "Vous avez ajoutez : " + ChatColor.AQUA + offlinePlayer.getName());
                    break;
                case "remove":
                    if (offlinePlayer.isWhitelisted()) {
                        player.sendMessage(ChatColor.RED + "Le joueur n'est pas WhiteListe");
                    } else {
                        offlinePlayer.setWhitelisted(false);
                        player.sendMessage(ChatColor.RED + "Vous avez enlevez : " + ChatColor.AQUA + offlinePlayer.getName());
                        break;
                    }

            }
        }
    }

    private void manageRevive(Player player, String[] args) {

        UHCPlayer target = UHCPlayerManager.get().getPlayer(Bukkit.getPlayer(args[1]));
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Nom du joueur invalide");
            return;
        }
        Collection<PotionEffect> effect = target.getPlayer().getActivePotionEffects();


        World world = Common.get().getArena();
        WorldBorder worldBorder = world.getWorldBorder();
        Random random = new Random();

        double radius = worldBorder.getSize() / 2;
        double x = worldBorder.getCenter().getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = worldBorder.getCenter().getZ() + (random.nextDouble() * 2 - 1) * radius;
        double y = world.getHighestBlockYAt((int) x, (int) z);

        Location location = new Location(world, x, y, z);

        if (UHCManager.get().getTeam_size() != 1) {
            if (target.getTeam().isPresent()) {
                UHCTeam team = target.getTeam().get();
                target.setTeam(Optional.of(team));
                TeamsTagsManager.setNameTag(target.getPlayer(), team.getName(), team.getPrefix(), "");
                target.setPlaying(true);
                target.getPlayer().setGameMode(GameMode.SURVIVAL);
                target.getPlayer().teleport(location);
                for (PotionEffect e : effect) {
                    target.getPlayer().addPotionEffect(e);
                }
            } else {
                player.sendMessage(ChatColor.RED + " Impossible car la game ont des équipes hors le target n'en a pas");
                return;
            }
        } else {
            TeamsTagsManager.setNameTag(target.getPlayer(), "", "", "");
            target.setPlaying(true);
            target.getPlayer().setGameMode(GameMode.SURVIVAL);
            target.getPlayer().teleport(location);
        }

        for (ItemStack item : target.getDeathIteam()) {
            if (item == null || item.getType() == Material.AIR) continue; // Ignore les items vides

            PlayerInventory inventory = target.getPlayer().getInventory();

            if (isHelmet(item)) {
                if (inventory.getHelmet() == null) {
                    inventory.setHelmet(item);
                    continue;
                }
            } else if (isChestplate(item)) {
                if (inventory.getChestplate() == null) {
                    inventory.setChestplate(item);
                    continue;
                }
            } else if (isLeggings(item)) {
                if (inventory.getLeggings() == null) {
                    inventory.setLeggings(item);
                    continue;
                }
            } else if (isBoots(item)) {
                if (inventory.getBoots() == null) {
                    inventory.setBoots(item);
                    continue;
                }
            }

            inventory.addItem(item);
        }
        Bukkit.broadcastMessage(CommonString.REVIVE_MESSAGE.getMessage(target.getPlayer()));
        target.getPlayer().sendMessage(ChatColor.RED + "Oublier pas de refaire votre Inventaire ! ");
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
