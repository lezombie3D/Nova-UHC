
package net.novaproject.novauhc.utils;

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
import eu.cloudnetservice.wrapper.holder.WrapperServiceInfoHolder;
import net.novaproject.novauhc.Main;

import java.util.ArrayList;
import java.util.List;

public class CloudNet {
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
}
