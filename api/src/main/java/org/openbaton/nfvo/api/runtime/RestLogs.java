/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.nfvo.api.runtime;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.openbaton.catalogue.nfvo.messages.VnfmOrLogMessage;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.core.interfaces.LogManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Created by lto on 17/05/16. */
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
      throws NotFoundException, InterruptedException, BadFormatException, ExecutionException {

    int lines = 0;
    if (request != null) {
      lines = request.get("lines").getAsInt();
    }

    log.debug("requesting last " + lines + " lines");

    VnfmOrLogMessage message = logManager.getLog(nsrId, vnfrName, hostname);
    int errorLines = message.getErrorLog().size();
    int outputLines = message.getOutputLog().size();
    log.debug("There are " + errorLines + " lines for error logs");
    log.debug("There are " + outputLines + " lines for output logs");
    if (lines > 0 && outputLines > lines) {
      return errorLines == 0
          ? message.getOutputLog().subList(outputLines - lines, outputLines)
          : message.getErrorLog();
    }
    return errorLines == 0 ? message.getOutputLog() : message.getErrorLog();
  }
}
