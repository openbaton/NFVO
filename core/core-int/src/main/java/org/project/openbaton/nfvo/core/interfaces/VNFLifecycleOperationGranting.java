package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.common.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.common.exceptions.VimException;

/**
 * Created by mpa on 05/05/15.
 */

public interface VNFLifecycleOperationGranting {
	
	/**
	 * This operation allows requesting the permission to 
	 * perform a certain VNF lifecycle operation on a new 
	 * or existing VNF. The sub-type of lifecycle operation 
	 * is parameterized in the operation.
	 * @param virtualNetworkFunctionRecord
	 */
	boolean grantLifecycleOperation(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws VimException;
}
