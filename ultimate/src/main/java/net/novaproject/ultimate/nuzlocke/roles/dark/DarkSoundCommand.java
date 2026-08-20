package net.novaproject.ultimate.nuzlocke.roles.dark;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.CommandAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DarkSoundCommand extends CommandAbility {

    @Var(name = "Nuzlocke Dark Sound Cd", type = VariableType.TIME)
    private int cdSeconds = 60;

    private static final Map<String, Sound> CURATED = new LinkedHashMap<>();
    static {
        CURATED.put("ghast", Sound.GHAST_SCREAM);
        CURATED.put("wither", Sound.WITHER_SPAWN);
        CURATED.put("creeper", Sound.CREEPER_HISS);
        CURATED.put("explosion", Sound.EXPLODE);
        CURATED.put("anvil", Sound.ANVIL_LAND);
        CURATED.put("wolf", Sound.WOLF_HOWL);
        CURATED.put("lightning", Sound.AMBIENCE_THUNDER);
        CURATED.put("zombie", Sound.ZOMBIE_HURT);
        CURATED.put("villager", Sound.VILLAGER_DEATH);
        CURATED.put("enderdragon", Sound.ENDERDRAGON_GROWL);
        CURATED.put("blaze", Sound.BLAZE_BREATH);
        CURATED.put("skeleton", Sound.SKELETON_HURT);
        CURATED.put("spider", Sound.SPIDER_DEATH);
        CURATED.put("door", Sound.DOOR_OPEN);
        CURATED.put("bell", Sound.NOTE_PLING);
        CURATED.put("cat", Sound.CAT_HISS);
        CURATED.put("pig", Sound.PIG_DEATH);
        CURATED.put("level", Sound.LEVEL_UP);
        CURATED.put("portal", Sound.PORTAL);
        CURATED.put("burp", Sound.BURP);
    }

    public DarkSoundCommand() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getCommandKey() { return "sound"; }
    @Override public String getName() { return "Sound"; }

    @Override
    public boolean onCommand(Player player, String[] args) {
        long time = player.getWorld().getTime();
        boolean night = time >= 13000 && time <= 23499;
        if (!night) {
            player.sendMessage("§8§lDark §8│ §7Ne fonctionne que la nuit.");
            return false;
        }
        if (args.length == 0) {
            player.sendMessage("§8§lDark §8│ §7Usage: §7/sound <player> <key> §fou §7/sound list");
            return false;
        }
        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage("§8§lSounds §8│ §7" + String.join("§7, §f", CURATED.keySet()));
            return false;
        }
        if (args.length < 2) {
            player.sendMessage("§8§lDark §8│ §7Usage: §7/sound <player> <key>");
            return false;
        }
        Player target = org.bukkit.Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§8§lDark §8│ §7Joueur introuvable.");
            return false;
        }
        Sound sound = CURATED.get(args[1].toLowerCase());
        if (sound == null) {
            player.sendMessage("§8§lDark §8│ §7Son inconnu. §7/sound list");
            return false;
        }
        target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
        player.sendMessage("§8§lDark §8│ §7Son envoyé à §f" + target.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args == null || args.length <= 1) {
            List<String> names = new ArrayList<>();
            for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                if (up.getPlayer() != null && !up.getPlayer().equals(player)) names.add(up.getPlayer().getName());
            }
            names.add("list");
            return names;
        }
        if (args.length == 2) {
            return new ArrayList<>(CURATED.keySet());
        }
        return Arrays.asList();
    }
}

