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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.ValidationMessage;

import org.openbaton.exceptions.BadRequestException;
import org.openbaton.nfvo.api.configuration.CustomHttpServletRequestWrapper;
import org.openbaton.nfvo.api.configuration.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class SchemaValidatorInterceptor extends HandlerInterceptorAdapter {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Autowired private Gson gson;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    String requestURL = request.getRequestURL().toString();
    if (request.getRequestURI().equalsIgnoreCase("/error")) return true;
    CustomHttpServletRequestWrapper wrapper = new CustomHttpServletRequestWrapper(request);
    String requestBody = wrapper.getBody();
    String classSchema = null;

    if (handler instanceof org.springframework.web.method.HandlerMethod) {
      org.springframework.web.method.HandlerMethod handlerMethod =
          (org.springframework.web.method.HandlerMethod) handler;

      Annotation[][] annotations = handlerMethod.getMethod().getParameterAnnotations();
      for (int i = 0; i < annotations.length; i++) {
        Annotation[] methodParameters = annotations[i];
        for (Annotation a : methodParameters) {
          if (a instanceof RequestBody) {
            Class<?> parameterClass = handlerMethod.getMethod().getParameterTypes()[i];
            if (!parameterClass.getName().equals("com.google.gson.JsonObject"))
              classSchema = getJsonSchemaFromClass(parameterClass);
            else log.trace("URL: " + request.getRequestURL());
          }
        }
      }
    }

    if (classSchema != null) {
      log.trace("Request Body is : " + requestBody.toString());
      log.trace("Request url is : " + requestURL);
      Set<ValidationMessage> validationMessages = null;
      try {
        validationMessages = SchemaValidator.validateSchema(classSchema, requestBody.toString());
      } catch (BadRequestException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        return false;
      }
      if (validationMessages.size() > 0) {
        StringBuffer validationResult = new StringBuffer();
        for (ValidationMessage s : validationMessages) {

          String message = s.getMessage();
          validationResult.append(message + ", ");
        }

        log.trace("Response body is : " + validationResult);
        log.trace(request.getHeader("Accept"));
        if (request.getHeader("Accept").equalsIgnoreCase("application/json") || request.getHeader("Accept").equalsIgnoreCase("application/octet-stream")) {
          response.setContentLength(validationResult.length());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, validationResult.toString().replace("$.", " "));
        }
        return false;
      } else {
        log.trace("Response body is : no error");
        return super.preHandle(wrapper, response, handler);
      }
    } else {
      if (requestURL.contains("/api/v1")
          || request.getMethod().equalsIgnoreCase("get")
          || request.getMethod().equalsIgnoreCase("delete")) ;
      else {
        log.warn("Not able to generate schema for url ...");
        log.warn("URL: " + requestURL);
      }
      return super.preHandle(wrapper, response, handler);
    }
  }

  @SuppressWarnings("unchecked")
  private String getJsonSchemaFromClass(Class javaClass) throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
    JsonNode jsonSchema = schemaGen.generateJsonSchema(javaClass);
    String jsonSchemaAsString = mapper.writeValueAsString(jsonSchema);
    log.trace(jsonSchemaAsString);
    return jsonSchemaAsString;
  }
}
