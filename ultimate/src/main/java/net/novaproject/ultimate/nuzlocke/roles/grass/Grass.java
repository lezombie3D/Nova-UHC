package net.novaproject.ultimate.nuzlocke.roles.grass;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Grass extends NuzlockeRole {

    @Var(name = "Nuzlocke Grass Nearme", type = VariableType.ABILITY)
    private Ability nearme;

    @Var(name = "Nuzlocke Grass Apple", type = VariableType.ABILITY)
    private Ability apple;

    @Var(name = "Nuzlocke Grass Burn", type = VariableType.ABILITY)
    private Ability burn;

    @Var(name = "Nuzlocke Grass Arrow", type = VariableType.ABILITY)
    private Ability arrowVuln;

    @Var(name = "Nuzlocke Grass Hydration Seconds", type = VariableType.TIME)
    private int hydrationSeconds = 300;

    private final transient Map<UUID, Integer> lastWater = new HashMap<>();

    public Grass() {
        this.nearme = new GrassNearCommand();
        this.apple = new GrassAppleDropEnv();
        this.burn = new GrassBurnVulnerableListener();
        this.arrowVuln = new GrassArrowVulnerableListener();
    }

    @Override public int getId() { return 2; }
    @Override public String getName() { return "Grass"; }
    @Override public String getTypeColor() { return "§a"; }
    @Override public Material getIconMaterial() { return Material.SAPLING; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.SAPLING).setName("§a§lGrass"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_GRASS; }

    public int getHydrationSeconds() { return hydrationSeconds; }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;

        Block at = owner.getLocation().getBlock();
        Block under = at.getRelative(0, -1, 0);

        boolean onGrass = under.getType() == Material.GRASS || under.getType() == Material.LEAVES
                || under.getType() == Material.LEAVES_2;
        if (onGrass) {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false)
            }, owner);
        } else {
            owner.removePotionEffect(PotionEffectType.SPEED);
        }

        boolean inWater = at.getType() == Material.WATER || at.getType() == Material.STATIONARY_WATER;
        int timer = net.novaproject.novauhc.UHCManager.get().getTimer();
        if (inWater) {
            lastWater.put(owner.getUniqueId(), timer);
            owner.removePotionEffect(PotionEffectType.SLOW);
        } else {
            int last = lastWater.getOrDefault(owner.getUniqueId(), timer);
            if (timer - last >= hydrationSeconds) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, true, false));
            }
        }
    }
}

