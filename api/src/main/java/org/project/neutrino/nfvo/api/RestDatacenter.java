package org.project.neutrino.nfvo.api;

import org.project.neutrino.nfvo.catalogue.nfvo.Datacenter;
import org.project.neutrino.nfvo.core.interfaces.DatacenterManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author dbo
 *
 */
@RestController
@RequestMapping("/datacenters")
public class RestDatacenter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DatacenterManagement datacenterManagement;

	/**
	 * Adds a new VNF software Image to the datacenter repository
	 * 
	 * @param datacenter
	 *            : Image to add
	 * @return datacenter: The datacenter filled with values from the core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public Datacenter create(@RequestBody @Valid Datacenter datacenter) {
		return datacenterManagement.add(datacenter);
	}

	/**
	 * Removes the VNF software Image from the Image repository
	 * 
	 * @param id
	 *            : The Image's id to be deleted
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") String id) {
		datacenterManagement.delete(id);
	}

	/**
	 * Returns the list of the VNF software images available
	 * 
	 * @return List<Image>: The list of VNF software images available
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<Datacenter> findAll() {
		return datacenterManagement.query();
	}

	/**
	 * This operation returns the VNF software image selected by id
	 * 
	 * @param id
	 *            : The id of the VNF software image
	 * @return image: The VNF software image selected
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public Datacenter findById(@PathVariable("id") String id) {
		Datacenter datacenter = datacenterManagement.query(id);

		return datacenter;
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 * 
	 * @param new_datacenter
	 *            : the new datacenter to be updated to
	 * @param id
	 *            : the id of the old datacenter
	 * @return datacenter: the Datacenter updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Datacenter update(@RequestBody @Valid Datacenter new_datacenter,
			@PathVariable("id") String id) {
		return datacenterManagement.update(new_datacenter, id);
	}
}
