package net.novaproject.ultimate.mysteryteam;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.display.TeamsTagsManager;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.*;
import java.util.stream.Collectors;

public class MysteryTeam extends Scenario {

    private static final Random RANDOM = new Random();

    @Var(name = "§eGive banner on start", desc = "§7Automatically give the team banner at start.", type = VariableType.BOOLEAN)
    private boolean giveBannerOnStart = true;

    @Var(name = "§eReset team on scatter", desc = "§7Remove player team before random assignment.", type = VariableType.BOOLEAN)
    private boolean resetTeamOnScatter = true;

    @Override
    public String getName() {
        return "MysteryTeam";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.MYSTERY_TEAM, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(createCustomBanner());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onStart(Player player) {
        if (player == null) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        LangManager.get().send(
                MysteryTeamLang.FIND_TEAMMATES,
                player,
                Map.of("%team_size%", uhcPlayer.getTeam().map(UHCTeam::teamSize).orElse(0))
        );

        if (giveBannerOnStart && uhcPlayer.getTeam().isPresent()) {
            player.getInventory().addItem(uhcPlayer.getTeam().get().getBanner().getItemstack());
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (uhcPlayer == null || uhcPlayer.getPlayer() == null) return;

        uhcPlayer.getPlayer().teleport(location);

        if (resetTeamOnScatter) {
            uhcPlayer.setTeam(Optional.empty());
        }

        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        chooseTeam(uhcPlayer, teams);
    }

    private void chooseTeam(UHCPlayer uhcPlayer, List<UHCTeam> teams) {
        List<UHCTeam> availableTeams = teams.stream()
                .filter(team -> team.getPlayers().size() < team.teamSize())
                .collect(Collectors.toList());

        if (availableTeams.isEmpty()) return;

        UHCTeam randomTeam = availableTeams.get(RANDOM.nextInt(availableTeams.size()));

        uhcPlayer.setTeam(Optional.of(randomTeam));
        TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), "", "", "");
    }

    private ItemStack createCustomBanner() {
        ItemStack banner = new ItemStack(Material.BANNER, 1, (short) 15);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        if (meta == null) return banner;

        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.CIRCLE_MIDDLE));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.SQUARE_BOTTOM_RIGHT));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.SQUARE_BOTTOM_RIGHT));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.STRIPE_TOP));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE));
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.STRIPE_DOWNLEFT));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
        meta.addPattern(new Pattern(DyeColor.ORANGE, PatternType.TRIANGLES_BOTTOM));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.CURLY_BORDER));
        meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.CURLY_BORDER));

        banner.setItemMeta(meta);
        return banner;
    }
}

