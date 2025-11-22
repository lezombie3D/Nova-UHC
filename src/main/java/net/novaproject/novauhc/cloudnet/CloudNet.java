
package net.novaproject.novauhc.cloudnet;

import eu.cloudnetservice.driver.document.DocumentFactory;
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
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CloudNet {


    public CloudNet() {
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

        document.append("host", PlayerConnectionEvent.getHost().getName());
        document.append("waiting", isLobby());
        document.append("players_online", getUhcPlayerManager().getOnlineUHCPlayers().size());
        document.append("players_max", getSlot());
        document.append("open", Bukkit.getServer().hasWhitelist());
        int teams = team_size;
        document.append("team_size", teams == 1 ? "FFA" : "To" + teams);
        ServiceInfoSnapshot serviceInfoSnapshot = CloudNet.get().getServiceInfo();
        serviceInfoSnapshot.provider().updateProperties(eu.cloudnetservice.driver.document.Document.newDocument(DocumentFactory.json()).writeProperty(NOVA, document.toJson()));
        CloudNet.get().getWrapperServiceInfoHolder().publishServiceInfoUpdate(serviceInfoSnapshot);
    }

}
