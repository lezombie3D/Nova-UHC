package net.novaproject.ultimate.legend;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.special.LegendLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;


public class ChooseUi extends CustomInventory {

    private final Legend legend;

    public ChooseUi(Player player) {
        super(player);
        this.legend = Legend.get();
    }

    @Override
    public void setup() {
        fillCadre(0);
        setupClassItems();
        addInfoItems();
    }

    private void setupClassItems() {
        List<LegendRole> roles = legend.getActivatedRoles();

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (int i = 0; i < roles.size() && i < slots.length; i++) {
            LegendRole role = roles.get(i);
            addClassItem(slots[i], role);
        }
    }

    private void addInfoItems() {
        UHCPlayer uhcPlayer = getUHCPlayer();
        if (uhcPlayer == null || !uhcPlayer.getTeam().isPresent()) return;

        UHCTeam team = uhcPlayer.getTeam().get();
        List<LegendRole> activated = legend.getActivatedRoles();
        long available = activated.stream()
                .filter(r -> !legend.isRoleTakenInTeam(uhcPlayer, r))
                .count();

        ItemCreator infoItem = new ItemCreator(Material.BOOK)
                .setName("§6Informations")
                .setLores(Arrays.asList(
                        "§7Classes disponibles : §a" + available + "§7/§a" + activated.size(),
                        "§7Équipe : §b" + team.name(),
                        "",
                        "§eCliquez sur une classe pour la choisir !"
                ));

        addItem(new StaticItem(40, infoItem));
    }

    private void addClassItem(int slot, LegendRole role) {
        ItemCreator icon = createClassIcon(role);

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                handleSelection(role);
            }
        });
    }

    private ItemCreator createClassIcon(LegendRole role) {
        ItemCreator icon = new ItemCreator(role.getIconMaterial())
                .setName("§6" + role.getName());


        UHCPlayer uhcPlayer = getUHCPlayer();
        if (uhcPlayer != null && uhcPlayer.getTeam().isPresent()) {
            if (legend.getRoleByUHCPlayer(uhcPlayer) != null) {
                icon.addLore("§c✗ Vous avez déjà une classe");
            } else if (legend.isRoleTakenInTeam(uhcPlayer, role)) {
                icon.addLore("§c✗ Déjà prise dans votre équipe");
            } else {
                icon.addLore("§a✓ Disponible");

                if (!role.getAbilities().isEmpty()) {
                    long activeCount = role.getAbilities().stream()
                            .filter(a -> a.getMaterial() != null)
                            .count();
                    if (activeCount > 0) {
                        icon.addLore("§b⚡ " + activeCount + " pouvoir(s) actif(s)");
                    }
                }
            }
        }

        icon.addLore("");
        icon.addLore("§eCliquez pour choisir cette classe !");

        return icon;
    }

    private void handleSelection(LegendRole role) {
        UHCPlayer uhcPlayer = getUHCPlayer();
        if (uhcPlayer == null) return;

        if (!uhcPlayer.getTeam().isPresent()) {
            LangManager.get().send(LegendLang.NO_TEAM_ERROR, getPlayer());
            return;
        }


        if (legend.chooseRole(uhcPlayer, role)) {

            getPlayer().closeInventory();

            getPlayer().sendMessage("");
            getPlayer().sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            getPlayer().sendMessage("§6§l          CLASSE SÉLECTIONNÉE");
            getPlayer().sendMessage("");
            getPlayer().sendMessage("§f  Classe : §6§l" + role.getName());
            getPlayer().sendMessage("");
            getPlayer().sendMessage("§a§l  ✓ Classe assignée avec succès !");
            getPlayer().sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            getPlayer().sendMessage("");
        }
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(LegendLang.UI_CHOOSE_TITLE, getPlayer())
                .replace("%count%", String.valueOf(legend.getActivatedRoles().size()));
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}