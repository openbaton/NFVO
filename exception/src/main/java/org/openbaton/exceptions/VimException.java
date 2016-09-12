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

package org.openbaton.exceptions;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;

/**
 * Created by lto on 20/05/15.
 */
public class VimException extends Exception {
  private VNFCInstance vnfcInstance;

  public VirtualDeploymentUnit getVirtualDeploymentUnit() {
    return virtualDeploymentUnit;
  }

  public void setVirtualDeploymentUnit(VirtualDeploymentUnit virtualDeploymentUnit) {
    this.virtualDeploymentUnit = virtualDeploymentUnit;
  }

  private VirtualDeploymentUnit virtualDeploymentUnit;

  public VimException(String s) {
    super(s);
  }

  public VimException(Throwable cause) {
    super(cause);
  }

  public VimException(String message, Throwable cause) {
    super(message, cause);
  }

  public VimException(
      String s,
      Exception e,
      VirtualDeploymentUnit virtualDeploymentUnit,
      VNFCInstance vnfcInstance) {
    super(s, e);
    this.virtualDeploymentUnit = virtualDeploymentUnit;
    this.vnfcInstance = vnfcInstance;
  }

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }
}
