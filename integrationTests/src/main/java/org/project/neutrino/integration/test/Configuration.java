package org.project.neutrino.integration.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.shell.support.util.FileUtils;

@SuppressWarnings("rawtypes")
public class Configuration implements Callable{

	@Override
	public Boolean call() throws Exception {

		Boolean output = false;
        output =  ConfigurationCreate();
       
		
		return output;
	}
	
	private boolean ConfigurationCreate() throws URISyntaxException
	{
		String body = FileUtils.read(new File("./src/main/resources/configuration.json"));
		 
		 System.out.println("SEND REQUEST");
		 try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			    URI uri = new URI(
					    "http://127.0.0.1:8080", 
					    "/api/v1/configurations",
					    null);
	            HttpPost request = new HttpPost(uri);
	            StringEntity params = new StringEntity(body);
	            request.addHeader("content-type", "application/json");
	            request.setEntity(params);
	            HttpResponse result = httpClient.execute(request);
	            System.out.println("RESPONSE RECIVED");
	     
	            
	            System.out.println(result.toString());

	            
	             } catch (IOException ex) {
	        }
		
		return true;
		
	}
	
	

}
