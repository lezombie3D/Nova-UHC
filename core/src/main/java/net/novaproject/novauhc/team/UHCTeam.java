package net.novaproject.novauhc.team;

import java.util.Objects;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
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
import java.util.Map;

public record UHCTeam(DyeColor dyeColor, String prefix, String name, Pattern[] patterns, int teamSize,
                      boolean isCustom) implements TeamSpi.IUHCTeam {

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
        List<UHCPlayer> players = getPlayers();
        List<String> lore = new ArrayList<>();
        lore.add("");
        if(teamSize <= 8){
            for (int i = 0; i < teamSize(); i++) {
                lore.add("§b➤ " + (players.size() < i + 1 ? "" : players.get(i).getPlayer().getName()));
            }
        }else{
            lore.add(LangManager.get().get(CoreLang.COMMON_TEAM_PLAYER_COUNT,
                    Map.of("%count%", players.size(), "%max%", teamSize())));
        }

        lore.add("");
        lore.add(LangManager.get().get(CoreLang.COMMON_CLICK_HERE_TO_APPLY));
        lore.add("");

        ItemStack banner = new ItemStack(Material.BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setDisplayName(prefix);
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
        return !getPlayers().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UHCTeam other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}