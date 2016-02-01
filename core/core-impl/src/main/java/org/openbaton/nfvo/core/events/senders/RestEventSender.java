/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.core.events.senders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openbaton.catalogue.nfvo.ApplicationEventNFVO;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.nfvo.core.interfaces.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by lto on 01/07/15.
 */
@Service
@Scope
public class RestEventSender implements EventSender {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    @Async
    public Future<Void> send(EventEndpoint endpoint, ApplicationEventNFVO event) throws IOException {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            Gson mapper = new GsonBuilder().create();
            String json = "{\"action\":\"" + event.getAction() + "\",\"payload\":" + mapper.toJson(event.getPayload()) + "}";

            log.trace("body is: " + json);

            log.trace("Invoking POST on URL: " + endpoint.getEndpoint());
            HttpPost request = new HttpPost(endpoint.getEndpoint());
            request.addHeader("content-type", "application/json");
            request.addHeader("accept", "application/json");
            StringEntity params = new StringEntity(json);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
        } catch (Exception e) {
            log.warn("Impossible to reach the endpoint with name: "+endpoint.getName()+ " via rest POST at url:"+endpoint.getEndpoint());
        }
        return new AsyncResult<>(null);
    }
}
