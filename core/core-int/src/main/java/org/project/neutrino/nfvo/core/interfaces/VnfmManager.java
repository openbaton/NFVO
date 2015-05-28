package org.project.neutrino.nfvo.core.interfaces;

import org.project.neutrino.nfvo.catalogue.mano.record.NetworkServiceRecord;
import org.project.neutrino.nfvo.core.interfaces.exception.NotFoundException;

import java.util.concurrent.Future;

/**
 * Created by lto on 26/05/15.
 */
public interface VnfmManager {
    Future<Void> deploy(NetworkServiceRecord networkServiceRecord) throws NotFoundException;
}
