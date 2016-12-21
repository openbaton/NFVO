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

package org.openbaton.nfvo.core.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.exceptions.EntityInUseException;
import org.openbaton.exceptions.NotFoundException;

/** Created by mpa on 01.10.15. */
public interface VirtualNetworkFunctionManagement {
  VirtualNetworkFunctionDescriptor add(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor, String projectId)
      throws NotFoundException;

  void delete(String id, String projectId) throws EntityInUseException;

  Iterable<VirtualNetworkFunctionDescriptor> query();

  VirtualNetworkFunctionDescriptor query(String id, String projectId);

  VirtualNetworkFunctionDescriptor update(
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      String id,
      String projectId);

  Iterable<VirtualNetworkFunctionDescriptor> queryByProjectId(String projectId);
}
