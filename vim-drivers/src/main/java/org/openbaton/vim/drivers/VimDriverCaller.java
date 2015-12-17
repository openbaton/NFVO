package org.openbaton.vim.drivers;

import com.google.gson.reflect.TypeToken;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.*;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.plugin.utils.PluginCaller;
import org.openbaton.vim.drivers.exceptions.VimDriverException;
import org.openbaton.vim.drivers.interfaces.VimDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by lto on 26/11/15.
 */
@Service
@Scope("prototype")
public class VimDriverCaller extends VimDriver {

    private PluginCaller pluginCaller;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public VimDriverCaller(String type, String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("vim-drivers." + type, "localhost", "admin", "openbaton", 5672, Integer.parseInt(managementPort));
    }

    public VimDriverCaller(String name, String type, String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("vim-drivers." + type + "." + name, "localhost", "admin", "openbaton", 5672, Integer.parseInt(managementPort));
    }

    public VimDriverCaller(String brokerIp, int port, String type, String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("vim-drivers." + type, brokerIp, "admin", "openbaton", port, Integer.parseInt(managementPort));
    }

    public VimDriverCaller(String brokerIp, String username, String password, String type, String managementPort) throws IOException, TimeoutException, NotFoundException {
        pluginCaller = new PluginCaller("vim-drivers." + type, brokerIp, username, password, 5672, Integer.parseInt(managementPort));
    }

    @PreDestroy
    public void stop() throws IOException, TimeoutException {
        if (pluginCaller != null)
            pluginCaller.close();
    }

