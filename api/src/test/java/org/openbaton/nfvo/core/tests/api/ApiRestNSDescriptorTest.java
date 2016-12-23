/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.core.tests.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openbaton.catalogue.mano.common.Security;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.api.catalogue.RestNetworkServiceDescriptor;
import org.openbaton.nfvo.core.interfaces.NetworkServiceDescriptorManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRestNSDescriptorTest {

  @Rule public ExpectedException exception = ExpectedException.none();

  @InjectMocks RestNetworkServiceDescriptor restNetworkService;

  @Mock NetworkServiceDescriptorManagement nsdManagement;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  private NetworkServiceDescriptor networkServiceDescriptor;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    networkServiceDescriptor = new NetworkServiceDescriptor();
    networkServiceDescriptor.setVendor("Fokus");
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        new VirtualNetworkFunctionDescriptor();
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
    when(nsdManagement.queryByProjectId(anyString())).thenReturn(list);
    assertEquals(list, restNetworkService.findAll("project-id"));
  }

  @Test
  public void NSDCreate()
      throws NotFoundException, BadFormatException, NetworkServiceIntegrityException,
          CyclicDependenciesException, EntityInUseException {
    when(nsdManagement.onboard(any(networkServiceDescriptor.getClass()), anyString()))
        .thenReturn(networkServiceDescriptor);
    NetworkServiceDescriptor networkServiceDescriptor2 =
        restNetworkService.create(networkServiceDescriptor, "default");
    assertEquals(networkServiceDescriptor, networkServiceDescriptor2);
  }

  @Test
  public void NSDFindBy() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    assertEquals(
        networkServiceDescriptor,
        restNetworkService.findById(networkServiceDescriptor.getId(), "project-id"));
  }

  @Test
  public void NSDUpdate() {
    when(nsdManagement.update(any(networkServiceDescriptor.getClass()), anyString()))
        .thenReturn(networkServiceDescriptor);
    assertEquals(
        networkServiceDescriptor,
        restNetworkService.update(
            networkServiceDescriptor, networkServiceDescriptor.getId(), "project-id"));
  }

  @Test
  public void NSDDelete() throws WrongStatusException, EntityInUseException {
    restNetworkService.delete("id", "project-id");
  }

  @Test
  public void postVNFD() {
    VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
    vnfd.setName("test_VNFD");
    Set<VirtualNetworkFunctionDescriptor> list = new HashSet<>();
    networkServiceDescriptor.setVnfd(list);

    when(nsdManagement.addVnfd(any(vnfd.getClass()), anyString(), anyString())).thenReturn(vnfd);

    VirtualNetworkFunctionDescriptor vnsDescriptor1 =
        restNetworkService.postVNFD(vnfd, networkServiceDescriptor.getId(), "projectid");
    assertEquals(vnsDescriptor1, vnfd);
  }

  @Test
  public void getVNFD() throws NotFoundException {
    VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor.getVnfd().iterator().next();
    when(nsdManagement.getVirtualNetworkFunctionDescriptor(anyString(), anyString(), anyString()))
        .thenReturn(vnfd);
    assertEquals(
        vnfd,
        restNetworkService.getVirtualNetworkFunctionDescriptor(
            networkServiceDescriptor.getId(), vnfd.getId(), "peojectid"));
  }

  @Test
  public void getVNFDs() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Set<VirtualNetworkFunctionDescriptor> vnfds = networkServiceDescriptor.getVnfd();
    assertEquals(
        vnfds,
        restNetworkService.getVirtualNetworkFunctionDescriptors(
            networkServiceDescriptor.getId(), "projectid"));
  }

  @Test
  public void VNFDNotFoundException() throws NotFoundException {
    exception.expect(NotFoundException.class);
    when(nsdManagement.getVirtualNetworkFunctionDescriptor(anyString(), anyString(), anyString()))
        .thenThrow(NotFoundException.class);
    restNetworkService.getVirtualNetworkFunctionDescriptor(
        networkServiceDescriptor.getId(), "-1", "pi");
  }

  @Test
  public void updateVNF() {
    VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor();
    vnfd.setVendor("FOKUS");

    when(nsdManagement.updateVNF(anyString(), anyString(), any(vnfd.getClass()), anyString()))
        .thenReturn(vnfd);
    assertEquals(
        vnfd,
        restNetworkService.updateVNF(vnfd, networkServiceDescriptor.getId(), vnfd.getId(), ""));
  }

  @Test
  public void deleteVNFD() throws NotFoundException, EntityInUseException {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    VirtualNetworkFunctionDescriptor vnfd = networkServiceDescriptor.getVnfd().iterator().next();
    restNetworkService.deleteVirtualNetworkFunctionDescriptor(
        networkServiceDescriptor.getId(), vnfd.getId(), "");
    log.info("" + networkServiceDescriptor);
  }

  // XXX to here VirtualNetworkFunctionDescriptor

  // XXX FROM VNFDependency
  @Test
  public void postVNFDependency() {

    VNFDependency vnfd = new VNFDependency();
    Set<VNFDependency> list = new HashSet<>();
    networkServiceDescriptor.setVnf_dependency(list);
    when(nsdManagement.saveVNFDependency(anyString(), any(VNFDependency.class), anyString()))
        .thenReturn(vnfd);
    VNFDependency vnsDependency1 =
        restNetworkService.postVNFDependency(vnfd, networkServiceDescriptor.getId(), "");
    assertEquals(vnfd, vnsDependency1);
  }

  @Test
  public void getVNFDependency() throws NotFoundException {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency().iterator().next();
    assertEquals(
        vnfd.getId(),
        restNetworkService.getVNFDependency(
            networkServiceDescriptor.getId(),
            networkServiceDescriptor.getVnf_dependency().iterator().next().getId(),
            "pi"));
  }

  @Test
  public void getVNFDependencies() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Set<VNFDependency> vnfds = networkServiceDescriptor.getVnf_dependency();
    assertEquals(
        vnfds, restNetworkService.getVNFDependencies(networkServiceDescriptor.getId(), "pi"));
  }

  @Test
  public void VNFDependencyNotFoundException() throws NotFoundException {
    exception.expect(NotFoundException.class);
    when(nsdManagement.getVnfDependency(anyString(), anyString(), anyString()))
        .thenThrow(NotFoundException.class);
    restNetworkService.getVNFDependency(networkServiceDescriptor.getId(), "-1", "pi");
  }

  @Test
  public void updateVNFDependency() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    VNFDependency vnfd = new VNFDependency();

    VNFDependency vnfd_toUp = networkServiceDescriptor.getVnf_dependency().iterator().next();
    log.info("" + vnfd_toUp);
    vnfd_toUp = vnfd;
    networkServiceDescriptor.getVnf_dependency().add(vnfd_toUp);
    log.info("" + vnfd);
    assertEquals(
        vnfd,
        restNetworkService.updateVNFDependency(
            vnfd, networkServiceDescriptor.getId(), vnfd_toUp.getId(), "pi"));
  }

  @Test
  public void deleteVNFDependency() throws NotFoundException {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    VNFDependency vnfd = networkServiceDescriptor.getVnf_dependency().iterator().next();
    restNetworkService.deleteVNFDependency(networkServiceDescriptor.getId(), vnfd.getId(), "pi");
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
    when(nsdManagement.addPnfDescriptor(any(pnfd.getClass()), anyString(), anyString()))
        .thenReturn(pnfd);
    //        NetworkServiceDescriptor nsdUpdate = nsdManagement.update(networkServiceDescriptor);
    PhysicalNetworkFunctionDescriptor pnfd1 =
        restNetworkService.postPhysicalNetworkFunctionDescriptor(
            pnfd, networkServiceDescriptor.getId(), "pi");

    assertEquals(pnfd, pnfd1);
  }

  @Test
  public void getPNFD() throws NotFoundException {
    PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor.getPnfd().iterator().next();
    when(nsdManagement.getPhysicalNetworkFunctionDescriptor(anyString(), anyString(), anyString()))
        .thenReturn(pnfd);
    assertEquals(
        pnfd,
        restNetworkService.getPhysicalNetworkFunctionDescriptor(
            networkServiceDescriptor.getId(), pnfd.getId(), "pi"));
    assertEquals(
        pnfd.getId(),
        restNetworkService
            .getPhysicalNetworkFunctionDescriptor(
                networkServiceDescriptor.getId(), pnfd.getId(), "pi")
            .getId());
  }

  @Test
  public void getPNFDs() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Set<PhysicalNetworkFunctionDescriptor> pnfds = networkServiceDescriptor.getPnfd();
    assertEquals(
        pnfds,
        restNetworkService.getPhysicalNetworkFunctionDescriptors(
            networkServiceDescriptor.getId(), ""));
  }

  @Test
  public void PNFDNotFoundException() throws NotFoundException {
    exception.expect(NotFoundException.class);
    when(nsdManagement.getPhysicalNetworkFunctionDescriptor(anyString(), anyString(), anyString()))
        .thenThrow(NotFoundException.class);
    PhysicalNetworkFunctionDescriptor physicalNetworkFunctionDescriptor =
        restNetworkService.getPhysicalNetworkFunctionDescriptor(
            networkServiceDescriptor.getId(), "-1", "pi");
  }

  @Test
  public void updatePNFD() {
    exception.expect(UnsupportedOperationException.class);
    PhysicalNetworkFunctionDescriptor actual =
        restNetworkService.updatePNFD(null, networkServiceDescriptor.getId(), null, "");
  }

  @Test
  public void deletePNFD() throws NotFoundException {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    PhysicalNetworkFunctionDescriptor pnfd = networkServiceDescriptor.getPnfd().iterator().next();
    restNetworkService.deletePhysicalNetworkFunctionDescriptor(
        networkServiceDescriptor.getId(), pnfd.getId(), "");
    log.info("" + networkServiceDescriptor);
  }

  // XXX HERE PhysicalNetworkFunctionDescriptor

  // XXX FROM Security
  @Test
  public void postSecurity() {
    Security security = new Security();
    when(nsdManagement.addSecurity(anyString(), any(security.getClass()), anyString()))
        .thenReturn(security);
    Security security1 =
        restNetworkService.postSecurity(security, networkServiceDescriptor.getId(), "pi");
    assertEquals(security, security1);
  }

  @Test
  public void getSecurity() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Security security = networkServiceDescriptor.getNsd_security();
    /*assertEquals(security, restNetworkService.getSecurity(
              networkServiceDescriptor.getId(), networkServiceDescriptor
    .getNsd_security().getId()));*/
  }

  @Test
  public void getSecurities() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Security security = networkServiceDescriptor.getNsd_security();
    assertEquals(security, restNetworkService.getSecurity(networkServiceDescriptor.getId(), "pi"));
  }

  @Test
  public void updateSecurity() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Security security = new Security();

    Security security_toUp = networkServiceDescriptor.getNsd_security();
    log.info("" + security_toUp);
    security_toUp = security;
    networkServiceDescriptor.setNsd_security(security_toUp);
    log.info("" + security);
    exception.expect(UnsupportedOperationException.class);
    assertEquals(
        security,
        restNetworkService.updateSecurity(
            security, networkServiceDescriptor.getId(), security_toUp.getId(), "pi"));
  }

  @Test
  public void deleteSecurity() {
    when(nsdManagement.query(anyString(), anyString())).thenReturn(networkServiceDescriptor);
    Security security = networkServiceDescriptor.getNsd_security();
    restNetworkService.deleteSecurity(networkServiceDescriptor.getId(), security.getId(), "pi");
    log.info("" + networkServiceDescriptor);
  }
  // XXX HERE Security
}
