package net.novaproject.ultimate.nuzlocke.roles.electric;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Electric extends NuzlockeRole {

    @Var(name = "Nuzlocke Electric Paralysis", type = VariableType.ABILITY)
    private Ability paralysis;

    @Var(name = "Nuzlocke Electric Motor", type = VariableType.ABILITY)
    private Ability motor;

    @Var(name = "Nuzlocke Electric Vuln", type = VariableType.ABILITY)
    private Ability vulnerability;

    @Var(name = "Nuzlocke Electric Storm", type = VariableType.ABILITY)
    private Ability storm;

    @Var(name = "Nuzlocke Electric Bedrock Range", type = VariableType.INTEGER)
    private int bedrockRange = 4;

    @Var(name = "Nuzlocke Electric Obsidian Range", type = VariableType.INTEGER)
    private int obsidianRange = 1;

    public Electric() {
        this.paralysis = new ElectricParalysisBow();
        this.motor = new ElectricMotorDriveUse();
        this.vulnerability = new ElectricVulnerableListener();
        this.storm = new ElectricStormEnv();
    }

    @Override public int getId() { return 9; }
    @Override public String getName() { return "Electric"; }
    @Override public String getTypeColor() { return "§e"; }
    @Override public Material getIconMaterial() { return Material.REDSTONE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.REDSTONE).setName("§e§lElectric"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_ELECTRIC; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, 80, 1, true, false)
        }, owner);
        Block c = owner.getLocation().getBlock();
        boolean nearBedrock = isNear(c, Material.BEDROCK, bedrockRange);
        boolean nearObsi = isNear(c, Material.OBSIDIAN, obsidianRange);
        if (nearBedrock || nearObsi) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, true, false), true);
        }
    }

    private boolean isNear(Block c, Material m, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (c.getRelative(dx, dy, dz).getType() == m) return true;
                }
            }
        }
        return false;
    }
}

