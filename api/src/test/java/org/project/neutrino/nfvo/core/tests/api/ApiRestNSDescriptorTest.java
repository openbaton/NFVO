package org.project.neutrino.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.neutrino.nfvo.api.RestNetworkService;
import org.project.neutrino.nfvo.api.exceptions.PNFDNotFoundException;
import org.project.neutrino.nfvo.api.exceptions.VNFDNotFoundException;
import org.project.neutrino.nfvo.api.exceptions.VNFDependencyNotFoundException;
import org.project.neutrino.nfvo.catalogue.mano.common.Security;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDependency;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.neutrino.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRestNSDescriptorTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestNetworkService restNetworkService;

	@Mock
	NetworkServiceDescriptorManagement nsdManagement;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private NetworkServiceDescriptor networkServiceDescriptor;

	private VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		networkServiceDescriptor = new NetworkServiceDescriptor();
		networkServiceDescriptor.setVendor("Fokus");
		virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
		virtualNetworkFunctionDescriptor.setVendor("Fokus");
		networkServiceDescriptor.getVnfd()
				.add(virtualNetworkFunctionDescriptor);
		VNFDependency vnfdependency = new VNFDependency();
		networkServiceDescriptor.getVnf_dependency().add(vnfdependency);
		PhysicalNetworkFunctionDescriptor pDescriptor = new PhysicalNetworkFunctionDescriptor();
		networkServiceDescriptor.getPnfd().add(pDescriptor);
		Security security = new Security();
		networkServiceDescriptor.setNsd_security(security);
	}

	@Test
	public void testNSDFindAll() {
		List<NetworkServiceDescriptor> list = nsdManagement.query();
		when(nsdManagement.query()).thenReturn(list);
		assertEquals(list, restNetworkService.findAll());
	}

	@Test
	public void testNSDCreate() {
		when(nsdManagement.onboard(networkServiceDescriptor)).thenReturn(
				networkServiceDescriptor);
		NetworkServiceDescriptor networkServiceDescriptor2 = null;
		networkServiceDescriptor2 = restNetworkService
				.create(networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, networkServiceDescriptor2);
	}

	@Test
	public void testNSDFindBy() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor,
				restNetworkService.findById(networkServiceDescriptor.getId()));
	}

	@Test
	public void testNSDUpdate() {
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, restNetworkService.update(
				networkServiceDescriptor, networkServiceDescriptor.getId()));
	}

	@Test
	public void testNSDDelete() {
		nsdManagement.delete(anyString());
		restNetworkService.delete(anyString());
	}

	// XXX from here VirtualNetworkFunctionDescriptor
	@Test
	public void testpostVNFD() {
		List<VirtualNetworkFunctionDescriptor> list = new ArrayList<VirtualNetworkFunctionDescriptor>();
		networkServiceDescriptor.setVnfd(list);
		VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
		vnfd.setName("test_VNFD");
		networkServiceDescriptor.getVnfd().add(vnfd);
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		NetworkServiceDescriptor nsdUpdate = nsdManagement.update(
				networkServiceDescriptor, networkServiceDescriptor.getId());
		VirtualNetworkFunctionDescriptor vnsDescriptor1 = restNetworkService
				.postVNFD(vnfd, networkServiceDescriptor.getId());

		List<VirtualNetworkFunctionDescriptor> listVnfds = nsdUpdate.getVnfd();
		for (VirtualNetworkFunctionDescriptor vnsDescriptor : listVnfds) {
			if (vnsDescriptor.getId().equals(vnfd.getId()))
				assertEquals(vnsDescriptor1, vnsDescriptor);
			else {
				fail("testpostVNFD FAILED: not found the VNFD into NSD");
			}
		}

	}

	@Test
	public void testgetVNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor
				.getVnfd().get(0);
		assertEquals(vnfd,
				restNetworkService.getVirtualNetworkFunctionDescriptor(
						networkServiceDescriptor.getId(),
						networkServiceDescriptor.getVnfd().get(0).getId()));
	}

	@Test
	public void testgetVNFDs() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		List<VirtualNetworkFunctionDescriptor> vnfds = networkServiceDescriptor
				.getVnfd();
		assertEquals(
				vnfds,
				restNetworkService
						.getVirtualNetworkFunctionDescriptors(networkServiceDescriptor
								.getId()));

	}

	@Test
	public void testVNFDNotFoundException() {
		exception.expect(VNFDNotFoundException.class);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		restNetworkService.getVirtualNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), "-1");
	}

	@Test
	public void testupdateVNF() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
		vnfd.setVendor("FOKUS");
		VirtualNetworkFunctionDescriptor vnfd_toUp = networkServiceDescriptor
				.getVnfd().get(0);
		log.info("" + vnfd_toUp);
		vnfd_toUp = vnfd;
		networkServiceDescriptor.getVnfd().add(vnfd_toUp);
		log.info("" + vnfd);
		assertEquals(
				vnfd,
				restNetworkService.updateVNF(vnfd,
						networkServiceDescriptor.getId(), vnfd_toUp.getId()));

	}

	@Test
	public void testdeleteVirtualNetworkFunctionDescriptor() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor
				.getVnfd().get(0);
		restNetworkService.deleteVirtualNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), vnfd.getId());
		log.info("" + networkServiceDescriptor);
	}

	// XXX to here VirtualNetworkFunctionDescriptor

	// XXX FROM VNFDependency
	@Test
	public void testpostVNFDependency() {
		List<VNFDependency> list = new ArrayList<VNFDependency>();
		networkServiceDescriptor.setVnf_dependency(list);
		VNFDependency vnfd = new VNFDependency();

		networkServiceDescriptor.getVnf_dependency().add(vnfd);
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		NetworkServiceDescriptor nsdUpdate = nsdManagement.update(
				networkServiceDescriptor, networkServiceDescriptor.getId());
		VNFDependency vnsDependency1 = restNetworkService.postVNFDependency(
				vnfd, networkServiceDescriptor.getId());

		List<VNFDependency> listVnfds = nsdUpdate.getVnf_dependency();
		for (VNFDependency vnsDependency : listVnfds) {
			if (vnsDependency.getId().equals(vnfd.getId()))
				assertEquals(vnsDependency1, vnsDependency);
			else {
				fail("testpostVNFDependency FAILED: not found the VNFDependency into NSD");
			}
		}

	}

	@Test
	public void testgetVNFDependency() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency()
				.get(0);
		assertEquals(vnfd, restNetworkService.getVNFDependency(
				networkServiceDescriptor.getId(), networkServiceDescriptor
						.getVnf_dependency().get(0).getId()));
	}

	@Test
	public void testgetVNFDependencies() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		List<VNFDependency> vnfds = networkServiceDescriptor
				.getVnf_dependency();
		assertEquals(vnfds,
				restNetworkService.getVNFDependencies(networkServiceDescriptor
						.getId()));

	}

	@Test
	public void testVNFDependencyNotFoundException() {
		exception.expect(VNFDependencyNotFoundException.class);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		restNetworkService.getVNFDependency(networkServiceDescriptor.getId(),
				"-1");
	}

	@Test
	public void testupdateVNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VNFDependency vnfd = new VNFDependency();

		VNFDependency vnfd_toUp = networkServiceDescriptor.getVnf_dependency()
				.get(0);
		log.info("" + vnfd_toUp);
		vnfd_toUp = vnfd;
		networkServiceDescriptor.getVnf_dependency().add(vnfd_toUp);
		log.info("" + vnfd);
		assertEquals(
				vnfd,
				restNetworkService.updateVNFD(vnfd,
						networkServiceDescriptor.getId(), vnfd_toUp.getId()));

	}

	@Test
	public void testdeleteVNFDependency() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency()
				.get(0);
		restNetworkService.deleteVNFDependency(
				networkServiceDescriptor.getId(), vnfd.getId());
		log.info("" + networkServiceDescriptor);
	}

	// XXX HERE VNFDependency

	// XXX FROM PhysicalNetworkFunctionDescriptor
	@Test
	public void testpostPhysicalNetworkFunctionDescriptor() {
		List<PhysicalNetworkFunctionDescriptor> list = new ArrayList<PhysicalNetworkFunctionDescriptor>();
		networkServiceDescriptor.setPnfd(list);
		PhysicalNetworkFunctionDescriptor pnfd = new PhysicalNetworkFunctionDescriptor();

		networkServiceDescriptor.getPnfd().add(pnfd);
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		NetworkServiceDescriptor nsdUpdate = nsdManagement.update(
				networkServiceDescriptor, networkServiceDescriptor.getId());
		PhysicalNetworkFunctionDescriptor pnfd1 = restNetworkService
				.postPhysicalNetworkFunctionDescriptor(pnfd,
						networkServiceDescriptor.getId());

		List<PhysicalNetworkFunctionDescriptor> listVnfds = nsdUpdate.getPnfd();
		for (PhysicalNetworkFunctionDescriptor pnfdescriptor : listVnfds) {
			if (pnfdescriptor.getId().equals(pnfd.getId()))
				assertEquals(pnfdescriptor, pnfd1);
			else {
				fail("testpostPhysicalNetworkFunctionDescriptor FAILED: not found the PhysicalNetworkFunctionDescriptor into NSD");
			}
		}

	}

	@Test
	public void testgetPhysicalNetworkFunctionDescriptor() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor
				.getPnfd().get(0);
		assertEquals(pnfd,
				restNetworkService.getPhysicalNetworkFunctionDescriptor(
						networkServiceDescriptor.getId(),
						networkServiceDescriptor.getPnfd().get(0).getId()));
	}

	@Test
	public void testgetPhysicalNetworkFunctionDescriptors() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		List<PhysicalNetworkFunctionDescriptor> pnfds = networkServiceDescriptor
				.getPnfd();
		assertEquals(
				pnfds,
				restNetworkService
						.getPhysicalNetworkFunctionDescriptors(networkServiceDescriptor
								.getId()));

	}

	@Test
	public void testPhysicalNetworkFunctionDescriptorNotFoundException() {
		exception.expect(PNFDNotFoundException.class);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		restNetworkService.getPhysicalNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), "-1");
	}

	@Test
	public void testupdatePNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		PhysicalNetworkFunctionDescriptor pnfd = new PhysicalNetworkFunctionDescriptor();

		PhysicalNetworkFunctionDescriptor pnfd_toUp = networkServiceDescriptor
				.getPnfd().get(0);
		log.info("" + pnfd_toUp);
		pnfd_toUp = pnfd;
		networkServiceDescriptor.getPnfd().add(pnfd_toUp);
		log.info("" + pnfd);
		assertEquals(
				pnfd,
				restNetworkService.updatePNFD(pnfd,
						networkServiceDescriptor.getId(), pnfd_toUp.getId()));

	}

	@Test
	public void testdeletePhysicalNetworkFunctionDescriptor() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor
				.getPnfd().get(0);
		restNetworkService.deletePhysicalNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), pnfd.getId());
		log.info("" + networkServiceDescriptor);
	}

	// XXX HERE PhysicalNetworkFunctionDescriptor

	// XXX FROM Security
	@Test
	public void testpostSecurity() {

		Security security = new Security();

		networkServiceDescriptor.setNsd_security(security);
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		NetworkServiceDescriptor nsdUpdate = nsdManagement.update(
				networkServiceDescriptor, networkServiceDescriptor.getId());
		Security security1 = restNetworkService.postSecurity(security,
				networkServiceDescriptor.getId());

		Security sec = nsdUpdate.getNsd_security();

		if (sec.getId().equals(security.getId()))
			assertEquals(sec, security1);
		else {
			fail("testpostSecurity FAILED: not found the Security into NSD");
		}

	}

	@Test
	public void testgetSecurity() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = networkServiceDescriptor.getNsd_security();
		assertEquals(security, restNetworkService.getSecurity(
				networkServiceDescriptor.getId(), networkServiceDescriptor
						.getNsd_security().getId()));
	}

	@Test
	public void testgetSecuritys() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = networkServiceDescriptor.getNsd_security();
		assertEquals(security,
				restNetworkService.getSecurity(networkServiceDescriptor
						.getId()));

	}


	@Test
	public void testupdateSecurity() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = new Security();

		Security security_toUp = networkServiceDescriptor.getNsd_security();
		log.info("" + security_toUp);
		security_toUp = security;
		networkServiceDescriptor.setNsd_security(security_toUp);
		log.info("" + security);
		assertEquals(security, restNetworkService.updateSecurity(security,
				networkServiceDescriptor.getId(), security_toUp.getId()));

	}

	@Test
	public void testdeleteSecurity() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = networkServiceDescriptor.getNsd_security();
		restNetworkService.deleteSecurity(networkServiceDescriptor.getId(),
				security.getId());
		log.info("" + networkServiceDescriptor);
	}
	// XXX HERE Security
}
