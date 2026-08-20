package net.novaproject.ultimate.nuzlocke.roles.dark;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.ultimate.nuzlocke.NuzlockeLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.nuzlocke.NuzlockeRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Dark extends NuzlockeRole {

    @Var(name = "Nuzlocke Dark Blind", type = VariableType.ABILITY)
    private Ability blindness;

    @Var(name = "Nuzlocke Dark Sound", type = VariableType.ABILITY)
    private Ability sound;

    @Var(name = "Nuzlocke Dark Envmelee", type = VariableType.ABILITY)
    private Ability envMelee;

    public Dark() {
        this.blindness = new DarkBlindnessBow();
        this.sound = new DarkSoundCommand();
        this.envMelee = new DarkNightBuffMeleeListener();
    }

    @Override public int getId() { return 8; }
    @Override public String getName() { return "Dark"; }
    @Override public String getTypeColor() { return "§8"; }
    @Override public Material getIconMaterial() { return Material.OBSIDIAN; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.OBSIDIAN).setName("§8§lDark"); }
    @Override public Lang getDescriptionLang() { return NuzlockeLang.ROLE_DESC_DARK; }

    public static boolean isExposedToSun(Player p) {
        long time = p.getWorld().getTime();
        boolean day = time < 13000 || time > 23499;
        if (!day) return false;
        return p.getWorld().getHighestBlockYAt(p.getLocation()) <= p.getLocation().getBlockY();
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;
        boolean sunExposed = isExposedToSun(owner);
        if (sunExposed) {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, true, false),
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, true, false),
                    new PotionEffect(PotionEffectType.SLOW, 80, 0, true, false)
            }, owner);
        } else {
            UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                    new PotionEffect(PotionEffectType.NIGHT_VISION, 80, 0, true, false)
            }, owner);
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false), true);
        }
    }
}

