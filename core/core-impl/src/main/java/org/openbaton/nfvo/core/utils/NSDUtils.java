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

package org.openbaton.nfvo.core.utils;

import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.CyclicDependenciesException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lto on 13/05/15.
 */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.start")
public class NSDUtils {

  @Autowired private VimRepository vimRepository;

  @Value("${nfvo.integrity.nsd.checks:in-all-vims}")
  private String inAllVims;

  public String getOrdered() {
    return ordered;
  }

  public void setOrdered(String ordered) {
    this.ordered = ordered;
  }

  private String ordered;

  @Autowired private VNFDRepository vnfdRepository;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  public void checkEndpoint(
      NetworkServiceDescriptor networkServiceDescriptor, Iterable<VnfmManagerEndpoint> endpoints)
      throws NotFoundException {
    boolean found = false;
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {
      for (VnfmManagerEndpoint endpoint : endpoints) {
        log.debug(endpoint.getType() + " == " + virtualNetworkFunctionDescriptor.getEndpoint());
        if (endpoint.getType().equals(virtualNetworkFunctionDescriptor.getEndpoint())
            && endpoint.isActive()
            && endpoint.isEnabled()) {
          found = true;
          break;
        }
      }
      if (!found)
        throw new NotFoundException(
            "VNFManager with endpoint: "
                + virtualNetworkFunctionDescriptor.getEndpoint()
                + " is not registered or not enable or not active.");
    }
    if (!found) throw new NotFoundException("No VNFManagers were found");
  }

