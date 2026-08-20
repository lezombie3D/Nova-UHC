package net.novaproject.ultimate.nuzlocke.roles.ground;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GroundDigUse extends UseAbility implements ProjectileImpacting {

    @Var(name = "Nuzlocke Ground Dig Cd", type = VariableType.TIME)
    private int cdSeconds = 20;

    @Var(name = "Nuzlocke Ground Dig Cd Fat", type = VariableType.TIME)
    private int fatCdSeconds = 10;

    @Var(name = "Nuzlocke Ground Dig Radius", type = VariableType.INTEGER)
    private int radius = 2;

    @Var(name = "Nuzlocke Ground Dig Fat Radius", type = VariableType.INTEGER)
    private int fatRadius = 3;

    @Var(name = "Nuzlocke Ground Dig Dur", type = VariableType.INTEGER)
    private int dirtDurationTicks = 40;

    @Var(name = "Nuzlocke Ground Dig Fat Stacks", type = VariableType.INTEGER)
    private int fatStacks = 10;

    private final Set<UUID> activeShots = new HashSet<>();

    public GroundDigUse() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Dig"; }
    @Override public Material getMaterial() { return Material.DIAMOND_SPADE; }

    @Override
    public ItemStack getItemStack() {
        return ItemCreator.AbilityNbt.stamp(new ItemCreator(getMaterial())
                .addEnchantment(Enchantment.DIG_SPEED, 5)
                .addEnchantment(Enchantment.DURABILITY, 3)
                .setName(itemName(getName()))
                .setUnbreakable(true)
                .getItemstack(), abilityId());
    }

    @Override
    public boolean onEnable(Player player) {
        boolean fat = countDirt(player) >= fatStacks * 64;
        setCooldown(fat ? fatCdSeconds : cdSeconds);
        Snowball sb = player.launchProjectile(Snowball.class);
        sb.setShooter(player);
        activeShots.add(sb.getUniqueId());
        return true;
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!activeShots.remove(event.getEntity().getUniqueId())) return;
        if (!(event.getEntity() instanceof Snowball sb)) return;
        if (!(sb.getShooter() instanceof Player shooter)) return;
        boolean fat = countDirt(shooter) >= fatStacks * 64;
        int r = fat ? fatRadius : radius;
        Location loc = sb.getLocation();
        Set<Block> placed = new HashSet<>();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx*dx + dy*dy + dz*dz > r*r) continue;
                    Block b = loc.getBlock().getRelative(dx, dy, dz);
                    if (b.getType() == Material.AIR) {
                        b.setType(Material.DIRT);
                        placed.add(b);
                    }
                }
            }
        }
        for (Block b : placed) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(),
                    () -> { if (b.getType() == Material.DIRT) b.setType(Material.AIR); },
                    dirtDurationTicks);
        }
    }

    private int countDirt(Player p) {
        int n = 0;
        for (ItemStack it : p.getInventory().getContents()) {
            if (it != null && it.getType() == Material.DIRT) n += it.getAmount();
        }
        return n;
    }
}

