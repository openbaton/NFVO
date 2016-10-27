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

import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;

/**
 * Created by lto on 11/06/15.
 */
public interface VirtualLinkManagement {
  VirtualLinkDescriptor add(VirtualLinkDescriptor virtualLinkDescriptor);

  VirtualLinkRecord add(VirtualLinkRecord virtualLinkRecord);

  void delete(String id);

  VirtualLinkDescriptor update(VirtualLinkDescriptor virtualLinkDescriptor_new, String id);

  VirtualLinkRecord update(VirtualLinkRecord virtualLinkRecord_new, String id);

  Iterable<VirtualLinkDescriptor> queryDescriptors();

  Iterable<VirtualLinkRecord> queryRecords();

  VirtualLinkRecord queryRecord(String id);

  VirtualLinkDescriptor queryDescriptor(String id);
}
