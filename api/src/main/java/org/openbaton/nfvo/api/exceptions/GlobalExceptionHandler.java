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

package org.openbaton.nfvo.api.exceptions;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import javax.persistence.NoResultException;
import org.openbaton.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Created by gca on 27/08/15. */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @ExceptionHandler({NotFoundException.class, NoResultException.class})
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  protected ResponseEntity<Object> handleNotFoundException(Exception e, WebRequest request) {
    if (log.isDebugEnabled()) {
      log.error("Exception was thrown -> Return message: " + e.getMessage(), e);
    } else {
      log.error("Exception was thrown -> Return message: " + e.getMessage());
    }
    ExceptionResource exc = new ExceptionResource("Not Found", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(e, exc, headers, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler({
    BadRequestException.class,
    BadFormatException.class,
    NetworkServiceIntegrityException.class,
    WrongStatusException.class,
    UnrecognizedPropertyException.class,
    VimException.class,
    CyclicDependenciesException.class,
    WrongAction.class,
    PasswordWeakException.class,
    AlreadyExistingException.class,
    IncompatibleVNFPackage.class,
    EntityInUseException.class,
    org.openbaton.exceptions.EntityUnreachableException.class,
    org.openbaton.exceptions.MissingParameterException.class,
    org.openbaton.exceptions.MonitoringException.class,
    org.openbaton.exceptions.NetworkServiceIntegrityException.class,
    org.openbaton.exceptions.PluginException.class,
    org.openbaton.exceptions.VnfmException.class,
    org.openbaton.exceptions.VimException.class,
    org.openbaton.exceptions.VimDriverException.class,
    org.openbaton.exceptions.QuotaExceededException.class,
  })
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  protected @ResponseBody ResponseEntity<Object> handleInvalidRequest(
      Exception e, WebRequest request) {
    if (log.isDebugEnabled()) {
      log.error("Exception was thrown -> Return message: " + e.getMessage(), e);
    } else {
      log.error("Exception was thrown -> Return message: " + e.getMessage());
    }
    ExceptionResource exc = new ExceptionResource("Bad Request", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(e, exc, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler({UnauthorizedUserException.class, NotAllowedException.class})
  @ResponseStatus(value = HttpStatus.FORBIDDEN)
  protected ResponseEntity<Object> handleUnauthorized(Exception e, WebRequest request) {
    if (log.isDebugEnabled()) {
      log.error("Exception was thrown -> Return message: " + e.getMessage(), e);
    } else {
      log.error("Exception was thrown -> Return message: " + e.getMessage());
    }
    ExceptionResource exc = new ExceptionResource("Unauthorized exception", e.getMessage());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(e, exc, headers, HttpStatus.FORBIDDEN, request);
  }
}
