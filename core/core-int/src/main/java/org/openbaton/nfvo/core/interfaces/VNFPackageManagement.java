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

package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongAction;

import java.io.IOException;

/**
 * Created by mpa on 05/05/15.
 */
public interface VNFPackageManagement {

  /**
   * This operation allows submitting and validating the VNF Package.
   *
   * @param pack
   * @param projectId
   */
  VirtualNetworkFunctionDescriptor onboard(byte[] pack, String projectId)
      throws IOException, VimException, NotFoundException, PluginException;

  /**
   * This operation allows disabling the VNF Package, so that it is not possible to instantiate any
   * further.
   */
  void disable();

  /**
   * This operation allows enabling the VNF Package.
   */
  void enable();

  /**
   * This operation allows updating the VNF Package.
   *
   * @param id
   * @param pack_new
   * @param projectId
   */
  VNFPackage update(String id, VNFPackage pack_new, String projectId);

  VNFPackage query(String id, String projectId);

  /**
   * This operation is used to query information on VNF Packages.
   */
  Iterable<VNFPackage> query();

  /**
   * This operation is used to remove a disabled VNF Package.
   *
   * @param id
   * @param projectId
   */
  void delete(String id, String projectId) throws WrongAction;

  Script updateScript(Script script, String vnfPackageId) throws NotFoundException;

  Iterable<VNFPackage> queryByProjectId(String projectId);
}
