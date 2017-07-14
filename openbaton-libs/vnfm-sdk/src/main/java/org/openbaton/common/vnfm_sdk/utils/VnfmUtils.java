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

package org.openbaton.common.vnfm_sdk.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrAllocateResourcesMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrErrorMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScaledMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrScalingMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrStartStopMessage;
import org.openbaton.catalogue.security.Key;

/** Created by lto on 23/09/15. */
public class VnfmUtils {

  public static NFVMessage getNfvInstantiateMessage(
      VirtualNetworkFunctionRecord payload,
      Map<String, VimInstance> vimInstances,
      String userData,
      Set<Key> keys) {
    VnfmOrAllocateResourcesMessage nfvMessage = new VnfmOrAllocateResourcesMessage();
    nfvMessage.setVirtualNetworkFunctionRecord(payload);
    nfvMessage.setVimInstances(vimInstances);
    nfvMessage.setUserdata(userData);
    nfvMessage.setKeyPairs(keys);
    return nfvMessage;
  }

  public static NFVMessage getNfvErrorMessage(
      VirtualNetworkFunctionRecord payload, Throwable exception, String nsrId) {
    NFVMessage nfvMessage;
    nfvMessage = new VnfmOrErrorMessage(new Exception(exception), payload, nsrId);
    nfvMessage.setAction(Action.ERROR);
    return nfvMessage;
  }

  public static NFVMessage getNfvMessage(Action action, VirtualNetworkFunctionRecord payload) {
    NFVMessage nfvMessage;
    if (Action.START.ordinal() == action.ordinal())
      nfvMessage = new VnfmOrStartStopMessage(payload, Action.START);
    else if (Action.INSTANTIATE.ordinal() == action.ordinal())
      nfvMessage = new VnfmOrInstantiateMessage(payload);
    else nfvMessage = new VnfmOrGenericMessage(payload, action);
    return nfvMessage;
  }

  public static NFVMessage getNfvMessageScaled(
      Action action, VirtualNetworkFunctionRecord payload, VNFCInstance vnfcInstance) {
    VnfmOrScaledMessage vnfmOrScaledMessage = new VnfmOrScaledMessage();
    vnfmOrScaledMessage.setVirtualNetworkFunctionRecord(payload);
    vnfmOrScaledMessage.setVnfcInstance(vnfcInstance);
    vnfmOrScaledMessage.setAction(action);
    return vnfmOrScaledMessage;
  }

  public static NFVMessage getNfvMessageHealed(
      Action action, VirtualNetworkFunctionRecord payload, VNFCInstance vnfcInstance) {
    VnfmOrHealedMessage vnfmOrHealedMessage = new VnfmOrHealedMessage();
    vnfmOrHealedMessage.setVirtualNetworkFunctionRecord(payload);
    vnfmOrHealedMessage.setVnfcInstance(vnfcInstance);
    vnfmOrHealedMessage.setAction(action);
    return vnfmOrHealedMessage;
  }

  public static LifecycleEvent getLifecycleEvent(Collection<LifecycleEvent> events, Event event) {
    for (LifecycleEvent lce : events)
      if (lce.getEvent().ordinal() == event.ordinal()) {
        return lce;
      }
    return null;
  }

  public static NFVMessage getNfvScalingMessage(
      String userData,
      VirtualNetworkFunctionRecord virtualNetworkFunctionRecord,
      VimInstance vimInstance) {
    VnfmOrScalingMessage vnfmOrScalingMessage = new VnfmOrScalingMessage();
    vnfmOrScalingMessage.setUserData(userData);
    vnfmOrScalingMessage.setAction(Action.SCALING);
    vnfmOrScalingMessage.setVirtualNetworkFunctionRecord(virtualNetworkFunctionRecord);
    vnfmOrScalingMessage.setVimInstance(vimInstance);
    return vnfmOrScalingMessage;
  }

  public static NFVMessage getNfvMessageStartStop(
      Action action, VirtualNetworkFunctionRecord payload, VNFCInstance vnfcInstance) {
    VnfmOrStartStopMessage vnfmOrStartStopMessage = new VnfmOrStartStopMessage();
    vnfmOrStartStopMessage.setVirtualNetworkFunctionRecord(payload);
    vnfmOrStartStopMessage.setVnfcInstance(vnfcInstance);
    vnfmOrStartStopMessage.setAction(action);
    return vnfmOrStartStopMessage;
  }
}
