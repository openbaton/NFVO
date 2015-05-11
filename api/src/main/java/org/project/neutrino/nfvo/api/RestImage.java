package org.project.neutrino.nfvo.api;

import java.util.List;

import javax.validation.Valid;

import org.project.neutrino.nfvo.catalogue.nfvo.NFVImage;
import org.project.neutrino.nfvo.core.interfaces.NFVImageManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dbo
 *
 */
@RestController
@RequestMapping("/images")
public class RestImage {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NFVImageManagement imageManagement;

	/**
	 * Adds a new VNF software Image to the image repository
	 * 
	 * @param image: Image to add
	 * @return image: The image filled with values from the core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	NFVImage create(@RequestBody @Valid NFVImage image) {
		return imageManagement.add(image);
	}

	/**
	 * Removes the VNF software Image from the Image repository
	 * 
	 * @param id: The Image's id to be deleted
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable("id") String id) {
		imageManagement.delete(id);
	}

	/**
	 * Returns the list of the VNF software images available
	 * 
	 * @return List<Image>: The list of VNF software images available
	 */
	@RequestMapping(method = RequestMethod.GET)
	List<NFVImage> findAll() {
		return imageManagement.query();
	}

	/**
	 * This operation returns the VNF software image selected by id
	 * 
	 * @param id: The id of the VNF software image
	 * @return image: The VNF software image selected
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	NFVImage findById(@PathVariable("id") String id) {
		NFVImage image = imageManagement.query(id);

		return image;
	}

	/**
	 * This operation updates the Network Service Descriptor (NSD)
	 * 
	 * @param networkServiceDescriptor
	 *            : the Network Service Descriptor to be updated
	 * @param id
	 *            : the id of Network Service Descriptor
	 * @return networkServiceDescriptor: the Network Service Descriptor updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	NFVImage update(@RequestBody @Valid NFVImage image, @PathVariable("id") String id) {
		return imageManagement.update(image, id);
	}
}
