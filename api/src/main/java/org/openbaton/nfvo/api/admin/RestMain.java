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

package org.openbaton.nfvo.api.admin;

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.security.interfaces.UserManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Created by lto on 13/10/16. */
@RestController
@RequestMapping("/api/v1/main")
@ConfigurationProperties
public class RestMain {

  public String getNfvoIp() {
    return nfvoIp;
  }

  public void setNfvoIp(String nfvoIp) {
    this.nfvoIp = nfvoIp;
  }

  public String getNfvoPort() {
    return nfvoPort;
  }

  public void setNfvoPort(String nfvoPort) {
    this.nfvoPort = nfvoPort;
  }

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String nfvoIp;

  @Value("${server.port:8080}")
  private String nfvoPort;

  @Autowired private UserManagement userManagement;

  @RequestMapping(
    value = "version",
    method = RequestMethod.GET,
    produces = MediaType.TEXT_PLAIN_VALUE
  )
  public String getVersion() {
    return RestMain.class.getPackage().getImplementationVersion();
  }

  @RequestMapping(
    value = "openbaton-rc",
    method = RequestMethod.GET,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  public String getOpenRCFile(@RequestHeader("project-id") String projectId)
      throws NotFoundException {
    return getOpenRcFile(projectId);
  }

  private String getOpenRcFile(String projectId) throws NotFoundException {
    return "#!/bin/bash\n"
        + "\n"
        + "unset OB_NFVO_IP\n"
        + "unset OB_NFVO_PORT\n"
        + "unset OB_PROJECT_ID\n"
        + "unset OB_USERNAME\n"
        + "unset OB_PASSWORD\n\n"
        + "export OB_NFVO_IP="
        + nfvoIp
        + "\n"
        + "export OB_NFVO_PORT="
        + nfvoPort
        + "\n"
        + "export OB_PROJECT_ID="
        + projectId
        + "\n"
        + "export OB_USERNAME="
        + userManagement.getCurrentUser().getUsername()
        + "\n"
        + "echo -n Insert Open Baton Password: \n"
        + "read -s password\n"
        + "export OB_PASSWORD=$password\n";
  }
}
