package net.novaproject.novauhc.uhcteam;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public record UHCTeam(DyeColor dyeColor, String prefix, String name, Pattern[] patterns, int teamSize,
                      boolean isCustom) {

    public UHCTeam(DyeColor dyeColor, String prefix, String name, Pattern[] patterns, int teamSize, boolean isCustom) {
        this.dyeColor = dyeColor;
        this.prefix = prefix;
        this.name = name;
        this.patterns = patterns;
        this.teamSize = teamSize;
        this.isCustom = isCustom;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getTeam(name) == null) {
            Team team = scoreboard.registerNewTeam(name);
            team.setPrefix(prefix);
            team.setAllowFriendlyFire(true);
        }
    }

    public Team getTeam() {

        return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name);

    }

    public ItemStack getItem() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (int i = 0; i < teamSize(); i++) {
            lore.add("§b➤ " + (getPlayers().size() < i + 1 ? "" : getPlayers().get(i).getPlayer().getName()));
        }
        lore.add("");
        lore.add(CommonString.CLICK_HERE_TO_APPLY.getMessage());
        lore.add("");

        ItemStack banner = new ItemStack(Material.BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(name());
        meta.setLore(lore);
        meta.setBaseColor(dyeColor);

        List<Pattern> p = new ArrayList<>();
        for (Pattern pattern : patterns) {
            p.add(new Pattern(pattern.getColor() == DyeColor.BLACK ? dyeColor : pattern.getColor(), pattern.getPattern()));
        }
        meta.setPatterns(p);
        banner.setItemMeta(meta);
        return banner;
    }


    public ItemCreator getBanner() {
        ItemStack banner = new ItemStack(Material.BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(name());
        meta.setBaseColor(dyeColor);

        List<Pattern> p = new ArrayList<>();
        for (Pattern pattern : patterns) {
            p.add(new Pattern(pattern.getColor() == DyeColor.BLACK ? dyeColor : pattern.getColor(), pattern.getPattern()));
        }
        meta.setPatterns(p);
        banner.setItemMeta(meta);
        return new ItemCreator(banner);
    }


    public List<UHCPlayer> getPlayers() {

        List<UHCPlayer> result = new ArrayList<>();
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {

            if (player.getTeam().isPresent() && player.getTeam().get() == this) {

                result.add(player);

            }

        }

        return result;
    }

    public boolean isAlive() {

        boolean alive = false;
        for (UHCPlayer uhcPlayer : getPlayers()) {
            if (uhcPlayer.isPlaying()) {
                alive = true;
                break;
            }
        }

        return alive;
    }
}
