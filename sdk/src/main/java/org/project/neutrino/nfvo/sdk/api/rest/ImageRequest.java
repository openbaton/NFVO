package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 * OpenBaton image-related api requester.
 */
public class ImageRequest {
	
private static Logger log = LoggerFactory.getLogger("SDKApi");

    /**
     * Adds a new VNF software Image to the image repository
     *
     * @param image
     *            : Image to add
     * @return string: The image filled with values from the api
     */
	public String create (final File image) throws SDKException {
        log.debug("Received CREATE IMAGE Request");

        String result;
        try {
            // deserialize the json as string from the file
            result = readFile(image);
            log.debug(result);

            // call the sdk request here
//            Requestor.post(result)

            // return the response of the request

            result = "IMAGE CREATED" + " " + result;

        } catch (IOException e) {
            // maybe use a custom SDK exception here
            result = "IMAGE COULD NOT BE CREATED" + e.getMessage();
            throw new SDKException("File Not Found");
        }
        // catch request exceptions here
        // maybe use a custom SDK exception here

		return result;
	}

    private String readFile(final File file) throws IOException {
        return FileUtils.readFileToString(file);
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
