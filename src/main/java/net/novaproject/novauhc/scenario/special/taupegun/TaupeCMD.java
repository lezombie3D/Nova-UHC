package net.novaproject.novauhc.scenario.special.taupegun;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.MessageUtils;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;


public class TaupeCMD extends Command {
    public TaupeGun taupe;
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Utilisation de la commande : \n" +

                "/t tc : Envoie vos coordonnée à votre équipe de pas Taupe.\n" +
                "/t ti : Ouvrir l'inventaire de team de l'équipe de pas Taupe.\n" +
                "/t kit : Vous permet de récupérer votre kit.\n" +
                "/t reveal : Vous affiche en tant que Taupe au yeux de tout les joueur.\n" +
                "/tc : Si vous êtes une taupe vous permet d'envoyer vous coordonnée a votre équipe de taupe.\n" +
                "/ti : Si vous êtes une taupe vous permet d'accédez au TI de votre équipe de taupe.\n"

        );

    }

    @Override
    public void execute(CommandArguments args) {
        Player player = (Player) args.getSender();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (args.getArguments().length == 0) {
            sendHelpMessage(player);
            return;
        }
        taupe = ScenarioManager.get().getScenario(TaupeGun.class);
        if (!taupe.isActive()) {
            CommonString.DISABLE_ACTION.send(player);
            return;
        }

        if (!taupe.getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {
            ScenarioLangManager.send(player, TaupeGunLang.NOT_TAUPE_COMMAND_ERROR);
            return;
        }


        switch (args.getLastArgument()) {

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
                ScenarioLangManager.send(player, TaupeGunLang.UNKNOWN_COMMAND);
        }
    }

    private void TaupeRevealManager(UHCPlayer uhcPlayer) {
        if (uhcPlayer.getTeam().isPresent()) {
            if (taupe.getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {

                UHCTeam team = uhcPlayer.getTeam().get();
                TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), team.getName(), "[§c" + team.getName() + "§r] ", "");
                Map<String, Object> placeholders = new HashMap<>();
                placeholders.put("%player%", uhcPlayer.getPlayer().getName());
                Bukkit.broadcastMessage(ScenarioLangManager.get(TaupeGunLang.REVEAL_SUCCESS, uhcPlayer, placeholders));
                uhcPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
            } else {
                ScenarioLangManager.send(uhcPlayer.getPlayer(), TaupeGunLang.REVEAL_NOT_TAUPE);
            }
        }
    }


    private void taupeTiManager(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        UHCTeam team = taupe.getOldTeamforPlayer(uhcPlayer);

        if (ScenarioManager.get().isScenarioActive("TeamInventory")) {
            if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying() && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
                player.openInventory(TeamInv.inventory.get(team));
            } else {
                MessageUtils.sendNotStarted(player);
            }
        } else {
            CommonString.DISABLE_ACTION.send(player);
        }
    }

    private void taupeCoordManager(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        UHCTeam team = taupe.getOldTeamforPlayer(uhcPlayer);
        if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying() && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();

            String coords = ChatColor.GREEN + "Coord : x: " + x + " y: " + y + " z: " + z;
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%co%", coords);

            String teamMessage = ScenarioLangManager.get(TaupeGunLang.TEAM_COORDS_FORMAT, uhcPlayer, placeholders);

            if (team != null) {
                team.getPlayers().forEach(teamPlayer ->
                        teamPlayer.getPlayer().sendMessage(teamMessage));
            }

            player.sendMessage(teamMessage);
        } else {
            MessageUtils.sendNotStarted(player);
        }

    }

    private void TaupeKitManager(UHCPlayer uhcPlayer) {
        int kit = taupe.getKit().get(uhcPlayer);
        Inventory inventory = uhcPlayer.getPlayer().getInventory();
        if (taupe.getCalimed().contains(uhcPlayer)) {
            uhcPlayer.getPlayer().sendMessage("[§cTaupeGun§r] Sois pas gourmant mon cochon :)");
            return;
        }
        taupe.getCalimed().add(uhcPlayer);
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
        ScenarioLangManager.send(uhcPlayer.getPlayer(), TaupeGunLang.KIT_RECEIVED);
    }


    @Override
    public List<String> tabComplete(CommandArguments commandArguments) {
        if (commandArguments.getArguments().length == 1) {
            return Arrays.asList("tc", "ti", "kit", "reveal");
        }
        return Collections.emptyList();
    }
}
