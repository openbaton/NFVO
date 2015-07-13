package org.project.openbaton.nfvo.core.interfaces;

import org.project.openbaton.common.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Created by lto on 30/06/15.
 */
public interface DependencyManagement {
    void provisionDependencies(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord) throws NotFoundException, JMSException, NamingException;
}
