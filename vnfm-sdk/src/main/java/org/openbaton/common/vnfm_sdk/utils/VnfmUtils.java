package org.openbaton.common.vnfm_sdk.utils;

import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrGenericMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrInstantiateMessage;

import java.util.Collection;

/**
 * Created by lto on 23/09/15.
 */
public class VnfmUtils {

    public static NFVMessage getNfvMessage(Action action, VirtualNetworkFunctionRecord payload) {
        NFVMessage nfvMessage = null;
        if (Action.INSTANTIATE.ordinal() == action.ordinal())
            nfvMessage = new VnfmOrInstantiateMessage(payload);
        else
            nfvMessage = new VnfmOrGenericMessage(payload, action);
        return nfvMessage;
    }

    public static LifecycleEvent getLifecycleEvent(Collection<LifecycleEvent> events, Event event) {
        for (LifecycleEvent lce : events)
            if (lce.getEvent().ordinal() == event.ordinal()) {
                return lce;
            }
        return null;
    }
}
