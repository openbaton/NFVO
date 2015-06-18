package org.project.neutrino.nfvo.sdk.api.rest;

import org.project.neutrino.nfvo.sdk.api.exception.SDKException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.io.FileUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * OpenBaton api request request abstraction for all requester. Shares common data and methods.
 */
public class Request {

	protected final String url;

	/**
	 * Create a request with a given url path
	 *
	 * @param url
	 * 				the url path used for the api requests
	 */
	public Request(final String url) {
		this.url = url;
	}

    /**
     * Executes a http post with to a given url, while serializing the file content as json
     * and returning the response
     *
     * @param url
     * 				the url path used for the api request
     * @param file
     * 				the file content to be serialized as json
     * @param message
     * 	            the prefix to be returned before the response content
     * @return a string containing the message as prefix and the response content
     */
	public String post(final String url, final File file, final String message) throws SDKException {
        try {
            // deserialize the json as string from the file
            String fileString = FileUtils.readFileToString(file);
            JsonNode fileJSONNode = new JsonNode(fileString);

            // call the api here
            HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(fileJSONNode)
                    .asJson();
            JsonNode jsonResponseBody = jsonResponse.getBody();
            // return the response of the request
            return message + " " + jsonResponseBody;

        } catch (IOException | UnirestException e) {
            // catch request exceptions here
            throw new SDKException("Could not http-post or open the file properly");
        }
	}

    /**
     * Executes a http delete with to a given url
     *
     * @param url
     * 				the url path used for the api request
     * @param message
     * 	            the prefix to be returned before the response content
     * @return a string containing the message as prefix
     */
    public String delete(final String url, final String message) throws SDKException {
        try {
            // call the api here
            HttpResponse<JsonNode> jsonResponse = Unirest.delete(url)
                    .asJson();
//            check response status
            if (jsonResponse.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new SDKException("Received wrong API HTTPStatus");
            }
            // return the response of the request
            return message;

        } catch (UnirestException | SDKException e) {
            // catch request exceptions here
            throw new SDKException("Could not http-delete or the api response was wrong");
        }
    }

    /**
     * Executes a http get with to a given url
     *
     * @param url
     * 				the url path used for the api request
     * @param message
     * 	            the prefix to be returned before the response content
     * @return a string containing the message as prefix and the response content
     */
    public String get(final String url, final String message) throws SDKException {
        try {
            // call the api here
            HttpResponse<JsonNode> jsonResponse = Unirest.get(url)
                    .asJson();
            JsonNode jsonResponseBody = jsonResponse.getBody();
            // return the response of the request
            return message + " " + jsonResponseBody;

        } catch (UnirestException e) {
            // catch request exceptions here
            throw new SDKException("Could not http-get properly");
        }
    }

    /**
     * Executes a http put with to a given url, while serializing the file content as json
     * and returning the response
     *
     * @param url
     * 				the url path used for the api request
     * @param file
     * 				the file content to be serialized as json
     * @param message
     * 	            the prefix to be returned before the response content
     * @return a string containing the message as prefix and the response content
     */
    public String put(final String url, final File file, final String message) throws SDKException {
        try {
            // deserialize the json as string from the file
            String fileString = FileUtils.readFileToString(file);
            JsonNode fileJSONNode = new JsonNode(fileString);

            // call the api here
            HttpResponse<JsonNode> jsonResponse = Unirest.put(url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(fileJSONNode)
                    .asJson();

//          check response status
            if (jsonResponse.getStatus() != HttpURLConnection.HTTP_ACCEPTED) {
                throw new SDKException("Received wrong API HTTPStatus");
            }

            JsonNode jsonResponseBody = jsonResponse.getBody();

            // return the response of the request
            return message + " " + jsonResponseBody;

        } catch (IOException | UnirestException | SDKException e) {
            // catch request exceptions here
            throw new SDKException("Could not http-put or the api response was wrong or open the file properly");
        }
    }

}
