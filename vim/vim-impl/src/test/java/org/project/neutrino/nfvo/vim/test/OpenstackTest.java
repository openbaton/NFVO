package org.project.neutrino.nfvo.vim.test;

import com.google.common.collect.Multimap;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Link;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.record.Status;
import org.project.neutrino.nfvo.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.neutrino.nfvo.catalogue.nfvo.*;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.Server;
import org.project.neutrino.nfvo.vim_interfaces.exceptions.VimException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static org.mockito.Matchers.*;
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

    private org.jclouds.openstack.nova.v2_0.domain.Server exp_server;

    @Before
    public void init() {
        vimInstance = createVimInstance();
        NeutronApi neutronApi = mock(NeutronApi.class);

        openstackClient.setNeutronApi(neutronApi);
        NovaApi novaApi = mock(NovaApi.class);
        ServerApi serverApi = mock(ServerApi.class);
        MyResource res = new MyResource("mocked_res_id","mocked_name",new HashSet<Link>());
        ServerExtendedStatus extStatus = new MyExtendedStatus("mocked_id","mocked_name",0);
        exception.expect(NullPointerException.class);
        exp_server = new MyServer("mocked_id","moked_name",null,"mocked_extId", "", "", new Date(),new Date(),"","mocked_ip4","mocked_ip6", org.jclouds.openstack.nova.v2_0.domain.Server.Status.ACTIVE,res,res,"","",mock(Multimap.class), new HashMap<String, String>(), extStatus,mock(ServerExtendedAttributes.class),"","");
        when(serverApi.get(anyString())).thenReturn(exp_server);
        ServerCreated serverCreated = mock(ServerCreated.class);
        when(serverCreated.getId()).thenReturn("mocked_id");
        when(serverApi.create(anyString(), anyString(), anyString(), any(CreateServerOptions.class))).thenReturn(serverCreated);
        when(novaApi.getServerApi(anyString())).thenReturn(serverApi);
        openstackClient.setNovaApi(novaApi);
        vdu = createVDU();
        vdu.setVimInstance(vimInstance);
        vnfr = createVNFR();
        definedNetwork = createNetwork();
        definedSubnet = createSubnet();
    }

    @Test
    public void testLauchInstance(){
        Server server = openstackClient.launchInstance(vdu.getHostname(), "image", "flavorid", "keypair", new ArrayList<String>(), new ArrayList<String>(), "#userdata");
        assertEqualsServers(exp_server,server);
    }

    @Test
    @Ignore
    public void test_server() throws VimException {
        Server server = test_launch_server();
        test_delete_server(server);
    }

    public Server test_launch_server() throws VimException {
        String hostname = vdu.getHostname();
        String image_name = vdu.getVm_image().get(0);
        String image_id = openstackClient.getImageIdByName(image_name);
        String flavor_name = vnfr.getDeployment_flavour_key();
        String flavor_id = openstackClient.getFlavorIdByName(flavor_name);
        String key_pair = "";
        List<String> networks = new ArrayList<String>();
        List<String> sec_groups = new ArrayList<String>();
        String user_data = "";

        Server definedServer = new Server();
        definedServer.setName(hostname);
        definedServer.setImage(openstackClient.getImageById(image_id));
        definedServer.setFlavor(openstackClient.getFlavorById(flavor_id));
        //Only when using the launch instance method with waiting
        definedServer.setStatus("ACTIVE");
        definedServer.setExtendedStatus("ACTIVE");

        Server expectedServer = openstackClient.launchInstanceAndWait(hostname, image_id, flavor_id, key_pair, networks, sec_groups, user_data);
        //Set external id of defined server
        definedServer.setExtId(expectedServer.getExtId());

        //check that server is existing
        try {
            openstackClient.getServerById(expectedServer.getExtId());
        } catch (NullPointerException e) {
            Assert.assertTrue("Server was was not created successfully.", false);
        }
        //Check values of the created server
        assertEqualsServers(expectedServer, definedServer);
        return expectedServer;
    }

    public void test_delete_server(Server server){
        openstackClient.deleteServerByIdAndWait(server.getExtId());
        try {
            openstackClient.getServerById(server.getExtId());
        } catch (NullPointerException e) {
            Assert.assertTrue("Server was deleted successfully", true);
        }
    }

    @Test
    @Ignore
    public void test_list_and_get_servers(){
        List<Server> actualServers = openstackClient.listServer();
        for (Server actualServer : actualServers) {
            Server expectedServer = openstackClient.getServerById(actualServer.getExtId());
            assertEqualsServers(expectedServer, actualServer);
        }
    }

    @Test
    @Ignore
    public void test_list_and_get_images(){
        List<NFVImage> actualImages = openstackClient.listImages();
        for (NFVImage actualImage: actualImages) {
            NFVImage expectedImage = openstackClient.getImageById(actualImage.getExtId());
            assertEqualsImages(expectedImage, actualImage);
        }
    }

    @Test
    @Ignore
    public void test_list_and_get_flavors() {
        List<DeploymentFlavour> actualFlavors = new ArrayList<DeploymentFlavour>();
        for (DeploymentFlavour actualFlavor : actualFlavors) {
            DeploymentFlavour expectedFlavor = openstackClient.getFlavorById(actualFlavor.getExtId());
            assertEqualsFlavors(expectedFlavor, actualFlavor);
        }
    }

    @Test
    @Ignore
    public void test_list_and_get_networks(){
        List<Network> actualNetworks = openstackClient.listNetworks();
        for (Network actualNetwork : actualNetworks) {
            Network expectedNetwork = openstackClient.getNetworkById(actualNetwork.getExtId());
            assertEqualsNetworks(expectedNetwork, actualNetwork);
        }
    }

    @Test
    @Ignore
    public void test_networks() {
        //create and check a new network
        Network testNetwork = test_create_network();
        //create and add subnet to the network
        Subnet testSubnet = test_create_subnet(testNetwork);
        //Check that subnet is associated with the network
        //testNetwork = clientInterfaces.getNetworkById(testNetwork.getExtId());
        //Assert.assertEquals(testNetwork.getId(), testSubnet.getNetworkId());
        //Assert.assertThat(testNetwork.getSubnets(), CoreMatchers.hasItem(testSubnet));
        //Delete and Check subnet
        test_delete_subnet(testNetwork, testSubnet);
        //delete and check network
        test_delete_network(testNetwork);
    }

    public Network test_create_network() {
        //Create a new network
        Network expectedNetwork = openstackClient.createNetwork(definedNetwork.getName(), definedNetwork.getNetworkType(), definedNetwork.getExternal(), definedNetwork.getShared(), definedNetwork.getSegmentationId(), definedNetwork.getPhysicalNetworkName());
        //Check that network is existing
        try {
            openstackClient.getNetworkById(expectedNetwork.getExtId());
            Assert.assertTrue("Network was created successfully.", true);
        } catch (NullPointerException e) {
            Assert.assertTrue("Network was not created successfully.", false);
        }
        //Check that network was created properly
        assertEqualsNetworks(expectedNetwork, definedNetwork);
        return expectedNetwork;
    }

    public void test_delete_network(Network network){
        //Delete network
        openstackClient.deleteNetwork(network.getExtId());
        //Check that network is deleted
        try {
            openstackClient.getNetworkById(network.getExtId());
            Assert.assertTrue("Network was not deleted successfully.", false);
        } catch (NullPointerException e) {
            Assert.assertTrue("Network was deleted successfully.", true);
        }
    }

    public Subnet test_create_subnet(Network network) {
        //Create subnet in the passed network
        Subnet expectedSubnet = openstackClient.createSubnet(network, definedSubnet.getName(), definedSubnet.getCidr());
        //update defined subnet values
        definedSubnet.setExtId(expectedSubnet.getExtId());
        definedSubnet.setNetworkId(network.getId());
        //Check that subnet is existing
        try {
            openstackClient.getSubnetById(expectedSubnet.getExtId());
            Assert.assertTrue("Subnet was created successfully", true);
        } catch (NullPointerException e) {
            Assert.assertTrue("Subnet was created successfully.", false);
        }
        //Check that the subnet was created properly
        assertEqualsSubnets(expectedSubnet, definedSubnet);
        return expectedSubnet;
    }

    public void test_delete_subnet(Network network, Subnet subnet) {
        openstackClient.deleteSubnet(subnet);
        //Check that Subnet was delete successfully
        try {
            openstackClient.getSubnetById(subnet.getExtId());
            Assert.assertTrue("Subnet was not deleted successfully", false);
        } catch (NullPointerException e) {
            Assert.assertTrue("Subnet was deleted successfully.", true);
        }
    }

    private VimInstance createVimInstance() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setUsername("user");
        vimInstance.setPassword("pass");
        vimInstance.setTenant("tenant");
        vimInstance.setAuthUrl("url");
        return vimInstance;
    }

    private VirtualNetworkFunctionRecord createVNFR(){
        VirtualNetworkFunctionRecord vnfr = new VirtualNetworkFunctionRecord();
        vnfr.setName("testVnfr");
        vnfr.setStatus(Status.INITIAILZED);
        vnfr.setAudit_log("audit_log");
        vnfr.setDescriptor_reference("test_dr");
        VNFDeploymentFlavour deployment_flavour = new VNFDeploymentFlavour();
        vnfr.setDeployment_flavour_key("m1.small");
        return vnfr;
    }

    private VirtualDeploymentUnit createVDU() {
        VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
        vdu.setHostname("test_server");
        ArrayList<String> monitoring_parameter = new ArrayList<>();
        monitoring_parameter.add("parameter_1");
        monitoring_parameter.add("parameter_2");
        monitoring_parameter.add("parameter_3");
        vdu.setMonitoring_parameter(monitoring_parameter);
        vdu.setComputation_requirement("computation_requirement");
        ArrayList<String> vm_images = new ArrayList<>();
        vm_images.add("cirros");
        vdu.setVm_image(vm_images);
        return vdu;
    }

    private Network createNetwork(){
        Network network = new Network();
        network.setName("test_network");
        network.setNetworkType("VLAN");
        network.setExternal(false);
        network.setShared(false);
        network.setSegmentationId(4000);
        network.setPhysicalNetworkName("invlan");
        return network;
    }

    private Subnet createSubnet(){
        Subnet subnet = new Subnet();
        subnet.setName("test_subnet");
        subnet.setCidr("192.168.123.0/24");
        return subnet;
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

    private void assertEqualsServers(org.jclouds.openstack.nova.v2_0.domain.Server expectedServer, Server actualServer) {
        Assert.assertEquals(expectedServer.getName(), actualServer.getName());
        Assert.assertEquals(expectedServer.getId(), actualServer.getExtId());
        Assert.assertEquals(expectedServer.getStatus().toString(), actualServer.getStatus());
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

}
