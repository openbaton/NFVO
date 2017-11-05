/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.interceptors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.openbaton.nfvo.api.configuration.CustomHttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class LegacyInterceptor extends HandlerInterceptorAdapter {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private Gson gson;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    String requestURI = request.getRequestURI();
    if ((requestURI.equalsIgnoreCase("/api/v1/ns-descriptors/")
            || requestURI.equalsIgnoreCase("/api/v1/ns-descriptors"))
        && request.getMethod().equalsIgnoreCase("post")) {
      if (request instanceof CustomHttpServletRequestWrapper) {
        String requestBody = ((CustomHttpServletRequestWrapper) request).getBody();
        JsonObject jsonObject;
        try {
          jsonObject = gson.fromJson(requestBody, JsonObject.class);
        } catch (JsonSyntaxException e) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                             e.getMessage().split(":")[1].replace("$.", " ").replaceAll("\\[[0-9]\\]", ""));
          return false;
        }
        if (jsonObject.has("vnf_dependency")) {
          for (JsonElement dep : jsonObject.getAsJsonArray("vnf_dependency")) {
            if (dep.isJsonObject()) {
              if (dep.getAsJsonObject().has("source") &&
                  dep.getAsJsonObject().get("source").isJsonObject() &&
                  dep.getAsJsonObject().get("source").isJsonObject()) {
                if (dep.getAsJsonObject().getAsJsonObject("source").has("name") &&
                    dep.getAsJsonObject().getAsJsonObject("source").get("name").isJsonPrimitive()) {
                  String sourceName = dep.getAsJsonObject().getAsJsonObject("source").get("name").getAsString();
                  dep.getAsJsonObject().addProperty("source", sourceName);
                }
              }
              if (dep.getAsJsonObject().has("target") &&
                  dep.getAsJsonObject().get("target").isJsonObject() &&
                  dep.getAsJsonObject().getAsJsonObject("target").has("name")) {
                if (dep.getAsJsonObject().getAsJsonObject("target").has("name") &&
                    dep.getAsJsonObject().getAsJsonObject("target").get("name").isJsonPrimitive()) {
                  String targetName = dep.getAsJsonObject().getAsJsonObject("target").get("name").getAsString();
                  dep.getAsJsonObject().addProperty("target", targetName);
                }
              }
            }
          }
        }
        ((CustomHttpServletRequestWrapper) request).setBody(gson.toJson(jsonObject));
      }
    }
    return true;
  }
}
