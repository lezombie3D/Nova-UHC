package net.novaproject.novauhc.scenario.special.legend;


import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static net.novaproject.novauhc.scenario.special.legend.LegendsUtils.*;

public class ChooseUi extends CustomInventory {
    private final Random random = new Random();

    public ChooseUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCadre(0);
        Legend legend = Legend.get();
        ItemCreator chevalier = new ItemCreator(Material.FLOWER_POT).setName(ChatColor.LIGHT_PURPLE + "Marionnettiste");
        addItem(new ActionItem(10, chevalier) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 0) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 0);
                    legend.classTeam.get(team).put(player, 0);
                    legend.marionettes.putIfAbsent(player, new ArrayList<>());
                    setMarionnette(getPlayer());
                }

            }

        });
        ItemCreator mage = new ItemCreator(Material.POTION).setName(ChatColor.LIGHT_PURPLE + "Mage");
        addItem(new ActionItem(11, mage) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 1) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 1);
                    legend.classTeam.get(team).put(player, 1);
                    setMage(getPlayer());
                }
            }
        });
        ItemCreator arch = new ItemCreator(Material.BOW).setName(ChatColor.LIGHT_PURPLE + "Archer");

        addItem(new ActionItem(12, arch) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 2) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    new AnvilUi(getPlayer(), event -> {
                        if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                            String enteredText = event.getName();
                            try {
                                int bowChoice = Integer.parseInt(enteredText);

                                if (bowChoice != 1 && bowChoice != 2) {
                                    getPlayer().sendMessage(ChatColor.RED + "Choix invalide : veuillez choisir 1 ou 2");
                                    return;
                                }

                                Inventory inv = getPlayer().getInventory();
                                ItemCreator bow;

                                if (bowChoice == 1) {
                                    bow = new ItemCreator(Material.BOW)
                                            .addEnchantment(Enchantment.ARROW_DAMAGE, 3)
                                            .addEnchantment(Enchantment.ARROW_INFINITE, 1)
                                            .setName(ChatColor.BOLD + "" + ChatColor.WHITE + "Arc de Lumière");
                                } else {
                                    bow = new ItemCreator(Material.BOW)
                                            .addEnchantment(Enchantment.ARROW_DAMAGE, 4)
                                            .setName(ChatColor.BOLD + "" + ChatColor.WHITE + "Arc de Lumière");
                                }

                                inv.addItem(bow.getItemstack());

                                legend.classe.put(player, 2);
                                legend.classTeam.get(team).put(player, 2);
                                setArcher(getPlayer());

                            } catch (NumberFormatException ex) {
                                getPlayer().sendMessage(ChatColor.RED + "Choix invalide : veuillez entrer un nombre");
                            }
                        }

                    }).setSlot("Choix de l'arc ( 1 ou 2 )").open();
                }
            }
        });
        ItemCreator assa = new ItemCreator(Material.IRON_SWORD).setName(ChatColor.LIGHT_PURPLE + "Assassin");
        addItem(new ActionItem(13, assa) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 3) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 3);
                    legend.classTeam.get(team).put(player, 3);
                    setAssasin(getPlayer());

                }
            }
        });
        ItemCreator tank = new ItemCreator(Material.DIAMOND_CHESTPLATE).setName(ChatColor.LIGHT_PURPLE + "Tank");
        addItem(new ActionItem(14, tank) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 4) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 4);
                    legend.classTeam.get(team).put(player, 4);
                    setTank(getPlayer());
                }
            }
        });
        ItemCreator nain = new ItemCreator(Material.GOLD_PICKAXE).setName(ChatColor.LIGHT_PURPLE + "Nain");
        addItem(new ActionItem(15, nain) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 5) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 5);
                    legend.classTeam.get(team).put(player, 5);
                    setNain(getPlayer());
                }
            }
        });
        ItemCreator pala = new ItemCreator(Material.ARROW).setName(ChatColor.LIGHT_PURPLE + "Zeus");
        addItem(new ActionItem(16, pala) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 6) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 6);
                    legend.classTeam.get(team).put(player, 6);
                    setZeus(getPlayer());
                }
            }
        });
        ItemCreator bouf = new ItemCreator(Material.BONE).setName(ChatColor.LIGHT_PURPLE + "Nécromentien");
        addItem(new ActionItem(19, bouf) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 7) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 7);
                    legend.classTeam.get(team).put(player, 7);
                    setNecro(getPlayer());
                }
            }
        });
        ItemCreator dem = new ItemCreator(Material.APPLE).setName(ChatColor.LIGHT_PURPLE + "Succube");
        addItem(new ActionItem(20, dem) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 8) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 8);
                    legend.classTeam.get(team).put(player, 8);
                    setSucube(getPlayer());
                }
            }
        });
        ItemCreator sold = new ItemCreator(Material.ANVIL).setName(ChatColor.LIGHT_PURPLE + "Forgerons");
        addItem(new ActionItem(21, sold) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 9) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 9);
                    legend.classTeam.get(team).put(player, 9);
                    setSoldat(getPlayer());
                }
            }
        });
        ItemCreator prin = new ItemCreator(Material.YELLOW_FLOWER).setName(ChatColor.LIGHT_PURPLE + "Princesse");
        addItem(new ActionItem(22, prin) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 10) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 10);
                    legend.classTeam.get(team).put(player, 10);
                    legend.princesse.put(team, team.getPlayers().get(random.nextInt(team.getTeamSize())));
                    setPrincesse(getPlayer());
                }
            }
        });
        ItemCreator cav = new ItemCreator(Material.SADDLE).setName(ChatColor.LIGHT_PURPLE + "Cavalier");
        addItem(new ActionItem(23, cav) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 11) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 11);
                    legend.classTeam.get(team).put(player, 11);
                    setCavalier(getPlayer());

                }
            }
        });
        ItemCreator orge = new ItemCreator(Material.COOKED_BEEF).setName(ChatColor.LIGHT_PURPLE + "Ogre");
        addItem(new ActionItem(24, orge) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 12) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 12);
                    legend.classTeam.get(team).put(player, 12);
                    setOgre(getPlayer());

                }
            }
        });
        ItemCreator drag = new ItemCreator(Material.FIREBALL).setName(ChatColor.LIGHT_PURPLE + "Dragon");
        addItem(new ActionItem(25, drag) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 13) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 13);
                    legend.classTeam.get(team).put(player, 13);
                    setDragon(getPlayer());
                }
            }
        });
        ItemCreator vik = new ItemCreator(Material.GOLDEN_APPLE).setName(ChatColor.LIGHT_PURPLE + "Soigneur");
        addItem(new ActionItem(30, vik) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 14) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 14);
                    legend.classTeam.get(team).put(player, 14);

                    setMedecin(getPlayer());

                }
            }
        });
        ItemCreator pri = new ItemCreator(Material.DIAMOND_AXE).setName(ChatColor.LIGHT_PURPLE + "Prisonnier");
        addItem(new ActionItem(31, pri) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 15) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 15);
                    legend.classTeam.get(team).put(player, 15);
                    setPrisonier(getPlayer());
                }
            }
        });
        ItemCreator corne = new ItemCreator(Material.JUKEBOX).setName(ChatColor.LIGHT_PURPLE + "Corne de Chasse");
        addItem(new ActionItem(32, corne) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (getUHCPlayer().getTeam().isPresent()) {
                    UHCTeam team = getUHCPlayer().getTeam().get();
                    UHCPlayer player = getUHCPlayer();

                    if (legend.classe.containsKey(player)) {
                        getPlayer().sendMessage(ChatColor.RED + "Vous avez déjà une classe");
                        return;
                    }

                    legend.classTeam.putIfAbsent(team, new HashMap<>());
                    for (UHCPlayer p : team.getPlayers()) {
                        if (legend.classTeam.get(team).getOrDefault(p, -1) == 16) {
                            getPlayer().sendMessage(ChatColor.RED + "La classe a déjà été choisie");
                            return;
                        }
                    }

                    legend.classe.put(player, 16);
                    legend.classTeam.get(team).put(player, 16);
                    setCorne(getPlayer());
                }
            }
        });
        fillLine(4, 0);
    }

    @Override
    public String getTitle() {
        return ChatColor.GOLD + "Choose a Legend";
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
