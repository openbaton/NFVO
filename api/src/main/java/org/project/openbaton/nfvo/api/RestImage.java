package org.project.openbaton.nfvo.api;

import org.project.openbaton.common.catalogue.nfvo.NFVImage;
import org.project.openbaton.nfvo.core.interfaces.NFVImageManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1//images")
public class RestImage {

	//	TODO add log prints
//	private Logger log = LoggerFactory.getLogger(this.getClass());


	@Autowired
	@Qualifier("NFVImageManagement")
	private NFVImageManagement imageManagement;

	/**
	 * Adds a new VNF software Image to the image repository
	 * 
	 * @param image
	 *            : Image to add
	 * @return image: The image filled with values from the core
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public NFVImage create(@RequestBody @Valid NFVImage image) {
		return imageManagement.add(image);
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
		imageManagement.delete(id);
	}

	/**
	 * Returns the list of the VNF software images available
	 * 
	 * @return List<Image>: The list of VNF software images available
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<NFVImage> findAll() {
		return imageManagement.query();
	}

	/**
	 * Returns the VNF software image selected by id
	 * 
	 * @param id
	 *            : The id of the VNF software image
	 * @return image: The VNF software image selected
	 */
	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public NFVImage findById(@PathVariable("id") String id) {
		NFVImage image = imageManagement.query(id);

		return image;
	}

	/**
	 * Updates the VNF software image
	 * 
	 * @param image
	 *            : the VNF software image to be updated
	 * @param id
	 *            : the id of VNF software image
	 * @return networkServiceDescriptor: the VNF software image updated
	 */

	@RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public NFVImage update(@RequestBody @Valid NFVImage image,
			@PathVariable("id") String id) {
		return imageManagement.update(image, id);
	}
}
