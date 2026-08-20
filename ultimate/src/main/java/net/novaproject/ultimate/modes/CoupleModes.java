package net.novaproject.ultimate.modes;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.role.Bonds;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CoupleModes {

    private static Player shooterOf(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) return shooter;
        return null;
    }

    private static void forceTeamMode(int minimum) {
        if (UHCManager.get().getTeam_size() < minimum) UHCManager.get().setTeam_size(minimum);
    }

    private static UHCTeam teamWithRoom(int needed) {
        int size = UHCManager.get().getTeam_size();
        if (needed > size) return null;
        for (int attempt = 0; attempt < 2; attempt++) {
            for (UHCTeam team : UHCTeamManager.get().getTeams()) {
                if (size - team.getPlayers().size() >= needed) return team;
            }
            UHCTeamManager.get().createTeam(size);
        }
        return null;
    }

    private static void formTeam(List<UHCPlayer> members) {
        UHCTeam team = teamWithRoom(members.size());
        if (team == null) return;
        for (UHCPlayer member : members) member.setTeam(Optional.of(team));
        if (members.size() == 2) {
            Bonds.link(members.get(0).getUniqueId(), members.get(1).getUniqueId()).announce().apply();
        }
    }

    public static class CupidMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.cupid.desc",
                "§7Chaque flèche qui touche un joueur te rend §d%percent%%§7 de ta vie maximale.");

        @Var(name = "Soin par flèche", desc = "Pourcentage de la vie maximale rendu à chaque flèche qui touche un joueur.", type = VariableType.PERCENTAGE, min = 0, max = 100)
        private int healPercent = 1;

        @Override
        public String getName() {
            return "Cupid";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%percent%", healPercent));
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.BOW);
        }

        @Override
        public Family getFamily() {
            return Family.COMBAT;
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (event.isCancelled() || !(entity instanceof Player victim) || !(damager instanceof Arrow)) return;
            Player shooter = shooterOf(damager);
            if (shooter == null || shooter == victim) return;
            double max = shooter.getMaxHealth();
            shooter.setHealth(Math.min(max, shooter.getHealth() + max * healPercent / 100.0));
        }
    }

    public static class BlindDatesMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.blinddates.desc",
                "§7Chacun reçoit une laine colorée. Au PvP, les porteurs de la même couleur forment une équipe.");
        private static final DynamicLang CLUE = DynamicLang.of("mode.blinddates.clue",
                "§d● §7Garde ta laine : ton rencard porte la §fmême couleur§7. Vous serez en équipe au PvP.");

        @Var(name = "Taille d'un rencard", desc = "Nombre de joueurs regroupés par couleur identique.", min = 2)
        private int dateSize = 2;

        private final Map<UUID, DyeColor> colors = new HashMap<>();

        @Override
        public String getName() {
            return "Blind Dates";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.WOOL);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void enable() {
            super.enable();
            forceTeamMode(dateSize);
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            if (isActive()) forceTeamMode(dateSize);
        }

        @Override
        public void onTeamUpdate() {
            forceTeamMode(dateSize);
        }

        @Override
        public void onStart(Player player) {
            if (!isActive() || !colors.isEmpty()) return;
            List<UHCPlayer> roster = new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers());
            Collections.shuffle(roster);
            DyeColor[] palette = DyeColor.values();
            for (int i = 0; i < roster.size(); i++) {
                Player member = roster.get(i).getPlayer();
                if (member == null) continue;
                DyeColor color = palette[(i / dateSize) % palette.length];
                colors.put(roster.get(i).getUniqueId(), color);
                member.getInventory().addItem(new ItemStack(Material.WOOL, 1, (short) color.getWoolData()));
                member.sendMessage(t(CLUE, member));
            }
        }

        @Override
        public void onPvP() {
            if (!isActive() || colors.isEmpty()) return;
            Map<DyeColor, List<UHCPlayer>> byColor = new LinkedHashMap<>();
            for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                DyeColor color = colors.get(up.getUniqueId());
                if (color != null) byColor.computeIfAbsent(color, c -> new ArrayList<>()).add(up);
            }
            for (List<UHCPlayer> group : byColor.values()) {
                for (int i = 0; i + dateSize <= group.size(); i += dateSize) {
                    formTeam(new ArrayList<>(group.subList(i, i + dateSize)));
                }
            }
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }

        @Override
        public void onStop() {
            super.onStop();
            colors.clear();
        }
    }

    public static class FirstHitLoveMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.firsthitlove.desc",
                "§7Le premier adversaire que tu blesses devient ton coéquipier.");

        @Override
        public String getName() {
            return "First Hit Love";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.DIAMOND_SWORD);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void enable() {
            super.enable();
            forceTeamMode(2);
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            if (isActive()) forceTeamMode(2);
        }

        @Override
        public void onTeamUpdate() {
            forceTeamMode(2);
        }

        @Override
        public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
            if (!isActive()) return;
            if (event.isCancelled() || !UHCManager.get().isGame() || !(entity instanceof Player victim)) return;
            Player attacker = shooterOf(damager);
            if (attacker == null || attacker == victim) return;
            UHCPlayer lover = UHCPlayerManager.get().getPlayer(attacker);
            UHCPlayer loved = UHCPlayerManager.get().getPlayer(victim);
            if (lover == null || loved == null || !lover.isPlaying() || !loved.isPlaying()) return;
            if (lover.getTeam().isPresent() || loved.getTeam().isPresent()) return;
            formTeam(List.of(lover, loved));
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }
    }

    public static class LoveAtFirstDamageMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.loveatfirstdamage.desc",
                "§7Le premier dégât que tu subis t'assigne à une équipe, prise dans une rotation d'au moins §f%teams%§7 équipes.");
        private static final DynamicLang JOINED = DynamicLang.of("mode.loveatfirstdamage.joined",
                "§d● §7La douleur t'a trouvé une famille : §f%team%§7.");

        @Var(name = "Équipes minimum", desc = "Nombre minimum d'équipes présentes dans la rotation.", min = 2)
        private int minTeams = 6;

        private int rotation = 0;

        @Override
        public String getName() {
            return "Love at First Damage";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player, Map.of("%teams%", minTeams));
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.REDSTONE);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void enable() {
            super.enable();
            forceTeamMode(2);
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            if (isActive()) forceTeamMode(2);
        }

        @Override
        public void onTeamUpdate() {
            forceTeamMode(2);
        }

        @Override
        public void onGameStart() {
            if (!isActive()) return;
            while (UHCTeamManager.get().getTeams().size() < minTeams) {
                int before = UHCTeamManager.get().getTeams().size();
                UHCTeamManager.get().createTeam(UHCManager.get().getTeam_size());
                if (UHCTeamManager.get().getTeams().size() == before) return;
            }
        }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (!isActive()) return;
            if (event.isCancelled() || !UHCManager.get().isGame()) return;
            UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
            if (up == null || !up.isPlaying() || up.getTeam().isPresent()) return;
            List<UHCTeam> teams = UHCTeamManager.get().getTeams();
            int size = UHCManager.get().getTeam_size();
            for (int i = 0; i < teams.size(); i++) {
                UHCTeam team = teams.get(Math.floorMod(rotation++, teams.size()));
                if (team.getPlayers().size() >= size) continue;
                up.setTeam(Optional.of(team));
                player.sendMessage(t(JOINED, player, Map.of("%team%", team.name())));
                return;
            }
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }

        @Override
        public void onStop() {
            super.onStop();
            rotation = 0;
        }
    }

    public static class LoveAtFirstGappleMode extends Scenario {

        private static final DynamicLang DESC = DynamicLang.of("mode.loveatfirstgapple.desc",
                "§7Ta première pomme d'or décide de ton équipe, dans l'ordre de consommation.");
        private static final DynamicLang JOINED = DynamicLang.of("mode.loveatfirstgapple.joined",
                "§d● §7Ta première pomme d'or t'a lié à §f%team%§7.");

        @Override
        public String getName() {
            return "Love at First Gapple";
        }

        @Override
        public String getDescription(Player player) {
            return t(DESC, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.GOLDEN_APPLE);
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public void enable() {
            super.enable();
            forceTeamMode(2);
        }

        @Override
        public void toggleActive() {
            super.toggleActive();
            if (isActive()) forceTeamMode(2);
        }

        @Override
        public void onTeamUpdate() {
            forceTeamMode(2);
        }

        @Override
        public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
            if (!isActive()) return;
            if (event.isCancelled() || !UHCManager.get().isGame()) return;
            if (item == null || item.getType() != Material.GOLDEN_APPLE) return;
            UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
            if (up == null || !up.isPlaying() || up.getTeam().isPresent()) return;
            UHCTeam team = teamWithRoom(1);
            if (team == null) return;
            up.setTeam(Optional.of(team));
            player.sendMessage(t(JOINED, player, Map.of("%team%", team.name())));
        }

        @Override
        public void scatter(UHCPlayer up, Location location, HashMap<UHCTeam, Location> teamloc) {
            if (up.getPlayer() != null) up.getPlayer().teleport(location);
        }
    }

    public static List<Scenario> all() {
        return List.of(new CupidMode(), new BlindDatesMode(), new FirstHitLoveMode(),
                new LoveAtFirstDamageMode(), new LoveAtFirstGappleMode());
    }
}
