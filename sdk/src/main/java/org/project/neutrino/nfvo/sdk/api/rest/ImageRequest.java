package org.project.neutrino.nfvo.sdk.api.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 * OpenBaton image-related api requester.
 */
public class ImageRequest {
	
private static Logger log = LoggerFactory.getLogger("SDKApi");
	
	@Autowired
	private ConfigurableApplicationContext context;

    /**
     * Adds a new VNF software Image to the image repository
     *
     * @param image
     *            : Image to add
     * @return string: The image filled with values from the api
     */
	public String create (final File image) {
        // create the json
        log.debug("Received CREATE Request");
        // call the sdk image create function here

		return "IMAGE CREATED";
	}

	/**
     * Removes the VNF software Image from the Image repository
     *
     * @param id
     *            : The Image's id to be deleted
     */
	public String delete(final String id) {
            return "IMAGE CREATED";
	}

    /**
     * Returns the list of the VNF software images available
     *
     * @return List<Image>: The list of VNF software images available
     */
	public String findAll() {
		return "IMAGE RESULTS";
	}

	/**
     * Returns the VNF software image selected by id
     *
     * @param id
     *            : The id of the VNF software image
     * @return image: The VNF software image selected
     */
	public String findById(final File image, String id) {
            return "IMAGE RESULT";
	}

    /**
     * Updates the VNF software image
     *
     * @param image
     *            : Image to add
     * @param id
     *            : the id of VNF software image
     * @return image: the VNF software image updated
     */
	public String update(final File image, final String id) {
            return "IMAGE UPDATED";
	}

}
