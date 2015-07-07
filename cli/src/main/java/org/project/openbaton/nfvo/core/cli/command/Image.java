package org.project.openbaton.nfvo.core.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.nfvo.catalogue.nfvo.NFVImage;
import org.project.openbaton.sdk.NFVORequestor;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.project.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * OpenBaton image-related commands implementation using the spring-shell library.
 */
@Component
public class Image implements CommandMarker {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private NFVORequestor requestor = new NFVORequestor("1");
	private AbstractRestAgent<org.project.openbaton.nfvo.catalogue.nfvo.NFVImage> imageAgent;
	private Gson mapper = new Gson();

	@PostConstruct
	private void init(){
		imageAgent = requestor.getImageAgent();
	}
	private NFVImage getObject(File file) throws FileNotFoundException {
		return mapper.<org.project.openbaton.nfvo.catalogue.nfvo.NFVImage>fromJson(new InputStreamReader(new FileInputStream(file)), org.project.openbaton.nfvo.catalogue.nfvo.NFVImage.class);
	}
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
			return "IMAGE CREATED: " + imageAgent.create(getObject(image));
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT CREATED";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "IMAGE NOT CREATED: ( " + e.getMessage() + " )";
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
			imageAgent.delete(id);
			return "IMAGE DELETED";
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT DELETED: ( " + e.getMessage() + " )";
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
			if (id != null) {
				return "FOUND IMAGE: " + imageAgent.findById(id);
			} else {
				return "FOUND IMAGES: " + imageAgent.findAll();
			}
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT FOUND: ( " + e.getMessage() + " )";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "IMAGE NOT FOUND: ( " + e.getMessage() + " )";
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
			return "IMAGE UPDATED: " + imageAgent.update(getObject(image), id);
		} catch (SDKException e) {
			log.debug(e.getMessage());
			return "IMAGE NOT UPDATED: ( " + e.getMessage() + " )";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "IMAGE NOT UPDATED: ( " + e.getMessage() + " )";
		}
	}

}
