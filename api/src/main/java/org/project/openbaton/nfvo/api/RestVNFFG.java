package org.project.openbaton.nfvo.api;

import org.project.openbaton.catalogue.mano.descriptor.VNFForwardingGraphDescriptor;
import org.project.openbaton.nfvo.core.interfaces.VNFFGManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1//vnf-forwarding-graphs")
public class RestVNFFG {

	//	TODO add log prints
//	private Logger log = LoggerFactory.getLogger(this.getClass());


	@Autowired
	@Qualifier("VNFFGManagement")
	private VNFFGManagement vnffgManagement;

	/**
	 * Adds a new VNF software VNFFG to the vnfForwardingGraphDescriptor repository
	 * 
	 * @param vnfForwardingGraphDescriptor
	 *            : VNFFG to add
	 * @return vnfForwardingGraphDescriptor: The vnfForwardingGraphDescriptor filled with values from the core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public VNFForwardingGraphDescriptor create(@RequestBody @Valid VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor) {
		return vnffgManagement.add(vnfForwardingGraphDescriptor);
	}

	/**
	 * Removes the VNF software VNFFG from the VNFFG repository
	 * 
	 * @param id
	 *            : The VNFFG's id to be deleted
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		vnffgManagement.delete(id);
	}

	/**
	 * Returns the list of the VNFFGs available
	 * 
	 * @return List<VNFForwardingGraphDescriptor>: The list of VNFFGs available
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<VNFForwardingGraphDescriptor> findAll() {
		return vnffgManagement.query();
	}

	/**
	 * Returns the VNFFG selected by id
	 * 
	 * @param id
	 *            : The id of the VNFFG
	 * @return vnfForwardingGraphDescriptor: The VNFFG selected
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public VNFForwardingGraphDescriptor findById(@PathVariable("id") String id) {
		VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor = vnffgManagement.query(id);

		return vnfForwardingGraphDescriptor;
	}

	/**
	 * Updates the VNF software vnfForwardingGraphDescriptor
	 * 
	 * @param vnfForwardingGraphDescriptor
	 *            : the VNF software vnfForwardingGraphDescriptor to be updated
	 * @param id
	 *            : the id of VNF software vnfForwardingGraphDescriptor
	 * @return networkServiceDescriptor: the VNF software vnfForwardingGraphDescriptor updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public VNFForwardingGraphDescriptor update(@RequestBody @Valid VNFForwardingGraphDescriptor vnfForwardingGraphDescriptor,
			@PathVariable("id") String id) {
		return vnffgManagement.update(vnfForwardingGraphDescriptor, id);
	}
}
