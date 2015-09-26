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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.openbaton.catalogue.mano.common.Security;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.exceptions.BadFormatException;
import org.project.openbaton.exceptions.NetworkServiceIntegrityException;
import org.project.openbaton.exceptions.NotFoundException;
import org.project.openbaton.nfvo.api.RestNetworkServiceDescriptor;
import org.project.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


public class ApiRestNSDescriptorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    RestNetworkServiceDescriptor restNetworkService;

    @Mock
    NetworkServiceDescriptorManagement nsdManagement;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NetworkServiceDescriptor networkServiceDescriptor;

    private VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        networkServiceDescriptor = new NetworkServiceDescriptor();
        networkServiceDescriptor.setVendor("Fokus");
        virtualNetworkFunctionDescriptor = new VirtualNetworkFunctionDescriptor();
        virtualNetworkFunctionDescriptor.setVendor("Fokus");
        networkServiceDescriptor.getVnfd().add(virtualNetworkFunctionDescriptor);
        VNFDependency vnfdependency = new VNFDependency();
        networkServiceDescriptor.getVnf_dependency().add(vnfdependency);
        PhysicalNetworkFunctionDescriptor pDescriptor = new PhysicalNetworkFunctionDescriptor();
        networkServiceDescriptor.getPnfd().add(pDescriptor);
        Security security = new Security();
        networkServiceDescriptor.setNsd_security(security);
    }

    @Test
    public void NSDFindAll() {
        Iterable<NetworkServiceDescriptor> list = nsdManagement.query();
        when(nsdManagement.query()).thenReturn(list);
        assertEquals(list, restNetworkService.findAll());
    }

    @Test
    public void NSDCreate() throws NotFoundException, BadFormatException, NetworkServiceIntegrityException {
        when(nsdManagement.onboard(networkServiceDescriptor)).thenReturn(networkServiceDescriptor);
        NetworkServiceDescriptor networkServiceDescriptor2 = restNetworkService.create(networkServiceDescriptor);
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
                nsdManagement.update(networkServiceDescriptor)).thenReturn(
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
        VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
        vnfd.setName("test_VNFD");
        Set<VirtualNetworkFunctionDescriptor> list = new HashSet<>();
        networkServiceDescriptor.setVnfd(list);

        when(nsdManagement.addVnfd(vnfd, networkServiceDescriptor.getId())).thenReturn(vnfd);

        VirtualNetworkFunctionDescriptor vnsDescriptor1 = restNetworkService.postVNFD(vnfd, networkServiceDescriptor.getId());
        assertEquals(vnsDescriptor1, vnfd);
    }

    @Test
    public void getVNFD() throws NotFoundException {
        VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor.getVnfd().iterator().next();
        when(nsdManagement.getVirtualNetworkFunctionDescriptor(networkServiceDescriptor.getId(), vnfd.getId())).thenReturn(vnfd);
        assertEquals(vnfd, restNetworkService.getVirtualNetworkFunctionDescriptor(networkServiceDescriptor.getId(), vnfd.getId()));
    }

    @Test
    public void getVNFDs() {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        Set<VirtualNetworkFunctionDescriptor> vnfds = networkServiceDescriptor.getVnfd();
        assertEquals(vnfds, restNetworkService.getVirtualNetworkFunctionDescriptors(networkServiceDescriptor.getId()));

    }

    @Test
    public void VNFDNotFoundException() throws NotFoundException {
        exception.expect(NotFoundException.class);
        when(nsdManagement.getVirtualNetworkFunctionDescriptor(anyString(), anyString())).thenThrow(NotFoundException.class);
        restNetworkService.getVirtualNetworkFunctionDescriptor(networkServiceDescriptor.getId(), "-1");
    }

    @Test
    public void updateVNF() {
        VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
        vnfd.setVendor("FOKUS");

        when(nsdManagement.updateVNF(networkServiceDescriptor.getId(), vnfd.getId(), vnfd)).thenReturn(vnfd);
        assertEquals(vnfd, restNetworkService.updateVNF(vnfd, networkServiceDescriptor.getId(), vnfd.getId()));
    }

    @Test
    public void deleteVNFD() throws NotFoundException {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor.getVnfd().iterator().next();
        restNetworkService.deleteVirtualNetworkFunctionDescriptor(networkServiceDescriptor.getId(), vnfd.getId());
        log.info("" + networkServiceDescriptor);
    }

    // XXX to here VirtualNetworkFunctionDescriptor

    // XXX FROM VNFDependency
    @Test
    public void postVNFDependency() {

        VNFDependency vnfd = new VNFDependency();
        Set<VNFDependency> list = new HashSet<>();
        networkServiceDescriptor.setVnf_dependency(list);
        when(nsdManagement.saveVNFDependency(anyString(), any(VNFDependency.class))).thenReturn(vnfd);
        VNFDependency vnsDependency1 = restNetworkService.postVNFDependency(vnfd, networkServiceDescriptor.getId());
        assertEquals(vnfd, vnsDependency1);
    }

    @Test
    public void getVNFDependency() throws NotFoundException {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency().iterator().next();
        assertEquals(vnfd.getId(), restNetworkService.getVNFDependency(networkServiceDescriptor.getId(), networkServiceDescriptor.getVnf_dependency().iterator().next().getId()));
    }

    @Test
    public void getVNFDependencies() {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        Set<VNFDependency> vnfds = networkServiceDescriptor.getVnf_dependency();
        assertEquals(vnfds, restNetworkService.getVNFDependencies(networkServiceDescriptor.getId()));

    }

    @Test
    public void VNFDependencyNotFoundException() throws NotFoundException {
        exception.expect(NotFoundException.class);
        when(nsdManagement.getVnfDependency(networkServiceDescriptor.getId(), "-1")).thenThrow(NotFoundException.class);
        restNetworkService.getVNFDependency(networkServiceDescriptor.getId(), "-1");
    }

    @Test
    public void updateVNFDependency() {
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
                restNetworkService.updateVNFDependency(vnfd,
                        networkServiceDescriptor.getId(), vnfd_toUp.getId()));

    }

    @Test
    public void deleteVNFDependency() throws NotFoundException {
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

//        networkServiceDescriptor.getPnfd().add(pnfd);
//        when(nsdManagement.update(networkServiceDescriptor)).thenReturn(networkServiceDescriptor);
//        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        when(nsdManagement.addPnfDescriptor(pnfd, networkServiceDescriptor.getId())).thenReturn(pnfd);
//        NetworkServiceDescriptor nsdUpdate = nsdManagement.update(networkServiceDescriptor);
        PhysicalNetworkFunctionDescriptor pnfd1 = restNetworkService.postPhysicalNetworkFunctionDescriptor(pnfd, networkServiceDescriptor.getId());

        assertEquals(pnfd, pnfd1);
    }

    @Test
    public void getPNFD() throws NotFoundException {
        PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor.getPnfd().iterator().next();
        when(nsdManagement.getPhysicalNetworkFunctionDescriptor(networkServiceDescriptor.getId(), pnfd.getId())).thenReturn(pnfd);
        assertEquals(pnfd, restNetworkService.getPhysicalNetworkFunctionDescriptor(networkServiceDescriptor.getId(), pnfd.getId()));
        assertEquals(pnfd.getId(), restNetworkService.getPhysicalNetworkFunctionDescriptor(networkServiceDescriptor.getId(), pnfd.getId()).getId());
    }

    @Test
    public void getPNFDs() {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        Set<PhysicalNetworkFunctionDescriptor> pnfds = networkServiceDescriptor.getPnfd();
        assertEquals(pnfds, restNetworkService.getPhysicalNetworkFunctionDescriptors(networkServiceDescriptor.getId()));

    }

    @Test
    public void PNFDNotFoundException() throws NotFoundException {
        exception.expect(NotFoundException.class);
        when(nsdManagement.getPhysicalNetworkFunctionDescriptor(anyString(), anyString())).thenThrow(NotFoundException.class);
        PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor = restNetworkService.getPhysicalNetworkFunctionDescriptor(networkServiceDescriptor.getId(), "-1");
    }

    @Test
    public void updatePNFD() {
        exception.expect(UnsupportedOperationException.class);
        PhysicalNetworkFunctionDescriptor actual = restNetworkService.updatePNFD(null, networkServiceDescriptor.getId(), null);
    }

    @Test
    public void deletePNFD() throws NotFoundException {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
        PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor.getPnfd().iterator().next();
        restNetworkService.deletePhysicalNetworkFunctionDescriptor(networkServiceDescriptor.getId(), pnfd.getId());
        log.info("" + networkServiceDescriptor);
    }

    // XXX HERE PhysicalNetworkFunctionDescriptor

    // XXX FROM Security
    @Test
    public void postSecurity() {
        Security security = new Security();
        when(nsdManagement.addSecurity(networkServiceDescriptor.getId(), security)).thenReturn(security);
        Security security1 = restNetworkService.postSecurity(security, networkServiceDescriptor.getId());
        assertEquals(security, security1);
    }

    @Test
    public void getSecurity() {
        when(nsdManagement.query(networkServiceDescriptor.getId())).thenReturn(networkServiceDescriptor);
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
