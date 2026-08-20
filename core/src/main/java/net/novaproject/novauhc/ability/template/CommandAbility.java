package net.novaproject.novauhc.ability.template;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CommandAbility extends Ability {

    public abstract String getCommandKey();
    public abstract boolean onCommand(Player player, String[] args);

    private String[] pendingArgs = new String[0];

    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        return onCommand(player, pendingArgs);
    }

    public boolean tryCommandUse(Player player, String[] args) {
        this.pendingArgs = args != null ? args : new String[0];
        return tryUse(player);
    }

    public List<String> onTabComplete(Player player, String[] args) {
        if (args != null && args.length == 1) {
            return otherPlayingPlayerNames(player);
        }
        return Collections.emptyList();
    }

    protected List<String> otherPlayingPlayerNames(Player self) {
        return UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                .map(UHCPlayer::getPlayer)
                .filter(p -> p != null && p.isOnline() && !p.equals(self))
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}

