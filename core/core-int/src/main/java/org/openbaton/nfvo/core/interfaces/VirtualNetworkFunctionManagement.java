package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

/**
 * Created by mpa on 01.10.15.
 */
public interface VirtualNetworkFunctionManagement {
  VirtualNetworkFunctionDescriptor add(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String projectId);

  void delete(String id, String projectId);

  Iterable<VirtualNetworkFunctionDescriptor> query();

  VirtualNetworkFunctionDescriptor query(String id, String projectId);

  VirtualNetworkFunctionDescriptor update(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      String id,
      String projectId);

  Iterable<VirtualNetworkFunctionDescriptor> queryByProjectId(String projectId);
}