  /**
   * Fetching vnfd already existing in thr DB based on the id
   *
   * @param networkServiceDescriptor
   * @throws NotFoundException
   */
  public void fetchExistingVnfd(NetworkServiceDescriptor networkServiceDescriptor)
      throws NotFoundException {
    Set<VirtualNetworkFunctionDescriptor> vnfd_add = new HashSet<>();
    Set<VirtualNetworkFunctionDescriptor> vnfd_remove = new HashSet<>();
    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      if (vnfd.getId() != null) {
        log.debug("VNFD to fetch is: " + vnfd.getId());
        VirtualNetworkFunctionDescriptor vnfd_new = vnfdRepository.findFirstById(vnfd.getId());
        log.trace("VNFD fetched: " + vnfd_new);
        if (!log.isTraceEnabled()) log.debug("Fetched VNFD: " + vnfd_new.getName());
        if (vnfd_new == null) {
          throw new NotFoundException(
              "Not found VNFD with id: "
                  + vnfd.getId()
                  + ". Please do not specify an id if you want to create one VirtualNetworkFunctionDescriptor. Or pick one existing");
        }
        vnfd_add.add(vnfd_new);
        vnfd_remove.add(vnfd);
      }
    }
    networkServiceDescriptor.getVnfd().removeAll(vnfd_remove);
    networkServiceDescriptor.getVnfd().addAll(vnfd_add);
  }

  public void fetchVimInstances(NetworkServiceDescriptor networkServiceDescriptor, String projectId)
      throws NotFoundException {
    /**
     * Fetching VimInstances
     */
    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      fetchVimInstances(vnfd, projectId);
    }
  }

  public void fetchVimInstances(VirtualNetworkFunctionDescriptor vnfd, String projectId)
      throws NotFoundException {
    Iterable<VimInstance> vimInstances = vimRepository.findByProjectId(projectId);
    if (!vimInstances.iterator().hasNext()) {
      throw new NotFoundException("No VimInstances in the Database");
    }
    for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {
      if (vdu.getVimInstanceName() != null) {
        for (String name : vdu.getVimInstanceName()) {
          log.debug("vim instance name=" + name);
          boolean fetched = false;
          for (VimInstance vimInstance : vimInstances) {
            if ((vimInstance.getName() != null
                && vimInstance
                    .getName()
                    .equals(
                        name)) /*|| (vimInstance.getId() != null && vimInstance.getId().equals(name_id))*/) {
              log.info("Found vimInstance: " + vimInstance.getName());
              fetched = true;
              break;
            }
          }
          if (!fetched) {
            throw new NotFoundException(
                "No VimInstance with name equals to " + name + " in the catalogue");
          }
        }
      } else {
        log.info(
            "No vimInstances are defined in the vdu "
                + vdu.getName()
                + ". Remember to define during the NSR onboarding.");
      }
    }
  }

  public void fetchDependencies(NetworkServiceDescriptor networkServiceDescriptor)
      throws NotFoundException, BadFormatException, CyclicDependenciesException {
    /**
     * Fetching dependencies
     */
    DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

    //Add a vertex to the graph for each vnfd
    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      g.addVertex(vnfd.getName());
    }

    // transform the requires attribute to VNFDependencies and add them to the networkServiceDescriptor
    createDependenciesFromRequires(networkServiceDescriptor);

    mergeMultipleDependency(networkServiceDescriptor);

    for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {
      log.trace("" + vnfDependency);
      VirtualNetworkFunctionDescriptor source = vnfDependency.getSource();
      VirtualNetworkFunctionDescriptor target = vnfDependency.getTarget();

      if (source == null
          || target == null
          || source.getName() == null
          || target.getName() == null) {
        throw new BadFormatException(
            "Source name and Target name must be defined in the request json file");
      }

      VirtualNetworkFunctionDescriptor vnfSource =
          getVnfdFromNSD(source.getName(), networkServiceDescriptor);
      if (vnfSource == null)
        throw new NotFoundException(
            "VNFD source name"
                + source.getName()
                + " was not found in the NetworkServiceDescriptor");
      else vnfDependency.setSource(vnfSource);

      VirtualNetworkFunctionDescriptor vnfTarget =
          getVnfdFromNSD(target.getName(), networkServiceDescriptor);
      if (vnfTarget == null)
        throw new NotFoundException(
            "VNFD target name"
                + source.getName()
                + " was not found in the NetworkServiceDescriptor");
      else vnfDependency.setTarget(vnfTarget);

      // Add an edge to the graph
      g.addEdge(source.getName(), target.getName());
    }

    // Get simple cycles
    DirectedSimpleCycles<String, DefaultEdge> dsc = new SzwarcfiterLauerSimpleCycles(g);
    List<List<String>> cycles = dsc.findSimpleCycles();
    // Set cyclicDependency param to the vnfd
    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      for (List<String> cycle : cycles)
        if (cycle.contains(vnfd.getName())) {
          vnfd.setCyclicDependency(true);
          if (ordered != null && Boolean.parseBoolean(ordered.trim()))
            throw new CyclicDependenciesException(
                "There is a cyclic exception and ordered start is selected. This cannot work.");
          break;
        }
    }
  }

  /**
   * If the requires field in the VNFD is used, this method will transform the values from requires
   * to VNFDependencies.
   *
   * @param networkServiceDescriptor
   * @throws NotFoundException
   */
  private void createDependenciesFromRequires(NetworkServiceDescriptor networkServiceDescriptor)
      throws NotFoundException {
    for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
      if (vnfd.getRequires() == null) continue;

      for (String vnfdName : vnfd.getRequires().keySet()) {
        VNFDependency dependency = new VNFDependency();
        for (VirtualNetworkFunctionDescriptor vnfd2 : networkServiceDescriptor.getVnfd()) {
          if (vnfd2.getName().equals(vnfdName)) dependency.setSource(vnfd2);
        }
        if (dependency.getSource() == null)
          throw new NotFoundException(
              "VNFD source name "
                  + vnfdName
                  + " from the requires field in the VNFD "
                  + vnfd.getName()
                  + " was not found in the NSD.");

        dependency.setTarget(vnfd);

        if (vnfd.getRequires().get(vnfdName).getParameters() == null
            || vnfd.getRequires().get(vnfdName).getParameters().isEmpty()) continue;

        dependency.setParameters(vnfd.getRequires().get(vnfdName).getParameters());
        networkServiceDescriptor.getVnf_dependency().add(dependency);
      }
    }
  }

  private VirtualNetworkFunctionDescriptor getVnfdFromNSD(
      String name, NetworkServiceDescriptor networkServiceDescriptor) {
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {
      if (virtualNetworkFunctionDescriptor.getName().equals(name))
        return virtualNetworkFunctionDescriptor;
    }

    return null;
  }

  /**
   * MergeMultipleDependency
   * <p/>
   * Merge two VNFDependency (A and B), where source and target are equals, in only one (C). C
   * contains the parameters of A and B. *
   */
  private void mergeMultipleDependency(NetworkServiceDescriptor networkServiceDescriptor) {

    Set<VNFDependency> newDependencies = new HashSet<>();

    for (VNFDependency oldDependency : networkServiceDescriptor.getVnf_dependency()) {
      boolean contained = false;
      for (VNFDependency newDependency : newDependencies) {
        if (newDependency.getTarget().getName().equals(oldDependency.getTarget().getName())
            && newDependency.getSource().getName().equals(oldDependency.getSource().getName())) {
          log.debug("Old is: " + oldDependency);
          if (oldDependency.getParameters() != null)
            newDependency.getParameters().addAll(oldDependency.getParameters());
          contained = true;
        }
      }
      if (!contained) {
        VNFDependency newDependency = new VNFDependency();
        newDependency.setSource(oldDependency.getSource());
        newDependency.setTarget(oldDependency.getTarget());
        newDependency.setParameters(new HashSet<String>());
        log.debug("Old is: " + oldDependency);
        if (oldDependency.getParameters() != null)
          newDependency.getParameters().addAll(oldDependency.getParameters());
        newDependencies.add(newDependency);
      }
    }

    log.debug("New Dependencies are: ");
    for (VNFDependency dependency : newDependencies) log.debug("" + dependency);
    networkServiceDescriptor.setVnf_dependency(newDependencies);
  }

  public void checkIntegrity(NetworkServiceDescriptor networkServiceDescriptor)
      throws NetworkServiceIntegrityException {
    /**
     * check names
     */
    Set<String> names = new HashSet<>();
    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {
      names.add(virtualNetworkFunctionDescriptor.getName());
    }

    if (networkServiceDescriptor.getVnfd().size() > names.size()) {
      throw new NetworkServiceIntegrityException(
          "All VirtualNetworkFunctionDescriptors in the same NetworkServiceDescriptor must have different names");
    }

    /**
     * check flavours and images
     */
    Set<String> flavors = new HashSet<>();
    Set<String> imageNames = new HashSet<>();
    Set<String> imageIds = new HashSet<>();
    Set<String> internalVirtualLink = new HashSet<>();
    Set<String> virtualLinkDescriptors = new HashSet<>();

    if (networkServiceDescriptor.getVld() != null)
      for (VirtualLinkDescriptor virtualLinkDescriptor1 : networkServiceDescriptor.getVld()) {
        virtualLinkDescriptors.add(virtualLinkDescriptor1.getName());
      }

    for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor :
        networkServiceDescriptor.getVnfd()) {

      names.clear();
      internalVirtualLink.clear();

      if (virtualNetworkFunctionDescriptor.getDeployment_flavour() != null)
        for (DeploymentFlavour deploymentFlavour :
            virtualNetworkFunctionDescriptor.getDeployment_flavour()) {
          names.add(deploymentFlavour.getFlavour_key());
        }
      else
        throw new NetworkServiceIntegrityException(
            "Flavour must be set in VNFD: "
                + virtualNetworkFunctionDescriptor.getName()
                + ". Come on... check the PoP page and pick at least one DeploymentFlavor");

      if (virtualNetworkFunctionDescriptor.getVirtual_link() != null)
        for (InternalVirtualLink internalVirtualLink1 :
            virtualNetworkFunctionDescriptor.getVirtual_link()) {
          internalVirtualLink.add(internalVirtualLink1.getName());
        }
      else virtualNetworkFunctionDescriptor.setVirtual_link(new HashSet<InternalVirtualLink>());
      if (virtualNetworkFunctionDescriptor.getVdu() != null)
        for (VirtualDeploymentUnit virtualDeploymentUnit :
            virtualNetworkFunctionDescriptor.getVdu()) {
          if (inAllVims.equals("in-all-vims")) {
            if (virtualDeploymentUnit.getVimInstanceName() != null
                && !virtualDeploymentUnit.getVimInstanceName().isEmpty()) {
              for (String vimName : virtualDeploymentUnit.getVimInstanceName()) {
                VimInstance vimInstance = null;

                for (VimInstance vi :
                    vimRepository.findByProjectId(virtualDeploymentUnit.getProjectId())) {
                  if (vimName.equals(vi.getName())) vimInstance = vi;
                }

                if (virtualDeploymentUnit.getScale_in_out() < 1)
                  throw new NetworkServiceIntegrityException(
                      "Regarding the VirtualNetworkFunctionDescriptor "
                          + virtualNetworkFunctionDescriptor.getName()
                          + ": in one of the VirtualDeploymentUnit, the scale_in_out parameter ("
                          + virtualDeploymentUnit.getScale_in_out()
                          + ") must be at least 1");
                if (virtualDeploymentUnit.getScale_in_out()
                    < virtualDeploymentUnit.getVnfc().size()) {
                  throw new NetworkServiceIntegrityException(
                      "Regarding the VirtualNetworkFunctionDescriptor "
                          + virtualNetworkFunctionDescriptor.getName()
                          + ": in one of the VirtualDeploymentUnit, the scale_in_out parameter ("
                          + virtualDeploymentUnit.getScale_in_out()
                          + ") must not be less than the number of starting VNFComponent: "
                          + virtualDeploymentUnit.getVnfc().size());
                }

                for (DeploymentFlavour deploymentFlavour : vimInstance.getFlavours()) {
                  flavors.add(deploymentFlavour.getFlavour_key());
                }

                for (NFVImage image : vimInstance.getImages()) {
                  imageNames.add(image.getName());
                  imageIds.add(image.getExtId());
                }

                //All "names" must be contained in the "flavors"
                if (!flavors.containsAll(names)) {
                  throw new NetworkServiceIntegrityException(
                      "Regarding the VirtualNetworkFunctionDescriptor "
                          + virtualNetworkFunctionDescriptor.getName()
                          + ": in one of the VirtualDeploymentUnit, not all DeploymentFlavour"
                          + names
                          + " are contained into the flavors of the vimInstance chosen. Please choose one from: "
                          + flavors);
                }
                for (String image : virtualDeploymentUnit.getVm_image()) {
                  if (!imageNames.contains(image) && !imageIds.contains(image))
                    throw new NetworkServiceIntegrityException(
                        "Regarding the VirtualNetworkFunctionDescriptor "
                            + virtualNetworkFunctionDescriptor.getName()
                            + ": in one of the VirtualDeploymentUnit, image"
                            + image
                            + " is not contained into the images of the vimInstance chosen. Please choose one from: "
                            + imageNames
                            + " or from "
                            + imageIds);
                }
                flavors.clear();

                for (VNFComponent vnfComponent : virtualDeploymentUnit.getVnfc()) {
                  for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()) {
                    if (!internalVirtualLink.contains(
                        connectionPoint.getVirtual_link_reference())) {
                      throw new NetworkServiceIntegrityException(
                          "Regarding the VirtualNetworkFunctionDescriptor "
                              + virtualNetworkFunctionDescriptor.getName()
                              + ": in one of the VirtualDeploymentUnit, the virtualLinkReference "
                              + connectionPoint.getVirtual_link_reference()
                              + " of a VNFComponent is not contained in the InternalVirtualLink "
                              + internalVirtualLink);
                    }
                  }
                }
              }
            } else {
              log.warn(
                  "Impossible to complete Integrity check because of missing VimInstances definition");
            }
          } else {
            log.error("" + inAllVims + " not yet implemented!");
            throw new UnsupportedOperationException("" + inAllVims + " not yet implemented!");
          }
        }
      else virtualNetworkFunctionDescriptor.setVdu(new HashSet<VirtualDeploymentUnit>());
      if (!virtualLinkDescriptors.containsAll(internalVirtualLink)) {
        throw new NetworkServiceIntegrityException(
            "Regarding the VirtualNetworkFunctionDescriptor "
                + virtualNetworkFunctionDescriptor.getName()
                + ": the InternalVirtualLinks "
                + internalVirtualLink
                + " are not contained in the VirtualLinkDescriptors "
                + virtualLinkDescriptors);
      }
    }
  }
}
