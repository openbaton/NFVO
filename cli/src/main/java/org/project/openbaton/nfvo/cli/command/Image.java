package org.project.openbaton.nfvo.cli.command;

import com.google.gson.Gson;
import org.project.openbaton.catalogue.nfvo.NFVImage;
import org.project.openbaton.nfvo.api.RestImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * OpenBaton image-related commands implementation using the spring-shell library.
 */
@Component
@Order
public class Image implements CommandMarker {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Gson mapper = new Gson();

    @Autowired
    private RestImage imageAgent;

    private NFVImage getObject(File file) throws FileNotFoundException {
        return mapper.<NFVImage>fromJson(new InputStreamReader(new FileInputStream(file)), NFVImage.class);
    }

    /**
     * Adds a new VNF software Image to the image repository
     *
     * @param image : Image to add
     * @return string: The image filled with values from the api
     */
    @CliCommand(value = "image create", help = "Adds a new VNF software Image to the image repository")
    public String create(
            @CliOption(key = {"imageFile"}, mandatory = true, help = "The image json file") final File image) {
        // call the sdk image create function
        try {
            return "IMAGE CREATED: " + imageAgent.create(getObject(image));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "IMAGE NOT CREATED: ( " + e.getMessage() + " )";
        }
    }

    /**
     * Removes the VNF software Image from the Image repository
     *
     * @param id : The Image's id to be deleted
     */
    @CliCommand(value = "image delete", help = "Removes the VNF software Image from the Image repository")
    public String delete(
            @CliOption(key = {"id"}, mandatory = true, help = "The image id") final String id) {
        imageAgent.delete(id);
        return "IMAGE DELETED";
    }

    /**
     * Returns the list of the VNF software images available or Returns the VNF software image selected by id
     *
     * @param id : The id of the VNF software image
     * @return image: The VNF software image(s) selected
     */
    @CliCommand(value = "image find", help = "Returns the VNF software image selected by id, or all if no id is given")
    public String findById(
            @CliOption(key = {"id"}, mandatory = false, help = "The image id") final String id) {
        if (id != null) {
            return "FOUND IMAGE: " + imageAgent.findById(id);
        } else {
            return "FOUND IMAGES: " + imageAgent.findAll();
        }
    }

    /**
     * Updates the VNF software image
     *
     * @param image : Image to add
     * @param id    : the id of VNF software image
     * @return image: the VNF software image updated
     */
    @CliCommand(value = "image update", help = "Updates the VNF software image")
    public String update(
            @CliOption(key = {"imageFile"}, mandatory = true, help = "The image json file") final File image,
            @CliOption(key = {"id"}, mandatory = true, help = "The image id") final String id) {
        try {
            return "IMAGE UPDATED: " + imageAgent.update(getObject(image), id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return e.getMessage();
        }
    }

}
