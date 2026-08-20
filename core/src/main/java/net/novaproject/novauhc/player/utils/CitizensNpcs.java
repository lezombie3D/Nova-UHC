package net.novaproject.novauhc.player.utils;

import com.mojang.authlib.properties.Property;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public final class CitizensNpcs {

    private CitizensNpcs() {}

    public static boolean present() {
        return Bukkit.getPluginManager().getPlugin("Citizens") != null;
    }

    public static LivingEntity spawnBody(Player player) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
        npc.setProtected(false);
        applySkin(npc, player);
        if (!npc.spawn(player.getLocation())) {
            npc.destroy();
            return null;
        }
        applyEquipment(npc, player.getInventory());
        return npc.getEntity() instanceof LivingEntity body ? body : null;
    }

    public static void remove(LivingEntity body) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(body);
        if (npc != null) npc.destroy();
    }

    private static void applySkin(NPC npc, Player player) {
        for (Property property : ((CraftPlayer) player).getHandle().getProfile().getProperties().get("textures")) {
            SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
            skin.setFetchDefaultSkin(false);
            skin.setShouldUpdateSkins(false);
            skin.setSkinPersistent(player.getName().toLowerCase(), property.getSignature(), property.getValue());
            return;
        }
    }

    private static void applyEquipment(NPC npc, PlayerInventory inventory) {
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HELMET, inventory.getHelmet());
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, inventory.getChestplate());
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, inventory.getLeggings());
        equipment.set(Equipment.EquipmentSlot.BOOTS, inventory.getBoots());
        equipment.set(Equipment.EquipmentSlot.HAND, inventory.getItemInHand());
    }
}
