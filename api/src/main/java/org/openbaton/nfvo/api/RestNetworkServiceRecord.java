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

package org.openbaton.nfvo.api;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.exceptions.*;
import org.openbaton.nfvo.api.exceptions.StateException;
import org.openbaton.nfvo.core.interfaces.NetworkServiceRecordManagement;
import org.openbaton.vim.drivers.exceptions.VimDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/ns-records")
public class RestNetworkServiceRecord {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NetworkServiceRecordManagement networkServiceRecordManagement;

    /**
     * This operation allows submitting and validating a Network Service
     * Descriptor (NSD), including any related VNFFGD and VLD.
     *
     * @param networkServiceDescriptor : the Network Service Descriptor to be created
     * @return NetworkServiceRecord: the Network Service Descriptor filled with
     * id and values from core
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkServiceRecord create(
            @RequestBody @Valid NetworkServiceDescriptor networkServiceDescriptor) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
        return networkServiceRecordManagement.onboard(networkServiceDescriptor);

    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NetworkServiceRecord create(@PathVariable("id") String id) throws InterruptedException, ExecutionException, VimException, NotFoundException, BadFormatException, VimDriverException, QuotaExceededException {
        return networkServiceRecordManagement.onboard(id);
    }


    /**
     * This operation is used to remove a disabled Network Service Descriptor
     *
     * @param id : the id of Network Service Descriptor
     */
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) throws VimException, InterruptedException, ExecutionException, NotFoundException {
        try {
            networkServiceRecordManagement.delete(id);
        } catch (WrongStatusException e) {
            e.printStackTrace();
            throw new StateException(id);
        }
    }

    /**
     * This operation returns the list of Network Service Descriptor (NSD)
     *
     * @return List<NetworkServiceRecord>: the list of Network Service
     * Descriptor stored
     */

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<NetworkServiceRecord> findAll() {
        return networkServiceRecordManagement.query();
    }

    /**
     * This operation returns the Network Service Descriptor (NSD) selected by
     * id
     *
     * @param id : the id of Network Service Descriptor
     * @return NetworkServiceRecord: the Network Service Descriptor selected
     */

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public NetworkServiceRecord findById(@PathVariable("id") String id) {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        return nsr;
    }

    /**
     * This operation updates the Network Service Descriptor (NSD)
     *
     * @param networkServiceRecord : the Network Service Descriptor to be updated
     * @param id                   : the id of Network Service Descriptor
     * @return NetworkServiceRecord: the Network Service Descriptor updated
     */

    @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public NetworkServiceRecord update(
            @RequestBody @Valid NetworkServiceRecord networkServiceRecord,
            @PathVariable("id") String id) {
        return networkServiceRecordManagement.update(networkServiceRecord, id);
    }

    /**
     * Returns the list of VirtualNetworkFunctionDescriptor into a NSD with id
     *
     * @param id of NSD
     * @return Set<VirtualNetworkFunctionDescriptor>: List of
     * VirtualNetworkFunctionDescriptor into NSD
     */
    @RequestMapping(value = "{id}/vnfrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Set<VirtualNetworkFunctionRecord> getVirtualNetworkFunctionRecords(
            @PathVariable("id") String id) {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        log.debug("*****" + nsr.getVnfr().toString());
        return nsr.getVnfr();
    }

    /**
     * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
     *
     * @param idNsd of NSR
     * @param idVnf of VirtualNetworkFunctionRecord
     * @return VirtualNetworkFunctionRecord selected by idVnf
     * @throws NotFoundException
     */
    @RequestMapping(value = "{idNsd}/vnfrecords/{idVnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
            @PathVariable("idNsd") String idNsd, @PathVariable("idVnf") String idVnf) throws NotFoundException {

        return networkServiceRecordManagement.getVirtualNetworkFunctionRecord(idNsd, idVnf);
    }

    /**
     * Removes the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
     *
     * @param idNsr
     * @param idVnf
     * @throws NotFoundException
     */
    @RequestMapping(value = "{idNsr}/vnfrecords/{idVnf}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVNFRecord(
            @PathVariable("idNsr") String idNsr, @PathVariable("idVnf") String idVnf) throws NotFoundException {
        networkServiceRecordManagement.deleteVNFRecord(idNsr, idVnf);

    }

    @RequestMapping(value = "{id}/vnfrecords/{idVnf}/vdunits/{idVdu}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addVNFCInstance(@RequestBody @Valid VNFComponent component, @PathVariable("id") String id, @PathVariable("idVnf") String idVnf, @PathVariable("idVdu") String idVdu) throws NotFoundException, BadFormatException, WrongStatusException {
        log.trace("Received: " + component);
        networkServiceRecordManagement.addVNFCInstance(id, idVnf, idVdu, component);
    }

    @RequestMapping(value = "{id}/vnfrecords/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VirtualNetworkFunctionRecord postVNFR(
            @RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
            @PathVariable("id") String id) {
        NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
        nsd.getVnfr().add(vnfRecord);
        networkServiceRecordManagement.update(nsd, id);
        return vnfRecord;
    }

    @RequestMapping(value = "{idNsr}/vnfrecords/{idVnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VirtualNetworkFunctionRecord updateVNF(
            @RequestBody @Valid VirtualNetworkFunctionRecord vnfRecord,
            @PathVariable("idNsr") String idNsr, @PathVariable("idVnf") String idVnf) {
        NetworkServiceRecord nsd = networkServiceRecordManagement.query(idNsr);
        nsd.getVnfr().add(vnfRecord);
        networkServiceRecordManagement.update(nsd, idNsr);
        return vnfRecord;
    }

    /**
     * Returns the list of VNFDependency into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<VNFDependency>: The List of VNFDependency into NSD
     */

    @RequestMapping(value = "{id}/vnfdependencies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Set<VNFRecordDependency> getVNFDependencies(@PathVariable("id") String id) {
        NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
        return nsd.getVnf_dependency();
    }

    @RequestMapping(value = "{id}/vnfdependencies/{id_vnfr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VNFRecordDependency getVNFDependency(@PathVariable("id") String id,
                                                @PathVariable("id_vnfr") String id_vnfr) throws NotFoundException {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        return findVNFD(nsr.getVnf_dependency(), id_vnfr);
    }

    @RequestMapping(value = "{idNsr}/vnfdependencies/{idVnfd}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVNFDependency(@PathVariable("idNsr") String idNsr,
                                    @PathVariable("idVnfd") String idVnfd) throws NotFoundException {
        networkServiceRecordManagement.deleteVNFDependency(idNsr, idVnfd);


    }

    @RequestMapping(value = "{id}/vnfdependencies/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VNFRecordDependency postVNFDependency(
            @RequestBody @Valid VNFRecordDependency vnfDependency,
            @PathVariable("id") String id) {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        nsr.getVnf_dependency().add(vnfDependency);
        networkServiceRecordManagement.update(nsr, id);
        return vnfDependency;
    }

    @RequestMapping(value = "{id}/vnfdependencies/{id_vnfd}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VNFRecordDependency updateVNFD(
            @RequestBody @Valid VNFRecordDependency vnfDependency,
            @PathVariable("id") String id,
            @PathVariable("id_vnfd") String id_vnfd) {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        nsr.getVnf_dependency().add(vnfDependency);
        networkServiceRecordManagement.update(nsr, id);
        return vnfDependency;
    }

    /**
     * Returns the list of PhysicalNetworkFunctionRecord into a NSD with id
     *
     * @param id : The id of NSD
     * @return List<PhysicalNetworkFunctionRecord>: The List of
     * PhysicalNetworkFunctionRecord into NSD
     */
    @RequestMapping(value = "{id}/pnfrecords", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Set<PhysicalNetworkFunctionRecord> getPhysicalNetworkFunctionRecord(
            @PathVariable("id") String id) {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        return nsr.getPnfr();
    }

    /**
     * Returns the PhysicalNetworkFunctionRecord
     *
     * @param id     : The NSD id
     * @param id_pnf : The PhysicalNetworkFunctionRecord id
     * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
     * selected
     */

    @RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PhysicalNetworkFunctionRecord getPhysicalNetworkFunctionRecord(
            @PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) throws NotFoundException {
        NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
        return findPNFD(nsd.getPnfr(), id_pnf);
    }

    /**
     * Deletes the PhysicalNetworkFunctionRecord with the id_pnf
     *
     * @param id     : The NSD id
     * @param id_pnf : The PhysicalNetworkFunctionRecord id
     */
    @RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhysicalNetworkFunctionRecord(
            @PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) throws NotFoundException {
        NetworkServiceRecord nsr = networkServiceRecordManagement.query(id);
        PhysicalNetworkFunctionRecord pDescriptor = findPNFD(nsr.getPnfr(),
                id_pnf);
        nsr.getVnfr().remove(pDescriptor);
    }

    /**
     * Stores the PhysicalNetworkFunctionRecord
     *
     * @param pDescriptor : The PhysicalNetworkFunctionRecord to be stored
     * @param id          : The NSD id
     * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
     * stored
     */
    @RequestMapping(value = "{id}/pnfrecords/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PhysicalNetworkFunctionRecord postPhysicalNetworkFunctionRecord(
            @RequestBody @Valid PhysicalNetworkFunctionRecord pDescriptor,
            @PathVariable("id") String id) {
        NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
        nsd.getPnfr().add(pDescriptor);
        networkServiceRecordManagement.update(nsd, id);
        return pDescriptor;
    }

    /**
     * Edits the PhysicalNetworkFunctionRecord
     *
     * @param pRecord : The PhysicalNetworkFunctionRecord to be edited
     * @param id      : The NSD id
     * @return PhysicalNetworkFunctionRecord: The PhysicalNetworkFunctionRecord
     * edited
     */
    @RequestMapping(value = "{id}/pnfrecords/{id_pnf}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PhysicalNetworkFunctionRecord updatePNFD(
            @RequestBody @Valid PhysicalNetworkFunctionRecord pRecord,
            @PathVariable("id") String id, @PathVariable("id_pnf") String id_pnf) {
        NetworkServiceRecord nsd = networkServiceRecordManagement.query(id);
        nsd.getPnfr().add(pRecord);
        networkServiceRecordManagement.update(nsd, id);
        return pRecord;
    }

    // TODO The Rest of the classes

    private PhysicalNetworkFunctionRecord findPNFD(Collection<PhysicalNetworkFunctionRecord> listPNFR, String id_pnf) throws NotFoundException {
        for (PhysicalNetworkFunctionRecord pRecord : listPNFR) {
            if (pRecord.getId().equals(id_pnf)) {
                return pRecord;
            }
        }
        throw new NotFoundException("PNFD with id " + id_pnf + " was not found");
    }

    private VNFRecordDependency findVNFD(Collection<VNFRecordDependency> vnf_dependency, String id_vnfd) throws NotFoundException {
        for (VNFRecordDependency vnfDependency : vnf_dependency) {
            if (vnfDependency.getId().equals(id_vnfd)) {
                return vnfDependency;
            }
        }
        throw new NotFoundException("VNFD with id " + id_vnfd + " was not found");
    }

    private VirtualNetworkFunctionRecord findVNF(
            Collection<VirtualNetworkFunctionRecord> listVNF, String id_vnf) throws NotFoundException {
        for (VirtualNetworkFunctionRecord vnfRecord : listVNF) {
            if (vnfRecord.getId().equals(id_vnf)) {
                return vnfRecord;
            }
        }
        throw new NotFoundException("VNFR with id " + id_vnf + " was not found");
    }


}
