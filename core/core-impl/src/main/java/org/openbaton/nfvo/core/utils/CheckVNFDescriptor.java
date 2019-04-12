/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashSet;
import java.util.Objects;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.DescriptorWrongFormat;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by mob on 05/04/2017. */
public class CheckVNFDescriptor {

  private static final Logger log = LoggerFactory.getLogger(CheckVNFPackage.class);

  public static void checkIntegrity(String vnfdJson) throws DescriptorWrongFormat {
    VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor;
    Gson mapper = new GsonBuilder().create();
    try {
      virtualNetworkFunctionDescriptor =
          mapper.fromJson(vnfdJson, VirtualNetworkFunctionDescriptor.class);
    } catch (Exception e) {
      throw new DescriptorWrongFormat("VNFD json is not well formatted", e);
    }

    try {
      checkVNFDIntegrity(virtualNetworkFunctionDescriptor);
    } catch (NetworkServiceIntegrityException e) {
      throw new DescriptorWrongFormat(e.getMessage(), e);
    }
  }

  private static void checkVNFDIntegrity(VirtualNetworkFunctionDescriptor vnfd)
      throws NetworkServiceIntegrityException {

    if (vnfd.getName() == null || vnfd.getName().isEmpty()) {
      throw new NetworkServiceIntegrityException("Not found name of VNFD. Must be defined");
    }

    if (vnfd.getType() == null || vnfd.getType().isEmpty()) {
      throw new NetworkServiceIntegrityException("Not found type of VNFD " + vnfd.getName());
    }

    if (vnfd.getVdu() == null || vnfd.getVdu().size() == 0)
      throw new NetworkServiceIntegrityException(
          "Not found any VDU defined in VNFD \" + virtualNetworkFunctionDescriptor.getName()");

    if (vnfd.getEndpoint() == null || vnfd.getEndpoint().isEmpty()) {
      throw new NetworkServiceIntegrityException("Not found endpoint in VNFD " + vnfd.getName());
    }

    // Ensure each VDU has at least one VNFC
    for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
      if (vdu.getVnfc() == null || vdu.getVnfc().size() == 0)
        throw new NetworkServiceIntegrityException(
            "Not found any VNFC in a VDU of the VNFD " + vnfd.getName());
    }

    // If the VNFManager is fixed-host, then ensure the VNFD has only one VDU and one VNFC
    if (vnfd.getEndpoint().equals("fixed-host")) {
      if (vnfd.getVdu().size() > 1)
        throw new NetworkServiceIntegrityException(
            "The VNFD "
                + vnfd.getName()
                + " contains more then one VDU. The fixed-host VNFM supports only one");
      if (vnfd.getVdu().iterator().next().getVnfc().size() > 1)
        throw new NetworkServiceIntegrityException(
            "The VNFD "
                + vnfd.getName()
                + " contains more then one VNF component. The fixed-host VNFM supports only one");
    }

    if (vnfd.getDeployment_flavour() != null && !vnfd.getDeployment_flavour().isEmpty()) {
      for (DeploymentFlavour deploymentFlavour : vnfd.getDeployment_flavour()) {
        if (deploymentFlavour.getFlavour_key() == null
            || deploymentFlavour.getFlavour_key().isEmpty()) {
          throw new NetworkServiceIntegrityException(
              "Deployment flavor of VNFD " + vnfd.getName() + " is not well defined");
        }
      }
    } else {

      for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {

        if (vdu.getComputation_requirement() == null
            || vdu.getComputation_requirement().isEmpty()) {
          throw new NetworkServiceIntegrityException(
              "Flavour must be set in VNFD or all VDUs: "
                  + vnfd.getName()
                  + ". Pick at least one "
                  + "DeploymentFlavor");
        }
      }
    }

    int i = 1;
    for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
      if (vdu.getName() == null || vdu.getName().isEmpty()) {
        vdu.setName(vnfd.getName() + "-" + i);
        i++;
      }
      //      if (vdu.getVm_image() == null || vdu.getVm_image().isEmpty()) {
      //        throw new NetworkServiceIntegrityException(
      //            "Not found image in a VDU of VNFD " + vnfd.getName());
      //      }
      vdu.setProjectId(vnfd.getProjectId());
    }

    if (vnfd.getVirtual_link() != null) {
      for (InternalVirtualLink vl : vnfd.getVirtual_link()) {
        if (vl.getName() == null || Objects.equals(vl.getName(), ""))
          throw new NetworkServiceIntegrityException(
              "The vnfd: " + vnfd.getName() + " has a virtual link with no name specified");
      }
    }

    if (vnfd.getLifecycle_event() != null) {
      for (LifecycleEvent event : vnfd.getLifecycle_event()) {
        if (event == null) {
          throw new NetworkServiceIntegrityException("LifecycleEvent is null");
        } else if (event.getEvent() == null) {
          throw new NetworkServiceIntegrityException("Event in one LifecycleEvent does not exist");
        }
      }
    }

    vnfd.setVirtual_link(new HashSet<InternalVirtualLink>());

    for (VirtualDeploymentUnit virtualDeploymentUnit : vnfd.getVdu()) {

      if (virtualDeploymentUnit.getScale_in_out() < 1) {
        throw new NetworkServiceIntegrityException(
            "Regarding the VirtualNetworkFunctionDescriptor "
                + vnfd.getName()
                + ": in one of the VirtualDeploymentUnit, the scale_in_out"
                + " parameter ("
                + virtualDeploymentUnit.getScale_in_out()
                + ") must be at least 1");
      }

      if (virtualDeploymentUnit.getScale_in_out() < virtualDeploymentUnit.getVnfc().size()) {
        throw new NetworkServiceIntegrityException(
            "Regarding the VirtualNetworkFunctionDescriptor "
                + vnfd.getName()
                + ": in one of the VirtualDeploymentUnit, the scale_in_out"
                + " parameter ("
                + virtualDeploymentUnit.getScale_in_out()
                + ") must not be less than the number of starting "
                + "VNFComponent: "
                + virtualDeploymentUnit.getVnfc().size());
      }
    }
  }
}
