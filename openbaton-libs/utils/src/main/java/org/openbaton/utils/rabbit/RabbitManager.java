/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.utils.rabbit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    public static List<String> getQueues(String brokerIp, String username, String password, int managementPort) throws IOException {
        List<String> result = new ArrayList<>();
        String encoding = Base64.encodeBase64String((username + ":" + password).getBytes());
        HttpGet httpGet = new HttpGet("http://" + brokerIp + ":" + managementPort + "/api/queues/");
        httpGet.setHeader("Authorization", "Basic " + encoding);

        log.trace("executing request " + httpGet.getRequestLine());
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
        log.trace("found queues: "+result.toString());
        return result;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(RabbitManager.getQueues("localhost", "admin","openbaton", 5672));
    }

}
