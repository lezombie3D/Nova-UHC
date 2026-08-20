package net.novaproject.ultimate.legend.roles;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.ultimate.legend.LegendRole;
import net.novaproject.ultimate.legend.roles.abilities.CavalierHorseActive;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Cavalier extends LegendRole {

    @Var(name = "Horse", type = VariableType.ABILITY)
    public Ability horseActive;

    public Cavalier() {
        this.horseActive = new CavalierHorseActive();
    }

    @Override public int getId() { return 11; }
    @Override public String getName() { return "Cavalier"; }
    @Override public Material getIconMaterial() { return Material.SADDLE; }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.SADDLE); }
    @Override public Lang getDescriptionLang() { return LegendLang.ROLE_DESC_CAVALIER; }

    @Override
    public void onGive(UHCPlayer u) {
        super.onGive(u); Player p = u.getPlayer(); if (p == null) return;
        p.getInventory().addItem(new ItemCreator(Material.DIAMOND_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 1)
                .setName("§6§lLance Royale").setUnbreakable(true).getItemstack());
    }
}

