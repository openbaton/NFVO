package org.project.openbaton.nfvo.vim.test;

import org.jclouds.openstack.glance.v1_0.domain.DiskFormat;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.clients.interfaces.client.openstack.OpenstackClient;
import org.project.openbaton.common.catalogue.mano.common.DeploymentFlavour;

import org.project.openbaton.common.catalogue.nfvo.*;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by mpa on 07.05.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class} )
@ContextConfiguration(classes = {ApplicationTest.class})
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class OpenstackVersionTest {

    //private String IMAGE_NAME = "larsks/thttpd";
    private String IMAGE_NAME = "ubuntu-14.04-server-cloudimg-amd64-disk1";
    private String FLAVOR_NAME = "m1.small";

    @Autowired
    OpenstackClient openstackClient;

    VimInstance vimInstance;

    Server definedServer;

    Network definedNetwork;

    Subnet definedSubnet;

    DeploymentFlavour definedFlavor;

    @Before
    public void init() {
        vimInstance = createVimInstance();
        openstackClient.init(vimInstance);
        definedServer = createServer();
        definedNetwork = createNetwork();
        definedSubnet = createSubnet();
        definedFlavor = createFlavor();
    }

    @Ignore
    @Test
    public void complex_test() throws VimDriverException {
        //Creating networks
        Network network = test_create_network();
        assertEqualsNetworks(definedNetwork, network);
        Subnet subnet = test_create_subnet(network);
        assertEqualsSubnets(definedSubnet, subnet);
        //Launch Server
        Server server = test_launch_server(network);
        assertEqualsServers(definedServer, server);
        //Delete Server
        test_delete_server(server);
        //Delete Subnet
        test_delete_subnet(network, subnet);
        //Delete Network
        test_delete_network(network);
    }

    @Ignore
    @Test
    public void test_server() throws VimDriverException{
        String network_id = openstackClient.getNetworkIdByName("vpn");
        List<String> networks = new ArrayList<String>();
        networks.add(network_id);
        Server server = openstackClient.launchInstanceAndWait(definedServer.getName(), definedServer.getImage().getExtId(), definedServer.getFlavor().getExtId(), null, networks, new ArrayList<String>(), "#userdata");
    }

    public Server test_launch_server(Network network) throws VimDriverException {
        List<String> networks = new ArrayList<String>();
        if (network != null)
            networks.add(network.getExtId());
        Server server = openstackClient.launchInstanceAndWait(definedServer.getName(), definedServer.getImage().getExtId(), definedServer.getFlavor().getExtId(), null, networks, new ArrayList<String>(), "#userdata");
        return server;
    }

    public void test_delete_server(Server server){
        openstackClient.deleteServerByIdAndWait(server.getExtId());
        try {
            openstackClient.getServerById(server.getExtId());
        } catch (NullPointerException e) {
            Assert.assertTrue("Server was deleted successfully", true);
        }
    }

    @Ignore
    @Test
    public void test_list_and_get_servers(){
        List<Server> actualServers = openstackClient.listServer();
        for (Server actualServer : actualServers) {
            Server expectedServer = openstackClient.getServerById(actualServer.getExtId());
            assertEqualsServers(expectedServer, actualServer);
        }
    }

    @Ignore
    @Test
    public void test_list_and_get_images(){
        List<NFVImage> actualImages = openstackClient.listImages();
        for (NFVImage actualImage: actualImages) {
            NFVImage expectedImage = openstackClient.getImageById(actualImage.getExtId());
            assertEqualsImages(expectedImage, actualImage);
        }
    }

    @Ignore
    @Test
    public void test_flavors() throws VimDriverException {
        //Create Flavor
        DeploymentFlavour flavor = openstackClient.addFlavor(definedFlavor);
        assertEqualsFlavors(definedFlavor, flavor);
        //Check flavor
        DeploymentFlavour fetchedFlavor = openstackClient.getFlavorById(flavor.getExtId());
        assertEqualsFlavors(definedFlavor, fetchedFlavor);
        //Update Flavor
        flavor.setFlavour_key("updated_flavor");
        flavor.setDisk(2);
        flavor.setRam(1024);
        flavor.setVcpus(2);
        DeploymentFlavour updatedFlavor = openstackClient.updateFlavor(flavor);
        assertEqualsFlavors(flavor, updatedFlavor);
        //Check flavor
        fetchedFlavor = openstackClient.getFlavorById(flavor.getExtId());
        assertEqualsFlavors(flavor, fetchedFlavor);
        //Delete existing flavor
        Boolean isDeleted = openstackClient.deleteFlavor(flavor.getExtId());
        Assert.assertTrue(isDeleted);
        //Delete non existing flavor
        isDeleted = openstackClient.deleteFlavor("mocked_id");
        Assert.assertFalse(isDeleted);

    }

    @Ignore
    @Test
    public void test_list_and_get_flavors() {
        List<DeploymentFlavour> actualFlavors = openstackClient.listFlavors();
        for (DeploymentFlavour actualFlavor : actualFlavors) {
            DeploymentFlavour expectedFlavor = openstackClient.getFlavorById(actualFlavor.getExtId());
            assertEqualsFlavors(expectedFlavor, actualFlavor);
        }
    }

    @Ignore
    @Test
    public void test_list_and_get_networks(){
        List<Network> actualNetworks = openstackClient.listNetworks();
        for (Network actualNetwork : actualNetworks) {
            Network expectedNetwork = openstackClient.getNetworkById(actualNetwork.getExtId());
            assertEqualsNetworks(expectedNetwork, actualNetwork);
        }
    }

    @Ignore
    @Test
    public void test_networks() {
        //create network and subnet
        Network network = test_create_network();
        Subnet subnet = test_create_subnet(network);
        //Fetch created network and check
        //Network fetchedNetwork = openstackClient.getNetworkById(network.getExtId());
        //assertEqualsNetworks(definedNetwork, fetchedNetwork);
        //Fetch created subnet and check
        //Subnet fetchedSubnet = openstackClient.getSubnetById(subnet.getExtId());
        //assertEqualsSubnets(definedSubnet, fetchedSubnet);
        //Update network
        network.setName("updated_name");
        openstackClient.updateNetwork(network);
        //fetchedNetwork = openstackClient.getNetworkById(network.getExtId());
        //assertEqualsNetworks(network, fetchedNetwork);
        //Delete and Check subnet
        //test_delete_subnet(network, subnet);
        //delete and check network
        //test_delete_network(network);
    }

    public Network test_create_network() {
        //Create a new network
        //Network expectedNetwork = openstackClient.createNetwork(definedNetwork.getName(), definedNetwork.getNetworkType(), definedNetwork.getExternal(), definedNetwork.getShared(), definedNetwork.getSegmentationId(), definedNetwork.getPhysicalNetworkName());
        Network expectedNetwork = new Network();
        //Check that network is existing
        try {
            openstackClient.getNetworkById(expectedNetwork.getExtId());
            Assert.assertTrue("Network was created successfully.", true);
        } catch (NullPointerException e) {
            Assert.assertTrue("Network was not created successfully.", false);
        }
        //Check that network was created properly
        //assertEqualsNetworks(expectedNetwork, definedNetwork);
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

    @Ignore
    @Test
    public void testImages(){
//        openstackClient.copyImage("957ff0f3-5e2e-4290-8052-ac5e73a1a549", "cirros_test", "QCOW2", "BARE", 1, 512, true);
//        openstackClient.updateImage("9a81af24-046e-4b93-a185-eaef08d9e334", "cirros_test1", "AMI", "AMI", 1, 512, true);
//        openstackClient.deleteImage("9a81af24-046e-4b93-a185-eaef08d9e334");
        System.out.println("IMAGE");
        System.out.println(openstackClient.getImageIdByName("ubuntu"));
    }

    @Ignore
    @Test
    public void testFlavor(){
        openstackClient.addFlavor("test_flavor", 2, 512, 1);
        //openstackClient.deleteFlavor("15950cf4-243c-4d80-bf24-36f5ce4488e2");
    }

    @Ignore
    @Test
    public void testSubnets(){
        System.out.println("SUBNETS: ");
        List<String> subnets = openstackClient.getSubnetsExtIds("fd704f1b-9238-4c2c-a0f5-4ffb4543e33a");
        for (String subnet : subnets) {
            System.out.println(subnet);
        }
    }

    @Ignore
    @Test
    public void testQuota() {
        System.out.println("QUOTA");
        Quota quota = openstackClient.getQuota();
        System.out.println(quota.toString());
    }

    @Ignore
    @Test
    public void testFloatingIp() {
        System.out.println("FIP");
        List<String> floatingIps = openstackClient.listFreeFloatingIps();
        for (String ip : floatingIps) {
            System.out.println(ip);
        }
        Server server = new Server();
        server.setExtId("a58a1c10-0b3c-4fea-a6e4-1b47f0b36b91");
        openstackClient.associateFloatingIpFromPool(server, "external");
    }

    private VimInstance createVimInstance2() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setUsername("trescimo");
        vimInstance.setPassword("TFOwCCnmtv");
        vimInstance.setTenant("trescimo");
        vimInstance.setAuthUrl("http://server1.av.tu-berlin.de:35357/v2.0");
        return vimInstance;
    }

    private VimInstance createVimInstance3() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setUsername("admin");
        vimInstance.setPassword("e0ebe520a17576cc3281");
        vimInstance.setTenant("admin");
        vimInstance.setAuthUrl("http://192.168.145.18:5000/v2.0");
        return vimInstance;
    }

    private VimInstance createVimInstance1() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setName("mock_vim_instance");
        vimInstance.setUsername("nubomedia");
        vimInstance.setPassword("nub0m3d1@");
        vimInstance.setTenant("nubomedia");
        vimInstance.setAuthUrl("http://80.96.122.48:5000/v2.0");
        return vimInstance;
    }

    private VimInstance createVimInstance() {
        VimInstance vimInstance = new VimInstance();
        vimInstance.setType("openstack");
        vimInstance.setName("mock_vim_instance");
        vimInstance.setUsername("admin");
        vimInstance.setPassword("pass");
        vimInstance.setTenant("admin");
        vimInstance.setAuthUrl("http://192.168.41.45:5000/v2.0");
        return vimInstance;
    }

    private Server createServer() {
        Server server = new Server();
        server.setName("test_server");
        server.setImage(openstackClient.getImageById(openstackClient.getImageIdByName(IMAGE_NAME)));
        server.setFlavor(openstackClient.getFlavorById(openstackClient.getFlavorIdByName(FLAVOR_NAME)));
        return server;
    }

    private Network createNetwork(){
        Network network = new Network();
        network.setName("test_network");
        network.setExternal(false);
        network.setShared(false);
        return network;
    }

    private Subnet createSubnet(){
        Subnet subnet = new Subnet();
        subnet.setName("test_subnet");
        subnet.setCidr("192.168." + (int) (Math.random() * 255) + ".0/24");
        return subnet;
    }

    private DeploymentFlavour createFlavor() {
        DeploymentFlavour flavor = new DeploymentFlavour();
        flavor.setFlavour_key("test_flavor");
        flavor.setDisk(2);
        flavor.setRam(1024);
        flavor.setVcpus(2);
        return flavor;
    }

    private void assertEqualsServers(Server expectedServer, Server actualServer) {
        Assert.assertEquals(expectedServer.getName(), actualServer.getName());
        //Assert.assertEquals(expectedServer.getExtId(), actualServer.getExtId());
        //Assert.assertEquals(expectedServer.getStatus(), actualServer.getStatus());
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
        Assert.assertEquals(expectedNetwork.isExternal(), actualNetwork.isExternal());
        Assert.assertEquals(expectedNetwork.isShared(), actualNetwork.isShared());
        Assert.assertEquals(expectedNetwork.getSubnets(), actualNetwork.getSubnets());
    }

    private void assertEqualsSubnets(Subnet expectedSubnet, Subnet actualSubnet) {
        Assert.assertEquals(expectedSubnet.getExtId(), actualSubnet.getExtId());
        Assert.assertEquals(expectedSubnet.getName(), actualSubnet.getName());
        Assert.assertEquals(expectedSubnet.getCidr(), actualSubnet.getCidr());
        //Assert.assertEquals(expectedSubnet.getNetworkId(), actualSubnet.getNetworkId());
    }

}