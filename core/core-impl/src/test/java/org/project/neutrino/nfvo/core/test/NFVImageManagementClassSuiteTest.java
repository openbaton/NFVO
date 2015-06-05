package org.project.neutrino.nfvo.core.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.catalogue.mano.common.DeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.common.HighAvailability;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDeploymentFlavour;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.catalogue.nfvo.Network;
import org.project.neutrino.nfvo.catalogue.nfvo.VimInstance;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;
import org.project.neutrino.nfvo.core.interfaces.NFVImageManagement;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.project.neutrino.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by lto on 20/04/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(classes = { ApplicationTest.class })
@TestPropertySource(properties = { "timezone = GMT", "port: 4242" })
public class NFVImageManagementClassSuiteTest {

	private Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();


	@Autowired
	private NFVImageManagement nfvImageManagement;

	@Autowired
	@Qualifier("imageRepository")
	private GenericRepository<NFVImage> imageRepository;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(ApplicationTest.class);
		log.info("Starting test");
	}

	@Test
	public void nfvImageManagementNotNull(){
		Assert.assertNotNull(nfvImageManagement);
	}

	@Test
	public void nfvImageManagementUpdateTest(){
		NFVImage nfvImage_exp = createNfvImage();
		when(imageRepository.find(nfvImage_exp.getId())).thenReturn(nfvImage_exp);

		NFVImage nfvImage_new = createNfvImage();
		nfvImage_new.setName("UpdatedName");
		nfvImage_new.setMinRam(2046);
		nfvImage_exp = nfvImageManagement.update(nfvImage_new, nfvImage_exp.getId());

		Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
		Assert.assertEquals(nfvImage_exp.getExtId(),nfvImage_new.getExtId());
		Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
	}

	@Test
	public void nfvImageManagementCopyTest(){
		exception.expect(UnsupportedOperationException.class);
		nfvImageManagement.copy();
	}

	@Test
	public void nfvImageManagementAddTest(){
		NFVImage nfvImage_exp = createNfvImage();
		when(imageRepository.create(any(NFVImage.class))).thenReturn(nfvImage_exp);
		NFVImage nfvImage_new = nfvImageManagement.add(nfvImage_exp);

		Assert.assertEquals(nfvImage_exp.getId(),nfvImage_new.getId());
		Assert.assertEquals(nfvImage_exp.getName(),nfvImage_new.getName());
		Assert.assertEquals(nfvImage_exp.getExtId(),nfvImage_new.getExtId());
		Assert.assertEquals(nfvImage_exp.getMinRam(),nfvImage_new.getMinRam());
	}

	@Test
	public void nfvImageManagementQueryTest(){
		when(imageRepository.findAll()).thenReturn(new ArrayList<NFVImage>());

		Assert.assertEquals(0, nfvImageManagement.query().size());

		NFVImage nfvImage_exp = createNfvImage();
		when(imageRepository.find(nfvImage_exp.getId())).thenReturn(nfvImage_exp);
		NFVImage nfvImage_new = nfvImageManagement.query(nfvImage_exp.getId());
		Assert.assertEquals(nfvImage_exp.getId(), nfvImage_new.getId());
		Assert.assertEquals(nfvImage_exp.getName(), nfvImage_new.getName());
		Assert.assertEquals(nfvImage_exp.getExtId(), nfvImage_new.getExtId());
		Assert.assertEquals(nfvImage_exp.getMinRam(), nfvImage_new.getMinRam());
	}

	@Test
	public void nfvImageManagementDeleteTest(){
		NFVImage nfvImage_exp = createNfvImage();
		when(imageRepository.find(nfvImage_exp.getId())).thenReturn(nfvImage_exp);
		nfvImageManagement.delete(nfvImage_exp.getId());
		when(imageRepository.find(nfvImage_exp.getId())).thenReturn(null);
		NFVImage nfvImage_new = nfvImageManagement.query(nfvImage_exp.getId());
		Assert.assertNull(nfvImage_new);
	}

	@AfterClass
	public static void shutdown() {
		// TODO Teardown to avoid exceptions during test shutdown
	}


	private NFVImage createNfvImage() {
		NFVImage nfvImage = new NFVImage();
		nfvImage.setName("image_name");
		nfvImage.setExtId("ext_id");
		nfvImage.setMinCPU("1");
		nfvImage.setMinRam(1024);
		return nfvImage;
	}

	private NetworkServiceDescriptor createNetworkServiceDescriptor() {
		final NetworkServiceDescriptor nsd = new NetworkServiceDescriptor();
		nsd.setVendor("FOKUS");
		List<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = new ArrayList<VirtualNetworkFunctionDescriptor>();
		VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
		virtualNetworkFunctionDescriptor
				.setMonitoring_parameter(new ArrayList<String>() {
					{
						add("monitor1");
						add("monitor2");
						add("monitor3");
					}
				});
		virtualNetworkFunctionDescriptor.setDeployment_flavour(new ArrayList<VNFDeploymentFlavour>() {{
			VNFDeploymentFlavour vdf = new VNFDeploymentFlavour();
			vdf.setExtId("ext_id");
			vdf.setFlavour_key("flavor_name");
			add(vdf);
		}});
		virtualNetworkFunctionDescriptor
				.setVdu(new ArrayList<VirtualDeploymentUnit>() {
					{
						VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
						vdu.setHigh_availability(HighAvailability.ACTIVE_ACTIVE);
						vdu.setComputation_requirement("high_requirements");
						VimInstance vimInstance = new VimInstance();
						vimInstance.setName("vim_instance");
						vimInstance.setType("test");
						vdu.setVimInstance(vimInstance);
						add(vdu);
					}
				});
		virtualNetworkFunctionDescriptors.add(virtualNetworkFunctionDescriptor);
		nsd.setVnfd(virtualNetworkFunctionDescriptors);
		return nsd;
	}

	private VimInstance createVimInstance() {
		VimInstance vimInstance = new VimInstance();
		vimInstance.setName("vim_instance");
		vimInstance.setType("test");
		vimInstance.setNetworks(new ArrayList<Network>() {{
			Network network = new Network();
			network.setExtId("ext_id");
			network.setName("network_name");
			add(network);
		}});
		vimInstance.setFlavours(new ArrayList<DeploymentFlavour>() {{
			DeploymentFlavour deploymentFlavour = new DeploymentFlavour();
			deploymentFlavour.setExtId("ext_id_1");
			deploymentFlavour.setFlavour_key("flavor_name");
			add(deploymentFlavour);

			deploymentFlavour = new DeploymentFlavour();
			deploymentFlavour.setExtId("ext_id_2");
			deploymentFlavour.setFlavour_key("m1.tiny");
			add(deploymentFlavour);
		}});
		vimInstance.setImages(new ArrayList<NFVImage>() {{
			NFVImage image = new NFVImage();
			image.setExtId("ext_id_1");
			image.setName("ubuntu-14.04-server-cloudimg-amd64-disk1");
			add(image);

			image = new NFVImage();
			image.setExtId("ext_id_2");
			image.setName("image_name_1");
			add(image);
		}});
		return vimInstance;
	}

}
