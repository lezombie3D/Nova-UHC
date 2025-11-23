
package net.novaproject.novauhc.cloudnet;

import eu.cloudnetservice.driver.document.DocumentFactory;
import eu.cloudnetservice.driver.document.property.DocProperty;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceFactory;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.driver.service.ServiceTask;
import eu.cloudnetservice.modules.bridge.BridgeDocProperties;
import eu.cloudnetservice.modules.bridge.BridgeServiceHelper;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor;
import eu.cloudnetservice.wrapper.impl.holder.WrapperServiceInfoHolder;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CloudNet {

    public static final DocProperty<String> NOVA = DocProperty.property("NovaUHC", String.class);
    private String gameName;
    public CloudNet() {
        System.out.println("CloudNet Active for UHC");
        new BukkitRunnable() {
            @Override
            public void run() {
                gameToDoc();
            }
        }.runTaskTimer(Main.get(), 20, 20);
    }
    public static CloudNet get() {
        return Main.get().getCloudNet();
    }

    public CloudServiceProvider getServices() {
        return InjectionLayer.ext().instance(CloudServiceProvider.class);
    }

    public ServiceInfoSnapshot getServiceInfo() {
        return this.getWrapperServiceInfoHolder().serviceInfo();
    }

    public WrapperServiceInfoHolder getWrapperServiceInfoHolder() {
        return InjectionLayer.ext().instance(WrapperServiceInfoHolder.class);
    }

    public BridgeServiceHelper getBridgeServiceHelper() {
        return InjectionLayer.ext().instance(BridgeServiceHelper.class);
    }

    public PlayerManager getPlayerManager() {
        return (PlayerManager) InjectionLayer.boot().instance(ServiceRegistry.class);
    }

    public PlayerExecutor getPlayerExecutor() {
        return (PlayerExecutor) InjectionLayer.boot().instance(ServiceRegistry.class);
    }

    public EventManager getEventManager() {
        return InjectionLayer.ext().instance(EventManager.class);
    }

    public List<ServiceInfoSnapshot> getServices(String task) {
        List<ServiceInfoSnapshot> services = new ArrayList();
        this.getServices().servicesByTask(task).forEach((serv) -> {
            if (serv.readPropertyOrDefault(BridgeDocProperties.IS_ONLINE, false)) {
                services.add(serv);
            }

        });
        return services;
    }

    public List<ServiceInfoSnapshot> getServicesByGroup(String... groups) {
        List<ServiceInfoSnapshot> services = new ArrayList();

        for (String group : groups) {
            this.getServices().servicesByGroup(group).forEach((serv) -> {
                if (serv.readPropertyOrDefault(BridgeDocProperties.IS_ONLINE, false)) {
                    services.add(serv);
                }

            });
        }

        return services;
    }

    public int getOnline(String task) {
        int slot = 0;

        for (ServiceInfoSnapshot serviceInfoSnapshot : this.getServices(task)) {
            slot += serviceInfoSnapshot.readPropertyOrDefault(BridgeDocProperties.ONLINE_COUNT, 0);
        }

        return slot;
    }

    public int getOnline(String... task) {
        int slot = 0;

        for (String s : task) {
            slot += this.getOnline(s);
        }

        return slot;
    }

    public CloudServiceFactory getCloudServiceFactory() {
        return InjectionLayer.ext().instance(CloudServiceFactory.class);
    }

    public ServiceTaskProvider getServiceTaskProvider() {
        return InjectionLayer.ext().instance(ServiceTaskProvider.class);
    }

    public ServiceTask getTask(String task) {
        return this.getServiceTaskProvider().serviceTask(task);
    }

    private void gameToDoc() {
        Document document = new Document();
        String host = Optional.ofNullable(PlayerConnectionEvent.getHost())
                .map(H -> H.getName())
                .orElse("Aucun");
        String gamename = Optional.ofNullable(gameName)
                .map(G -> "§5§l" + gameName)
                .orElse("§5§lGame de : §e" + host);
        document.append("game_name", gamename);
        document.append("host", host);
        document.append("waiting", UHCManager.get().isLobby());
        document.append("players_online", UHCPlayerManager.get().getOnlineUHCPlayers().size());
        document.append("players_max", UHCManager.get().getSlot());
        document.append("open", Bukkit.getServer().hasWhitelist());
        int teams = UHCManager.get().getTeam_size();
        document.append("team_size", teams == 1 ? "FFA" : "To" + teams);
        ServiceInfoSnapshot serviceInfoSnapshot = CloudNet.get().getServiceInfo();
        serviceInfoSnapshot.provider().updateProperties(eu.cloudnetservice.driver.document.Document.newDocument(DocumentFactory.json()).writeProperty(NOVA, document.toJson()));
        CloudNet.get().getWrapperServiceInfoHolder().publishServiceInfoUpdate(serviceInfoSnapshot);
    }


    public CustomInventory getCloudNetUi(Player player) {
        return new CustomInventory(player) {
            @Override
            public void setup() {
                fillDesign(getConfig().getInt("menu.cloudnet.color"));

                addItem(new ActionItem(0, new ItemCreator(Material.PAPER).setName("§2Modifier le nom de la parties")) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        new AnvilUi(getPlayer(), event -> {
                            if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                                String enteredText = event.getName();
                                gameName = enteredText;
                                CommonString.SUCCESSFUL_MODIFICATION.send(getPlayer());
                                openAll();
                            }

                        }).setSlot("Nom de la parties").open();
                    }
                });

            }

            @Override
            public String getTitle() {
                return getConfig().getString("menu.cloudnet.title", "CloudNet • Paramètres");
            }

            @Override
            public int getLines() {
                return 5;
            }

            @Override
            public boolean isRefreshAuto() {
                return false;
            }
        };
    }
}
