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
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.ValidationMessage;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDependency;
import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.nfvo.api.configuration.CustomHttpServletRequestWrapper;
import org.openbaton.nfvo.common.utils.schema.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashSet;
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
    String requestMethod = request.getMethod();
    log.trace("Validation of schema for " + requestMethod + " on url: " + request.getRequestURI());
    if (request.getRequestURI().equalsIgnoreCase("/error")) {
      return true;
    }
    //TODO fix the date of the image
    if (request.getRequestURI().contains("/datacenters") && requestMethod.equalsIgnoreCase("put")) {
      return true;
    }
    CustomHttpServletRequestWrapper wrapper = new CustomHttpServletRequestWrapper(request);
    String requestBody = wrapper.getBody();
    String classSchema = null;
    Class<?> parameterClass = null;
    if (handler instanceof org.springframework.web.method.HandlerMethod) {
      org.springframework.web.method.HandlerMethod handlerMethod =
          (org.springframework.web.method.HandlerMethod) handler;

      Annotation[][] annotations = handlerMethod.getMethod().getParameterAnnotations();
      for (int i = 0; i < annotations.length; i++) {
        Annotation[] methodParameters = annotations[i];
        for (Annotation a : methodParameters) {
          if (a instanceof RequestBody) {
            parameterClass = handlerMethod.getMethod().getParameterTypes()[i];
            if (!parameterClass.getCanonicalName().equals(JsonObject.class.getCanonicalName())
                && !parameterClass.isPrimitive()
                && !Modifier.isAbstract(parameterClass.getModifiers())
                && !parameterClass.getCanonicalName().equals(String.class.getCanonicalName())) {
              classSchema = getJsonSchemaFromClass(parameterClass);
            }
          }
        }
      }
    }

    if (classSchema != null) {
      log.trace("Request Body is : " + requestBody);
      log.trace("Request url is : " + requestURL);
      Set<ValidationMessage> validationMessages;
      try {
        if (parameterClass
            .getCanonicalName()
            .equals(NetworkServiceDescriptor.class.getCanonicalName())) {
          NetworkServiceDescriptor networkServiceDescriptor =
              gson.fromJson(requestBody, NetworkServiceDescriptor.class);
          if (networkServiceDescriptor
                  .getVnfd()
                  .stream()
                  .filter(vnfd -> vnfd.getId() != null)
                  .count()
              > 0) {
            Set<ValidationMessage> errors = new HashSet<>();
            //Validating VLDs
            networkServiceDescriptor
                .getVld()
                .forEach(
                    vld -> {
                      try {
                        errors.addAll(
                            SchemaValidator.validateSchema(
                                getJsonSchemaFromClass(VirtualLinkDescriptor.class),
                                gson.toJson(vld)));
                      } catch (BadRequestException | IOException e) {
                        e.printStackTrace();
                      }
                    });
            //Validating VNFDeps
            networkServiceDescriptor
                .getVnf_dependency()
                .forEach(
                    vnfDependency -> {
                      try {
                        errors.addAll(
                            SchemaValidator.validateSchema(
                                getJsonSchemaFromClass(VNFDependency.class),
                                gson.toJson(vnfDependency)));
                      } catch (BadRequestException | IOException e) {
                        e.printStackTrace();
                      }
                    });
            if (!errors.isEmpty()) {
              handleErrorMessages(request, response, errors);
              return false;
            }
            return true;
          }
        }
        validationMessages = SchemaValidator.validateSchema(classSchema, requestBody);
      } catch (BadRequestException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        return false;
      }catch (JsonSyntaxException | MalformedJsonException e) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage().split(":")[1].replace("$.", " ").replaceAll("\\[[0-9]\\]",""));
        return false;
      }
      if (validationMessages.size() > 0) {
        handleErrorMessages(request, response, validationMessages);
        return false;
      } else {
        log.trace("Response body is : no error");
        return super.preHandle(wrapper, response, handler);
      }
    } else {
      if (!requestURL.contains("/api/v1")
          && !requestMethod.equalsIgnoreCase("get")
          && !requestMethod.equalsIgnoreCase("delete")) {
        log.warn("Not able to generate schema for url ...");
        log.warn("URL: " + requestURL);
      }
    }
    boolean b = super.preHandle(wrapper, response, handler);
    return b;
  }

  private void handleErrorMessages(
      HttpServletRequest request,
      HttpServletResponse response,
      Set<ValidationMessage> validationMessages)
      throws IOException {
    StringBuilder validationResult = new StringBuilder();
    for (ValidationMessage s : validationMessages) {
      String message = s.getMessage();
      validationResult.append(message).append(", ");
    }

    log.trace("Response body is : " + validationResult);
    log.trace(request.getHeader("Accept"));
    if (request.getHeader("Accept").equalsIgnoreCase("application/json")
        || request.getHeader("Accept").equalsIgnoreCase("application/octet-stream")) {
      response.setContentLength(validationResult.length());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST, validationResult.toString().replace("$.", " "));
    }
  }

  @SuppressWarnings("unchecked")
  private String getJsonSchemaFromClass(Class javaClass) throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
    JsonNode jsonSchema = schemaGen.generateJsonSchema(javaClass);
    String jsonSchemaAsString = mapper.writeValueAsString(jsonSchema);
    log.trace("The schema is: " + jsonSchemaAsString);
    return jsonSchemaAsString;
  }
}
