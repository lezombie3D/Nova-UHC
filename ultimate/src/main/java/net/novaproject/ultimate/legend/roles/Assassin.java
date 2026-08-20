package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.AssassinForcePassive;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Assassin extends LegendRole {

    @Var(name = "Assassin Extra Hearts", desc = "Extra hearts (half-hearts).", type = VariableType.INTEGER)
    private int extraHearts = 4;

    @Var(name = "Force", type = VariableType.ABILITY)
    public Ability forcePassive;

    public Assassin() {
        this.forcePassive = new AssassinForcePassive();
    }

    @Override public int getId() { return 1; }
    @Override public String getName() { return "Assassin"; }
    @Override public Material getIconMaterial() { return Material.IRON_SWORD; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.IRON_SWORD); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_ASSASSIN; }

    @Override
    protected String applyPlaceholders(String desc) {
        return desc.replace("%extra_hearts%", String.valueOf(extraHearts / 2));
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        Player p = uhcPlayer.getPlayer(); if (p == null) return;
        p.setMaxHealth(20 + extraHearts); p.setHealth(20 + extraHearts);
        p.getInventory().addItem(new ItemCreator(Material.IRON_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 1)
                .setName("§8§lLame Secrète").setUnbreakable(true).getItemstack());
    }
}

