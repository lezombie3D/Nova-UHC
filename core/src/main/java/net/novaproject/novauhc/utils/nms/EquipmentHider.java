package net.novaproject.novauhc.utils.nms;

import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.UHCManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Field;
import java.util.logging.Level;
import org.bukkit.inventory.ItemStack;

public class EquipmentHider implements Listener {

    private static final String HANDLER_NAME = "novauhc_equipment_hider";
    private static Field entityIdField;
    private static Field slotField;
    private static Field itemField;
    private static boolean warned = false;
    private static final java.util.Set<Integer> armorHiddenEntities = java.util.concurrent.ConcurrentHashMap.newKeySet();

    static {
        try {
            entityIdField = PacketPlayOutEntityEquipment.class.getDeclaredField("a");
            entityIdField.setAccessible(true);
            slotField = PacketPlayOutEntityEquipment.class.getDeclaredField("b");
            slotField.setAccessible(true);
            itemField = PacketPlayOutEntityEquipment.class.getDeclaredField("c");
            itemField.setAccessible(true);
        } catch (Throwable t) {
            entityIdField = null;
            slotField = null;
            itemField = null;
        }
    }

    public static void setArmorHidden(Player target, boolean hidden) {
        if (target == null) return;
        if (hidden) {
            armorHiddenEntities.add(target.getEntityId());
            broadcastArmor(target, true);
        } else {
            armorHiddenEntities.remove(target.getEntityId());
            broadcastArmor(target, false);
        }
    }

    public static void clearArmorHidden() {
        armorHiddenEntities.clear();
    }

    private static void broadcastArmor(Player target, boolean asAir) {
        ItemStack[] armor = target.getInventory().getArmorContents();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target) || !viewer.getWorld().equals(target.getWorld())) continue;
            for (int slot = 1; slot <= 4; slot++) {
                ItemStack piece = asAir ? null : armor[slot - 1];
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(piece);
                PacketPlayOutEntityEquipment packet =
                        new PacketPlayOutEntityEquipment(target.getEntityId(), slot, nms);
                ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    private static boolean isEnabled() {
        return UHCManager.get().isAbilityItemsHideFromOthers();
    }

    private static boolean isAbilityItem(ItemStack item) {
        return ItemCreator.AbilityNbt.isAbilityItem(item);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        inject(event.getPlayer());
    }

    public static void inject(Player player) {
        if (entityIdField == null || itemField == null) return;
        try {
            Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
            if (channel == null || channel.pipeline().get(HANDLER_NAME) != null) return;

            final int selfId = player.getEntityId();
            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    if (msg instanceof PacketPlayOutEntityEquipment packet) {
                        try {
                            int entityId = entityIdField.getInt(packet);
                            if (entityId != selfId) {
                                Object nmsItem = itemField.get(packet);
                                if (nmsItem instanceof net.minecraft.server.v1_8_R3.ItemStack nmsStack) {
                                    int slot = slotField.getInt(packet);
                                    if (armorHiddenEntities.contains(entityId) && slot >= 1 && slot <= 4) {
                                        return;
                                    }
                                    ItemStack bukkit = CraftItemStack.asBukkitCopy(nmsStack);
                                    if (isEnabled() && isAbilityItem(bukkit)) {
                                        return;
                                    }
                                }
                            }
                        } catch (Throwable error) {
            DebugLog.warnOnce("EquipmentHider", "echec silencieux", error);
                        }
                    }
                    super.write(ctx, msg, promise);
                }
            });
        } catch (Throwable t) {
            if (!warned) {
                warned = true;
                Bukkit.getLogger().log(Level.WARNING,
                        "[EquipmentHider] injection pipeline impossible (avertissement unique) : " + t.getMessage(), t);
            }
        }
    }
}

