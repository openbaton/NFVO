/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
 *
 */

package org.openbaton.nfvo.api;

import com.google.gson.JsonObject;

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.LogManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lto on 17/05/16.
 */
@RestController
@RequestMapping("/api/v1/logs")
public class RestLogs {

  @Autowired private LogManagement logManager;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @RequestMapping(
    value = "{nsrId}/vnfrecord/{vnfrName}/hostname/{hostname}",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public List<String> getLog(
      @PathVariable("nsrId") String nsrId,
      @PathVariable("vnfrName") String vnfrName,
      @PathVariable("hostname") String hostname,
      @RequestBody(required = false) JsonObject request)
      throws NotFoundException {

    int lines = 0;
    if (request != null) {
      lines = request.get("lines").getAsInt();
    }

    log.debug("requesting last " + lines + " lines");

    HashMap<String, List<String>> logs = logManager.getLog(nsrId, vnfrName, hostname);
    log.debug("There are " + logs.get("error").size() + " lines for error logs");
    log.debug("There are " + logs.get("output").size() + " lines for output logs");
    if (lines > 0) {
      logs.put(
          "output",
          logs.get("output").subList(logs.size() - lines - 1, logs.get("output").size() - 1));
    }
    return logs.get("error").size() == 0 ? logs.get("output") : logs.get("error");
  }
}
