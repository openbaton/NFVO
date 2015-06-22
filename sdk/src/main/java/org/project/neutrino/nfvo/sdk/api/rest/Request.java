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
     * @return a string containing the response content
     */
    public String requestPost(final String url, final File file) throws SDKException {
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
//            check response status
            checkStatus(jsonResponse, HttpURLConnection.HTTP_CREATED);
            // return the response of the request
            return jsonResponse.getBody().toString();

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
     */
    public void requestDelete(final String url) throws SDKException {
        try {
            // call the api here
            HttpResponse<JsonNode> jsonResponse = Unirest.delete(url)
                    .asJson();
//            check response status
            checkStatus(jsonResponse, HttpURLConnection.HTTP_NO_CONTENT);

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
     * @return a string containing he response content
     */
    public String requestGet(final String url) throws SDKException {
        return requestGetWithStatus(url, null);
    }

    /**
     * Executes a http get with to a given url, and possible executed an http (accept) status check of the response if an httpStatus is delivered.
     * If httpStatus is null, no check will be executed.
     *
     * @param url
     * 				the url path used for the api request
     * @param httpStatus
     * 	            the http status to be checked.
     * @return a string containing the response content
     */
    private String requestGetWithStatus(final String url, final Integer httpStatus) throws SDKException {
        try {
            // call the api here
            HttpResponse<JsonNode> jsonResponse = Unirest.get(url)
                    .asJson();

            // check response status
            if (httpStatus != null) {
                checkStatus(jsonResponse, httpStatus);
            }
            // return the response of the request
            return jsonResponse.getBody().toString();

        } catch (UnirestException e) {
            // catch request exceptions here
            throw new SDKException("Could not http-get properly");
        }
    }

    /**
     * Executes a http get with to a given url, in contrast to the normal get it uses an http (accept) status check of the response
     *
     * @param url
     * 				the url path used for the api request
     * @return a string containing the response content
     */
    public String requestGetWithStatusAccepted(final String url) throws SDKException {
        return requestGetWithStatus(url, new Integer(HttpURLConnection.HTTP_ACCEPTED));
    }



    /**
     * Executes a http put with to a given url, while serializing the file content as json
     * and returning the response
     *
     * @param url
     * 				the url path used for the api request
     * @param file
     * 				the file content to be serialized as json
     * @return a string containing the response content
     */
    public String requestPut(final String url, final File file) throws SDKException {
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
            checkStatus(jsonResponse, HttpURLConnection.HTTP_ACCEPTED);

            // return the response of the request
            return jsonResponse.getBody().toString();

        } catch (IOException | UnirestException | SDKException e) {
            // catch request exceptions here
            throw new SDKException("Could not http-put or the api response was wrong or open the file properly");
        }
    }

    /**
     * Check wether a json repsonse has the right http status. If not, an SDKException is thrown.
     *
     * @param jsonResponse
     * 				the http json response
     * @param httpStatus
     * 				the (desired) http status of the repsonse
     */
    private void checkStatus(HttpResponse<JsonNode> jsonResponse, final int httpStatus) throws SDKException {
        if (jsonResponse.getStatus() != httpStatus) {
            throw new SDKException("Received wrong API HTTPStatus");
        }
    }

}
