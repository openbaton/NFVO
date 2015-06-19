package org.project.neutrino.nfvo.core.cli.command;

import org.project.neutrino.nfvo.sdk.api.rest.Requestor;
import org.project.neutrino.nfvo.sdk.api.rest.ImageRequest;
import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OpenBaton image-related commands implementation using the spring-shell library.
 */
@Component
public class Image implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Adds a new VNF software Image to the image repository
     *
     * @param image
     *            : Image to add
     * @return string: The image filled with values from the api
     */
	@CliCommand(value = "image create", help = "Adds a new VNF software Image to the image repository")
	public String create(
            @CliOption(key = { "imageFile" }, mandatory = true, help = "The image json file") final File image) {
		// call the sdk image create function
		try {
			ImageRequest imageRequest = Requestor.getImageRequest();
			return imageRequest.create(image);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT CREATED";
		}
	}

	/**
     * Removes the VNF software Image from the Image repository
     *
     * @param id
     *            : The Image's id to be deleted
     */
	@CliCommand(value = "image delete", help = "Removes the VNF software Image from the Image repository")
	public String delete(
            @CliOption(key = { "id" }, mandatory = true, help = "The image id") final String id) {
		try {
			ImageRequest imageRequest = Requestor.getImageRequest();
			return imageRequest.delete(id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT DELETED";
		}
	}

	/**
     * Returns the list of the VNF software images available or Returns the VNF software image selected by id
     *
     * @param id
     *            : The id of the VNF software image
     * @return image: The VNF software image(s) selected
     */
	@CliCommand(value = "image find", help = "Returns the VNF software image selected by id, or all if no id is given")
	public String findById(
            @CliOption(key = { "id" }, mandatory = false, help = "The image id") final String id) {
		try {
			ImageRequest imageRequest = Requestor.getImageRequest();
			if (id != null) {
				return imageRequest.findById(id);
			} else {
				return imageRequest.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "NO IMAGE FOUND";
		}
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
	@CliCommand(value = "image update", help = "Updates the VNF software image")
	public String update(
            @CliOption(key = { "imageFile" }, mandatory = true, help = "The image json file") final File image,
            @CliOption(key = { "id" }, mandatory = true, help = "The image id") final String id) {
		try {
			ImageRequest imageRequest = Requestor.getImageRequest();
			return imageRequest.update(image, id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT UPDATED";
		}
	}

}
