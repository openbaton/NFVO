package org.project.neutrino.nfvo.vim.client.openstack;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.collect.IterableWithMarker;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.ByteArrayPayload;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.glance.v1_0.GlanceApi;
import org.jclouds.openstack.glance.v1_0.domain.ContainerFormat;
import org.jclouds.openstack.glance.v1_0.domain.DiskFormat;
import org.jclouds.openstack.glance.v1_0.domain.ImageDetails;
import org.jclouds.openstack.glance.v1_0.features.ImageApi;
import org.jclouds.openstack.glance.v1_0.options.CreateImageOptions;
import org.jclouds.openstack.glance.v1_0.options.UpdateImageOptions;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.Network.CreateNetwork;
import org.jclouds.openstack.neutron.v2.domain.NetworkType;
import org.jclouds.openstack.neutron.v2.domain.Subnet.CreateSubnet;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.neutron.v2.features.SubnetApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.RebootType;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.vim_interfaces.client_interfaces.ClientInterfaces;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
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
    private GlanceApi glanceApi;

    private Set<String> zones;
    private String defaultZone = null;

    public void setNovaApi(NovaApi novaApi) {
        this.novaApi = novaApi;
    }

    public void setNeutronApi(NeutronApi neutronApi) {
        this.neutronApi = neutronApi;
    }

    public OpenstackClient() {
        //TODO get properties from configurations
        neutronApi = null;
        zones = null;
        novaApi = null;
        glanceApi = null;
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
        glanceApi = ContextBuilder.newBuilder("openstack-glance").endpoint(vimInstance.getAuthUrl()).credentials(vimInstance.getTenant() + ":" + vimInstance.getUsername(), vimInstance.getPassword()).modules(modules).overrides(overrides).buildApi(GlanceApi.class);
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
                                  String userData) throws VimException {
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
            if (server.getStatus().equals("ERROR")){
                throw new VimException(server.getExtendedStatus());
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
                getServerById(extId);
            } catch (NullPointerException e) {
                deleteCompleted = true;
            }
        }
    }

    @Override
    public List<NFVImage> listImages() {
        ImageApi imageApi = this.glanceApi.getImageApi(defaultZone);
        List<NFVImage> images = new ArrayList<NFVImage>();
        for (IterableWithMarker<ImageDetails> jcloudsImage : imageApi.listInDetail().toList()){
            for(int i = 0; i < jcloudsImage.size() ; i++){
                NFVImage image = new NFVImage();
                image.setName(jcloudsImage.get(i).getName());
                image.setExtId(jcloudsImage.get(i).getId());
                image.setMinRam(jcloudsImage.get(i).getMinRam());
                image.setMinDiskSpace(jcloudsImage.get(i).getMinDisk());
                image.setCreated(jcloudsImage.get(i).getCreatedAt());
                image.setUpdated(jcloudsImage.get(i).getUpdatedAt());
                image.setIsPublic(jcloudsImage.get(i).isPublic());
                image.setDiskFormat(jcloudsImage.get(i).getDiskFormat().toString());
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
            log.trace("" + jcloudsServer);
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

    public NFVImage addImage(String name, InputStream payload, String diskFormat, String containerFromat, long minDisk, long minRam, boolean isPublic) {
        ImageApi imageApi = this.glanceApi.getImageApi(this.defaultZone);
        CreateImageOptions createImageOptions = new CreateImageOptions();
        createImageOptions.minDisk(minDisk);
        createImageOptions.minRam(minRam);
        createImageOptions.isPublic(isPublic);
        createImageOptions.diskFormat(DiskFormat.valueOf(diskFormat));
        createImageOptions.containerFormat(ContainerFormat.valueOf(containerFromat));

        Payload jcloudsPayload = new InputStreamPayload(payload);
        try {
            ByteArrayOutputStream bufferedPayload = new ByteArrayOutputStream();
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = payload.read(bytes)) != -1) {
                bufferedPayload.write(bytes, 0, read);
            }
            bufferedPayload.flush();
            jcloudsPayload = new ByteArrayPayload(bufferedPayload.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageDetails imageDetails = imageApi.create(name, jcloudsPayload, new CreateImageOptions[]{createImageOptions});
        NFVImage image = new NFVImage();
        image.setName(imageDetails.getName());
        image.setExtId(imageDetails.getId());
        image.setCreated(imageDetails.getCreatedAt());
        image.setUpdated(imageDetails.getUpdatedAt());
        image.setMinDiskSpace(imageDetails.getMinDisk());
        image.setMinRam(imageDetails.getMinDisk());
        image.setIsPublic(imageDetails.isPublic());
        image.setDiskFormat(imageDetails.getDiskFormat().toString());
        image.setContainerFormat(imageDetails.getContainerFormat().toString());
        return image;
    }

    public boolean deleteImage(String extId) {
        ImageApi imageApi = this.glanceApi.getImageApi(this.defaultZone);
        boolean isDeleted = imageApi.delete(extId);
        return isDeleted;
    }

    public NFVImage updateImage(String extId, String name, String diskFormat, String containerFromat, long minDisk, long minRam, boolean isPublic){
        ImageApi imageApi = this.glanceApi.getImageApi(this.defaultZone);
        UpdateImageOptions updateImageOptions = new UpdateImageOptions();
        updateImageOptions.name(name);
        updateImageOptions.minRam(minRam);
        updateImageOptions.minDisk(minDisk);
        updateImageOptions.isPublic(isPublic);
        updateImageOptions.diskFormat(DiskFormat.valueOf(diskFormat));
        updateImageOptions.containerFormat(ContainerFormat.valueOf(containerFromat));
        ImageDetails imageDetails = imageApi.update(extId, updateImageOptions);
        NFVImage image = new NFVImage();
        image.setName(imageDetails.getName());
        image.setExtId(imageDetails.getId());
        image.setCreated(imageDetails.getCreatedAt());
        image.setUpdated(imageDetails.getUpdatedAt());
        image.setMinDiskSpace(imageDetails.getMinDisk());
        image.setMinRam(imageDetails.getMinDisk());
        image.setIsPublic(imageDetails.isPublic());
        image.setDiskFormat(imageDetails.getDiskFormat().toString());
        image.setContainerFormat(imageDetails.getContainerFormat().toString());
        return image;
    }

    public NFVImage copyImage(String extId, String name, String diskFormat, String containerFormat, long minDisk, long minRam, boolean isPublic) {
        ImageApi imageApi = this.glanceApi.getImageApi(this.defaultZone);
        InputStream inputStream = imageApi.getAsStream(extId);
        NFVImage image = addImage(name, inputStream, diskFormat, containerFormat, minDisk, minRam, isPublic);
        return image;
    }

    public NFVImage getImageById(String extId) {
        ImageApi imageApi = this.glanceApi.getImageApi(this.defaultZone);
        try {
            ImageDetails jcloudsImage = imageApi.get(extId);
            NFVImage image = new NFVImage();
            image.setExtId(jcloudsImage.getId());
            image.setName(jcloudsImage.getName());
            image.setCreated(jcloudsImage.getCreatedAt());
            image.setUpdated(jcloudsImage.getUpdatedAt());
            image.setMinDiskSpace(jcloudsImage.getMinDisk());
            image.setMinRam(jcloudsImage.getMinRam());
            image.setIsPublic(jcloudsImage.isPublic());
            image.setDiskFormat(jcloudsImage.getDiskFormat().toString());
            image.setContainerFormat(jcloudsImage.getContainerFormat().toString());
            return image;
        } catch (NullPointerException e) {
            throw new NullPointerException("Image not found");
        }
    }

    public String getImageIdByName(String name) {
        ImageApi imageApi = this.glanceApi.getImageApi(this.defaultZone);
        for (Resource i : imageApi.list().concat()) {
            if (i.getName().equalsIgnoreCase(name))
                return i.getId();
        }
        throw new NullPointerException("Image not found");
    }

    public DeploymentFlavour addFlavor(String name, int vcpus, int ram, int disk) {
        UUID id = java.util.UUID.randomUUID();
        Flavor newFlavor = Flavor.builder().id(id.toString()).name(name).disk(disk).ram(ram).vcpus(vcpus).build();
        FlavorApi flavorApi = this.novaApi.getFlavorApi(this.defaultZone);
        Flavor jcloudsFlavor = flavorApi.create(newFlavor);
        DeploymentFlavour flavor = new DeploymentFlavour();
        flavor.setExtId(jcloudsFlavor.getId());
        flavor.setFlavour_key(jcloudsFlavor.getName());
        flavor.setVcpus(jcloudsFlavor.getVcpus());
        flavor.setRam(jcloudsFlavor.getRam());
        flavor.setDisk(jcloudsFlavor.getVcpus());
        return flavor;
    }

    public boolean deleteFlavor(String extId) {
        FlavorApi flavorApi = this.novaApi.getFlavorApi(this.defaultZone);
        flavorApi.delete(extId);
        return true;
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
            log.trace("got flavor: " + jcloudsFlavor);
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
