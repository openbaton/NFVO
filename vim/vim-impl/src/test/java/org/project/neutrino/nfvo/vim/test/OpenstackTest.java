package org.project.neutrino.nfvo.vim.test;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.jclouds.collect.IterableWithMarker;
import org.jclouds.collect.PagedIterable;
import org.jclouds.collect.PagedIterables;
import org.jclouds.io.Payload;
import org.jclouds.openstack.glance.v1_0.GlanceApi;
import org.jclouds.openstack.glance.v1_0.domain.ContainerFormat;
import org.jclouds.openstack.glance.v1_0.domain.DiskFormat;
import org.jclouds.openstack.glance.v1_0.domain.ImageDetails;
import org.jclouds.openstack.glance.v1_0.features.ImageApi;
import org.jclouds.openstack.glance.v1_0.options.CreateImageOptions;
import org.jclouds.openstack.glance.v1_0.options.UpdateImageOptions;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.*;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.neutron.v2.features.SubnetApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.extensions.QuotaApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Link;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.junit.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Quota;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.catalogue.nfvo.Subnet;
import org.project.neutrino.nfvo.common.exceptions.VimException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by mpa on 07.05.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class} )
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class OpenstackTest {

    @Autowired
    org.project.neutrino.nfvo.vim.client.openstack.OpenstackClient openstackClient;

    org.project.neutrino.nfvo.catalogue.nfvo.VimInstance vimInstance;

    org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit vdu;

    org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord vnfr;

    org.project.neutrino.nfvo.catalogue.nfvo.Network definedNetwork;

    org.project.neutrino.nfvo.catalogue.nfvo.Subnet definedSubnet;

    org.project.neutrino.nfvo.catalogue.nfvo.Server definedServer;

    org.project.neutrino.nfvo.catalogue.nfvo.NFVImage definedImage;

    org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour definedFlavor;

    org.project.neutrino.nfvo.catalogue.nfvo.Quota definedQuota;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private class MyExtendedStatus extends ServerExtendedStatus{
        protected MyExtendedStatus(String taskState, String vmState, int powerState) {
            super(taskState, vmState, powerState);
        }
    }

    private class MyResource extends Resource{
        protected MyResource(String id, String name, Set<Link> links) {
            super(id, name, links);
        }
    }

    private class MyServer extends org.jclouds.openstack.nova.v2_0.domain.Server{
        protected MyServer(String id, String name, Set<Link> links, String uuid, String tenantId, String userId, Date updated, Date created, String hostId, String accessIPv4, String accessIPv6, Status status, Resource image, Resource flavor, String keyName, String configDrive, Multimap<String, Address> addresses, Map<String, String> metadata, ServerExtendedStatus extendedStatus, ServerExtendedAttributes extendedAttributes, String diskConfig, String availabilityZone) {
            super(id, name, links, uuid, tenantId, userId, updated, created, hostId, accessIPv4, accessIPv6, status, image, flavor, keyName, configDrive, addresses, metadata, extendedStatus, extendedAttributes, diskConfig, availabilityZone);
        }
    }

    private class MyFlavor extends org.jclouds.openstack.nova.v2_0.domain.Flavor {
        protected MyFlavor(String id, String name, Set<Link> links, int ram, int disk, int vcpus, String swap, Double rxtxFactor, Integer ephemeral) {
            super(id, name, links, ram, disk, vcpus, swap, rxtxFactor, ephemeral);
        }
    }

    private class MyNovaImage extends org.jclouds.openstack.nova.v2_0.domain.Image {
        protected MyNovaImage(String id, String name, Set<Link> links, Date updated, Date created, String tenantId, String userId, Status status, int progress, int minDisk, int minRam, List<BlockDeviceMapping> blockDeviceMapping, Resource server, Map<String, String> metadata) {
            super(id, name, links, updated, created, tenantId, userId, status, progress, minDisk, minRam, blockDeviceMapping, server, metadata);
        }
    }

    private class MyGlanceImage extends org.jclouds.openstack.glance.v1_0.domain.Image {
        protected MyGlanceImage(String id, String name, Set<Link> links, ContainerFormat containerFormat, DiskFormat diskFormat, Long size, String checksum) {
            super(id, name, links, containerFormat, diskFormat, size, checksum);
        }
    }

    private class MyImageDetails extends ImageDetails {
        protected MyImageDetails(String id, String name, Set<Link> links, ContainerFormat containerFormat, DiskFormat diskFormat, Long size, String checksum, long minDisk, long minRam, String location, String owner, Date updatedAt, Date createdAt, Date deletedAt, Status status, boolean isPublic, Map<String, String> properties) {
            super(id, name, links, containerFormat, diskFormat, size, checksum, minDisk, minRam, location, owner, updatedAt, createdAt, deletedAt, status, isPublic, properties);
        }
    }

    private class MyFloatingIP extends org.jclouds.openstack.nova.v2_0.domain.FloatingIP {
        protected MyFloatingIP(String id, String ip, String fixedIp, String instanceId, String pool) {
            super(id, ip, fixedIp, instanceId, pool);
        }
    }

    private class MyQuota extends org.jclouds.openstack.nova.v2_0.domain.Quota {
        protected MyQuota(String id, int metadataItems, int injectedFileContentBytes, int volumes, int gigabytes, int ram, int floatingIps, int instances, int injectedFiles, int cores, int securityGroups, int securityGroupRules, int keyPairs) {
            super(id, metadataItems, injectedFileContentBytes, volumes, gigabytes, ram, floatingIps, instances, injectedFiles, cores, securityGroups, securityGroupRules, keyPairs);
        }
    }

    private MyServer expServer;
    private MyResource expServerResource;
    private MyGlanceImage expImageResource;
    private MyResource expFlavorResource;
    private MyFlavor expFlavor;
    private MyNovaImage expImage;
    private MyFloatingIP expFreeFloatingIP;
    private MyFloatingIP expUsedFloatingIP;
    private MyQuota expQuota;

    @Before
    public void init() {
        //pre-defined entities
        vimInstance = createVimInstance();
        definedImage = createImage();
        definedNetwork = createNetwork();
        definedSubnet = createSubnet();
        definedFlavor = createFlavor();
        definedServer = createServer();
        definedQuota = createQuota();
        //VimInstance
        openstackClient.setVimInstance(vimInstance);
        //NeutronApi
        NeutronApi neutronApi = mock(NeutronApi.class);
        openstackClient.setNeutronApi(neutronApi);
        //NovaApi
        NovaApi novaApi = mock(NovaApi.class);
        openstackClient.setNovaApi(novaApi);
        //Glance Api
        GlanceApi glanceApi = mock(GlanceApi.class);
        openstackClient.setGlanceApi(glanceApi);

        //Resources
        expFlavorResource = new MyResource(definedFlavor.getExtId(),definedFlavor.getFlavour_key(), new HashSet<Link>());
        List<Resource> resFlavorArray = new ArrayList<Resource>();
        resFlavorArray.add(expFlavorResource);
        FluentIterable<Resource> resFlavorFI = FluentIterable.from(resFlavorArray);

        expImageResource = new MyGlanceImage(definedImage.getExtId(), definedImage.getName(), new HashSet<Link>(), ContainerFormat.valueOf(definedImage.getContainerFormat()), DiskFormat.fromValue(definedImage.getDiskFormat()), (long) 1000, "");
        List<org.jclouds.openstack.glance.v1_0.domain.Image> resImageArray = new ArrayList<org.jclouds.openstack.glance.v1_0.domain.Image>();
        resImageArray.add(expImageResource);
        FluentIterable<org.jclouds.openstack.glance.v1_0.domain.Image> resImageFI = FluentIterable.from(resImageArray);

        expServerResource = new MyResource(definedServer.getExtId(), definedServer.getName(), new HashSet<Link>());
        List<Resource> resServerArray = new ArrayList<Resource>();
        resServerArray.add(expServerResource);
        FluentIterable<Resource> resServerFI = FluentIterable.from(resServerArray);

        //Flavor
        expFlavor = new MyFlavor(definedFlavor.getExtId(), definedFlavor.getFlavour_key(), new HashSet<Link>(), 512, 1, 2, "", 1.1, 1);
        //Image
        expImage = new MyNovaImage(definedImage.getExtId(), definedImage.getName(), new HashSet<Link>(), new Date(), new Date(), "", "", Image.Status.ACTIVE, 1, (int) definedImage.getMinDiskSpace(), (int) definedImage.getMinRam(), new ArrayList<BlockDeviceMapping>(), expImageResource, new HashMap<String, String>());
        //Server and Resources
        ServerExtendedStatus extStatus = new MyExtendedStatus("mocked_id","mocked_name",0);
        expServer = new MyServer(definedServer.getExtId(), definedServer.getName(), new HashSet<Link>(), definedServer.getExtId(), "", "", definedServer.getUpdated(), definedServer.getCreated(), "", "mocked_ip4", "mocked_ip6", org.jclouds.openstack.nova.v2_0.domain.Server.Status.fromValue(definedServer.getStatus()), expImage, expFlavor, "", "", mock(Multimap.class), new HashMap<String, String>(), extStatus, mock(ServerExtendedAttributes.class), "", "");
        ServerCreated serverCreated = mock(ServerCreated.class);

        //FloatingIP
        expFreeFloatingIP = new MyFloatingIP("mocked_ext_id", "mocked_free_ip", "mocked_fixed_ip", null, "mocked_pool");
        expUsedFloatingIP = new MyFloatingIP("mocked_ext_id", "mocked_used_ip", "mocked_fixed_ip", "mocked_instance_id", "mocked_pool");
        Set<FloatingIP> fipSet = new HashSet<FloatingIP>();
        fipSet.add(expFreeFloatingIP);
        fipSet.add(expUsedFloatingIP);
        FluentIterable<FloatingIP> fipFI = FluentIterable.from(fipSet);

        //Quota
        expQuota = new MyQuota(definedQuota.getTenant(), 10, 10, 10, 10, definedQuota.getRam(), definedQuota.getFloatingIps(), definedQuota.getInstances(), 10, definedQuota.getCores(), 10, 10, definedQuota.getKeyPairs());

        //exception.expect(NullPointerException.class);
        //ServerApi
        ServerApi serverApi = mock(ServerApi.class);
        when(novaApi.getServerApi(anyString())).thenReturn(serverApi);
        when(serverApi.get(definedServer.getExtId())).thenReturn(expServer);
        when(serverApi.list()).thenReturn(mock(PagedIterable.class));
        when(serverApi.list().concat()).thenReturn(resServerFI);
        when(serverCreated.getId()).thenReturn(definedServer.getExtId());
        when(serverApi.create(anyString(), anyString(), anyString(), any(CreateServerOptions.class))).thenReturn(serverCreated);

        //ImageApi
        ImageApi imageApi = mock(ImageApi.class);
        ImageDetails imageDetails = new MyImageDetails(definedImage.getExtId(), definedImage.getName(), null, ContainerFormat.fromValue(definedImage.getContainerFormat()), DiskFormat.fromValue(definedImage.getDiskFormat()), null, null, definedImage.getMinDiskSpace(), definedImage.getMinRam(), null, null, definedImage.getUpdated(), definedImage.getCreated(), null, org.jclouds.openstack.glance.v1_0.domain.Image.Status.ACTIVE, definedImage.isPublic(), new HashMap<String, String>());
        List<ImageDetails> imageList = new ArrayList<ImageDetails>();
        imageList.add(imageDetails);
        //ImmutableList<IterableWithMarker<ImageDetails>> imageILIWM = ImmutableList.of(((IterableWithMarker.from(imageList))));
        when(glanceApi.getImageApi(anyString())).thenReturn(imageApi);
        when(imageApi.get(definedImage.getExtId())).thenReturn(imageDetails);
        //when(imageApi.listInDetail()).thenReturn(mock(PagedIterable.class));
        //when(imageApi.listInDetail().toList()).thenReturn(mock(ImmutableList.class));
        when(imageApi.list()).thenReturn(mock(PagedIterable.class));
        when(imageApi.list().concat()).thenReturn(resImageFI);
        when(imageApi.create(anyString(), any(Payload.class), any(CreateImageOptions.class))).thenReturn(imageDetails);
        when(imageApi.update(anyString(), any(UpdateImageOptions.class))).thenReturn(imageDetails);
        when(imageApi.delete(anyString())).thenReturn(true);

//        IterableWithMarker<ImageDetails> jcloudsImages = mock(IterableWithMarker.class);
//        when(jcloudsImages.size()).thenReturn(1);
//        when(jcloudsImages.get(anyInt())).thenReturn(imageDetails);

        //FlavorApi
        FlavorApi flavorApi = mock(FlavorApi.class);
        when(novaApi.getFlavorApi(anyString())).thenReturn(flavorApi);
        when(flavorApi.get(definedFlavor.getExtId())).thenReturn(expFlavor);
        when(flavorApi.create(Matchers.<Flavor>anyObject())).thenReturn(expFlavor);
        when(flavorApi.list()).thenReturn(mock(PagedIterable.class));
        when(flavorApi.list().concat()).thenReturn(resFlavorFI);

        //NetworkApi
        NetworkApi networkApi = mock(NetworkApi.class);
        when(neutronApi.getNetworkApi(anyString())).thenReturn(networkApi);
        org.jclouds.openstack.neutron.v2.domain.Network network = mock(org.jclouds.openstack.neutron.v2.domain.Network.class);
        when(networkApi.create(any(org.jclouds.openstack.neutron.v2.domain.Network.CreateNetwork.class))).thenReturn(network);
        when(networkApi.update(anyString(), any(org.jclouds.openstack.neutron.v2.domain.Network.UpdateNetwork.class))).thenReturn((network));
        when(networkApi.delete(anyString())).thenReturn(true);
        when(networkApi.get(definedNetwork.getExtId())).thenReturn(network);
        when(network.getName()).thenReturn(definedNetwork.getName());
        when(network.getId()).thenReturn(definedNetwork.getExtId());
        when(network.getExternal()).thenReturn(definedNetwork.getExternal());
        when(network.getNetworkType()).thenReturn(NetworkType.fromValue(definedNetwork.getNetworkType()));
        when(network.getShared()).thenReturn(definedNetwork.getShared());
        when(network.getPhysicalNetworkName()).thenReturn(definedNetwork.getPhysicalNetworkName());
        when(network.getSegmentationId()).thenReturn(definedNetwork.getSegmentationId());
        when(network.getSubnets()).thenReturn(ImmutableSet.<String>of(definedSubnet.getExtId()));

        //SubnetApi
        SubnetApi subnetApi = mock(SubnetApi.class);
        when(neutronApi.getSubnetApi(anyString())).thenReturn(subnetApi);
        org.jclouds.openstack.neutron.v2.domain.Subnet subnet = mock(org.jclouds.openstack.neutron.v2.domain.Subnet.class);
        when(subnetApi.create(any(org.jclouds.openstack.neutron.v2.domain.Subnet.CreateSubnet.class))).thenReturn(subnet);
        when(subnetApi.update(anyString(), any(org.jclouds.openstack.neutron.v2.domain.Subnet.UpdateSubnet.class))).thenReturn((subnet));
        when(subnetApi.delete(anyString())).thenReturn(true);
        when(subnetApi.get(definedSubnet.getExtId())).thenReturn(subnet);
        when(subnet.getName()).thenReturn(definedSubnet.getName());
        when(subnet.getId()).thenReturn(definedSubnet.getExtId());
        when(subnet.getCidr()).thenReturn(definedSubnet.getCidr());

        //FloatingIPApi
        FloatingIPApi floatingIPApi = mock(FloatingIPApi.class);
        when(novaApi.getFloatingIPApi(anyString())).thenReturn(mock(Optional.class));
        when(novaApi.getFloatingIPApi(anyString()).get()).thenReturn(floatingIPApi);
        when(floatingIPApi.list()).thenReturn(fipFI);
        when(floatingIPApi.allocateFromPool(anyString())).thenReturn(expFreeFloatingIP);

        //QuotaApi
        QuotaApi quotaApi = mock(QuotaApi.class);
        when(novaApi.getQuotaApi(anyString())).thenReturn(mock(Optional.class));
        when(novaApi.getQuotaApi(anyString()).get()).thenReturn(quotaApi);
        when(quotaApi.getByTenant(vimInstance.getTenant())).thenReturn(expQuota);
    }

    @Test
    public void testLauchInstance(){
        Server server = openstackClient.launchInstance(definedServer.getName(), definedServer.getImage().getExtId(), definedServer.getFlavor().getExtId(), "keypair", new ArrayList<String>(), new ArrayList<String>(), "#userdata");
        assertEqualsServers(definedServer, server);
    }

    @Test
    public void testLauchInstanceAndWait() throws VimException{
        Server server = openstackClient.launchInstanceAndWait(definedServer.getName(), definedServer.getImage().getExtId(), definedServer.getFlavor().getExtId(), "keypair", new ArrayList<String>(), new ArrayList<String>(), "#userdata");
        assertEqualsServers(definedServer, server);
    }

    @Test
    public void testRebootServer() {
        openstackClient.rebootServer(definedServer.getExtId(), RebootType.SOFT);
    }

    @Test
    public void testDeleteServerById() {
        openstackClient.deleteServerById(definedServer.getExtId());
    }

    @Ignore
    @Test
    public void testDeleteServerByIdAndWait() {
        NovaApi novaApi = mock(NovaApi.class);
        ServerApi serverApi = mock(ServerApi.class);
        when(novaApi.getServerApi(anyString())).thenReturn(serverApi);
        when(serverApi.get(anyString())).thenThrow(new NullPointerException());
        openstackClient.deleteServerByIdAndWait(definedServer.getExtId());
    }

    @Test
    public void testGetServerById() {
        Server server = openstackClient.getServerById(definedServer.getExtId());
        assertEqualsServers(definedServer, server);
        exception.expect(NullPointerException.class);
        openstackClient.getServerById("not_existing_id");
    }

    @Test
    public void testGetServerIdByName() {
        String serverId = openstackClient.getServerIdByName(definedServer.getName());
        Assert.assertEquals(definedServer.getExtId(), serverId);
        exception.expect(NullPointerException.class);
        openstackClient.getServerIdByName("not_existing_name");
    }

    @Test
    public void testAddImage() {
        NFVImage image = openstackClient.addImage(definedImage, new ByteArrayInputStream("mocked_inputstream".getBytes()));
        assertEqualsImages(image, definedImage);
    }

    @Test
    public void testUpdateImage() {
        NFVImage image = openstackClient.updateImage(definedImage);
        assertEqualsImages(image, definedImage);
    }

    @Test
    public void testDeleteImage() {
        boolean isDeleted = openstackClient.deleteImage(definedImage);
        Assert.assertEquals(true, isDeleted);
    }

    @Test
    public void testGetImageById() {
        NFVImage image = openstackClient.getImageById(definedImage.getExtId());
        assertEqualsImages(definedImage, image);
        exception.expect(NullPointerException.class);
        openstackClient.getImageById("not_existing_id");
    }

    @Test
    public void testGetImageIdByName() {
        String imageId = openstackClient.getImageIdByName(definedImage.getName());
        Assert.assertEquals(definedImage.getExtId(), imageId);
        exception.expect(NullPointerException.class);
        openstackClient.getImageIdByName("not_existing_name");
    }

    @Test
    public void testListImages() {
//        List<NFVImage> images = openstackClient.listImages();
//        if (images.contains(definedImage)) {
//            Assert.assertTrue(true);
//        } else {
//            Assert.assertTrue(false);
//        }
    }

    @Test
    public void testAddFlavor() {
        DeploymentFlavour flavor = openstackClient.addFlavor(definedFlavor);
        assertEqualsFlavors(definedFlavor, flavor);
    }

    @Test
    public void testUpdateFlavor() throws VimException{
        DeploymentFlavour flavor = openstackClient.updateFlavor(definedFlavor);
        assertEqualsFlavors(definedFlavor, flavor);
        exception.expect(VimException.class);
        openstackClient.updateFlavor(new DeploymentFlavour());
    }

    @Test
    public void testGetFlavorIdByName() {
        String flavorId = openstackClient.getFlavorIdByName(definedFlavor.getFlavour_key());
        Assert.assertEquals(definedFlavor.getExtId(), flavorId);
        exception.expect(NullPointerException.class);
        openstackClient.getFlavorIdByName("not_existing_name");
    }

    @Test
    public void testListFlavors() {

    }

    @Test
    public void testGetFlavorById() {
        DeploymentFlavour flavor = openstackClient.getFlavorById(definedFlavor.getExtId());
        assertEqualsFlavors(definedFlavor, flavor);
    }

    @Test
    public void testCreateNetwork() {
        Network network = openstackClient.createNetwork(definedNetwork);
        assertEqualsNetworks(definedNetwork, network);
    }

    @Test
    public void testUpdateNetwork() {
        Network network = openstackClient.updateNetwork(definedNetwork);
        assertEqualsNetworks(definedNetwork, network);
    }

    @Test
    public void testDeleteNetwork() {
        boolean isDeleted = openstackClient.deleteNetwork(definedNetwork);
        Assert.assertEquals(true, isDeleted);
        isDeleted = openstackClient.deleteNetwork(definedNetwork.getExtId());
        Assert.assertEquals(true, isDeleted);
    }

    @Test
    public void testGetNetworkById() {
        Network network = openstackClient.getNetworkById(definedNetwork.getExtId());
        assertEqualsNetworks(definedNetwork, network);
        exception.expect(NullPointerException.class);
        openstackClient.getNetworkById("not_existing_id");
    }

    @Test
    public void testGetNetworkIdByName() {

    }

    @Test
    public void testGetSubnetsExtIds() {
        List<String> subnetExtIds = openstackClient.getSubnetsExtIds(definedNetwork.getExtId());
        Assert.assertEquals(subnetExtIds.get(0), definedSubnet.getExtId());
        exception.expect(NullPointerException.class);
        openstackClient.getSubnetsExtIds("not_existing_id");

    }

    @Test
    public void testListNetworks() {

    }

    @Test
    public void testCreateSubnet() {
        Subnet subnet = openstackClient.createSubnet(definedNetwork, definedSubnet);
        assertEqualsSubnets(definedSubnet, subnet);
    }

    @Test
    public void testUpdateSubnet() {
        Subnet subnet = openstackClient.updateSubnet(definedNetwork, definedSubnet);
        assertEqualsSubnets(definedSubnet, subnet);
    }

    @Test
    public void testDeleteSubnet() {
        boolean isDeleted = openstackClient.deleteSubnet(definedSubnet);
        Assert.assertEquals(true, isDeleted);
        isDeleted = openstackClient.deleteSubnet(definedSubnet.getExtId());
        Assert.assertEquals(true, isDeleted);
    }

    @Test
    public void testGetSubnetById() {
        Subnet subnet = openstackClient.getSubnetById(definedSubnet.getExtId());
        assertEqualsSubnets(definedSubnet, subnet);
        exception.expect(NullPointerException.class);
        openstackClient.getSubnetById("not_existing_id");
    }

    @Test
    public void testListSubnets() {

    }

    @Test
    public void testListAllFloatingIps() {
        List<String> floatingIPs = openstackClient.listAllFloatingIps();
        if (floatingIPs.contains(expFreeFloatingIP.getIp()) && floatingIPs.contains(expUsedFloatingIP.getIp())) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testListAssociatedFloatingIps() {
        List<String> floatingIPs = openstackClient.listAssociatedFloatingIps();
        if (!floatingIPs.contains(expFreeFloatingIP.getIp()) && floatingIPs.contains(expUsedFloatingIP.getIp())) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testListFreeFloatingIps() {
        List<String> floatingIPs = openstackClient.listFreeFloatingIps();
        if (floatingIPs.contains(expFreeFloatingIP.getIp()) && !floatingIPs.contains(expUsedFloatingIP.getIp())) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAssociateFloatingIpFromPool() {
        openstackClient.associateFloatingIpFromPool(definedServer, "mocked_pool");
    }

    @Test
    public void testAssociateFloatingIp() {
        openstackClient.associateFloatingIp(definedServer, "mocked_ip");
    }

    @Test
    public void testDisassociateFloatingIp() {
        openstackClient.disassociateFloatingIp(definedServer, "mocked_ip");
    }

    @Test
    public void testGetQuota() {
        Quota quota = openstackClient.getQuota();
        assertEqualsQuotas(definedQuota, quota);
    }

    private VimInstance createVimInstance() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setTenant("mocked_tenant");
        vimInstance.setImages(new ArrayList<NFVImage>() {{
            NFVImage nfvImage = new NFVImage();
            nfvImage.setName("mocked_image_name");
            nfvImage.setExtId("mocked_image_extId");
            add(nfvImage);
        }});
        return vimInstance;
    }

    private NFVImage createImage() {
        NFVImage image = new NFVImage();
        image.setName("mocked_image_name");
        image.setExtId("mocked_image_id");
        image.setMinRam(512);
        image.setMinDiskSpace(2);
        image.setIsPublic(true);
        image.setContainerFormat("AMI");
        image.setDiskFormat("AMI");
        image.setCreated(new Date());
        image.setUpdated(new Date());
        return image;
    }

    private DeploymentFlavour createFlavor() {
        DeploymentFlavour flavor = new DeploymentFlavour();
        flavor.setExtId("mocked_flavor_id");
        flavor.setFlavour_key("mocked_flavor_name");
        flavor.setRam(512);
        flavor.setDisk(1);
        flavor.setVcpus(2);
        return flavor;
    }

    private Server createServer() {
        Server server = new Server();
        server.setExtId("mocked_server_id");
        server.setName("mocked_server_name");
        server.setImage(definedImage);
        server.setFlavor(definedFlavor);
        server.setStatus("ACTIVE");
        server.setExtendedStatus("mocked_extended_status");
        HashMap<String, List<String>> ipMap = new HashMap<String, List<String>>();
        LinkedList<String> ips = new LinkedList();
        ips.add("mocked_ip");
        ipMap.put("mocked_network", ips);
        server.setIps(ipMap);
        server.setFloatingIp("mocked_floating_ip");
        server.setCreated(new Date());
        server.setUpdated(new Date());
        return server;
    }

    private Network createNetwork(){
        Network network = new Network();
        network.setName("mocked_network_name");
        network.setExtId("mocked_network_ext_id");
        network.setNetworkType("VLAN");
        network.setExternal(false);
        network.setShared(false);
        network.setSegmentationId(4000);
        network.setPhysicalNetworkName("invlan");
        return network;
    }

    private Subnet createSubnet(){
        Subnet subnet = new Subnet();
        subnet.setName("mocked_subnet_name");
        subnet.setExtId("mocked_subnet_ext_id");
        subnet.setCidr("192.168.123.0/24");
        return subnet;
    }

    private Quota createQuota() {
        Quota quota = new Quota();
        quota.setCores(10);
        quota.setInstances(10);
        quota.setRam(10);
        quota.setTenant("mocked_tenant");
        quota.setKeyPairs(10);
        quota.setFloatingIps(10);
        return quota;
    }

    private void assertEqualsServers(Server expectedServer, Server actualServer) {
        Assert.assertEquals(expectedServer.getName(), actualServer.getName());
        Assert.assertEquals(expectedServer.getExtId(), actualServer.getExtId());
        Assert.assertEquals(expectedServer.getStatus(), actualServer.getStatus());
        //Assert.assertEquals(expectedServer.getExtendedStatus(), actualServer.getExtendedStatus());
        //Assert.assertEquals(expectedServer.getCreated(), actualServer.getCreated());
        //Assert.assertEquals(expectedServer.getUpdated(), actualServer.getUpdated());
        //Assert.assertEquals(expectedServer.getIp(), actualServer.getIp());
    }

    private void assertEqualsImages(NFVImage expectedImage, NFVImage actualImage) {
        Assert.assertEquals(expectedImage.getName(), actualImage.getName());
        Assert.assertEquals(expectedImage.getExtId(), actualImage.getExtId());
        //Assert.assertEquals(expectedImage.getMinCPU(), actualImage.getMinCPU());
        Assert.assertEquals(expectedImage.getMinDiskSpace(), actualImage.getMinDiskSpace());
        Assert.assertEquals(expectedImage.getMinRam(), actualImage.getMinRam());
        Assert.assertEquals(expectedImage.getCreated(), actualImage.getCreated());
        Assert.assertEquals(expectedImage.getUpdated(), actualImage.getUpdated());
    }

    private void assertEqualsFlavors(DeploymentFlavour expectedFlavor, DeploymentFlavour actualFlavor) {
        Assert.assertEquals(expectedFlavor.getExtId(), actualFlavor.getExtId());
        Assert.assertEquals(expectedFlavor.getFlavour_key(), actualFlavor.getFlavour_key());
    }

    private void assertEqualsNetworks(Network expectedNetwork, Network actualNetwork) {
        Assert.assertEquals(expectedNetwork.getName(), actualNetwork.getName());
        Assert.assertEquals(expectedNetwork.getNetworkType().toLowerCase(), actualNetwork.getNetworkType().toLowerCase());
        Assert.assertEquals(expectedNetwork.getExternal(), actualNetwork.getExternal());
        Assert.assertEquals(expectedNetwork.getShared(), actualNetwork.getShared());
        Assert.assertEquals(expectedNetwork.getSubnets(), actualNetwork.getSubnets());
        Assert.assertEquals(expectedNetwork.getPhysicalNetworkName(), actualNetwork.getPhysicalNetworkName());
        Assert.assertEquals(expectedNetwork.getSegmentationId(), actualNetwork.getSegmentationId());
    }

    private void assertEqualsSubnets(Subnet expectedSubnet, Subnet actualSubnet) {
        Assert.assertEquals(expectedSubnet.getExtId(), actualSubnet.getExtId());
        Assert.assertEquals(expectedSubnet.getName(), actualSubnet.getName());
        Assert.assertEquals(expectedSubnet.getCidr(), actualSubnet.getCidr());
        Assert.assertEquals(expectedSubnet.getNetworkId(), actualSubnet.getNetworkId());
    }

    private void assertEqualsQuotas(Quota expectedQuota, Quota actualQuota) {
        Assert.assertEquals(expectedQuota.getTenant(), actualQuota.getTenant());
        Assert.assertEquals(expectedQuota.getCores(), actualQuota.getCores());
        Assert.assertEquals(expectedQuota.getFloatingIps(), actualQuota.getFloatingIps());
        Assert.assertEquals(expectedQuota.getInstances(), actualQuota.getInstances());
        Assert.assertEquals(expectedQuota.getKeyPairs(), actualQuota.getKeyPairs());
        Assert.assertEquals(expectedQuota.getRam(), actualQuota.getRam());
    }
}
