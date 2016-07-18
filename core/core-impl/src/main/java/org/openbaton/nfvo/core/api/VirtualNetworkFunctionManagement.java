package org.openbaton.nfvo.core.api;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VnfPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

/**
 * Created by mpa on 01.10.15.
 */
@Service
@Scope
@ConfigurationProperties
public class VirtualNetworkFunctionManagement
    implements org.openbaton.nfvo.core.interfaces.VirtualNetworkFunctionManagement {

  @Autowired private VNFDRepository vnfdRepository;
  @Autowired private VnfPackageRepository vnfPackageRepository;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${vnfd.vnfp.cascade.delete:false}")
  private boolean cascadeDelete;

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public void setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
  }

  @Override
  public VirtualNetworkFunctionDescriptor add(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String projectId) {
    // TODO check integrity of VNFD
    virtualNetworkFunctionDescriptor.setProjectId(projectId);
    return vnfdRepository.save(virtualNetworkFunctionDescriptor);
  }

  @Override
  public void delete(String id, String projectId) {
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfdRepository.findFirstById(id);
    if (!virtualNetworkFunctionDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    log.info("Removing VNFD: " + virtualNetworkFunctionDescriptor.getName());
    vnfdRepository.delete(virtualNetworkFunctionDescriptor);
    if (cascadeDelete) {
      log.info(
          "Removing vnfPackage with id: "
              + virtualNetworkFunctionDescriptor.getVnfPackageLocation());
      vnfPackageRepository.delete(virtualNetworkFunctionDescriptor.getVnfPackageLocation());
    }
  }

  @Override
  public Iterable<VirtualNetworkFunctionDescriptor> query() {
    return vnfdRepository.findAll();
  }

  @Override
  public VirtualNetworkFunctionDescriptor query(String id, String projectId) {
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor =
        vnfdRepository.findFirstById(id);
    if (!virtualNetworkFunctionDescriptor.getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    return virtualNetworkFunctionDescriptor;
  }

  @Override
  public VirtualNetworkFunctionDescriptor update(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      String id,
      String projectId) {
    //TODO Update inner fields
    if (!vnfdRepository.findFirstById(id).getProjectId().equals(projectId))
      throw new UnauthorizedUserException(
          "VNFD not under the project chosen, are you trying to hack us? Just kidding, it's a bug :)");
    return vnfdRepository.save(virtualNetworkFunctionDescriptor);
  }

  @Override
  public Iterable<VirtualNetworkFunctionDescriptor> queryByProjectId(String projectId) {
    return vnfdRepository.findByProjectId(projectId);
  }
}
