package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import org.apache.commons.io.FileUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

/**
 * OpenBaton image-related api requester.
 */
public class ImageRequest {

    private final String path;

    /**
     * Create a image requester with a given url path
     *
     * @param path
     * 				the url path used for the api requests
     */
    public ImageRequest(final String path) {
        this.path = path;
    }

    /**
     * Adds a new VNF software Image to the image repository
     *
     * @param image
     *            : Image to add
     * @return string: The image filled with values from the api
     */
	public String create (final File image) throws SDKException {

        String result = "";
        try {
            // deserialize the json as string from the file
            result = FileUtils.readFileToString(image);
//            System.out.println(path);

            // call the api here
            String url = "http://localhost:8080/images";
            HttpResponse<JsonNode> httpResponse = Unirest.post(url)
                    .header("accept", "application/json")
                    .body(result)
                    .asJson();
            JSONObject responseJSONBody = httpResponse.getBody().getObject();
//          call toString() of the response body
            System.out.println(responseJSONBody);

            // return the response of the request
            result = "IMAGE CREATED" + " " + responseJSONBody;

        } catch (IOException | UnirestException e) {
            // close the unirest threadpool
//            Unirest.shutdown();

            // catch request exceptions here
            result = "IMAGE COULD NOT BE CREATED";
            throw new SDKException("Something went wrong.");
        }
		return result;
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
