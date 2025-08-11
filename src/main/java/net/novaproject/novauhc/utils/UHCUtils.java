package net.novaproject.novauhc.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UHCUtils {

    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64Texture));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    public static String applyPlayerPlaceholders(String message, UHCPlayer uhcPlayer) {
        if (uhcPlayer == null) return message;

        return message
                .replace("%kills%", String.valueOf(uhcPlayer.getKill()))
                .replace("%team%", uhcPlayer.getTeam().isPresent() ? uhcPlayer.getTeam().get().getName() : "Solo")
                .replace("%state%", uhcPlayer.getPlayer().getGameMode().toString())
                .replace("%health%", String.valueOf(uhcPlayer.getPlayer().getHealth()))
                .replace("%role%", ScenarioManager.get().getActiveSpecialScenarios().stream().filter(scenario -> scenario instanceof ScenarioRole).findFirst().map(scenario -> ((ScenarioRole<?>) scenario).getRoleByUHCPlayer(uhcPlayer).getName()).orElse("Aucun"))
                .replace("%arrow%", uhcPlayer.getArrowDirection(uhcPlayer.getPlayer().getLocation(), new Location(Common.get().getArena(), 0, 100, 0), uhcPlayer.getPlayer().getLocation().getYaw()));
    }

    public static String translateGamePlaceholders(String text, Object gameData) {
        return text
                .replace("<time>", new SimpleDateFormat("HH:mm:ss").format(new Date()))
                .replace("<date>", new SimpleDateFormat("dd/MM/yyyy").format(new Date()))
                .replace("<max>", String.valueOf(UHCManager.get().getSlot()))
                .replace("<border>", String.valueOf(Common.get().getArena().getWorldBorder().getSize()))
                .replace("<difficulty>", Bukkit.getWorlds().get(0).getDifficulty().name())
                .replace("<timer>", UHCManager.get().getTimerFormatted())
                .replace("<serveurname>", Common.get().getServername())
                .replace("<gamestate>", UHCManager.get().getGameState().name())
                .replace("<players>", String.valueOf(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size()))
                .replace("<maxplayers>", String.valueOf(UHCManager.get().getSlot()));

    }


    public static ItemCreator createCustomButon(String texture, String name, List<String> lore) {
        ItemCreator item = new ItemCreator(createCustomHead(texture));
        item.setName(name);
        if (lore != null) item.setLores(lore);
        return item;
    }

    public static String getFormattedTime(int seconds) {
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static ItemCreator redMinus(List<String> lore, String name) {

        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=;";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator greenPlus(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkwZjYyZWM1ZmEyZTkzZTY3Y2YxZTAwZGI4YWY0YjQ3YWM3YWM3NjlhYTA5YTIwM2ExZjU3NWExMjcxMGIxMCJ9fX0=";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator arrowDroite(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkwZjYyZWM1ZmEyZTkzZTY3Y2YxZTAwZGI4YWY0YjQ3YWM3YWM3NjlhYTA5YTIwM2ExZjU3NWExMjcxMGIxMCJ9fX0=";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator arrowGauche(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19";
        return createCustomButon(text, name, lore);
    }

    public static String calculPourcentage(double valeur, double total) {
        if (total <= 0) return "0.0%";
        double pourcentage = (valeur / total) * 100;
        return String.format("%.1f%%", pourcentage);
    }

    public static ItemCreator getReset() {
        return new ItemCreator(Material.REDSTONE).setName(ChatColor.RED + "Réinitialisée les valeur.");
    }

    public static List<String> getLoreForCLique(String left, String rigth) {
        return Arrays.asList("", ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + left, ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + rigth, "");

    }
}
