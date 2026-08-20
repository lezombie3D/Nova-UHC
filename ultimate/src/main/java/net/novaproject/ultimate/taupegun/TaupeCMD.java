package net.novaproject.ultimate.taupegun;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.item.ItemCreator;

import net.novaproject.novauhc.display.TeamsTagsManager;
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

    @Override
    public void execute(CommandArguments args) {
        Player player = (Player) args.getSender();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (args.getArguments().length == 0) {
            LangManager.get().send(TaupeGunLang.HELP_MESSAGE, player);
            return;
        }

        taupe = ScenarioManager.get().getScenario(TaupeGun.class);

        if (!taupe.isActive()) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            return;
        }

        if (!taupe.getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {
            LangManager.get().send(TaupeGunLang.NOT_TAUPE_COMMAND_ERROR, player);
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
                taupeKitManager(uhcPlayer);
                break;
            case "reveal":
                taupeRevealManager(uhcPlayer);
                break;
            default:
                LangManager.get().send(TaupeGunLang.UNKNOWN_COMMAND, player);
        }
    }

    private void taupeRevealManager(UHCPlayer uhcPlayer) {
        if (!uhcPlayer.getTeam().isPresent()) return;

        if (taupe.getTeamsTaupe().contains(uhcPlayer.getTeam().get())) {
            UHCTeam team = uhcPlayer.getTeam().get();
            TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), team.name(), "[§c" + team.name() + "§r] ", "");
            Bukkit.broadcastMessage(LangManager.get().get(TaupeGunLang.REVEAL_SUCCESS,
                    Map.of("%player%", uhcPlayer.getPlayer().getName())));
            uhcPlayer.getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
        } else {
            LangManager.get().send(TaupeGunLang.REVEAL_NOT_TAUPE, uhcPlayer.getPlayer());
        }
    }

    private void taupeTiManager(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        UHCTeam team = taupe.getOldTeamforPlayer(uhcPlayer);

        if (ScenarioManager.get().isScenarioActive("TeamInventory")) {
            if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying()
                    && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
                player.openInventory(TeamInv.inventory.get(team));
            } else {

            }
        } else {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
        }
    }

    private void taupeCoordManager(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        UHCTeam team = taupe.getOldTeamforPlayer(uhcPlayer);

        if (!uhcPlayer.getTeam().isPresent() || !uhcPlayer.isPlaying()
                || UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            return;
        }

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        String coords = ChatColor.GREEN + "Coord : x: " + x + " y: " + y + " z: " + z;

        String teamMessage = LangManager.get().get(TaupeGunLang.TEAM_COORDS_FORMAT, player,
                Map.of("%co%", coords));

        if (team != null) {
            team.getPlayers().forEach(tp -> tp.getPlayer().sendMessage(teamMessage));
        }
        player.sendMessage(teamMessage);
    }

    private void taupeKitManager(UHCPlayer uhcPlayer) {
        if (taupe.getCalimed().contains(uhcPlayer)) {
            LangManager.get().send(TaupeGunLang.KIT_ALREADY_CLAIMED, uhcPlayer.getPlayer());
            return;
        }

        taupe.getCalimed().add(uhcPlayer);
        int kit = taupe.getKit().get(uhcPlayer);
        Inventory inventory = uhcPlayer.getPlayer().getInventory();

        switch (kit) {
            case 0:
                inventory.addItem(
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_DAMAGE, uhcPlayer.getEnchantLimits().get(Enchants.POWER)).getItemstack(),
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_KNOCKBACK, uhcPlayer.getEnchantLimits().get(Enchants.KNOCKBACK)).getItemstack(),
                        new ItemCreator(Material.ARROW).setAmount(64).getItemstack(),
                        new ItemCreator(Material.STRING).setAmount(3).getItemstack()
                );
                uhcPlayer.setEnchantLimit(Enchants.POWER,uhcPlayer.getEnchantLimits().get(Enchants.POWER) + 1);
                uhcPlayer.setEnchantLimit(Enchants.KNOCKBACK,uhcPlayer.getEnchantLimits().get(Enchants.KNOCKBACK) + 1);
                break;
            case 1:
                inventory.addItem(
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.PROTECTION_FALL, uhcPlayer.getEnchantLimits().get(Enchants.FEATHER_FALLING)).getItemstack(),
                        new ItemCreator(Material.ENDER_PEARL).setAmount(4).getItemstack()
                );
                uhcPlayer.setEnchantLimit(Enchants.FEATHER_FALLING, uhcPlayer.getEnchantLimits().get(Enchants.FEATHER_FALLING) + 1);
                break;
            case 2:
                inventory.addItem(
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.BLUE + "Speed 1").getItemstack(),
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.GOLD + "FireResistance 1").getItemstack(),
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30 * 20, 0, false, false), true).setName(ChatColor.GREEN + "Poison 1").getItemstack()
                );
                break;
            case 3:
                inventory.addItem(
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, uhcPlayer.getEnchantLimits().get(Enchants.PROTECTION_ENVIRONMENTAL)).getItemstack(),
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.DAMAGE_ALL, uhcPlayer.getEnchantLimits().get(Enchants.SHARPNESS)).getItemstack(),
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_DAMAGE, uhcPlayer.getEnchantLimits().get(Enchants.POWER)).getItemstack()
                );
                uhcPlayer.setEnchantLimit(Enchants.PROTECTION_ENVIRONMENTAL, uhcPlayer.getEnchantLimits().get(Enchants.PROTECTION_ENVIRONMENTAL) + 1);
                uhcPlayer.setEnchantLimit(Enchants.SHARPNESS, uhcPlayer.getEnchantLimits().get(Enchants.SHARPNESS) + 1);
                uhcPlayer.setEnchantLimit(Enchants.POWER, uhcPlayer.getEnchantLimits().get(Enchants.POWER) + 1);
                break;
            case 4:
                inventory.addItem(
                        new ItemCreator(Material.OBSIDIAN).setAmount(14).getItemstack(),
                        new ItemCreator(Material.DIAMOND).setAmount(10).getItemstack(),
                        new ItemCreator(Material.GOLD_INGOT).setAmount(32).getItemstack(),
                        new ItemCreator(Material.IRON_INGOT).setAmount(64).getItemstack()
                );
                break;
            case 5:
                inventory.addItem(
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.FIRE_ASPECT, uhcPlayer.getEnchantLimits().get(Enchants.FIRE_ASPECT)).getItemstack(),
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_FIRE, uhcPlayer.getEnchantLimits().get(Enchants.FLAME)).getItemstack()
                );
                uhcPlayer.setEnchantLimit(Enchants.FIRE_ASPECT, uhcPlayer.getEnchantLimits().get(Enchants.FIRE_ASPECT) + 1 );
                uhcPlayer.setEnchantLimit(Enchants.FLAME, uhcPlayer.getEnchantLimits().get(Enchants.FLAME) + 1);

                break;
            case 6:
                inventory.addItem(
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60 * 20, 1, false, false), false).setName(ChatColor.GRAY + "Invisibilité 2").getItemstack(),
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.RED + "Force 1").getItemstack()
                );
                break;
        }

        LangManager.get().send(TaupeGunLang.KIT_RECEIVED, uhcPlayer.getPlayer());
    }

    @Override
    public List<String> tabComplete(CommandArguments commandArguments) {
        if (commandArguments.getArguments().length == 1) {
            return Arrays.asList("tc", "ti", "kit", "reveal");
        }
        return Collections.emptyList();
    }
}

