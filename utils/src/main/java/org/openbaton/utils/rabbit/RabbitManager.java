package org.openbaton.utils.rabbit;

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lto on 25/11/15.
 */
@Service
@Scope
public class RabbitManager {

    private static Logger log = LoggerFactory.getLogger(RabbitManager.class);

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<String> getQueues(String brokerIp, String username, String password, int port) throws IOException {
        List<String> result = new ArrayList<>();
        String encoding = Base64.encodeBase64String((username + ":" + password).getBytes());
        HttpGet httpGet = new HttpGet("http://" + brokerIp + ":" + port + "/api/queues/");
        httpGet.setHeader("Authorization", "Basic " + encoding);

        log.debug("executing request " + httpGet.getRequestLine());
        HttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        InputStreamReader inputStreamReader = new InputStreamReader(entity.getContent());

        JsonArray array = gson.fromJson(inputStreamReader, JsonArray.class);
        if (array != null)
            for (JsonElement queueJson : array){
                String name = queueJson.getAsJsonObject().get("name").getAsString();
                result.add(name);
                log.trace("found queue: " + name);
            }
        //TODO check for errors
        log.debug("found queues: "+result.toString());
        return result;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(RabbitManager.getQueues("localhost", "admin","openbaton", 5672));
    }

}
