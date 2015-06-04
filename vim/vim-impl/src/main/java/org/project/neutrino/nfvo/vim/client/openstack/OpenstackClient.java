package org.project.neutrino.nfvo.vim.client.openstack;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.collect.IterableWithMarker;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.NetworkType;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.neutron.v2.features.SubnetApi;
import org.jclouds.openstack.neutron.v2.domain.Network.CreateNetwork;
import org.jclouds.openstack.neutron.v2.domain.Subnet.CreateSubnet;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Subnet;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by mpa on 06.05.15.
 */
@Service
@Scope
public class OpenstackClient implements ClientInterfaces {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private NovaApi novaApi;
    private NeutronApi neutronApi;
    private Set<String> zones;
    private String defaultZone = null;

    public OpenstackClient() {
        //TODO get properties from configurations
        neutronApi = null;
        zones = null;
        novaApi = null;
    }

    @Override
    public void init(VimInstance vimInstance) {
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
        Properties overrides = new Properties();
        overrides.setProperty(KeystoneProperties.CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
        overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
        overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
        novaApi = ContextBuilder.newBuilder("openstack-nova").endpoint(vimInstance.getAuthUrl()).credentials(vimInstance.getTenant() + ":" + vimInstance.getUsername(), vimInstance.getPassword()).modules(modules).overrides(overrides).buildApi(NovaApi.class);
        neutronApi = ContextBuilder.newBuilder("openstack-neutron").endpoint(vimInstance.getAuthUrl()).credentials(vimInstance.getTenant() + ":" + vimInstance.getUsername(), vimInstance.getPassword()).modules(modules).overrides(overrides).buildApi(NeutronApi.class);
        zones = novaApi.getConfiguredRegions();
        if (null == defaultZone) {
            defaultZone = zones.iterator().next();
        }
    }

    public void setZone(String zone) {
        if (null != zone && "" == zone) {
            defaultZone = zone;
        }
    }

    public void createKeyPair(String name, String path) throws IOException {
        KeyPairApi keypairApi = this.novaApi.getKeyPairApi(
                this.defaultZone).get();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
            keypairApi.createWithPublicKey(name, line);
        } catch (IOException e) {
            System.out.println("ERROR::Given file path is not valid.");
        } finally {
            br.close();
        }
    }

