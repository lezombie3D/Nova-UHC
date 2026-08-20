package net.novaproject.novauhc.scenario.role.camps;

import java.util.EnumMap;
import java.util.Map;
import net.novaproject.novauhc.utils.item.ItemCreator.Heads;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;

import java.util.ArrayList;
import java.util.List;

public class CampUtils {

    public static boolean hasSubCamps(Camps parent, Camps[] allCamps) {
        for (Camps c : allCamps) {
            if (!c.isMainCamp() && c.getParent() != null && c.getParent().is(parent)) {
                return true;
            }
        }
        return false;
    }

    public static List<Camps> getSubCamps(Camps parent, Camps[] allCamps) {
        List<Camps> list = new ArrayList<>();
        for (Camps c : allCamps) {
            if (!c.isMainCamp() && c.getParent() != null && c.getParent().is(parent)) {
                list.add(c);
            }
        }
        return list;
    }

    public static List<UHCPlayer> getPlayersInCamp(Camps camp, ScenarioRole scenarioRole) {
        List<UHCPlayer> list = new ArrayList<>();
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = scenarioRole.getRoleByUHCPlayer(player);
            if (role != null && role.getCamp() != null && role.getCamp().is(camp)) list.add(player);
        }
        return list;
    }

    public static boolean roleBelongsToCamp(Role role, Camps parentCamp, Camps[] allCamps) {
        Camps c = role.getCamp();
        if (c == null || parentCamp == null) return false;
        if (c.is(parentCamp)) return true;
        for (Camps sub : getSubCamps(parentCamp, allCamps)) {
            if (c.is(sub)) return true;
        }
        return false;
    }

    public static final class CampColors {

        private static final Map<DyeColor, String> CHAT = new EnumMap<>(DyeColor.class);
        private static final Map<DyeColor, String> SKULLS = new EnumMap<>(DyeColor.class);

        static {
            CHAT.put(DyeColor.WHITE, "§f");
            CHAT.put(DyeColor.ORANGE, "§6");
            CHAT.put(DyeColor.PINK, "§d");
            CHAT.put(DyeColor.LIGHT_BLUE, "§b");
            CHAT.put(DyeColor.YELLOW, "§e");
            CHAT.put(DyeColor.LIME, "§a");
            CHAT.put(DyeColor.GRAY, "§8");
            CHAT.put(DyeColor.CYAN, "§3");
            CHAT.put(DyeColor.PURPLE, "§5");
            CHAT.put(DyeColor.BLUE, "§9");
            CHAT.put(DyeColor.GREEN, "§2");
            CHAT.put(DyeColor.RED, "§c");

            SKULLS.put(DyeColor.WHITE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg0YTEzNWYwZWM3YzBjMzcyZjY3ZDdmYjU3NWYwN2QxNGY5Yzk3NWI1Mjg4ZWQzNzRhZDFhZjI4ZTJiNjExZiJ9fX0=");
            SKULLS.put(DyeColor.ORANGE,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYzMTE1OWIyYmI3NDg1NGIyYTM3NzM3MDE2ZjBhNzM1OTM2MDAwNDlhMWYyNjRmZWRhNWRiNzY5ZDMwN2I1YyJ9fX0=");
            SKULLS.put(DyeColor.PINK,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIyMTAwNTFmZjRmZjVhNzM5ZGY5NDUxYzc5YWYwZmQwYTNiMDU1NmMxMzg5M2Y3Yzk2YWY1OTk3ZDAxNjY1MiJ9fX0=");
            SKULLS.put(DyeColor.LIGHT_BLUE,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIyMTAwNTFmZjRmZjVhNzM5ZGY5NDUxYzc5YWYwZmQwYTNiMDU1NmMxMzg5M2Y3Yzk2YWY1OTk3ZDAxNjY1MiJ9fX0=");
            SKULLS.put(DyeColor.YELLOW,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTViYWNkMWQ3ZTY3NDZjN2YyNjg4ZDE4NDE3OGM2MjVlNzFjOWFhMTczMWUwMmQ3ZGZlNzg3NDlhNzU0YWY0MSJ9fX0=");
            SKULLS.put(DyeColor.LIME,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzExMDFjNDk4YWFhYzkzZTc2YjhmMTZkMjljYmZhNDczZWQyNGQ5YzZjNzU1NjNjMjdlZWRkNDljOTYzZTk4YiJ9fX0=");
            SKULLS.put(DyeColor.GRAY,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmZkZjZkMzMyOTRkZWMwM2Y0NDlhOTdjMGZkY2E3MDE3NzNmMmUyMThlYzExNzcxZDZiNDhiYjcxMzIyYWIwMyJ9fX0=");
            SKULLS.put(DyeColor.CYAN,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQ5YzBmNTcxYTViYzAwMTRmMzVjOGQzNzFmMzFmNmI1MWMxNjg2ZDcxMzU5NzU5YmFlODhjNjUzNzQ4MWEzYSJ9fX0=");
            SKULLS.put(DyeColor.PURPLE,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE3ZGM2NDU4MzZiOThkMTg5ZWY2YzA1MmNjMzRkNGQxMjdjOTRhMjEwM2EwNjNjOTExMzQ0MDM3OTE3MDc2OCJ9fX0=");
            SKULLS.put(DyeColor.BLUE,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2NjN2M4ZjdhODliNjhlZWNmOWQ0NDE3Yjg1ODlmOWY2ZDExMTVjYmZmYjYzYjFmMzQ5MWU4YzUyMzM4In19fQ==");
            SKULLS.put(DyeColor.GREEN,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzExMDFjNDk4YWFhYzkzZTc2YjhmMTZkMjljYmZhNDczZWQyNGQ5YzZjNzU1NjNjMjdlZWRkNDljOTYzZTk4YiJ9fX0=");
            SKULLS.put(DyeColor.RED,"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQ3OGNjMzkxYWZmYjgwYjJiMzVlYjczNjRmZjc2MmQzODQyNGMwN2U3MjRiOTkzOTZkZWU5MjFmYmJjOWNmIn19fQ==");
        }

        public static String chatCode(DyeColor color) {
            return color == null ? "§7" : CHAT.getOrDefault(color, "§f");
        }

        public static ItemStack dyeItem(DyeColor color) {
            short data = color == null ? 0 : (short) color.getDyeData();
            return new ItemStack(Material.INK_SACK, 1, data);
        }

        public static void registerSkull(DyeColor color, String base64Texture) {
            if (color != null && base64Texture != null) SKULLS.put(color, base64Texture);
        }

        public static String skullTexture(DyeColor color) {
            return color == null ? null : SKULLS.get(color);
        }

        public static ItemStack skull(DyeColor color) {
            String texture = skullTexture(color);
            return texture != null ? Heads.createCustomHead(texture) : dyeItem(color);
        }
    }
}
