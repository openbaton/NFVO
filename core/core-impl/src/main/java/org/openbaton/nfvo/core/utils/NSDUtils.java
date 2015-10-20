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
import org.openbaton.catalogue.mano.common.VNFDeploymentFlavour;
import org.openbaton.catalogue.mano.descriptor.*;
import org.openbaton.catalogue.nfvo.NFVImage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.repositories.VNFDRepository;
import org.openbaton.nfvo.repositories.VimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NSDUtils {

    @Autowired
    private VimRepository vimRepository;

    @Autowired
    private VNFDRepository vnfdRepository;


    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Fetching vnfd already existing in thr DB based on the id
     *
     * @param networkServiceDescriptor
     * @throws NotFoundException
     */
    public void fetchExistingVnfd(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException {
        Set<VirtualNetworkFunctionDescriptor> vnfd_add = new HashSet<>();
        Set<VirtualNetworkFunctionDescriptor> vnfd_remove = new HashSet<>();
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            if (vnfd.getId() != null) {
                VirtualNetworkFunctionDescriptor vnfd_new = vnfdRepository.findOne(vnfd.getId());
                log.debug("VNFD fetched: " + vnfd_new);
                if (vnfd_new == null) {
                    throw new NotFoundException("Not found VNFD with id: " + vnfd.getId() + ". Please do not specify an id if you want to create one VirtualNetworkFunctionDescriptor. Or pick one existing");
                }
                vnfd_add.add(vnfd_new);
                vnfd_remove.add(vnfd);
            }
        }
        networkServiceDescriptor.getVnfd().removeAll(vnfd_remove);
        networkServiceDescriptor.getVnfd().addAll(vnfd_add);
    }

    public void fetchVimInstances(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException {
        /**
         * Fetching VimInstances
         */
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            fetchVimInstances(vnfd);
        }
    }

    public void fetchVimInstances(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException {
        Iterable<VimInstance> vimInstances = vimRepository.findAll();
        if (!vimInstances.iterator().hasNext()) {
            throw new NotFoundException("No VimInstances in the Database");
        }
        for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {

            String name_id = vdu.getVimInstanceName();
            log.debug("vim instance name=" + name_id);
            boolean fetched = false;
            for (VimInstance vimInstance : vimInstances) {
                if ((vimInstance.getName() != null && vimInstance.getName().equals(name_id)) /*|| (vimInstance.getId() != null && vimInstance.getId().equals(name_id))*/) {
                    vdu.setVimInstance(vimInstance);
                    log.debug("Found vimInstance: " + vimInstance);
                    fetched = true;
                    break;
                }
            }
            if (!fetched) {
                throw new NotFoundException("No VimInstance with name equals to " + name_id + " in the catalogue");
            }
        }
    }

    public void fetchDependencies(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        /**
         * Fetching dependencies
         */

        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        //Add a vertex to the graph for each vnfd
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            g.addVertex(vnfd.getName());
        }

        mergeMultipleDependency(networkServiceDescriptor);


        for (VNFDependency vnfDependency : networkServiceDescriptor.getVnf_dependency()) {
            log.trace("" + vnfDependency);
            VirtualNetworkFunctionDescriptor source = vnfDependency.getSource();
            VirtualNetworkFunctionDescriptor target = vnfDependency.getTarget();

            if (source == null || target == null || source.getName() == null || target.getName() == null) {
                throw new BadFormatException("Source name and Target name must be defined in the request json file");
            }

            boolean sourceFound = false;
            boolean targetFound = false;
            for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()) {
                sourceFound = false;
                targetFound = false;
                if (virtualNetworkFunctionDescriptor.getName().equals(source.getName())) {
                    vnfDependency.setSource(virtualNetworkFunctionDescriptor);
                    sourceFound = true;
                    log.trace("Found source " + virtualNetworkFunctionDescriptor.getName());
                } else if (virtualNetworkFunctionDescriptor.getName().equals(target.getName())) {
                    vnfDependency.setTarget(virtualNetworkFunctionDescriptor);
                    targetFound = true;
                    log.trace("Found target " + virtualNetworkFunctionDescriptor.getName());
                }
            }

            if (!(sourceFound || targetFound)) {
                String name = sourceFound ? target.getName() : source.getName();
                throw new NotFoundException(name + " was not found in the NetworkServiceDescriptor");
            }
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
                    break;
                }
        }
    }

    /**
     * MergeMultipleDependency
     * <p/>
     * Merge two VNFDependency (A and B), where source and target are equals, in only one (C).
     * C contains the parameters of A and B.     *
     */
    private void mergeMultipleDependency(NetworkServiceDescriptor networkServiceDescriptor) {

        Set<VNFDependency> newDependencies = new HashSet<>();

        for (VNFDependency oldDependency : networkServiceDescriptor.getVnf_dependency()) {
            boolean contained = false;
            for (VNFDependency newDependency : newDependencies) {
                if (newDependency.getTarget().getName().equals(oldDependency.getTarget().getName()) && newDependency.getSource().getName().equals(oldDependency.getSource().getName())) {
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

        log.debug("New dependencies are: " + newDependencies);
        networkServiceDescriptor.setVnf_dependency(newDependencies);
    }

    public void checkIntegrity(NetworkServiceDescriptor networkServiceDescriptor) throws NetworkServiceIntegrityException {
        /**
         * check names
         */
        Set<String> names = new HashSet<>();
        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()) {
            names.add(virtualNetworkFunctionDescriptor.getName());
        }

        if (networkServiceDescriptor.getVnfd().size() > names.size()) {
            throw new NetworkServiceIntegrityException("All VirtualNetworkFunctionDescriptors in the same NetworkServiceDescriptor must have different names");
        }

        /**
         * check flavours and images
         */

        Set<String> flavors = new HashSet<>();
        Set<String> imageNames = new HashSet<>();
        Set<String> imageIds = new HashSet<>();
        Set<String> internalVirtualLink = new HashSet<>();
        Set<String> virtualLinkDescriptors= new HashSet<>();

        if (networkServiceDescriptor.getVld() != null)
            for (VirtualLinkDescriptor virtualLinkDescriptor1 : networkServiceDescriptor.getVld()){
                virtualLinkDescriptors.add(virtualLinkDescriptor1.getName());
            }


        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()) {

            names.clear();
            internalVirtualLink.clear();

            if (virtualNetworkFunctionDescriptor.getDeployment_flavour() != null)
                for (DeploymentFlavour deploymentFlavour : virtualNetworkFunctionDescriptor.getDeployment_flavour()) {
                    names.add(deploymentFlavour.getFlavour_key());
                }
            else
                virtualNetworkFunctionDescriptor.setDeployment_flavour(new HashSet<VNFDeploymentFlavour>());

            if (virtualNetworkFunctionDescriptor.getVirtual_link() != null)
                for (InternalVirtualLink internalVirtualLink1 : virtualNetworkFunctionDescriptor.getVirtual_link()){
                    internalVirtualLink.add(internalVirtualLink1.getName());
                }
            else
                virtualNetworkFunctionDescriptor.setVirtual_link(new HashSet<InternalVirtualLink>());
            if (virtualNetworkFunctionDescriptor.getVdu() != null)
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionDescriptor.getVdu()){

                    if (virtualDeploymentUnit.getScale_in_out() < 1)
                        throw new NetworkServiceIntegrityException("Regarding the VirtualNetworkFunctionDescriptor " + virtualNetworkFunctionDescriptor.getName() + ": in one of the VirtualDeploymentUnit, the scale_in_out parameter (" + virtualDeploymentUnit.getScale_in_out() + ") must be at least 1");
                    if (virtualDeploymentUnit.getScale_in_out() < virtualDeploymentUnit.getVnfc().size()){
                        throw new NetworkServiceIntegrityException("Regarding the VirtualNetworkFunctionDescriptor " + virtualNetworkFunctionDescriptor.getName() + ": in one of the VirtualDeploymentUnit, the scale_in_out parameter (" + virtualDeploymentUnit.getScale_in_out() + ") must not be less than the number of starting VNFComponent: " + virtualDeploymentUnit.getVnfc().size());
                    }

                    for (DeploymentFlavour deploymentFlavour : virtualDeploymentUnit.getVimInstance().getFlavours()){
                        flavors.add(deploymentFlavour.getFlavour_key());
                    }

                    for (NFVImage image : virtualDeploymentUnit.getVimInstance().getImages()){
                        imageNames.add(image.getName());
                        imageIds.add(image.getExtId());
                    }

                    //All "names" must be contained in the "flavors"
                    if (!flavors.containsAll(names)){
                        throw new NetworkServiceIntegrityException("Regarding the VirtualNetworkFunctionDescriptor " + virtualNetworkFunctionDescriptor.getName() + ": in one of the VirtualDeploymentUnit, not all DeploymentFlavour" + names + " are contained into the flavors of the vimInstance chosen. Please choose one from: " + flavors );
                    }
                    for (String image : virtualDeploymentUnit.getVm_image()){
                        if (!imageNames.contains(image) && !imageIds.contains(image))
                            throw new NetworkServiceIntegrityException("Regarding the VirtualNetworkFunctionDescriptor " + virtualNetworkFunctionDescriptor.getName() + ": in one of the VirtualDeploymentUnit, image" + image + " is not contained into the images of the vimInstance chosen. Please choose one from: " + imageNames + " or from " + imageIds);
                    }
                    flavors.clear();

                    for (VNFComponent vnfComponent : virtualDeploymentUnit.getVnfc()){
                        for (VNFDConnectionPoint connectionPoint : vnfComponent.getConnection_point()){
                            if (!internalVirtualLink.contains(connectionPoint.getVirtual_link_reference())){
                                throw new NetworkServiceIntegrityException("Regarding the VirtualNetworkFunctionDescriptor " + virtualNetworkFunctionDescriptor.getName() + ": in one of the VirtualDeploymentUnit, the virtualLinkReference " + connectionPoint.getVirtual_link_reference() + " of a VNFComponent is not contained in the InternalVirtualLink " + internalVirtualLink);
                            }
                        }
                    }
                }
            else
                virtualNetworkFunctionDescriptor.setVdu(new HashSet<VirtualDeploymentUnit>());
            if (!virtualLinkDescriptors.containsAll(internalVirtualLink)){
                throw new NetworkServiceIntegrityException("Regarding the VirtualNetworkFunctionDescriptor " + virtualNetworkFunctionDescriptor.getName() + ": the InternalVirtualLinks " + internalVirtualLink + " are not contained in the VirtualLinkDescriptors " + virtualLinkDescriptors);
            }
        }
    }
}
