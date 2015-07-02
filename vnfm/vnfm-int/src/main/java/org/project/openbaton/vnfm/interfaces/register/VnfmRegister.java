package org.project.openbaton.vnfm.interfaces.register;

import org.project.openbaton.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.openbaton.nfvo.common.exceptions.NotFoundException;

import java.util.List;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmRegister {

    List<VnfmManagerEndpoint> listVnfm();

    void addManagerEndpoint(VnfmManagerEndpoint endpoint);

    VnfmManagerEndpoint getVnfm(String type) throws NotFoundException;
}
