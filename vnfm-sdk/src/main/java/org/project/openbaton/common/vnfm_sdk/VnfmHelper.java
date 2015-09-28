package org.project.openbaton.common.vnfm_sdk;

import org.project.openbaton.catalogue.mano.common.Event;
import org.project.openbaton.catalogue.mano.record.VNFRecordDependency;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.project.openbaton.common.vnfm_sdk.exception.VnfmSdkException;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by lto on 23/09/15.
 */
public abstract class VnfmHelper {

    public abstract Future<VirtualNetworkFunctionRecord> grantLifecycleOperation(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VnfmSdkException;

    public abstract Future<VirtualNetworkFunctionRecord> allocateResources(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VnfmSdkException;

    public abstract void sendMessageToQueue(String sendToQueueName, Serializable message);

    public abstract void sendToNfvo(NFVMessage nfvMessage);

    public abstract Iterable<String> executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event, Map<String, String> env) throws Exception;

    public abstract String executeScriptsForEvent(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Event event, VNFRecordDependency dependency) throws Exception;

    public abstract void saveScriptOnEms(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Object scriptsLink) throws Exception;

    public abstract NFVMessage sendAndReceive(Action action, VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws Exception;
}
