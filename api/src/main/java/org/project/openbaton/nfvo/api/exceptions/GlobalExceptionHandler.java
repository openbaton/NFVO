/*
 * Copyright (c) 2015 Fraunhofer FOKUS. All rights reserved.
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

package org.project.openbaton.nfvo.api.exceptions;

import org.project.openbaton.nfvo.common.exceptions.BadFormatException;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by gca on 27/08/15.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler({NotFoundException.class, BadFormatException.class, NoResultException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(HttpServletRequest request, Exception ex){
        log.error("Requested URL="+request.getRequestURL());
        log.error("Exception Raised="+ex);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exception", ex);
        modelAndView.addObject("url", request.getRequestURL());

        modelAndView.setViewName("error");
        return modelAndView;
    }


}
