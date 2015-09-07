/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.openbaton.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.catalogue.mano.common.Security;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;


import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.nfvo.api.RestNetworkServiceDescriptor;

import org.project.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApiRestNSDescriptorTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@InjectMocks
	RestNetworkServiceDescriptor restNetworkService;

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
	public void NSDFindAll() {
		List<NetworkServiceDescriptor> list = nsdManagement.query();
		when(nsdManagement.query()).thenReturn(list);
		assertEquals(list, restNetworkService.findAll());
	}

	@Test
	public void NSDCreate() throws NotFoundException, BadFormatException {
		when(nsdManagement.onboard(networkServiceDescriptor)).thenReturn(networkServiceDescriptor);
		NetworkServiceDescriptor networkServiceDescriptor2  = restNetworkService.create(networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, networkServiceDescriptor2);
	}

	@Test
	public void NSDFindBy() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor,
				restNetworkService.findById(networkServiceDescriptor.getId()));
	}

	@Test
	public void NSDUpdate() {
		when(
				nsdManagement.update(networkServiceDescriptor,
						networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		assertEquals(networkServiceDescriptor, restNetworkService.update(
				networkServiceDescriptor, networkServiceDescriptor.getId()));
	}

	@Test
	public void NSDDelete() {
		nsdManagement.delete(anyString());
		restNetworkService.delete(anyString());
	}


	@Test
	public void postVNFD() {
		Set<VirtualNetworkFunctionDescriptor> list = new HashSet<>();
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
		Set<VirtualNetworkFunctionDescriptor> listVnfds = nsdUpdate.getVnfd();
		for (VirtualNetworkFunctionDescriptor vnsDescriptor : listVnfds) {
			if (vnsDescriptor.getId().equals(vnfd.getId()))
				assertEquals(vnsDescriptor1, vnsDescriptor);
			else {
				fail("postVNFD FAILED: not found the VNFD into NSD");
			}
		}

	}

	@Test
	public void getVNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor
				.getVnfd().iterator().next();
		assertEquals(vnfd,
				restNetworkService.getVirtualNetworkFunctionDescriptor(
						networkServiceDescriptor.getId(),
						networkServiceDescriptor.getVnfd().iterator().next().getId()));
	}

	@Test
	public void getVNFDs() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Set<VirtualNetworkFunctionDescriptor> vnfds = networkServiceDescriptor
				.getVnfd();
		assertEquals(
				vnfds,
				restNetworkService
						.getVirtualNetworkFunctionDescriptors(networkServiceDescriptor
								.getId()));

	}

	@Test
	public void VNFDNotFoundException() {
		exception.expect(VNFDNotFoundException.class);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		restNetworkService.getVirtualNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), "-1");
	}

	@Test
	public void updateVNF() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
		vnfd.setVendor("FOKUS");
		VirtualNetworkFunctionDescriptor vnfd_toUp = networkServiceDescriptor
				.getVnfd().iterator().next();
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
	public void deleteVNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor
				.getVnfd().iterator().next();
		restNetworkService.deleteVirtualNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), vnfd.getId());
		log.info("" + networkServiceDescriptor);
	}

	// XXX to here VirtualNetworkFunctionDescriptor

	// XXX FROM VNFDependency
	@Test
	public void postVNFDependency() {

		Set<VNFDependency> list = new HashSet<>();
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

		Set<VNFDependency> listVnfds = nsdUpdate.getVnf_dependency();
		for (VNFDependency vnsDependency : listVnfds) {
			if (vnsDependency.getId().equals(vnfd.getId()))
				assertEquals(vnsDependency1, vnsDependency);
			else {
				fail("postVNFDependency FAILED: not found the VNFDependency into NSD");
			}
		}

	}

	@Test
	public void getVNFDependency() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency()
				.iterator().next();
		assertEquals(vnfd, restNetworkService.getVNFDependency(
				networkServiceDescriptor.getId(), networkServiceDescriptor
						.getVnf_dependency().iterator().next().getId()));
	}

	@Test
	public void getVNFDependencies() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Set<VNFDependency> vnfds = networkServiceDescriptor
				.getVnf_dependency();
		assertEquals(vnfds,
				restNetworkService.getVNFDependencies(networkServiceDescriptor
						.getId()));

	}

	@Test
	public void VNFDependencyNotFoundException() {
		exception.expect(VNFDependencyNotFoundException.class);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		restNetworkService.getVNFDependency(networkServiceDescriptor.getId(),
				"-1");
	}

	@Test
	public void updateVNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VNFDependency vnfd = new VNFDependency();

		VNFDependency vnfd_toUp = networkServiceDescriptor.getVnf_dependency()
				.iterator().next();
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
	public void deleteVNFDependency() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency()
				.iterator().next();
		restNetworkService.deleteVNFDependency(
				networkServiceDescriptor.getId(), vnfd.getId());
		log.info("" + networkServiceDescriptor);
	}

	// XXX HERE VNFDependency

	// XXX FROM PhysicalNetworkFunctionDescriptor
	@Test
	public void postPNFD() {

		Set<PhysicalNetworkFunctionDescriptor> list = new HashSet<>();
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

		Set<PhysicalNetworkFunctionDescriptor> listVnfds = nsdUpdate.getPnfd();
		for (PhysicalNetworkFunctionDescriptor pnfdescriptor : listVnfds) {
			if (pnfdescriptor.getId().equals(pnfd.getId()))
				assertEquals(pnfdescriptor, pnfd1);
			else {
				fail("postPNFD FAILED: not found the PhysicalNetworkFunctionDescriptor into NSD");
			}
		}

	}

	@Test
	public void getPNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor
				.getPnfd().iterator().next();
		assertEquals(pnfd,
				restNetworkService.getPhysicalNetworkFunctionDescriptor(
						networkServiceDescriptor.getId(),
						networkServiceDescriptor.getPnfd().iterator().next().getId()));
	}

	@Test
	public void getPNFDs() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Set<PhysicalNetworkFunctionDescriptor> pnfds = networkServiceDescriptor
				.getPnfd();
		assertEquals(
				pnfds,
				restNetworkService
						.getPhysicalNetworkFunctionDescriptors(networkServiceDescriptor
								.getId()));

	}

	@Test
	public void PNFDNotFoundException() {
		exception.expect(PNFDNotFoundException.class);
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		restNetworkService.getPhysicalNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), "-1");
	}

	@Test
	public void updatePNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		PhysicalNetworkFunctionDescriptor pnfd = new PhysicalNetworkFunctionDescriptor();

		PhysicalNetworkFunctionDescriptor pnfd_toUp = networkServiceDescriptor
				.getPnfd().iterator().next();
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
	public void deletePNFD() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor
				.getPnfd().iterator().next();
		restNetworkService.deletePhysicalNetworkFunctionDescriptor(
				networkServiceDescriptor.getId(), pnfd.getId());
		log.info("" + networkServiceDescriptor);
	}

	// XXX HERE PhysicalNetworkFunctionDescriptor

	// XXX FROM Security
	@Test
	public void postSecurity() {

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
			fail("postSecurity FAILED: not found the Security into NSD");
		}

	}

	@Test
	public void getSecurity() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = networkServiceDescriptor.getNsd_security();
		/*assertEquals(security, restNetworkService.getSecurity(
				networkServiceDescriptor.getId(), networkServiceDescriptor
						.getNsd_security().getId()));*/
	}

	@Test
	public void getSecurities() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = networkServiceDescriptor.getNsd_security();
		assertEquals(security,
				restNetworkService.getSecurity(networkServiceDescriptor
						.getId()));

	}


	@Test
	public void updateSecurity() {
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
	public void deleteSecurity() {
		when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(
				networkServiceDescriptor);
		Security security = networkServiceDescriptor.getNsd_security();
		restNetworkService.deleteSecurity(networkServiceDescriptor.getId(),
				security.getId());
		log.info("" + networkServiceDescriptor);
	}
	// XXX HERE Security
}
