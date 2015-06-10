package org.project.neutrino.nfvo.core.cli.command;

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
	
	private static Logger log = LoggerFactory.getLogger("CLInterface");
	
	@Autowired
	private ConfigurableApplicationContext context;

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
		return "IMAGE CREATED";
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
		return "IMAGE CREATED";
	}

    /**
     * Returns the list of the VNF software images available
     *
     * @return List<Image>: The list of VNF software images available
     */
	@CliCommand(value = "image find all", help = "Returns the list of the VNF software images available")
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
	@CliCommand(value = "image find", help = "Returns the VNF software image selected by id")
	public String findById(
            @CliOption(key = { "id" }, mandatory = true, help = "The image id") final String id) {
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
	@CliCommand(value = "image update", help = "Updates the VNF software image")
	public String update(
            @CliOption(key = { "imageFile" }, mandatory = true, help = "The image json file") final File image,
            @CliOption(key = { "id" }, mandatory = true, help = "The image id") final String id) {
		return "IMAGE UPDATED";
	}

}
