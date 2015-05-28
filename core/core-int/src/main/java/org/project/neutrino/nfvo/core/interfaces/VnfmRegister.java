package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.nfvo.VnfmManagerEndpoint;
import org.project.neutrino.nfvo.core.interfaces.exception.NotFoundException;

import java.util.List;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmRegister {

    List<VnfmManagerEndpoint> listVnfm();

    void addManagerEndpoint(VnfmManagerEndpoint endpoint);

    VnfmManagerEndpoint getVnfm(String type) throws NotFoundException;
}
