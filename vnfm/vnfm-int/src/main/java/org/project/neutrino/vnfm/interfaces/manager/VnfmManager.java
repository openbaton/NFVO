package org.project.neutrino.vnfm.interfaces.manager;

import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.common.exceptions.NotFoundException;

import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmManager {
    Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException;
}
