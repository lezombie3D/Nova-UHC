package net.novaproject.ultimate.nuzlocke.roles.psychic;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PsychicMindreadCommand extends CommandAbility {

    @Var(name = "Nuzlocke Psychic Mindread Cd", type = VariableType.TIME)
    private int cdSeconds = 300;

    @Var(name = "Nuzlocke Psychic Alarm Range", type = VariableType.INTEGER)
    private int alarmRange = 100;

    public PsychicMindreadCommand() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getCommandKey() { return "mindread"; }
    @Override public String getName() { return "Mindread"; }

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("§d§lPsychic §8│ §7Usage: §7/mindread <player>");
            return false;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§d§lPsychic §8│ §7Joueur introuvable.");
            return false;
        }
        Inventory copy = Bukkit.createInventory(player, 45, "§d§lMindread " + target.getName());
        for (int i = 0; i < 36; i++) {
            ItemStack it = target.getInventory().getItem(i);
            if (it != null) copy.setItem(i, it.clone());
        }
        ItemStack helm = target.getInventory().getHelmet();
        ItemStack chest = target.getInventory().getChestplate();
        ItemStack legs = target.getInventory().getLeggings();
        ItemStack boots = target.getInventory().getBoots();
        if (helm != null) copy.setItem(36, helm.clone());
        if (chest != null) copy.setItem(37, chest.clone());
        if (legs != null) copy.setItem(38, legs.clone());
        if (boots != null) copy.setItem(39, boots.clone());
        player.openInventory(copy);

        if (player.getWorld().equals(target.getWorld())
                && player.getLocation().distance(target.getLocation()) <= alarmRange) {
            target.playSound(target.getLocation(), Sound.NOTE_PLING, 1.0f, 0.5f);
            target.sendMessage("§c§l⚠ §fQuelqu'un fouille votre esprit…");
        }
        return true;
    }
}

