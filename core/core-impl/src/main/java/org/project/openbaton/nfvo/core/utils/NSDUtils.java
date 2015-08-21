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

package org.project.openbaton.nfvo.core.utils;

import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.repositories_interfaces.GenericRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Qualifier("vimRepository")
    private GenericRepository<VimInstance> vimRepository;

    @Autowired
    @Qualifier("VNFDRepository")
    private GenericRepository<VirtualNetworkFunctionDescriptor> vnfdRepository;


    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void fetchVimInstances(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException {

        /**
         * Fetching VNFD
         */
        List<VirtualNetworkFunctionDescriptor> vnfdToAdd = new ArrayList<>();
        int size = networkServiceDescriptor.getVnfd().size();
        for (int i=0; i< size; i++) {
            VirtualNetworkFunctionDescriptor vnfd = (VirtualNetworkFunctionDescriptor) networkServiceDescriptor.getVnfd().toArray()[i];
            log.debug("The VNFD to fetch is: " + vnfd.getName());
            for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : vnfdRepository.findAll()) {
                log.debug("Checking: " + virtualNetworkFunctionDescriptor.getName());
                if (vnfd.getName().equals(virtualNetworkFunctionDescriptor.getName())) {
                    networkServiceDescriptor.getVnfd().remove(vnfd);
                    vnfdToAdd.add(virtualNetworkFunctionDescriptor);
                    log.debug("Found VNFD: " + vnfd.getName() + " of type: " + vnfd.getType());
                    break;
                }
            }
        }


        for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : vnfdToAdd){
            networkServiceDescriptor.getVnfd().add(virtualNetworkFunctionDescriptor);
        }
        /**
         * Fetching VimInstances
         */
        for (VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()) {
            fetchVimInstances(vnfd);
        }
    }

    public void fetchVimInstances(VirtualNetworkFunctionDescriptor vnfd) throws NotFoundException {
        List<VimInstance> vimInstances = vimRepository.findAll();
        if (vimInstances.size() == 0){
            throw new NotFoundException("No VimInstances in the Database");
        }
        for (VirtualDeploymentUnit vdu : vnfd.getVdu()) {

            String name_id = vdu.getVimInstance().getName() != null ? vdu.getVimInstance().getName() : vdu.getVimInstance().getId();
            boolean fetched = false;
            for(VimInstance vimInstance : vimInstances){
                if ((vimInstance.getName() != null && vimInstance.getName().equals(name_id)) || (vimInstance.getId() != null && vimInstance.getId().equals(name_id))){
                    vdu.setVimInstance(vimInstance);
                    log.debug("Found vimInstance: "+ vimInstance);
                    fetched=true;
                    break;
                }
            }
            if (!fetched){
                throw new NotFoundException("No VimInstance with name or id equals to " + name_id);
            }
        }
    }

    public void fetchDependencies(NetworkServiceDescriptor networkServiceDescriptor) throws NotFoundException, BadFormatException {
        /**
         * Fetching dependencies
         */

        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        //Add a vertex to the graph for each vnfd
        for(VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
            g.addVertex(vnfd.getName());
        }

        mergeMultipleDependency(networkServiceDescriptor);

        for (VNFDependency vnfDependency:networkServiceDescriptor.getVnf_dependency()){
            log.trace(""+vnfDependency);
            VirtualNetworkFunctionDescriptor source = vnfDependency.getSource();
            VirtualNetworkFunctionDescriptor target = vnfDependency.getTarget();

            if(source == null || target == null || source.getName() == null || target.getName() == null){
                throw new BadFormatException("Source name and Target name must be defined in the request json file");
            }
            boolean sourceFound = false;
            boolean targetFound = false;

            for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()){
                if (virtualNetworkFunctionDescriptor.getName().equals(source.getName())){
                    vnfDependency.setSource(virtualNetworkFunctionDescriptor);
                    sourceFound = true;
                    log.trace("Found " + virtualNetworkFunctionDescriptor);
                }
                else if (virtualNetworkFunctionDescriptor.getName().equals(target.getName())){
                    vnfDependency.setTarget(virtualNetworkFunctionDescriptor);
                    targetFound = true;
                    log.trace("Found " + virtualNetworkFunctionDescriptor);
                }
            }

            if (!(sourceFound || targetFound)) {
                String name = sourceFound ? target.getName() : source.getName();
                throw new NotFoundException(name + " was not found in the NetworkServiceDescriptor");
            }
            // Add an edge to the graph
            g.addEdge(source.getName(),target.getName());
        }

        // Get simple cycles
        DirectedSimpleCycles<String,DefaultEdge> dsc = new SzwarcfiterLauerSimpleCycles(g);
        List<List<String>> cycles = dsc.findSimpleCycles();
        // Set cyclicDependency param to the vnfd
        for(VirtualNetworkFunctionDescriptor vnfd : networkServiceDescriptor.getVnfd()){
            for(List<String> cycle : cycles)
                if(cycle.contains(vnfd.getName())) {
                    vnfd.setCyclicDependency(true);break;
                }
        }
    }
    /**
     * MergeMultipleDependency
     *
     * Merge two VNFDependency (A and B), where source and target are equals, in only one (C).
     * C contains the parameters of A and B.     *
     */
    private void mergeMultipleDependency(NetworkServiceDescriptor networkServiceDescriptor) {

        Set<VNFDependency> newDependencies = new HashSet<>();

        for (VNFDependency oldDependency : networkServiceDescriptor.getVnf_dependency()){
            boolean contained = false;
            for (VNFDependency newDependency: newDependencies){
                if (newDependency.getTarget().getId().equals(oldDependency.getTarget().getId()) && newDependency.getSource().getId().equals(oldDependency.getSource().getId())){
                    newDependency.getParameters().addAll(oldDependency.getParameters());
                    contained = true;
                }
            }
            if (!contained){
                VNFDependency newDependency = new VNFDependency();
                newDependency.setSource(oldDependency.getSource());
                newDependency.setTarget(oldDependency.getTarget());
                newDependency.setParameters(new HashSet<String>());
                if (oldDependency.getParameters() != null)
                    newDependency.getParameters().addAll(oldDependency.getParameters());
                newDependencies.add(newDependency);
            }
        }

        log.debug("New dependencies are: " + newDependencies);
        networkServiceDescriptor.setVnf_dependency(newDependencies);
    }

}
