package org.project.openbaton.nfvo.plugin.concretes;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.nfvo.*;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.clients.interfaces.ClientInterfaces;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.PluginInvokeException;
import org.project.openbaton.nfvo.plugin.PluginAgent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 14/08/15.
 */
@Service
@Scope
public class ClientInterfacePluginAgent extends PluginAgent {


    public Server launchInstance(String type, VimInstance vimInstance, String name, String image, String flavor, String keypair, Set<String> network, Set<String> secGroup, String userData) throws NoSuchMethodException, NotFoundException, PluginInvokeException {
//        return this.invokeMethod(this.getClass().getMethod("launchInstance"), ClientInterfaces.class, name, image, flavor, keypair, network, secGroup, userData);
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, name, image, flavor, keypair, network, secGroup, userData);
    }

    public List<NFVImage> listImages(String type, VimInstance vimInstance) throws NotFoundException, PluginInvokeException {
//        return this.invokeMethod(this.getClass().getMethod("listImages"), ClientInterfaces.class);
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type);
    }

    public List<Server> listServer(String type, VimInstance vimInstance) throws NotFoundException, PluginInvokeException {
//        return this.invokeMethod(this.getClass().getMethod("listServers"), ClientInterfaces.class);
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type);
    }

    public List<Network> listNetworks(String type, VimInstance vimInstance) throws NotFoundException, PluginInvokeException {
//        return this.invokeMethod(this.getClass().getMethod("listNetworks"), ClientInterfaces.class);
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type);
    }

    public List<DeploymentFlavour> listFlavors(String type, VimInstance vimInstance) throws NotFoundException, PluginInvokeException {
//        return this.invokeMethod(this.getClass().getMethod("listNetworks"), ClientInterfaces.class);
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type);
    }

    public Server launchInstanceAndWait(String type, VimInstance vimInstance, String hostname, String image, String extId, String keyPair, Set<String> networks, Set<String> securityGroups, String s) throws VimDriverException, NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, hostname, image, extId, keyPair, networks, securityGroups, s);
    }

    public Void deleteServerByIdAndWait(String type, VimInstance vimInstance, String id) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class,type, id);
    }

    public Network createNetwork(String type, VimInstance vimInstance, Network network) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, network);
    }

    public DeploymentFlavour addFlavor(String type, VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, deploymentFlavour);
    }

    public NFVImage addImage(String type, VimInstance vimInstance, NFVImage image, InputStream inputStream) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, image, inputStream);
    }

    public NFVImage updateImage(String type, VimInstance vimInstance, NFVImage image) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, image);
    }

    public NFVImage copyImage(String type, VimInstance vimInstance, NFVImage image, InputStream inputStream) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, image, inputStream);
    }

    public boolean deleteImage(String type, VimInstance vimInstance, NFVImage image) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, image);
    }

    public DeploymentFlavour updateFlavor(String type, VimInstance vimInstance, DeploymentFlavour deploymentFlavour) throws VimDriverException, NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, deploymentFlavour);
    }

    public boolean deleteFlavor(String type, VimInstance vimInstance, String extId) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, extId);
    }

    public Subnet createSubnet(String type, VimInstance vimInstance, Network createdNetwork, Subnet subnet) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, createdNetwork, subnet);
    }

    public Network updateNetwork(String type, VimInstance vimInstance, Network network) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, network);
    }

    public Subnet updateSubnet(String type, VimInstance vimInstance, Network updatedNetwork, Subnet subnet) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, updatedNetwork, subnet);
    }

    public List<String> getSubnetsExtIds(String type, VimInstance vimInstance, String network_extId) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, network_extId);
    }

    public boolean deleteSubnet(String type, VimInstance vimInstance, String existingSubnetExtId) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, existingSubnetExtId);
    }

    public boolean deleteNetwork(String type, VimInstance vimInstance, String extId) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, extId);
    }

    public Network getNetworkById(String type, VimInstance vimInstance, String id) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type, id);
    }

    public Quota getQuota(String type, VimInstance vimInstance) throws NotFoundException, PluginInvokeException {
        Method method = new Object() {
        }.getClass().getEnclosingMethod();
        return this.invokeMethod(method, ClientInterfaces.class, type);
    }
}