    @Override
    public Server launchInstance(VimInstance vimInstance, String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData) throws RemoteException, VimDriverException {
        List<Serializable> params = new ArrayList<>();
        params.add(vimInstance);
        params.add(name);
        params.add(image);
        params.add(flavor);
        params.add(keypair);
        params.add((Serializable) network);
        params.add((Serializable) secGroup);
        params.add(userData);
        Serializable res = null;
        try {
            res = pluginCaller.executeRPC("launchInstance", params, Server.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (Server) res;
    }

    @Override
    public List<NFVImage> listImages(VimInstance vimInstance) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        Serializable res;
        Type listType = new TypeToken<ArrayList<NFVImage>>() { }.getType();
        try {
            res = pluginCaller.executeRPC("listImages", params, listType);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        for (Object obj : (List) res){
            log.debug(obj.toString());
        }
        return (List<NFVImage>) res;
    }

    @Override
    public List<Server> listServer(VimInstance vimInstance) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        Serializable res;
        try {
            Type listType = new TypeToken<ArrayList<Server>>() { }.getType();
            res = pluginCaller.executeRPC("listServer", params, listType);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (List<Server>) res;
    }

    @Override
    public List<Network> listNetworks(VimInstance vimInstance) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        Serializable res;
        try {
            Type listType = new TypeToken<ArrayList<Network>>() { }.getType();
            res = pluginCaller.executeRPC("listNetworks", params, listType);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (List<Network>) res;
    }

    @Override
    public List<DeploymentFlavour> listFlavors(VimInstance vimInstance) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        Serializable res;
        try {
            Type listType = new TypeToken<ArrayList<DeploymentFlavour>>() { }.getType();
            res = pluginCaller.executeRPC("listFlavors", params, listType);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (List<DeploymentFlavour>) res;
    }

    @Override
    public Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s, Map<String, String> floatingIps) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(hostname);
        params.add(image);
        params.add(extId);
        params.add(keyPair);
        params.add((Serializable) networks);
        params.add((Serializable) securityGroups);
        params.add(s);
        params.add((Serializable) floatingIps);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("launchInstanceAndWait", params, Server.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (Server) res;
    }

    @Override
    public Server launchInstanceAndWait(VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(hostname);
        params.add(image);
        params.add(extId);
        params.add(keyPair);
        params.add((Serializable) networks);
        params.add((Serializable) securityGroups);
        params.add(s);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("launchInstanceAndWait", params, Server.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (Server) res;
    }

    @Override
    public void deleteServerByIdAndWait(VimInstance vimInstance, String id) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(id);
        try {
            pluginCaller.executeRPC("deleteServerByIdAndWait", params, null);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
    }

    @Override
    public Network createNetwork(VimInstance vimInstance, Network network) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(network);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("createNetwork", params, Network.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }

        return (Network) res;
    }

    @Override
    public DeploymentFlavour addFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(deploymentFlavour);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("addFlavor", params, DeploymentFlavour.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (DeploymentFlavour) res;
    }

    @Override
    public NFVImage addImage(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(image);
        params.add(imageFile);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("addImage", params, NFVImage.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (NFVImage) res;
    }

    @Override
    public NFVImage addImage(VimInstance vimInstance, NFVImage image, String image_url) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(image);
        params.add(image_url);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("addImage", params, NFVImage.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (NFVImage) res;
    }

    @Override
    public NFVImage updateImage(VimInstance vimInstance, NFVImage image) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(image);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("updateImage", params, NFVImage.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (NFVImage) res;
    }

    @Override
    public NFVImage copyImage(VimInstance vimInstance, NFVImage image, byte[] imageFile) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(image);
        params.add(imageFile);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("copyImage", params, NFVImage.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (NFVImage) res;
    }

    @Override
    public boolean deleteImage(VimInstance vimInstance, NFVImage image) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(image);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("deleteImage", params, Boolean.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (boolean) res;
    }

    @Override
    public DeploymentFlavour updateFlavor(VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(deploymentFlavour);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("updateFlavor", params, DeploymentFlavour.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (DeploymentFlavour) res;
    }

    @Override
    public boolean deleteFlavor(VimInstance vimInstance, String extId) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(extId);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("deleteFlavor", params, Boolean.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (boolean) res;
    }

    @Override
    public Network updateNetwork(VimInstance vimInstance, Network network) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(network);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("updateNetwork", params, Network.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }

        return (Network) res;
    }

    @Override
    public Subnet createSubnet(VimInstance vimInstance, Network createdNetwork, Subnet subnet) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(createdNetwork);
        params.add(subnet);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("createSubnet", params, Subnet.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (Subnet) res;
    }

    @Override
    public Subnet updateSubnet(VimInstance vimInstance, Network updatedNetwork, Subnet subnet) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(updatedNetwork);
        params.add(subnet);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("updateSubnet", params, Subnet.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (Subnet) res;
    }

    @Override
    public List<String> getSubnetsExtIds(VimInstance vimInstance, String network_extId) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(network_extId);
        Serializable res;
        try {
            Type listType = new TypeToken<ArrayList<String>>() { }.getType();
            res = pluginCaller.executeRPC("getSubnetsExtIds", params, listType);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (List<String>) res;
    }

    @Override
    public boolean deleteSubnet(VimInstance vimInstance, String existingSubnetExtId) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(existingSubnetExtId);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("deleteSubnet", params, Boolean.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (boolean) res;
    }

    @Override
    public boolean deleteNetwork(VimInstance vimInstance, String extId) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(extId);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("deleteNetwork", params, Boolean.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (boolean) res;

    }

    @Override
    public Network getNetworkById(VimInstance vimInstance, String id) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        params.add(id);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("getNetworkById", params, Network.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }

        return (Network) res;
    }

    @Override
    public Quota getQuota(VimInstance vimInstance) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("getQuota", params, Quota.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }
        return (Quota) res;
    }

    @Override
    public String getType(VimInstance vimInstance) throws RemoteException, VimDriverException {
        List<Serializable> params = new LinkedList<>();
        params.add(vimInstance);
        Serializable res;
        try {
            res = pluginCaller.executeRPC("getType", params, String.class);
        } catch (IOException e) {
            throw new VimDriverException(e.getMessage());
        } catch (InterruptedException e) {
            throw new VimDriverException(e.getMessage());
        } catch (PluginException e) {
            throw new VimDriverException(e.getMessage());
        }

        return (String) res;
    }
}