    public Server launchInstance(String name, String imageId, String flavorId,
                                  String keypair, List<String> network, List<String> secGroup,
                                  String userData) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        CreateServerOptions options = CreateServerOptions.Builder.keyPairName(keypair).networks(network).securityGroupNames(secGroup).userData(userData.getBytes());
        String extId  = serverApi.create(name, imageId, flavorId, options).getId();
        Server server = getServerById(extId);
        return server;
    }

    public Server launchInstanceAndWait(String name, String imageId, String flavorId,
                                  String keypair, List<String> network, List<String> secGroup,
                                  String userData) {
        boolean bootCompleted = false;
        Server server = launchInstance(name, imageId, flavorId, keypair, network, secGroup, userData);
        while (bootCompleted==false) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            server = getServerById(server.getExtId());
            if (server.getStatus().equals("ACTIVE")) {
                bootCompleted = true;
            }
        }
        return server;
    }

    public void rebootServer(String extId, RebootType type) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        serverApi.reboot(extId, type);
    }

    public void deleteServerById(String extId) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        serverApi.delete(extId);
    }

    public void deleteServerByIdAndWait(String extId) {
        boolean deleteCompleted = false;
        deleteServerById(extId);
        while (deleteCompleted==false) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Server instance = getServerById(extId);
            } catch (NullPointerException e) {
                deleteCompleted = true;
            }
        }
    }

    @Override
    public List<NFVImage> listImages() {
        ImageApi imageApi = this.novaApi.getImageApi(defaultZone);
        List<NFVImage> images = new ArrayList<NFVImage>();
        for (IterableWithMarker<Image> im : imageApi.listInDetail().toList()){
            for(int i = 0; i < im.size() ; i++){
                NFVImage image = new NFVImage();
                image.setName(im.get(i).getName());
                image.setExtId(im.get(i).getId());
                image.setMinRam(im.get(i).getMinRam());
                image.setMinDiskSpace(im.get(i).getMinDisk());
                image.setCreated(im.get(i).getCreated());
                image.setUpdated(im.get(i).getUpdated());
                images.add(image);
            }
        }
        return images;
    }

    public List<Server> listServer(){
        List<Server> servers = new ArrayList<Server>();
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        for (org.jclouds.openstack.nova.v2_0.domain.Server jcloudsServer : serverApi.listInDetail().concat()){
            Server server = new Server();
            server.setExtId(jcloudsServer.getId());
            server.setName(jcloudsServer.getName());
            server.setStatus(jcloudsServer.getStatus().value());
            server.setExtendedStatus(jcloudsServer.getExtendedStatus().toString());
            server.setIp(jcloudsServer.getAccessIPv4());
            server.setCreated(jcloudsServer.getCreated());
            server.setUpdated(jcloudsServer.getUpdated());
            server.setImage(getImageById(jcloudsServer.getImage().getId()));
            server.setFlavor(getFlavorById(jcloudsServer.getFlavor().getId()));
            servers.add(server);
        }
        return servers;
    }

    public Server getServerById(String extId) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        try {
            org.jclouds.openstack.nova.v2_0.domain.Server jcloudsServer = serverApi.get(extId);
            Server server = new Server();
            server.setExtId(jcloudsServer.getId());
            server.setName(jcloudsServer.getName());
            server.setStatus(jcloudsServer.getStatus().value());
            server.setExtendedStatus(jcloudsServer.getExtendedStatus().toString());
            server.setIp(jcloudsServer.getAccessIPv4());
            server.setCreated(jcloudsServer.getCreated());
            server.setUpdated(jcloudsServer.getUpdated());
            server.setImage(getImageById(jcloudsServer.getImage().getId()));
            server.setFlavor(getFlavorById(jcloudsServer.getFlavor().getId()));
            return server;
        } catch (NullPointerException e) {
            throw new NullPointerException("Server not found");
        }
    }

    public String getServerIdByName(String name) {
        ServerApi serverApi = this.novaApi.getServerApi(defaultZone);
        for (Resource s : serverApi.list().concat()) {
            if (s.getName().equalsIgnoreCase(name))
                return s.getId();
        }
        throw new NullPointerException("Server not found");
    }

    public NFVImage getImageById(String extId) {
        ImageApi imageApi = this.novaApi.getImageApi(this.defaultZone);
        try {
            Image jcloudsImage = imageApi.get(extId);
            NFVImage image = new NFVImage();
            image.setExtId(jcloudsImage.getId());
            image.setName(jcloudsImage.getName());
            image.setCreated(jcloudsImage.getCreated());
            image.setUpdated(jcloudsImage.getUpdated());
            //image.setMinCPU(jcloudsImage.getMinCPU());
            image.setMinDiskSpace(jcloudsImage.getMinDisk());
            image.setMinRam(jcloudsImage.getMinRam());
            return image;
        } catch (NullPointerException e) {
            throw new NullPointerException("Image not found");
        }
    }

    public String getImageIdByName(String name) {
        ImageApi imageApi = this.novaApi.getImageApi(this.defaultZone);
        for (Resource i : imageApi.list().concat()) {
            if (i.getName().equalsIgnoreCase(name))
                return i.getId();
        }
        throw new NullPointerException("Image not found");
    }

    public DeploymentFlavour getFlavorById(String extId) {
        FlavorApi flavorApi = this.novaApi.getFlavorApi(this.defaultZone);
        try {
            Flavor jcloudsFlavor = flavorApi.get(extId);
            DeploymentFlavour flavor = new DeploymentFlavour();
            flavor.setFlavour_key(jcloudsFlavor.getName());
            flavor.setExtId(jcloudsFlavor.getId());
            flavor.setRam(jcloudsFlavor.getRam());
            flavor.setDisk(jcloudsFlavor.getDisk());
            flavor.setVcpus(jcloudsFlavor.getVcpus());
            return flavor;
        } catch (NullPointerException e) {
            throw new NullPointerException("Flavor not found");
        }
    }

    public String getFlavorIdByName(String name) {
        FlavorApi flavorApi = this.novaApi.getFlavorApi(this.defaultZone);
        for (Resource f : flavorApi.list().concat()) {
            if (f.getName().equalsIgnoreCase(name))
                return f.getId();
        }
        throw new NullPointerException("Flavor not found");
    }

    @Override
    public List<DeploymentFlavour> listFlavors() {
        List<DeploymentFlavour> flavors = new ArrayList<DeploymentFlavour>();
        FlavorApi flavorApi = this.novaApi.getFlavorApi(this.defaultZone);
        for (Flavor jcloudsFlavor : flavorApi.listInDetail().concat()) {
            DeploymentFlavour flavor = new DeploymentFlavour();
            flavor.setExtId(jcloudsFlavor.getId());
            flavor.setFlavour_key(jcloudsFlavor.getName());
            flavor.setRam(jcloudsFlavor.getRam());
            flavor.setDisk(jcloudsFlavor.getDisk());
            flavor.setVcpus(jcloudsFlavor.getVcpus());
            flavors.add(flavor);
        }
        return flavors;
    }

    public String getSecurityGroupById(String extId) {
        SecurityGroupApi securityGroupApi = novaApi.getSecurityGroupApi(defaultZone).get();
        try {
            SecurityGroup securityGroup = securityGroupApi.get(extId);
            return securityGroup.getId();
        } catch (Exception e) {
            throw new NullPointerException("Security Group not found");
        }
    }

    public String getSecurityGroupIdByName(String name) {
        SecurityGroupApi securityGroupApi = novaApi.getSecurityGroupApi(defaultZone).get();
        Iterator<? extends SecurityGroup> sgList = securityGroupApi.list().iterator();
        while (sgList.hasNext()) {
            SecurityGroup group = sgList.next();
            if (group.getName().equalsIgnoreCase(name))
                return group.getId();
        }
        throw new NullPointerException("Security Group not found");
    }

    public Network createNetwork(String name, String networkType, boolean external, boolean shared, int segmentationId, String physicalNetworkName ) {
        NetworkApi networkApi = neutronApi.getNetworkApi(defaultZone);
        CreateNetwork createNetwork = CreateNetwork.createBuilder(name).networkType(NetworkType.fromValue(networkType)).external(external).shared(shared).segmentationId(segmentationId).physicalNetworkName(physicalNetworkName).build();
        org.jclouds.openstack.neutron.v2.domain.Network jcloudsNetwork = networkApi.create(createNetwork);
        Network network = new Network();
        network.setName(jcloudsNetwork.getName());
        network.setExtId(jcloudsNetwork.getId());
        network.setExternal(jcloudsNetwork.getExternal());
        network.setNetworkType(jcloudsNetwork.getNetworkType().toString());
        network.setShared(jcloudsNetwork.getShared());
        //network.setSubnets(jcloudsNetwork.getSubnets());
        network.setPhysicalNetworkName(jcloudsNetwork.getPhysicalNetworkName());
        network.setSegmentationId(jcloudsNetwork.getSegmentationId());
        return network;
    }

    public Boolean deleteNetwork(String extId) {
        NetworkApi networkApi = neutronApi.getNetworkApi(defaultZone);
        Boolean deleted = networkApi.delete(extId);
        return deleted;
    }

    public Network getNetworkById(String extId) {
        NetworkApi networkApi = neutronApi.getNetworkApi(defaultZone);
        try {
            org.jclouds.openstack.neutron.v2.domain.Network jcloudsNetwork = networkApi.get(extId);
            Network network = new Network();
            network.setName(jcloudsNetwork.getName());
            network.setExtId(jcloudsNetwork.getId());
            network.setExternal(jcloudsNetwork.getExternal());
            network.setNetworkType(jcloudsNetwork.getNetworkType().toString());
            network.setShared(jcloudsNetwork.getShared());
            //network.setSubnets(jcloudsNetwork.getSubnets());
            network.setPhysicalNetworkName(jcloudsNetwork.getPhysicalNetworkName());
            network.setSegmentationId(jcloudsNetwork.getSegmentationId());
            return network;
        } catch (Exception e) {
            throw new NullPointerException("Network not found");
        }
    }

    public String getNetworkIdByName(String name) {
        NetworkApi networkApi = neutronApi.getNetworkApi(defaultZone);
        for (org.jclouds.openstack.neutron.v2.domain.Network net : networkApi.list().concat()) {
            if (net.getName().equalsIgnoreCase(name))
                return net.getId();
        }
        throw new NullPointerException("Network not found");
    }

    @Override
    public List<Network> listNetworks() {
        List<Network> networks = new ArrayList<Network>();
        for (org.jclouds.openstack.neutron.v2.domain.Network jcloudsNetwork : this.neutronApi.getNetworkApi(defaultZone).list().concat()){
            log.trace("OpenstackNetwork: " + jcloudsNetwork.toString());
            Network network = new Network();
            network.setName(jcloudsNetwork.getName());
            network.setExtId(jcloudsNetwork.getId());
            network.setExternal(jcloudsNetwork.getExternal());
            network.setNetworkType(jcloudsNetwork.getNetworkType().toString());
            network.setShared(jcloudsNetwork.getShared());
            //network.setSubnets(jcloudsNetwork.getSubnets());
            network.setPhysicalNetworkName(jcloudsNetwork.getPhysicalNetworkName());
//            network.setSegmentationId(jcloudsNetwork.getSegmentationId());
            networks.add(network);
        }
        return networks;
    }

    public Subnet createSubnet(Network network, String name, String cidr) {
        SubnetApi subnetApi = neutronApi.getSubnetApi(defaultZone);
        CreateSubnet createSubnet = CreateSubnet.createBuilder(network.getExtId(), cidr).name(name).ipVersion(4).build();
        org.jclouds.openstack.neutron.v2.domain.Subnet jcloudsSubnet = subnetApi.create(createSubnet);
        Subnet subnet = new Subnet();
        subnet.setExtId(jcloudsSubnet.getId());
        subnet.setName(jcloudsSubnet.getName());
        subnet.setCidr(jcloudsSubnet.getCidr());
        //Association between network and subnet
        subnet.setNetworkId(network.getId());
        network.addSubnet(subnet);
        return subnet;
    }

    public void deleteSubnet(Network network, Subnet subnet) {
        SubnetApi subnetApi = neutronApi.getSubnetApi(defaultZone);
        subnetApi.delete(subnet.getExtId());
        network.removeSubnet(subnet);
    }

    public Subnet getSubnetById(String extId) {
        SubnetApi subnetApi = neutronApi.getSubnetApi(defaultZone);
        try {
            org.jclouds.openstack.neutron.v2.domain.Subnet jcloudsSubnet = subnetApi.get(extId);
            Subnet subnet = new Subnet();
            subnet.setName(jcloudsSubnet.getName());
            subnet.setExtId(jcloudsSubnet.getId());
            subnet.setNetworkId(jcloudsSubnet.getNetworkId());
            subnet.setCidr(jcloudsSubnet.getCidr());
            return subnet;
        } catch (Exception e) {
            throw new NullPointerException("Subnet not found");
        }
    }

    public List<Subnet> listSubnets() {
        List<Subnet> subnets = new ArrayList<org.project.neutrino.nfvo.catalogue.nfvo.Subnet>();
        for (org.jclouds.openstack.neutron.v2.domain.Subnet net : this.neutronApi.getSubnetApi(defaultZone).list().concat()){
            Subnet subnet = new Subnet();
            subnet.setName(net.getName());
            subnet.setExtId(net.getId());
            subnet.setNetworkId(net.getNetworkId());
            subnet.setCidr(net.getCidr());
            subnets.add(subnet);
        }
        return subnets;
    }
}
