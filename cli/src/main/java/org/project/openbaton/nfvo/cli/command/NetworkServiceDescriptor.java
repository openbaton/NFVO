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

package org.project.openbaton.nfvo.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.catalogue.mano.common.Security;
import org.project.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.PhysicalNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.clients.exceptions.VimDriverException;
import org.project.openbaton.nfvo.api.RestNetworkServiceDescriptor;
import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.project.openbaton.nfvo.common.exceptions.QuotaExceededException;
import org.project.openbaton.nfvo.common.exceptions.VimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * OpenBaton network-service-descriptor-related commands implementation using the spring-shell library.
 */
@Component
public class NetworkServiceDescriptor implements CommandMarker {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Gson mapper = new Gson();

    @Autowired
    private RestNetworkServiceDescriptor networkServiceDescriptorAgent;

    private org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor getObject(File networkServiceDescriptor) throws FileNotFoundException {
        return mapper.<org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor>fromJson(new InputStreamReader(new FileInputStream(networkServiceDescriptor)), org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor.class);
    }

    private <T> T getObject(File object, Class<T> clazz) throws FileNotFoundException {
        return mapper.fromJson(new InputStreamReader(new FileInputStream(object)), clazz);
    }

    /**
     * This operation allows submitting and validating a Network Service
     * Descriptor (NSD), including any related VNFFGD and VLD.
     *
     * @param networkServiceDescriptor : the Network Service Descriptor to be created
     * @return networkServiceDescriptor: the Network Service Descriptor filled
     * with id and values from core
     */
    @CliCommand(value = "networkServiceDescriptor create", help = "Submit and validate a new Network Service Descriptor (NSD)")
    public String create(
            @CliOption(key = {"networkServiceDescriptorFile"}, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
        org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor nsd = null;
        try {
            nsd = networkServiceDescriptorAgent.create(getObject(networkServiceDescriptor));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "NSD NOT CREATED";
        } catch (BadFormatException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return "NSD CREATED" +nsd;
    }

    /**
     * This operation is used to remove a disabled Network Service Descriptor
     *
     * @param id : the id of Network Service Descriptor
     */
    @CliCommand(value = "networkServiceDescriptor delete", help = "Remove a disabled Network Service Descriptor (NSD)")
    public String delete(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        networkServiceDescriptorAgent.delete(id);
        return "NETWORKSERVICEDESCRIPTOR DELETED";
    }

    /**
     * This operation returns the Network Service Descriptor (NSD) selected by
     * id
     *
     * @param id : the id of Network Service Descriptor
     * @return NetworkServiceDescriptor: the Network Service Descriptor selected
     */
    @CliCommand(value = "networkServiceDescriptor find", help = "Return the Network Service Descriptor (NSD) selected by id, or all if no id is given")
    public String findById(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        if (id != null) {
            return "FOUND NSD: " + networkServiceDescriptorAgent.findById(id);
        } else {
            return "FOUND NSDs: " + networkServiceDescriptorAgent.findAll();
        }
    }

    /**
     * This operation updates the Network Service Descriptor (NSD)
     *
     * @param networkServiceDescriptor : the Network Service Descriptor to be updated
     * @param id                       : the id of Network Service Descriptor
     * @return networkServiceDescriptor: the Network Service Descriptor updated
     */
    @CliCommand(value = "networkServiceDescriptor update", help = "Update he Network Service Descriptor (NSD)")
    public String update(
            @CliOption(key = {"networkServiceDescriptorFile"}, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            return "UPDATED NSD: " + networkServiceDescriptorAgent.update(getObject(networkServiceDescriptor), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "NSD NOT UPDATED";
        }
    }

    /**
     * Return the list of VirtualNetworkFunctionDescriptor into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<VirtualNetworkFunctionDescriptor>: The List of
     * VirtualNetworkFunctionDescriptor into NSD
     */
    @CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptors", help = "Return the list of VirtualNetworkFunctionDescriptor into a NSD with id")
    public String getVirtualNetworkFunctionDescriptors(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        return "FOUND VNFDESCRIPTORs: " + networkServiceDescriptorAgent.getVirtualNetworkFunctionDescriptors(id);
    }

    /**
     * Return a VirtualNetworkFunctionDescriptor into a NSD with id
     *
     * @param id     : The id of NSD
     * @param id_vfn : The id of the VNF Descriptor
     * @return List<VirtualNetworkFunctionDescriptor>: The List of
     * VirtualNetworkFunctionDescriptor into NSD
     */
    @CliCommand(value = "networkServiceDescriptor getVirtualNetworkFunctionDescriptor", help = "Return the list of VirtualNetworkFunctionDescriptor into a NSD with id")
    public String getVirtualNetworkFunctionDescriptor(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_vfn"}, mandatory = true, help = "The virtual network function descriptor id") final String id_vfn) {
        return "FOUND VNFDESCRIPTOR: " + networkServiceDescriptorAgent.getVirtualNetworkFunctionDescriptor(id, id_vfn);
    }

    /**
     * Delete the VirtualNetworkFunctionDescriptor
     *
     * @param id     : The id of NSD
     * @param id_vfn : The id of the VNF Descriptor
     */
    @CliCommand(value = "networkServiceDescriptor deleteVirtualNetworkFunctionDescriptor", help = "Delete the VirtualNetworkFunctionDescriptor")
    public String deleteVirtualNetworkFunctionDescriptors(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_vfn"}, mandatory = true, help = "The virtual network function descriptor id") final String id_vfn) {
        networkServiceDescriptorAgent.deleteVirtualNetworkFunctionDescriptor(id, id_vfn);
        return "DELETED VNFDESCRIPTOR";
    }

    /**
     * Create a VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor : : the Network Service Descriptor to be updated
     * @param id                               : The id of the networkServiceDescriptor the vnfd shall be created at
     */
    @CliCommand(value = "networkServiceDescriptor postVNFD", help = "Create a VirtualNetworkFunctionDescriptor")
    public String postVNFD(
            @CliOption(key = {"virtualNetworkFunctionDescriptorFile"}, mandatory = true, help = "The virtualNetworkFunction json file") final File virtualNetworkFunctionDescriptor,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            return "CREATED VNFDESCRIPTOR: " + networkServiceDescriptorAgent.postVNFD(getObject(virtualNetworkFunctionDescriptor, VirtualNetworkFunctionDescriptor.class), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Update the VirtualNetworkFunctionDescriptor
     *
     * @param virtualNetworkFunctionDescriptor : : the Network Service Descriptor to be updated
     * @param id                               : The id of the (old) VNF Descriptor
     * @param id_vfn                           : The id of the VNF Descriptor
     * @return List<VirtualNetworkFunctionDescriptor>: The updated virtualNetworkFunctionDescriptor
     */
    @CliCommand(value = "networkServiceDescriptor updateVNF", help = "update the VirtualNetworkFunctionDescriptor")
    public String updateVNF(
            @CliOption(key = {"virtualNetworkFunctionDescriptorFile"}, mandatory = true, help = "The virtualNetworkFunction json file") final File virtualNetworkFunctionDescriptor,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_vfn"}, mandatory = true, help = "The virtual network function descriptor id") final String id_vfn) {
        try {
            return "UPDATED VNFDESCRIPTOR: " + networkServiceDescriptorAgent.updateVNF(getObject(virtualNetworkFunctionDescriptor, VirtualNetworkFunctionDescriptor.class), id, id_vfn);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Return the list of VNFDependencies into a NSD
     *
     * @param id : The id of the networkServiceDescriptor
     * @return List<VNFDependency>:  The List of VNFDependency into NSD
     */
    @CliCommand(value = "networkServiceDescriptor getVNFDependencies", help = "Return the list of VNFDependencies into a NSD")
    public String getVNFDependencies(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        return "FOUND VNFDEPENDENCIES: " + networkServiceDescriptorAgent.getVNFDependencies(id);
    }

    /**
     * Return a VNFDependency into a NSD
     *
     * @param id      : The id of the VNF Descriptor
     * @param id_vnfd : The VNFDependencies id
     * @return VNFDependency:  The List of VNFDependency into NSD
     */
    @CliCommand(value = "networkServiceDescriptor getVNFDependency", help = "Return a VNFDependency into a NSD")
    public String getVNFDependency(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_vnfd"}, mandatory = true, help = "The VNFDependencies id") final String id_vnfd) {
        return "FOUND VNFDEPENDENCY: " + networkServiceDescriptorAgent.getVNFDependency(id, id_vnfd);
    }

    /**
     * Delets a VNFDependency
     *
     * @param id      : The id of the networkServiceDescriptor
     * @param id_vnfd : The id of the VNFDependency
     */
    @CliCommand(value = "networkServiceDescriptor deleteVNFDependency", help = "Delete a VNFDependency")
    public String deleteVNFDependency(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_vnfd"}, mandatory = true, help = "The VNFDependencies id") final String id_vnfd) {
        networkServiceDescriptorAgent.deleteVNFDependency(id, id_vnfd);
        return "Deleted VNFDependency";
    }

    /**
     * Create a VNFDependency
     *
     * @param vnfDependency : The VNFDependency to be updated
     * @param id            : The id of the networkServiceDescriptor
     */
    @CliCommand(value = "networkServiceDescriptor postVNFDependency", help = "Creates a VNFDependency")
    public String postVNFDependency(
            @CliOption(key = {"vnfDependencyFile"}, mandatory = true, help = "The VNFDependency json file") final File vnfDependency,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            return "CREATED VNFDEPENDENCY: " + networkServiceDescriptorAgent.postVNFDependency(getObject(vnfDependency, VNFDependency.class), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Update the VNFDependency
     *
     * @param vnfDependency : The VNFDependency to be updated
     * @param id            : The id of the networkServiceDescriptor
     * @param id_vnfd       : The id of the VNFDependency
     * @return The updated VNFDependency
     */
    @CliCommand(value = "networkServiceDescriptor updateVNFD", help = "Update the VNFDependency")
    public String updateVNFD(
            @CliOption(key = {"vnfDependencyFile"}, mandatory = true, help = "Update the VNFDependency") final File vnfDependency,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_vnfd"}, mandatory = true, help = "The VNFDependencies id") final String id_vnfd) {
        try {
            return "UPDATED VNFDEPENDENCY: " + networkServiceDescriptorAgent.updateVNFD(getObject(vnfDependency, VNFDependency.class), id, id_vnfd);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Return the list of PhysicalNetworkFunctionDescriptor into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<PhysicalNetworkFunctionDescriptor>: The List of
     * PhysicalNetworkFunctionDescriptor into NSD
     */
    @CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptors", help = "Return the list of PhysicalNetworkFunctionDescriptor into a NSD with id")
    public String getPhysicalNetworkFunctionDescriptors(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        return "FOUND PNFDESCRIPTORs: " + networkServiceDescriptorAgent.getPhysicalNetworkFunctionDescriptors(id);
    }

    /**
     * Returns the PhysicalNetworkFunctionDescriptor into a NSD with id
     *
     * @param id     : The NSD id
     * @param id_pnf : The PhysicalNetworkFunctionDescriptor id
     * @return PhysicalNetworkFunctionDescriptor: The
     * PhysicalNetworkFunctionDescriptor selected
     */
    @CliCommand(value = "networkServiceDescriptor getPhysicalNetworkFunctionDescriptor", help = "Return the PhysicalNetworkFunctionDescriptor into a NSD with id")
    public String getPhysicalNetworkFunctionDescriptor(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_pnf"}, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        return "FOUND PNFDESCRIPTOR: " + networkServiceDescriptorAgent.getPhysicalNetworkFunctionDescriptor(id, id_pnf);
    }

    /**
     * Delete the PhysicalNetworkFunctionDescriptor with the id_pnf
     *
     * @param id     : The NSD id
     * @param id_pnf : The PhysicalNetworkFunctionDescriptor id
     */
    @CliCommand(value = "networkServiceDescriptor deletePhysicalNetworkFunctionDescriptor", help = "Delete the PhysicalNetworkFunctionDescriptor with the id_pnf")
    public String deletePhysicalNetworkFunctionDescriptor(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_pnf"}, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        networkServiceDescriptorAgent.deletePhysicalNetworkFunctionDescriptor(id, id_pnf);
        return "Deleted PhysicalNetworkFunctionDescriptor";
    }

    /**
     * Store the PhysicalNetworkFunctionDescriptor
     *
     * @param pnf : The PhysicalNetworkFunctionDescriptor to be stored
     * @param id  : The NSD id
     * @return PhysicalNetworkFunctionDescriptor: The PhysicalNetworkFunctionDescriptor stored
     */
    @CliCommand(value = "networkServiceDescriptor postPhysicalNetworkFunctionDescriptor", help = "Store the PhysicalNetworkFunctionDescriptor")
    public String postPhysicalNetworkFunctionDescriptor(
            @CliOption(key = {"pnfFile"}, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor json file") final File pnf, @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            return "CREATED PNFDESCRIPTOR: " + networkServiceDescriptorAgent.postPhysicalNetworkFunctionDescriptor(getObject(pnf, PhysicalNetworkFunctionDescriptor.class), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Update the PhysicalNetworkFunctionDescriptor
     *
     * @param pnf    : The PhysicalNetworkFunctionDescriptor to be edited
     * @param id     : The NSD id
     * @param id_pnf : The PhysicalNetworkFunctionDescriptor id
     * @return PhysicalNetworkFunctionDescriptor: The
     * PhysicalNetworkFunctionDescriptor edited
     * @
     */
    @CliCommand(value = "networkServiceDescriptor updatePNFD", help = "Update the PhysicalNetworkFunctionDescriptor")
    public String updatePNFD(
            @CliOption(key = {"pnfFile"}, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor json file") final File pnf,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_pnf"}, mandatory = true, help = "The PhysicalNetworkFunctionDescriptor id") final String id_pnf) {
        try {
            return "UPDATED PNFDESCRIPTOR: " + networkServiceDescriptorAgent.updatePNFD(getObject(pnf, PhysicalNetworkFunctionDescriptor.class), id, id_pnf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Return the Security into a NSD
     *
     * @param id : The id of NSD
     * @return Security: The Security of PhysicalNetworkFunctionDescriptor into
     * NSD
     */
    @CliCommand(value = "networkServiceDescriptor getSecurities", help = "Return all Security from a nsd")
    public String getSecurities(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        return "FOUND SECURITIES: " + networkServiceDescriptorAgent.getSecurity(id);
    }

//    /**
//     * Return the Security with the id_s
//     *
//     * @param id   : The NSD id
//     * @param id_s : The Security id
//     * @return Security: The Security selected by id_s
//     */
//    @CliCommand(value = "networkServiceDescriptor getSecurity", help = "Return the Security with the id_s")
//    public String getSecurity(
//            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
//            @CliOption(key = {"id_s"}, mandatory = true, help = "The security id") final String id_s) {
//        return "FOUND SECURITY: " + networkServiceDescriptorAgent.getSecurity(id, id_s);
//    }

    /**
     * Delete the Security with the id_s
     *
     * @param id   : The NSD id
     * @param id_s : The Security id
     * @
     */
    @CliCommand(value = "networkServiceDescriptor deleteSecurity", help = "Delete the Security with the id_s")
    public String deleteSecurity(
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_s"}, mandatory = true, help = "The security id") final String id_s) {
        networkServiceDescriptorAgent.deleteSecurity(id, id_s);
        return "DELETED SECURITY";
    }

    /**
     * Store the Security into NSD
     *
     * @param security : The Security to be stored
     * @param id       : The id of NSD
     * @return Security: The Security stored
     */
    @CliCommand(value = "networkServiceDescriptor postSecurity", help = " Store the Security into NSD")
    public String postSecurity(
            @CliOption(key = {"securityFile"}, mandatory = true, help = "The Security json file") final File security,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id) {
        try {
            return "CREATED SECURITY: " + networkServiceDescriptorAgent.postSecurity(getObject(security, Security.class), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Update the Security into NSD
     *
     * @param security : The Security to be stored
     * @param id       : The id of NSD
     * @param id_s     : The security id
     * @return Security: The Security stored
     */
    @CliCommand(value = "networkServiceDescriptor updateSecurity", help = "Update the Security into NSD")
    public String updateSecurity(
            @CliOption(key = {"securityFile"}, mandatory = true, help = "The Security json file") final File security,
            @CliOption(key = {"id"}, mandatory = true, help = "The networkServiceDescriptor id") final String id,
            @CliOption(key = {"id_s"}, mandatory = true, help = "The security id") final String id_s) {
        try {
            return "UPDATED SECURITY: " + networkServiceDescriptorAgent.updateSecurity(getObject(security, Security.class), id, id_s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

    /**
     * Create a record into NSD
     *
     * @param networkServiceDescriptor : the networkServiceDescriptor JSON File
     */
    @CliCommand(value = "networkServiceDescriptor createRecord", help = "Create a record into NSD")
    public String createRecord(
            @CliOption(key = {"networkServiceDescriptorFile"}, mandatory = true, help = "The networkServiceDescriptor json file") final File networkServiceDescriptor) {
        try {
            return "CREATED RECORD: " + networkServiceDescriptorAgent.createRecord(getObject(networkServiceDescriptor, org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor.class));
        } catch (BadFormatException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (ExecutionException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (VimException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (NotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (VimDriverException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        } catch (QuotaExceededException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }
}
