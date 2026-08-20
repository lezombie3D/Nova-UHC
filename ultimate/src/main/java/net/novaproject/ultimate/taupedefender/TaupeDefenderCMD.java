package net.novaproject.ultimate.taupedefender;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TaupeDefenderCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        Player player = (Player) args.getSender();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        TaupeDefender scenario = ScenarioManager.get().getScenario(TaupeDefender.class);

        if (!scenario.isActive()) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            return;
        }

        if (scenario.getDefTeam() == null || !scenario.getDefTeam().getPlayers().contains(uhcPlayer)) {
            LangManager.get().send(TaupeDefenderLang.NOT_DEFENDER_CMD, player);
            return;
        }

        if (args.getArguments().length == 0) {
            LangManager.get().send(TaupeDefenderLang.UNKNOWN_COMMAND, player);
            return;
        }

        switch (args.getLastArgument()) {
            case "reveal":
                handleReveal(uhcPlayer);
                break;
            case "claim":
                handleClaim(uhcPlayer);
                break;
            default:
                LangManager.get().send(TaupeDefenderLang.UNKNOWN_COMMAND, player);
        }
    }

    private void handleReveal(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        TaupeDefender scenario = ScenarioManager.get().getScenario(TaupeDefender.class);

        if (scenario.getRevealedDefenders().contains(uhcPlayer)) {
            LangManager.get().send(TaupeDefenderLang.REVEAL_ALREADY_REVEALED, player);
            return;
        }

        scenario.getRevealedDefenders().add(uhcPlayer);

        if (uhcPlayer.getTeam().isPresent()) {
            TeamsTagsManager.setNameTag(player,
                    uhcPlayer.getTeam().get().name(),
                    "[§9" + uhcPlayer.getTeam().get().name() + "§r] ",
                    "");
        }

        Bukkit.broadcastMessage(LangManager.get().get(TaupeDefenderLang.DEFENDER_REVEALED_BROADCAST,
                Map.of("%player%", player.getName())));

        player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
    }

    private void handleClaim(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        TaupeDefender scenario = ScenarioManager.get().getScenario(TaupeDefender.class);

        if (scenario.getClaimedKit().contains(uhcPlayer)) {
            LangManager.get().send(TaupeDefenderLang.KIT_ALREADY_CLAIMED, player);
            return;
        }

        scenario.getClaimedKit().add(uhcPlayer);
        Integer kitNumber = scenario.getKit().get(uhcPlayer);
        if (kitNumber == null) return;

        giveKit(uhcPlayer, kitNumber);
        LangManager.get().send(TaupeDefenderLang.KIT_RECEIVED, player);
    }

    private void giveKit(UHCPlayer uhcPlayer, int kitNumber) {
        Player player = uhcPlayer.getPlayer();
        Inventory inventory = player.getInventory();

        switch (kitNumber) {
            case 0:
                inventory.addItem(
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_DAMAGE, uhcPlayer.getEnchantLimits().get(Enchants.POWER)).getItemstack(),
                        new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.ARROW_KNOCKBACK, uhcPlayer.getEnchantLimits().get(Enchants.KNOCKBACK)).getItemstack(),
                        new ItemCreator(Material.ARROW).setAmount(64).getItemstack(),
                        new ItemCreator(Material.STRING).setAmount(3).getItemstack()
                );
                uhcPlayer.setEnchantLimit(Enchants.POWER, uhcPlayer.getEnchantLimits().get(Enchants.POWER) + 1);
                uhcPlayer.setEnchantLimit(Enchants.KNOCKBACK, uhcPlayer.getEnchantLimits().get(Enchants.KNOCKBACK) + 1);
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
                uhcPlayer.setEnchantLimit(Enchants.FIRE_ASPECT, uhcPlayer.getEnchantLimits().get(Enchants.FIRE_ASPECT) + 1);
                uhcPlayer.setEnchantLimit(Enchants.FLAME, uhcPlayer.getEnchantLimits().get(Enchants.FLAME) + 1);
                break;
            case 6:
                inventory.addItem(
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60 * 20, 1, false, false), false).setName(ChatColor.GRAY + "Invisibilité 2").getItemstack(),
                        new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 8 * 60 * 20, 0, false, false), false).setName(ChatColor.RED + "Force 1").getItemstack()
                );
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments commandArguments) {
        if (commandArguments.getArguments().length == 1) {
            return Arrays.asList("reveal", "claim");
        }
        return Collections.emptyList();
    }
}

